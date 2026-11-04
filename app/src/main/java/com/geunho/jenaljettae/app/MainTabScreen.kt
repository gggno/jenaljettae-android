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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.geunho.jenaljettae.data.local.AppDatabase
import com.geunho.jenaljettae.data.local.CouponImageStorage
import com.geunho.jenaljettae.data.repository.LocalCouponImageRepository
import com.geunho.jenaljettae.data.repository.RoomManagedItemRepository
import com.geunho.jenaljettae.data.repository.SampleManagedItemRepository
import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.usecase.SaveCouponUseCase
import com.geunho.jenaljettae.features.home.HomeScreen
import com.geunho.jenaljettae.features.home.HomeViewModel
import com.geunho.jenaljettae.features.home.registration.LocalOnCouponSaved
import com.geunho.jenaljettae.features.home.registration.LocalSaveCouponUseCase
import com.geunho.jenaljettae.features.home.registration.RegistrationNavHost
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

// MARK: - Main Tab Screen | 탭 이동과 Room Repository 연결
@Composable
fun MainTabScreen(
    homeViewModel: HomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val application = checkNotNull(
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                )
                val database = AppDatabase.getInstance(application)
                val repository = RoomManagedItemRepository(database.managedItemDao())
                HomeViewModel(repository)
            }
        }
    )
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var showsRegistrationTypeSelection by rememberSaveable { mutableStateOf(false) }
    val colors = AppTheme.colors

    // MARK: - Registration | 저장 UseCase와 완료 콜백을 등록 NavHost에 제공
    if (showsRegistrationTypeSelection) {
        val context = LocalContext.current.applicationContext
        val saveCouponUseCase = remember(context) {
            val database = AppDatabase.getInstance(context)
            val repository = RoomManagedItemRepository(database.managedItemDao())
            val imageStorage = CouponImageStorage(context)
            val imageRepository = LocalCouponImageRepository(imageStorage)

            SaveCouponUseCase(repository, imageRepository)
        }

        val onCouponSaved: (ManagedItem) -> Unit = { savedItem ->
            homeViewModel.pickDate(savedItem.endDate ?: savedItem.startDate)
            selectedTab = MainTab.HOME
            showsRegistrationTypeSelection = false
        }

        CompositionLocalProvider(
            LocalSaveCouponUseCase provides saveCouponUseCase,
            LocalOnCouponSaved provides onCouponSaved
        ) {
            RegistrationNavHost(
                onClose = { showsRegistrationTypeSelection = false }
            )
        }
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
        // MARK: - Selected Tab | 선택한 탭의 화면 표시
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
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = AppSpacing.screenHorizontal,
                vertical = AppSpacing.large
            ),
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

// MARK: - Light Preview | 라이트 모드 미리보기
@Preview(name = "Light", showBackground = true)
@Composable
private fun MainTabLightPreview() {
    JENALJETTAETheme(darkTheme = false) {
        MainTabScreen(homeViewModel = HomeViewModel(SampleManagedItemRepository()))
    }
}

// MARK: - Dark Preview | 다크 모드 미리보기
@Preview(name = "Dark", showBackground = true)
@Composable
private fun MainTabDarkPreview() {
    JENALJETTAETheme(darkTheme = true) {
        MainTabScreen(homeViewModel = HomeViewModel(SampleManagedItemRepository()))
    }
}
