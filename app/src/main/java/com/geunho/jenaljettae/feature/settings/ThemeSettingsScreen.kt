package com.geunho.jenaljettae.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.geunho.jenaljettae.domain.model.AppThemeMode
import com.geunho.jenaljettae.ui.components.AppNavigationHeader
import com.geunho.jenaljettae.ui.theme.AppIcon
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import com.geunho.jenaljettae.ui.theme.description
import com.geunho.jenaljettae.ui.theme.iconRes
import com.geunho.jenaljettae.ui.theme.title

// MARK: - Theme Settings Screen | 화면 테마 선택
@Composable
fun ThemeSettingsScreen(
    selectedTheme: AppThemeMode,
    onSelectTheme: (AppThemeMode) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Column(modifier = modifier.fillMaxSize().background(colors.background)) {
        // MARK: - Header | 화면 테마
        AppNavigationHeader(title = "화면 테마", onBack = onBack)

        Column(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.screenHorizontal)
                .padding(top = AppSpacing.large, bottom = AppSpacing.large),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
        ) {
            Text("앱에서 사용할 화면 스타일을 선택해 주세요.", style = AppTypography.body, color = colors.textSecondary)
            Spacer(Modifier.height(8.dp))

            AppThemeMode.entries.forEach { theme ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.textPrimary.copy(alpha = 0.045f))
                        .clickable { onSelectTheme(theme) }
                        .heightIn(min = 64.dp)
                        .padding(AppSpacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
                ) {
                    Icon(painterResource(theme.iconRes), contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(24.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(theme.title, style = AppTypography.body, color = colors.textPrimary)
                        Text(theme.description, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
                    if (selectedTheme == theme) {
                        Icon(painterResource(AppIcon.check), contentDescription = "선택됨", tint = colors.primaryForeground, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
