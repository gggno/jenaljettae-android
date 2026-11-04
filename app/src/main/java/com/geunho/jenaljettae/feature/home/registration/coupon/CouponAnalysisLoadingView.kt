package com.geunho.jenaljettae.features.home.registration.coupon

import android.animation.ValueAnimator
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geunho.jenaljettae.R
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import com.geunho.jenaljettae.ui.theme.JENALJETTAETheme

// MARK: - Coupon Analysis Loading | AI 분석 중 표시하는 펄스 오버레이
@Composable
fun CouponAnalysisLoadingView() {
    val colors = AppTheme.colors
    val reduceMotion = remember { !ValueAnimator.areAnimatorsEnabled() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background.copy(alpha = 0.9f))
            .pointerInput(Unit) {
                // MARK: - Touch Blocking | 로딩 중 하위 화면 터치 차단
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()

                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    } while (event.changes.any { it.pressed })
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .padding(horizontal = AppSpacing.screenHorizontal)
                .padding(AppSpacing.xLarge)
                .clearAndSetSemantics {
                    contentDescription = "AI가 쿠폰을 분석하고 있어요. 쿠폰 정보를 확인하고 있어요."
                    liveRegion = LiveRegionMode.Polite
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
        ) {
            // MARK: - Pulse Container | 원형 파동 및 아이콘 배치
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // MARK: - Waves | 두 원이 0.9초 간격으로 확산
                if (!reduceMotion) {
                    AnalysisWave(delayMillis = 0, color = colors.primaryForeground)
                    AnalysisWave(delayMillis = 900, color = colors.primaryForeground)
                }

                // MARK: - Icon Background | 기존 디자인 시스템 배경색 적용
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(colors.primaryTint, CircleShape)
                )

                // MARK: - Sparkles Pulse | 아이콘을 0.9초 간격으로 확대·축소
                if (reduceMotion) {
                    Icon(
                        painter = painterResource(R.drawable.ic_jnt_sparkles),
                        contentDescription = null,
                        tint = colors.primaryForeground,
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    PulsingSparklesIcon()
                }
            }

            // MARK: - Loading Description | 분석 중 안내 문구
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
            ) {
                Text(
                    text = "AI가 쿠폰을 분석하고 있어요",
                    style = AppTypography.sectionTitle,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "쿠폰 정보를 확인하고 있어요.",
                    style = AppTypography.body,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// MARK: - Analysis Wave | 1.8초 동안 원이 확산되며 투명해지는 효과
@Composable
private fun AnalysisWave(delayMillis: Int, color: Color) {
    val transition = rememberInfiniteTransition(label = "Analysis Wave $delayMillis")

    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800,
                easing = LinearOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(
                offsetMillis = delayMillis,
                offsetType = StartOffsetType.Delay
            )
        ),
        label = "Wave Progress"
    )

    Box(
        modifier = Modifier
            .size(88.dp)
            .scale(0.85f + progress * 0.8f)
            .alpha(0.36f * (1f - progress))
            .border(1.5.dp, color, CircleShape)
    )
}

// MARK: - Pulsing Sparkles | 중앙 아이콘 펄스 애니메이션
@Composable
private fun PulsingSparklesIcon() {
    val colors = AppTheme.colors
    val transition = rememberInfiniteTransition(label = "Sparkles Pulse")

    val scale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 900,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Sparkles Scale"
    )

    Icon(
        painter = painterResource(R.drawable.ic_jnt_sparkles),
        contentDescription = null,
        tint = colors.primaryForeground,
        modifier = Modifier
            .size(36.dp)
            .scale(scale)
    )
}

// MARK: - Light Preview | 라이트 모드 로딩 확인
@Preview(name = "Light", showBackground = true)
@Composable
private fun CouponAnalysisLoadingLightPreview() {
    JENALJETTAETheme(darkTheme = false) {
        CouponAnalysisLoadingView()
    }
}

// MARK: - Dark Preview | 다크 모드 로딩 확인
@Preview(name = "Dark", showBackground = true)
@Composable
private fun CouponAnalysisLoadingDarkPreview() {
    JENALJETTAETheme(darkTheme = true) {
        CouponAnalysisLoadingView()
    }
}
