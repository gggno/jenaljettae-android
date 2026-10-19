package com.geunho.jenaljettae.core.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geunho.jenaljettae.R
import com.geunho.jenaljettae.ui.theme.AppRadius
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import com.geunho.jenaljettae.ui.theme.JENALJETTAETheme
import com.geunho.jenaljettae.ui.theme.AppIcon

// MARK: - Item Card

@Composable
fun ItemCard(
    title: String,
    subtitle: String,
    @DrawableRes iconRes: Int,
    statusLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    statusTone: StatusBadgeTone = StatusBadgeTone.NEUTRAL
) {
    val colors = AppTheme.colors

    // 카드 전체에 클릭 이벤트 적용
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.card),
        color = colors.card,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // MARK: - Item Icon

            Box(
                modifier = Modifier.size(42.dp).background(colors.primaryTint, RoundedCornerShape(AppRadius.input)),
                contentAlignment = Alignment.Center
            ) {
                // drawable에 등록한 공통 벡터 아이콘 사용
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = colors.primaryForeground
                )
            }

            // MARK: - Item Information

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xxSmall)
            ) {
                Text(
                    text = title,
                    style = AppTypography.itemTitle,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    style = AppTypography.caption,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // MARK: - Status Badge

            StatusBadge(label = statusLabel, tone = statusTone)
        }
    }
}

// MARK: - Preview Content

@Composable
private fun ItemCardPreviewContent() {
    Column(
        modifier = Modifier.fillMaxWidth().background(AppTheme.colors.background).padding(AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.cardSpacing)
    ) {
        ItemCard(
            title = "아메리카노 교환권",
            subtitle = "쿠폰 · 만료 10.20",
            iconRes = AppIcon.coupon,
            statusLabel = "D-6",
            statusTone = StatusBadgeTone.ATTENTION,
            onClick = {}
        )

        ItemCard(
            title = "헤어 커트 예약",
            subtitle = "예약 · 10.21 15:00",
            iconRes = AppIcon.reservation,
            statusLabel = "예정",
            onClick = {}
        )
    }
}

// MARK: - Light / Dark Preview

@Preview(name = "ItemCard Light", showBackground = true)
@Composable
private fun ItemCardLightPreview() {
    JENALJETTAETheme(darkTheme = false) {
        ItemCardPreviewContent()
    }
}

@Preview(name = "ItemCard Dark", showBackground = true)
@Composable
private fun ItemCardDarkPreview() {
    JENALJETTAETheme(darkTheme = true) {
        ItemCardPreviewContent()
    }
}
