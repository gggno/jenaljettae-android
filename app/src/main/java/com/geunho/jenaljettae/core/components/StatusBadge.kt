package com.geunho.jenaljettae.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import com.geunho.jenaljettae.ui.theme.JENALJETTAETheme

// MARK: - Status Badge Tone

enum class StatusBadgeTone {
    ATTENTION,
    NEUTRAL
}

// MARK: - Status Badge

@Composable
fun StatusBadge(
    label: String,
    modifier: Modifier = Modifier,
    tone: StatusBadgeTone = StatusBadgeTone.NEUTRAL
) {
    val colors = AppTheme.colors
    val isAttention = tone == StatusBadgeTone.ATTENTION

    // 상태에 따라 배경색, 글자색, 테두리를 구분
    val backgroundColor = if (isAttention) colors.primaryTint else colors.inputSurface
    val foregroundColor = if (isAttention) colors.primaryForeground else colors.textSecondary
    val border = if (isAttention) null else BorderStroke(1.dp, colors.border)

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = backgroundColor,
        border = border
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = AppSpacing.small, vertical = AppSpacing.xxSmall),
            style = AppTypography.status,
            color = foregroundColor
        )
    }
}

// MARK: - Preview Content

@Composable
private fun StatusBadgePreviewContent() {
    Column(
        modifier = Modifier.fillMaxWidth().background(AppTheme.colors.background).padding(AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            StatusBadge(label = "오늘", tone = StatusBadgeTone.ATTENTION)
            StatusBadge(label = "D-3", tone = StatusBadgeTone.ATTENTION)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            StatusBadge(label = "완료")
            StatusBadge(label = "날짜 미등록")
        }
    }
}

// MARK: - Light / Dark Preview

@Preview(name = "StatusBadge Light", showBackground = true)
@Composable
private fun StatusBadgeLightPreview() {
    JENALJETTAETheme(darkTheme = false) {
        StatusBadgePreviewContent()
    }
}

@Preview(name = "StatusBadge Dark", showBackground = true)
@Composable
private fun StatusBadgeDarkPreview() {
    JENALJETTAETheme(darkTheme = true) {
        StatusBadgePreviewContent()
    }
}
