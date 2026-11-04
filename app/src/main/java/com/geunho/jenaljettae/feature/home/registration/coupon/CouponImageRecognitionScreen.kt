package com.geunho.jenaljettae.features.home.registration.coupon

import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.geunho.jenaljettae.R
import com.geunho.jenaljettae.domain.model.CouponDraft
import com.geunho.jenaljettae.domain.usecase.AnalyzeCouponImageUseCase
import com.geunho.jenaljettae.ui.theme.AppRadius
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import java.io.ByteArrayInputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

// MARK: - Coupon Image Recognition | Gemini 분석 후 쿠폰 등록 화면으로 이동
@Composable
fun CouponImageRecognitionScreen(
    imageData: ByteArray,
    analyzeCouponImageUseCase: AnalyzeCouponImageUseCase,
    onBack: () -> Unit,
    onAnalysisComplete: (CouponDraft, ByteArray) -> Unit
) {
    val colors = AppTheme.colors
    val coroutineScope = rememberCoroutineScope()

    var isRecognizing by remember { mutableStateOf(false) }
    var recognitionError by remember { mutableStateOf<String?>(null) }

    // MARK: - Back Navigation | 분석 중 시스템 뒤로가기 차단
    BackHandler(enabled = isRecognizing) {}

    // MARK: - Image Preview | EXIF 회전 정보를 반영한 미리보기 생성
    val imageBitmap = remember(imageData) { decodeCouponPreview(imageData) }

    // MARK: - Analyze Image | 중복 요청 방지 및 분석 완료 시 결과 전달
    fun analyzeImage() {
        if (isRecognizing) return

        isRecognizing = true

        coroutineScope.launch {
            try {
                val draft = analyzeCouponImageUseCase.execute(imageData = imageData)
                currentCoroutineContext().ensureActive()
                onAnalysisComplete(draft, imageData)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                recognitionError = error.localizedMessage ?: "쿠폰 분석에 실패했어요."
            } finally {
                isRecognizing = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // MARK: - Navigation | 다른 등록 화면과 동일한 상단바 구성
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = AppSpacing.xSmall,
                        vertical = AppSpacing.small
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    enabled = !isRecognizing,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_jnt_chevron_left),
                        contentDescription = "뒤로 가기",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "쿠폰 이미지 확인",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.size(48.dp))
            }

            // MARK: - Description | 이미지 확인 및 AI 분석 안내
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.screenHorizontal)
                    .padding(vertical = AppSpacing.large),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
            ) {
                Text(
                    text = "선택한 쿠폰을 확인해주세요.",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )

                Text(
                    text = "AI가 이미지에서 쿠폰 정보를 자동으로 추출해요.",
                    style = AppTypography.body,
                    color = colors.textSecondary
                )
            }

            // MARK: - Coupon Image | 원본 방향으로 보정된 쿠폰 이미지 표시
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = AppSpacing.screenHorizontal)
                    .clip(RoundedCornerShape(AppRadius.card))
                    .background(colors.card),
                contentAlignment = Alignment.Center
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "선택한 쿠폰 이미지",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = "이미지를 표시할 수 없어요.",
                        style = AppTypography.body,
                        color = colors.textSecondary
                    )
                }
            }

            // MARK: - Recognition Button | Gemini 분석 요청
            Button(
                onClick = { analyzeImage() },
                enabled = !isRecognizing && imageBitmap != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.screenHorizontal)
                    .padding(vertical = AppSpacing.small)
                    .height(52.dp),
                shape = RoundedCornerShape(AppRadius.card),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            ) {
                Text(
                    text = "쿠폰 정보 인식하기",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // MARK: - Analysis Overlay | 분석 중 펄스 로딩 및 화면 조작 차단
        if (isRecognizing) {
            CouponAnalysisLoadingView()
        }
    }

    // MARK: - Recognition Error | 이미지 분석 실패 안내
    if (recognitionError != null) {
        AlertDialog(
            onDismissRequest = { recognitionError = null },
            title = { Text("쿠폰 분석 실패") },
            text = { Text(recognitionError.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { recognitionError = null }) {
                    Text("확인")
                }
            }
        )
    }
}

// MARK: - Decode Coupon Preview | 이미지 크기 축소 및 EXIF 회전 방향 보정
private fun decodeCouponPreview(imageData: ByteArray): ImageBitmap? {
    // MARK: - Image Bounds | 원본 크기 확인 후 최대 2048px로 축소
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(imageData, 0, imageData.size, bounds)

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (maxOf(bounds.outWidth / sampleSize, bounds.outHeight / sampleSize) > 2048) {
        sampleSize *= 2
    }

    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size, options) ?: return null

    // MARK: - EXIF Orientation | 사진에 저장된 회전 방향 읽기
    val orientation = runCatching {
        ByteArrayInputStream(imageData).use { stream ->
            ExifInterface(stream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    // MARK: - Rotation | EXIF 값에 따라 필요한 회전 각도 결정
    val rotationDegrees = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90,
        ExifInterface.ORIENTATION_TRANSPOSE -> 90f

        ExifInterface.ORIENTATION_ROTATE_180 -> 180f

        ExifInterface.ORIENTATION_ROTATE_270,
        ExifInterface.ORIENTATION_TRANSVERSE -> -90f

        else -> 0f
    }

    // MARK: - Mirroring | 좌우 또는 상하 반전 이미지 처리
    val flipX = orientation in listOf(
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
        ExifInterface.ORIENTATION_TRANSPOSE,
        ExifInterface.ORIENTATION_TRANSVERSE
    )
    val flipY = orientation == ExifInterface.ORIENTATION_FLIP_VERTICAL

    if (rotationDegrees == 0f && !flipX && !flipY) return bitmap.asImageBitmap()

    // MARK: - Bitmap Transform | 원본 데이터는 유지하고 미리보기만 회전
    val matrix = Matrix().apply {
        postRotate(rotationDegrees)
        postScale(if (flipX) -1f else 1f, if (flipY) -1f else 1f)
    }

    val correctedBitmap = android.graphics.Bitmap.createBitmap(
        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
    )

    if (correctedBitmap !== bitmap) bitmap.recycle()

    return correctedBitmap.asImageBitmap()
}
