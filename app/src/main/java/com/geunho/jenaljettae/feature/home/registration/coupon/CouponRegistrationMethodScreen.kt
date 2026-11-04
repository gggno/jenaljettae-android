package com.geunho.jenaljettae.features.home.registration.coupon

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.geunho.jenaljettae.R
import com.geunho.jenaljettae.ui.theme.AppRadius
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// MARK: - Coupon Registration Method | 등록 방법 선택 및 화면 이동 이벤트 전달
@Composable
fun CouponRegistrationMethodScreen(
    onBack: () -> Unit,
    onManualEntry: () -> Unit,
    onImageSelected: (ByteArray, Uri) -> Unit
) {
    val colors = AppTheme.colors
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isLoadingPhoto by remember { mutableStateOf(false) }
    var photoLoadError by remember { mutableStateOf<String?>(null) }

    // MARK: - Photo Picker | 선택한 사진을 검증한 뒤 NavHost에 전달
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && !isLoadingPhoto) {
            isLoadingPhoto = true

            coroutineScope.launch {
                try {
                    val imageData = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            val data = input.readBytes()

                            if (data.isEmpty() || data.size > 20 * 1024 * 1024) {
                                throw IOException("20MB 이하의 이미지를 선택해주세요.")
                            }

                            data
                        } ?: throw IOException("이미지를 불러올 수 없어요.")
                    }

                    // MARK: - Image Validation | 이미지 형식 및 디코딩 가능 여부 확인
                    val isValidImage = withContext(Dispatchers.Default) {
                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }

                        BitmapFactory.decodeByteArray(
                            imageData, 0, imageData.size, options
                        )

                        options.outWidth > 0 && options.outHeight > 0
                    }

                    if (!isValidImage) throw IOException("지원하지 않는 이미지 형식이에요.")

                    onImageSelected(imageData, uri)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    photoLoadError = error.localizedMessage ?: "이미지를 불러올 수 없어요."
                } finally {
                    isLoadingPhoto = false
                }
            }
        }
    }

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // MARK: - Header | 뒤로가기 및 화면 제목
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
                text = "쿠폰 등록 방법",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        // MARK: - Content | 등록 방식 선택 및 안내
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.screenHorizontal)
                .padding(
                    top = AppSpacing.large,
                    bottom = AppSpacing.xLarge
                ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionSpacing)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
            ) {
                Text(
                    text = "쿠폰·기프티콘",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primaryForeground
                )

                Text(
                    text = "어떻게 등록할까요?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Text(
                    text = "이미지 속 글자를 AI로 읽어 입력을 도와줘요.",
                    style = AppTypography.body,
                    color = colors.textSecondary
                )
            }

            // MARK: - Registration Buttons | AI 이미지 선택 및 수동 입력
            Column(
                verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                Button(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    enabled = !isLoadingPhoto,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(AppRadius.card),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    )
                ) {
                    Text(
                        text = if (isLoadingPhoto) "이미지 불러오는 중..." else "AI로 쿠폰 읽기",
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                OutlinedButton(
                    onClick = onManualEntry,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(AppRadius.card),
                    border = BorderStroke(1.dp, colors.border),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = colors.card,
                        contentColor = colors.primaryForeground
                    )
                ) {
                    Text(
                        text = "직접 입력하기",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            // MARK: - Sample Coupon | 기존 안내 카드 유지
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        colors.primaryTint,
                        RoundedCornerShape(AppRadius.card)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    AppSpacing.xSmall,
                    Alignment.CenterVertically
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_jnt_coupon),
                    contentDescription = null,
                    tint = colors.primaryForeground,
                    modifier = Modifier.size(32.dp)
                )

                Text(
                    text = "아메리카노 교환권",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary
                )

                Text(
                    text = "COUPON SAMPLE",
                    style = AppTypography.caption,
                    color = colors.textSecondary
                )
            }

            Text(
                text = "사진 한 장을 선택한 뒤 인식한 내용을 확인하고 수정할 수 있어요.",
                style = AppTypography.caption,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // MARK: - Photo Load Error | 이미지 불러오기 실패 안내
    if (photoLoadError != null) {
        AlertDialog(
            onDismissRequest = { photoLoadError = null },
            title = { Text("이미지를 불러올 수 없어요") },
            text = { Text(photoLoadError.orEmpty()) },
            confirmButton = {
                TextButton(onClick = { photoLoadError = null }) {
                    Text("확인")
                }
            }
        )
    }
}
