package com.geunho.jenaljettae.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.geunho.jenaljettae.ui.theme.AppIcon
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography

// MARK: - Screen Header

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    onAdd: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.screenHorizontal)
            .padding(top = AppSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xSmall)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = AppTypography.screenTitle,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.weight(1f))

            if (onAdd != null) {
                Box(
                    Modifier
                        .size(width = 48.dp, height = 24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = onAdd
                        ),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        painter = painterResource(AppIcon.add),
                        contentDescription = "항목 추가",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        if (subtitle != null) {
            Text(
                text = subtitle,
                style = AppTypography.body,
                color = colors.textSecondary
            )
        }
    }
}
