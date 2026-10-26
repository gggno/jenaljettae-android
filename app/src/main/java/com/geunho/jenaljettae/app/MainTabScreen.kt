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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.geunho.jenaljettae.data.repository.SampleManagedItemRepository
import com.geunho.jenaljettae.features.home.HomeScreen
import com.geunho.jenaljettae.features.home.HomeViewModel
import com.geunho.jenaljettae.features.home.RegistrationTypeSelectionScreen
import com.geunho.jenaljettae.ui.theme.AppIcon
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import com.geunho.jenaljettae.ui.theme.JENALJETTAETheme

// MARK: - Main Tab | 하단 탭과 아이콘 정의
private enum class MainTab(val title: String, @DrawableRes val iconRes: Int) {
    HOME("홈", AppIcon.home),
    LIBRARY("보관함", AppIcon.vault),
    SETTINGS("설정", AppIcon.settings)
}

// MARK: - Main Tab Screen | 탭 이동과 HomeViewModel 생성·주입 담당
@Composable
fun MainTabScreen(
    // MARK: - Dependency Injection | 앱에서는 ViewModelStore에 보관하고 Preview에서는 직접 주입 가능
    homeViewModel: HomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer { HomeViewModel(SampleManagedItemRepository()) }
        }
    )
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var showsRegistrationTypeSelection by rememberSaveable { mutableStateOf(false) }
    val colors = AppTheme.colors

    // MARK: - Registration | 등록 유형 선택 화면을 열면 기존 탭 화면을 일시 교체
    if (showsRegistrationTypeSelection) {
        RegistrationTypeSelectionScreen(onClose = { showsRegistrationTypeSelection = false })
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        bottomBar = {
            // MARK: - Bottom Navigation | 기존 하단 탭바 디자인 유지
            Column {
                NavigationBar(containerColor = colors.card) {
                    MainTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = { Icon(painter = painterResource(tab.iconRes), contentDescription = null) },
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
        // MARK: - Selected Tab | ViewModel을 홈 화면에 전달
        when (selectedTab) {
            MainTab.HOME -> HomeScreen(
                viewModel = homeViewModel,
                modifier = Modifier.padding(innerPadding),
                onAdd = { showsRegistrationTypeSelection = true }
            )

            MainTab.LIBRARY, MainTab.SETTINGS -> TabPlaceholderScreen(
                title = selectedTab.title,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

// MARK: - Temporary Tab Screen | 보관함·설정 준비 중 화면
@Composable
private fun TabPlaceholderScreen(title: String, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier.fillMaxSize()
            .padding(horizontal = AppSpacing.screenHorizontal, vertical = AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = title, style = AppTypography.screenTitle, color = colors.textPrimary)
        Text(text = "$title 화면 개발 예정", style = AppTypography.body, color = colors.textSecondary)
    }
}

// MARK: - Light / Dark Preview | 기존 테마 미리보기
@Preview(name = "Light", showBackground = true)
@Composable
private fun MainTabLightPreview() {
    JENALJETTAETheme(darkTheme = false) { MainTabScreen(homeViewModel = HomeViewModel(SampleManagedItemRepository())) }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun MainTabDarkPreview() {
    JENALJETTAETheme(darkTheme = true) { MainTabScreen(homeViewModel = HomeViewModel(SampleManagedItemRepository())) }
}
