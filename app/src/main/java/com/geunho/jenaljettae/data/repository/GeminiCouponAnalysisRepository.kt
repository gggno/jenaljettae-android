package com.geunho.jenaljettae.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.geunho.jenaljettae.domain.model.CouponDraft
import com.geunho.jenaljettae.domain.repository.CouponAnalysisRepository
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeParseException

// MARK: - Gemini Coupon Analysis Repository | Firebase 인증 및 쿠폰 AI 분석
class GeminiCouponAnalysisRepository(
    workerBaseUrl: String,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val appCheck: FirebaseAppCheck = FirebaseAppCheck.getInstance()
) : CouponAnalysisRepository {

    private val endpointUrl = URL("${workerBaseUrl.trimEnd('/')}/analyze").also {
        require(it.protocol == "https" && it.host.isNotBlank()) { "HTTPS Worker 주소가 올바르지 않아요." }
    }

    // MARK: - Analyze Coupon | 이미지 준비, Firebase 인증 및 Worker 분석 요청
    override suspend fun analyze(imageData: ByteArray): CouponDraft {
        if (imageData.isEmpty()) throw IOException(ErrorMessage.INVALID_IMAGE)

        val uploadImage = withContext(Dispatchers.Default) { prepareUploadImage(imageData) }
        val idToken = getIdToken()
        val appCheckToken = getAppCheckToken()

        return withContext(Dispatchers.IO) { requestAnalysis(uploadImage, idToken, appCheckToken) }
    }

    // MARK: - Firebase ID Token | 로그인 및 토큰 발급 실패 시 iOS와 동일한 안내
    private suspend fun getIdToken(): String {
        return try {
            val user = auth.currentUser ?: auth.signInAnonymously().await().user
            ?: throw IOException(ErrorMessage.AUTHENTICATION_FAILED)

            user.getIdToken(false).await().token?.takeIf { it.isNotBlank() }
                ?: throw IOException(ErrorMessage.AUTHENTICATION_FAILED)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw IOException(ErrorMessage.AUTHENTICATION_FAILED, error)
        }
    }

    // MARK: - Firebase App Check | 앱 검증 토큰 실패 시 iOS 인증 오류와 통일
    private suspend fun getAppCheckToken(): String {
        return try {
            appCheck.getAppCheckToken(false).await().token.takeIf { it.isNotBlank() }
                ?: throw IOException(ErrorMessage.AUTHENTICATION_FAILED)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            throw IOException(ErrorMessage.AUTHENTICATION_FAILED, error)
        }
    }

    // MARK: - Prepare Image | 지원 형식은 유지하고 큰 이미지만 JPEG 축소
    private fun prepareUploadImage(imageData: ByteArray): UploadImage {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(imageData, 0, imageData.size, bounds)

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw IOException(ErrorMessage.INVALID_IMAGE)
        }

        val mimeType = bounds.outMimeType?.lowercase()
        val supportedTypes = setOf("image/jpeg", "image/png", "image/webp")

        if (mimeType in supportedTypes && imageData.size <= 5_900_000) {
            val base64 = Base64.encodeToString(imageData, Base64.NO_WRAP)
            if (base64.length <= MAX_BASE64_LENGTH) return UploadImage(base64, mimeType!!)
        }

        // MARK: - Downsample Image | 긴 변이 약 2048px 이하가 되도록 축소
        var sampleSize = 1
        while (maxOf(bounds.outWidth / sampleSize, bounds.outHeight / sampleSize) > 2048) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size, options)
            ?: throw IOException(ErrorMessage.INVALID_IMAGE)

        return try {
            val output = ByteArrayOutputStream()
            output.use {
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 82, it)) {
                    throw IOException(ErrorMessage.INVALID_IMAGE)
                }
            }

            val base64 = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
            if (base64.length > MAX_BASE64_LENGTH) throw IOException(ErrorMessage.IMAGE_TOO_LARGE)

            UploadImage(base64, "image/jpeg")
        } finally {
            bitmap.recycle()
        }
    }

    // MARK: - Worker Request | Firebase 인증 헤더 및 이미지 데이터 전송
    private suspend fun requestAnalysis(
        image: UploadImage,
        idToken: String,
        appCheckToken: String
    ): CouponDraft {
        val body = JSONObject().apply {
            put("imageBase64", image.base64)
            put("mimeType", image.mimeType)
        }.toString()

        val connection = (endpointUrl.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 45_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $idToken")
            setRequestProperty("X-Firebase-AppCheck", appCheckToken)
        }

        // MARK: - Cancellation | 작업 취소 시 연결 종료
        val cancellationHandle = currentCoroutineContext().job.invokeOnCompletion { cause ->
            if (cause != null) connection.disconnect()
        }

        try {
            // MARK: - Network | 통신 오류를 iOS 연결 실패 안내로 통일
            val status: Int
            val responseText: String

            try {
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                status = connection.responseCode
                val stream = if (status in 200..299) connection.inputStream else connection.errorStream
                responseText = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            } catch (error: IOException) {
                currentCoroutineContext().ensureActive()
                throw IOException(ErrorMessage.CONNECTION_FAILED, error)
            }

            // MARK: - HTTP Status | iOS CouponAnalysisError와 동일한 상태별 메시지
            if (status !in 200..299) throw IOException(errorMessage(status))
            if (responseText.isBlank()) throw IOException(ErrorMessage.INVALID_RESPONSE)

            return parseCouponDraft(responseText)
        } finally {
            cancellationHandle.dispose()
            connection.disconnect()
        }
    }

    // MARK: - Parse Coupon Draft | Worker 응답을 쿠폰 초안으로 변환
    private fun parseCouponDraft(responseText: String): CouponDraft {
        try {
            val json = JSONObject(responseText)
            val reviewArray = json.getJSONArray("needsReview")
            val notes = mutableListOf<String>()

            for (index in 0 until reviewArray.length()) notes.add(reviewArray.getString(index))

            val expiryText = json.nullableString("expiryDate")
            val expiryDate = try {
                expiryText?.let { LocalDate.parse(it) }
            } catch (_: DateTimeParseException) {
                notes.add("만료일 형식을 확인해주세요.")
                null
            }

            return CouponDraft(
                title = json.nullableString("title"),
                brand = json.nullableString("brand"),
                expiryDate = expiryDate,
                couponNumber = json.nullableString("couponNumber"),
                reviewNotes = notes
            )
        } catch (error: JSONException) {
            throw IOException(ErrorMessage.INVALID_RESPONSE, error)
        }
    }

    // MARK: - Nullable JSON String | null 또는 공백 문자열 정리
    private fun JSONObject.nullableString(key: String): String? {
        if (!has(key) || isNull(key)) return null

        val value = get(key)
        if (value !is String) throw JSONException("$key 값이 문자열이 아니에요.")
        return value.trim().ifEmpty { null }
    }

    // MARK: - Worker Error | iOS HTTP 응답 오류 안내와 동일하게 처리
    private fun errorMessage(status: Int): String {
        return when (status) {
            400 -> ErrorMessage.INVALID_IMAGE
            401 -> ErrorMessage.UNAUTHORIZED
            413 -> ErrorMessage.IMAGE_TOO_LARGE
            429 -> ErrorMessage.RATE_LIMITED
            else -> ErrorMessage.SERVER_UNAVAILABLE
        }
    }

    // MARK: - Upload Image | 전송용 Base64 및 MIME 정보
    private data class UploadImage(val base64: String, val mimeType: String)

    // MARK: - Error Message | iOS CouponAnalysisError와 동일한 사용자 안내
    private object ErrorMessage {
        const val INVALID_IMAGE = "이미지를 처리할 수 없어요. 다른 사진을 선택해주세요."
        const val IMAGE_TOO_LARGE = "이미지 크기가 너무 커요. 다른 사진으로 다시 시도해주세요."
        const val UNAUTHORIZED = "앱 인증에 실패했어요. 앱을 다시 실행하고 시도해주세요."
        const val AUTHENTICATION_FAILED = "Firebase 인증에 실패했어요. 네트워크 연결을 확인해주세요."
        const val RATE_LIMITED = "무료 AI 분석 한도에 도달했어요. 잠시 후 다시 시도해주세요."
        const val SERVER_UNAVAILABLE = "분석 서버에 문제가 발생했어요. 잠시 후 다시 시도해주세요."
        const val INVALID_RESPONSE = "쿠폰 분석 결과를 읽을 수 없어요. 다시 시도해주세요."
        const val CONNECTION_FAILED = "인터넷 연결을 확인하고 다시 시도해주세요."
    }

    private companion object {
        const val MAX_BASE64_LENGTH = 8_000_000
    }
}
