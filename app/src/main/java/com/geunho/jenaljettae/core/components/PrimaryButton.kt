package com.geunho.jenaljettae.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geunho.jenaljettae.ui.theme.AppRadius
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import com.geunho.jenaljettae.ui.theme.JENALJETTAETheme

enum class PrimaryButtonRole {
    NORMAL,
    DESTRUCTIVE
}

@Composable
fun PrimaryButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isEnabled: Boolean = true,
    role: PrimaryButtonRole = PrimaryButtonRole.NORMAL
) {
    val colors = AppTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor = when {
        role == PrimaryButtonRole.DESTRUCTIVE -> colors.destructiveBackground
        isPressed -> colors.primaryPressed
        else -> colors.primary
    }

    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        enabled = isEnabled && !isLoading,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(AppRadius.button),
        contentPadding = PaddingValues(horizontal = AppSpacing.medium),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = colors.onPrimary,
            disabledContainerColor = if (isLoading) backgroundColor else colors.disabledSurface,
            disabledContentColor = if (isLoading) colors.onPrimary else colors.disabledText
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = colors.onPrimary, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(AppSpacing.xSmall))
        }

        Text(text = title, style = AppTypography.button)
    }
}

@Composable
private fun PrimaryButtonPreviewContent() {
    Column(
        modifier = Modifier.fillMaxWidth().background(AppTheme.colors.background).padding(AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        PrimaryButton(title = "쿠폰 저장", onClick = {})
        PrimaryButton(title = "저장 중...", isLoading = true, onClick = {})
        PrimaryButton(title = "쿠폰 저장", isEnabled = false, onClick = {})
        PrimaryButton(title = "삭제하기", role = PrimaryButtonRole.DESTRUCTIVE, onClick = {})
    }
}

@Preview(name = "PrimaryButton Light", showBackground = true)
@Composable
private fun PrimaryButtonLightPreview() {
    JENALJETTAETheme(darkTheme = false) {
        PrimaryButtonPreviewContent()
    }
}

@Preview(name = "PrimaryButton Dark", showBackground = true)
@Composable
private fun PrimaryButtonDarkPreview() {
    JENALJETTAETheme(darkTheme = true) {
        PrimaryButtonPreviewContent()
    }
}
