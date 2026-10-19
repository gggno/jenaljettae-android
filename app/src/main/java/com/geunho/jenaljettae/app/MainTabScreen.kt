
package com.geunho.jenaljettae.app

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.geunho.jenaljettae.ui.theme.AppIcon
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import com.geunho.jenaljettae.ui.theme.JENALJETTAETheme

// MARK: - Main Tab

private enum class MainTab(val title: String, @DrawableRes val iconRes: Int) {
    HOME("홈", AppIcon.home),
    LIBRARY("보관함", AppIcon.vault),
    SETTINGS("설정", AppIcon.settings)
}

// MARK: - Main Tab Screen

@Composable
fun MainTabScreen() {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    val colors = AppTheme.colors

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        bottomBar = {

            // MARK: - Bottom Navigation Bar

            Column {
                NavigationBar(containerColor = colors.card) {
                    MainTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    painter = painterResource(tab.iconRes),
                                    contentDescription = null
                                )
                            },
                            label = { Text(tab.title) },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = colors.primaryForeground,
                                selectedTextColor = colors.primaryForeground,
                                indicatorColor = colors.primaryTint,
                                unselectedIconColor = colors.textSecondary,
                                unselectedTextColor = colors.textSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        // 실제 화면 개발 전에는 선택된 탭의 임시 화면 표시
        TabPlaceholderScreen(
            title = selectedTab.title,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

// MARK: - Temporary Tab Screen

@Composable
private fun TabPlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = AppTypography.screenTitle,
            color = colors.textPrimary
        )

        Text(
            text = "$title 화면 개발 예정",
            style = AppTypography.body,
            color = colors.textSecondary
        )
    }
}

// MARK: - Light / Dark Preview

@Preview(name = "Light", showBackground = true)
@Composable
private fun MainTabLightPreview() {
    JENALJETTAETheme(darkTheme = false) {
        MainTabScreen()
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun MainTabDarkPreview() {
    JENALJETTAETheme(darkTheme = true) {
        MainTabScreen()
    }
}
