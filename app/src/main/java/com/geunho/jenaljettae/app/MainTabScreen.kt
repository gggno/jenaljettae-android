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
import com.geunho.jenaljettae.domain.usecase.SavePassUseCase
import com.geunho.jenaljettae.domain.usecase.SaveSubscriptionUseCase
import com.geunho.jenaljettae.domain.usecase.SaveScheduleUseCase
import com.geunho.jenaljettae.features.home.HomeScreen
import com.geunho.jenaljettae.features.home.HomeViewModel
import com.geunho.jenaljettae.features.home.registration.LocalOnCouponSaved
import com.geunho.jenaljettae.features.home.registration.LocalOnPassSaved
import com.geunho.jenaljettae.features.home.registration.LocalOnSubscriptionSaved
import com.geunho.jenaljettae.features.home.registration.LocalOnScheduleSaved
import com.geunho.jenaljettae.features.home.registration.LocalSaveCouponUseCase
import com.geunho.jenaljettae.features.home.registration.LocalSavePassUseCase
import com.geunho.jenaljettae.features.home.registration.LocalSaveSubscriptionUseCase
import com.geunho.jenaljettae.features.home.registration.LocalSaveScheduleUseCase
import com.geunho.jenaljettae.features.home.registration.RegistrationNavHost
import com.geunho.jenaljettae.ui.theme.AppIcon
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import com.geunho.jenaljettae.ui.theme.JENALJETTAETheme
import java.time.LocalDate

// MARK: - Registration Dependencies | 네 유형의 저장 UseCase 관리
private data class RegistrationDependencies(
    val coupon: SaveCouponUseCase,
    val pass: SavePassUseCase,
    val subscription: SaveSubscriptionUseCase,
    val schedule: SaveScheduleUseCase
)

// MARK: - Main Tab | 탭 정보
private enum class MainTab(
    val title: String,
    @DrawableRes val iconRes: Int
) {
    HOME("홈", AppIcon.home),
    LIBRARY("보관함", AppIcon.vault),
    SETTINGS("설정", AppIcon.settings)
}

// MARK: - Main Tab Screen | 탭 및 등록 화면 연결
@Composable
fun MainTabScreen(
    homeViewModel: HomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val application = checkNotNull(
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                )
                val database = AppDatabase.getInstance(application)
                HomeViewModel(RoomManagedItemRepository(database.managedItemDao()))
            }
        }
    )
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var showsRegistrationTypeSelection by rememberSaveable { mutableStateOf(false) }
    val colors = AppTheme.colors

    // MARK: - Registration | UseCase 및 저장 완료 콜백
    if (showsRegistrationTypeSelection) {
        val context = LocalContext.current.applicationContext

        val useCases = remember(context) {
            val database = AppDatabase.getInstance(context)
            val repository = RoomManagedItemRepository(database.managedItemDao())
            val imageRepository = LocalCouponImageRepository(CouponImageStorage(context))

            RegistrationDependencies(
                coupon = SaveCouponUseCase(repository, imageRepository),
                pass = SavePassUseCase(repository, imageRepository),
                subscription = SaveSubscriptionUseCase(repository, imageRepository),
                schedule = SaveScheduleUseCase(repository, imageRepository)
            )
        }

        // MARK: - Coupon Saved | 쿠폰 만료일로 이동
        val onCouponSaved: (ManagedItem) -> Unit = { savedItem ->
            homeViewModel.pickDate(savedItem.endDate ?: savedItem.startDate)
            selectedTab = MainTab.HOME
            showsRegistrationTypeSelection = false
        }

        // MARK: - Pass Saved | 이용권 종료일로 이동
        val onPassSaved: (ManagedItem) -> Unit = { savedItem ->
            homeViewModel.pickDate(savedItem.endDate ?: savedItem.startDate)
            selectedTab = MainTab.HOME
            showsRegistrationTypeSelection = false
        }

        // MARK: - Subscription Saved | 시작일 또는 다음 반복 결제일로 이동
        val onSubscriptionSaved: (ManagedItem) -> Unit = { savedItem ->
            val today = LocalDate.now()

            val targetDate = if (!savedItem.startDate.isBefore(today)) {
                savedItem.startDate
            } else {
                savedItem.subscriptionCycle?.upcomingBillingDates(
                    startDate = savedItem.startDate,
                    count = 1,
                    referenceDate = today,
                    includeStartDate = false
                )?.firstOrNull() ?: savedItem.startDate
            }

            homeViewModel.pickDate(targetDate)
            selectedTab = MainTab.HOME
            showsRegistrationTypeSelection = false
        }

        // MARK: - Schedule Saved | 일정 시작일로 이동
        val onScheduleSaved: (ManagedItem) -> Unit = { savedItem ->
            homeViewModel.pickDate(savedItem.startDate)
            selectedTab = MainTab.HOME
            showsRegistrationTypeSelection = false
        }

        // MARK: - Dependency Injection | Compose CompositionLocal
        CompositionLocalProvider(
            LocalSaveCouponUseCase provides useCases.coupon,
            LocalOnCouponSaved provides onCouponSaved,
            LocalSavePassUseCase provides useCases.pass,
            LocalOnPassSaved provides onPassSaved,
            LocalSaveSubscriptionUseCase provides useCases.subscription,
            LocalOnSubscriptionSaved provides onSubscriptionSaved,
            LocalSaveScheduleUseCase provides useCases.schedule,
            LocalOnScheduleSaved provides onScheduleSaved
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
            // MARK: - Bottom Navigation | 기존 하단 탭바
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
        // MARK: - Selected Tab | 선택한 화면 표시
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

// MARK: - Temporary Tab Screen | 보관함·설정 임시 화면
@Composable
private fun TabPlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier
) {
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

// MARK: - Previews | 라이트·다크 모드
@Preview(name = "Light", showBackground = true)
@Composable
private fun MainTabLightPreview() {
    JENALJETTAETheme(darkTheme = false) {
        MainTabScreen(
            homeViewModel = HomeViewModel(SampleManagedItemRepository())
        )
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun MainTabDarkPreview() {
    JENALJETTAETheme(darkTheme = true) {
        MainTabScreen(
            homeViewModel = HomeViewModel(SampleManagedItemRepository())
        )
    }
}
