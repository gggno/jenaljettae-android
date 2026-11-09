package com.geunho.jenaljettae.features.home.registration

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.SubcomposeAsyncImage
import com.geunho.jenaljettae.ui.theme.AppRadius
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import com.geunho.jenaljettae.R

// MARK: - Registration Image Preview | iOS와 동일한 쿠폰 이미지 확대·이동·닫기 화면
@Composable
fun RegistrationImagePreview(
    itemName: String,
    imageUri: String,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val imageShape = RoundedCornerShape(AppRadius.card)
    val currentOnDismiss by rememberUpdatedState(onDismiss)

    // MARK: - Preview State | 이미지 배율·위치·스와이프 거리 관리
    var scale by remember(imageUri) { mutableFloatStateOf(1f) }
    var imageOffset by remember(imageUri) { mutableStateOf(Offset.Zero) }
    var dismissOffset by remember(imageUri) { mutableFloatStateOf(0f) }
    var viewportSize by remember(imageUri) { mutableStateOf(IntSize.Zero) }
    var displayedImageSize by remember(imageUri) { mutableStateOf(IntSize.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnClickOutside = false
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // MARK: - Close Button | 공통 닫기 아이콘 및 오른쪽 여백 통일
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = AppSpacing.xSmall),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_jnt_close),
                        contentDescription = "이미지 닫기",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // MARK: - Image Area | 상단 버튼을 제외한 영역에 이미지 비율 유지
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(
                        horizontal = AppSpacing.screenHorizontal,
                        vertical = AppSpacing.small
                    )
                    .clipToBounds()
                    .onSizeChanged { viewportSize = it },
                contentAlignment = Alignment.Center
            ) {
                val availableWidth = maxWidth
                val availableHeight = maxHeight
                val dismissThreshold = with(LocalDensity.current) { 120.dp.toPx() }

                Box(
                    modifier = Modifier
                        .fillMaxSize()

                        // MARK: - Double Tap | 두 번 터치하면 2.5배 확대 또는 원래 크기 복원
                        .pointerInput(imageUri) {
                            detectTapGestures(
                                onDoubleTap = { tapPosition ->
                                    if (scale > 1f) {
                                        scale = 1f
                                        imageOffset = Offset.Zero
                                    } else {
                                        val newScale = 2.5f
                                        val center = Offset(
                                            viewportSize.width / 2f,
                                            viewportSize.height / 2f
                                        )

                                        scale = newScale
                                        imageOffset = clampPreviewOffset(
                                            offset = (center - tapPosition) * (newScale - 1f),
                                            scale = newScale,
                                            imageSize = displayedImageSize,
                                            viewportSize = viewportSize
                                        )
                                    }

                                    dismissOffset = 0f
                                }
                            )
                        }

                        // MARK: - Image Gestures | 핀치 확대·이미지 이동·아래로 스와이프 종료
                        .pointerInput(imageUri) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                var usedMultiTouch = false

                                do {
                                    val event = awaitPointerEvent()
                                    val pointerCount = event.changes.count { it.pressed }
                                    val zoomChange = event.calculateZoom()
                                    val panChange = event.calculatePan()

                                    if (pointerCount >= 2) usedMultiTouch = true

                                    if (pointerCount >= 2 || scale > 1f) {
                                        // MARK: - Pinch & Pan | 확대 시 실제 이미지 크기 기준으로 이동 제한
                                        dismissOffset = 0f

                                        val newScale = (scale * zoomChange).coerceIn(1f, 4f)
                                        val newOffset = imageOffset + panChange

                                        imageOffset = clampPreviewOffset(
                                            offset = newOffset,
                                            scale = newScale,
                                            imageSize = displayedImageSize,
                                            viewportSize = viewportSize
                                        )

                                        scale = newScale
                                        if (scale == 1f) imageOffset = Offset.Zero

                                    } else if (!usedMultiTouch) {
                                        // MARK: - Swipe Dismiss | 기본 배율에서 아래 방향으로만 이동
                                        dismissOffset = (dismissOffset + panChange.y).coerceAtLeast(0f)
                                    }

                                    if (panChange != Offset.Zero || zoomChange != 1f) {
                                        event.changes.forEach { it.consume() }
                                    }

                                } while (event.changes.any { it.pressed })

                                // MARK: - Gesture End | 일정 거리 이상 내리면 종료
                                if (scale == 1f && !usedMultiTouch && dismissOffset >= dismissThreshold) {
                                    currentOnDismiss()
                                } else {
                                    dismissOffset = 0f
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // MARK: - Image Loading | 원본 비율로 표시하고 이미지 자체에 라운드 적용
                    SubcomposeAsyncImage(
                        model = Uri.parse(imageUri),
                        contentDescription = "${itemName} 이미지 확대 보기",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = colors.primary)
                            }
                        },
                        success = { state ->
                            val intrinsicSize = state.painter.intrinsicSize
                            val imageRatio = if (
                                intrinsicSize.width.isFinite() &&
                                intrinsicSize.height.isFinite() &&
                                intrinsicSize.width > 0f &&
                                intrinsicSize.height > 0f
                            ) {
                                intrinsicSize.width / intrinsicSize.height
                            } else {
                                1f
                            }

                            val imageWidth = minOf(availableWidth, availableHeight * imageRatio)
                            val imageHeight = imageWidth / imageRatio

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = state.painter,
                                    contentDescription = "${itemName} 이미지",
                                    modifier = Modifier
                                        .size(imageWidth, imageHeight)
                                        .onSizeChanged { displayedImageSize = it }
                                        .clip(imageShape)
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                            translationX = imageOffset.x
                                            translationY = imageOffset.y + dismissOffset
                                        },
                                    contentScale = ContentScale.FillBounds
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "이미지를 표시할 수 없어요.",
                                    color = colors.textSecondary
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

// MARK: - Clamp Preview Offset | 확대된 이미지가 화면 밖으로 과도하게 이동하지 않도록 제한
private fun clampPreviewOffset(
    offset: Offset,
    scale: Float,
    imageSize: IntSize,
    viewportSize: IntSize
): Offset {
    if (scale <= 1f) return Offset.Zero

    val maxX = ((imageSize.width * scale - viewportSize.width) / 2f).coerceAtLeast(0f)
    val maxY = ((imageSize.height * scale - viewportSize.height) / 2f).coerceAtLeast(0f)

    return Offset(
        x = offset.x.coerceIn(-maxX, maxX),
        y = offset.y.coerceIn(-maxY, maxY)
    )
}
