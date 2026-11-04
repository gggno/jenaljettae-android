package com.geunho.jenaljettae.data.local

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

// MARK: - Coupon Image Storage | 쿠폰 이미지 파일 저장·조회·삭제 관리
class CouponImageStorage(context: Context) {

    private val appContext = context.applicationContext
    private val imageDirectory = File(appContext.filesDir, "coupon_images")
    private val fileNamePattern = Regex("""coupon_[0-9a-f-]{36}\.(jpg|png|webp|heic|heif|gif|avif|img)""")

    // MARK: - Save Image | 선택한 이미지의 원본 데이터를 내부 저장소에 복사
    suspend fun saveImage(uri: Uri): String = withContext(Dispatchers.IO) {
        if (!imageDirectory.exists() && !imageDirectory.mkdirs()) {
            throw IOException("이미지 저장 폴더를 생성할 수 없어요.")
        }

        val mimeType = appContext.contentResolver.getType(uri)?.lowercase()

        require(mimeType == null || mimeType.startsWith("image/")) {
            "이미지 파일만 저장할 수 있어요."
        }

        // MARK: - File Extension | 이미지 형식에 맞는 확장자 결정
        val extension = when (mimeType) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/heic" -> "heic"
            "image/heif" -> "heif"
            "image/gif" -> "gif"
            "image/avif" -> "avif"
            else -> "img"
        }

        val fileName = "coupon_${UUID.randomUUID()}.$extension"
        val savedFile = File(imageDirectory, fileName)
        val tempFile = File.createTempFile("coupon_pending_", ".tmp", imageDirectory)

        try {
            // MARK: - Copy Original | 이미지 변환이나 압축 없이 원본 바이트 복사
            val inputStream = appContext.contentResolver.openInputStream(uri)
                ?: throw IOException("선택한 이미지를 읽을 수 없어요.")

            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var totalBytes = 0L

                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val bytesRead = input.read(buffer)
                        if (bytesRead == -1) break

                        output.write(buffer, 0, bytesRead)
                        totalBytes += bytesRead
                    }

                    if (totalBytes == 0L) throw IOException("이미지 파일이 비어 있어요.")
                }
            }

            // MARK: - Complete Save | 복사가 완료된 파일만 정식 이미지로 등록
            currentCoroutineContext().ensureActive()
            if (!tempFile.renameTo(savedFile)) {
                throw IOException("이미지 파일 저장에 실패했어요.")
            }

            fileName
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }

    // MARK: - Get Image File | 저장된 이미지의 파일 경로 반환
    fun getImageFile(fileName: String?): File? {
        if (fileName == null || !fileNamePattern.matches(fileName)) return null
        return File(imageDirectory, fileName)
    }

    // MARK: - Delete Image | 앱 내부 저장소에서 쿠폰 이미지 삭제
    suspend fun deleteImage(fileName: String?) = withContext(Dispatchers.IO) {
        val file = getImageFile(fileName) ?: return@withContext
        if (file.exists() && !file.delete()) {
            throw IOException("이미지 파일을 삭제할 수 없어요.")
        }
    }
}
