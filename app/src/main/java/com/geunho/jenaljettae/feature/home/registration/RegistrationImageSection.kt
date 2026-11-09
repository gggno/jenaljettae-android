package com.geunho.jenaljettae.features.home.registration

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// MARK: - Registration Image Section | 쿠폰 이미지 선택·변경·삭제 UI
@Composable
fun RegistrationImageSection(
    itemName: String,
    imageUri: String?,
    onImageSelected: (String) -> Unit,
    onRemoveImage: () -> Unit,
    onPreviewImage: () -> Unit,
    modifier: Modifier = Modifier,
    onLoadingChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imageShape = RoundedCornerShape(12.dp)

    var isLoadingPhoto by remember { mutableStateOf(false) }
    var showsPhotoLoadError by remember { mutableStateOf(false) }

    // MARK: - Photo Picker | 이미지를 한 장 선택하고 유효성 확인
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isLoadingPhoto = true
                onLoadingChanged(true)

                try {
                    val isValidImage = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            BitmapFactory.decodeStream(stream, null, options)
                            options.outWidth > 0 && options.outHeight > 0
                        } == true
                    }

                    if (isValidImage) {
                        onImageSelected(uri.toString())
                    } else {
                        showsPhotoLoadError = true
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    showsPhotoLoadError = true
                } finally {
                    isLoadingPhoto = false
                    onLoadingChanged(false)
                }
            }
        }
    }

    // MARK: - Open Photo Picker | 사진 선택 화면 열기
    val openPhotoPicker = {
        photoPicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "${itemName} 이미지 (선택)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (imageUri == null) {
            // MARK: - Empty Image | 이미지가 없을 때 추가 카드 표시
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(imageShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, imageShape)
                    .clickable(enabled = !isLoadingPhoto) { openPhotoPicker() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isLoadingPhoto) {
                        CircularProgressIndicator()
                    } else {
                        Text(
                            text = "+",
                            fontSize = 32.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "${itemName} 이미지 추가",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "이미지가 없어도 등록할 수 있어요.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // MARK: - Selected Image | 원본 비율을 유지하면서 실제 이미지 모서리에 라운드 적용
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, imageShape),
                contentAlignment = Alignment.Center
            ) {
                val availableWidth = maxWidth
                val availableHeight = 200.dp

                SubcomposeAsyncImage(
                    model = Uri.parse(imageUri),
                    contentDescription = "선택한 ${itemName} 이미지",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    },
                    success = { state ->
                        val imageSize = state.painter.intrinsicSize

                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (imageSize.width > 0f && imageSize.height > 0f) {
                                val ratio = imageSize.width / imageSize.height
                                val displayWidth = minOf(availableWidth, availableHeight * ratio)
                                val displayHeight = displayWidth / ratio

                                Image(
                                    painter = state.painter,
                                    contentDescription = "선택한 ${itemName} 이미지",
                                    modifier = Modifier
                                        .size(displayWidth, displayHeight)
                                        .clip(imageShape)
                                        .clickable(onClick = onPreviewImage),
                                    contentScale = ContentScale.FillBounds
                                )
                            } else {
                                Image(
                                    painter = state.painter,
                                    contentDescription = "선택한 ${itemName} 이미지",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(imageShape)
                                        .clickable(onClick = onPreviewImage),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    },
                    error = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "이미지를 표시할 수 없어요.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }

            // MARK: - Image Actions | 이미지 변경 및 삭제
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = openPhotoPicker,
                    enabled = !isLoadingPhoto
                ) {
                    Text("사진 변경", color = MaterialTheme.colorScheme.primary)
                }

                TextButton(
                    onClick = onRemoveImage,
                    enabled = !isLoadingPhoto
                ) {
                    Text("삭제", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    // MARK: - Photo Error | 유효하지 않은 이미지 선택 시 오류 안내
    if (showsPhotoLoadError) {
        AlertDialog(
            onDismissRequest = { showsPhotoLoadError = false },
            title = { Text("이미지를 불러올 수 없어요") },
            text = { Text("사진을 다시 선택해주세요. 기존 입력 정보는 유지돼요.") },
            confirmButton = {
                TextButton(onClick = { showsPhotoLoadError = false }) {
                    Text("확인")
                }
            }
        )
    }
}
