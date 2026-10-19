package com.geunho.jenaljettae.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geunho.jenaljettae.ui.theme.AppRadius
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import com.geunho.jenaljettae.ui.theme.JENALJETTAETheme

// MARK: - Secondary Button

@Composable
fun SecondaryButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true
) {
    val colors = AppTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 비활성화 및 눌림 상태에 따른 배경색
    val backgroundColor = when {
        !isEnabled -> colors.disabledSurface
        isPressed -> colors.secondaryPressed
        else -> colors.card
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        enabled = isEnabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(AppRadius.button),
        color = backgroundColor,
        border = BorderStroke(1.dp, if (isEnabled) colors.border else colors.disabledSurface)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.medium, vertical = AppSpacing.small),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = AppTypography.button,
                color = if (isEnabled) colors.textPrimary else colors.disabledText
            )
        }
    }
}

// MARK: - Preview Content

@Composable
private fun SecondaryButtonPreviewContent() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        PrimaryButton(title = "쿠폰 저장", onClick = {})

        SecondaryButton(title = "취소", onClick = {})

        SecondaryButton(title = "다른 사진 선택", onClick = {})

        SecondaryButton(title = "비활성화", isEnabled = false, onClick = {})
    }
}

// MARK: - Light / Dark Preview

@Preview(name = "SecondaryButton Light", showBackground = true)
@Composable
private fun SecondaryButtonLightPreview() {
    JENALJETTAETheme(darkTheme = false) {
        SecondaryButtonPreviewContent()
    }
}

@Preview(name = "SecondaryButton Dark", showBackground = true)
@Composable
private fun SecondaryButtonDarkPreview() {
    JENALJETTAETheme(darkTheme = true) {
        SecondaryButtonPreviewContent()
    }
}
