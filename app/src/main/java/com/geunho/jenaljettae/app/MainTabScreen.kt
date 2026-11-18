package com.geunho.jenaljettae.app

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.DrawableRes
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.geunho.jenaljettae.data.local.AppDatabase
import com.geunho.jenaljettae.data.local.CouponImageStorage
import com.geunho.jenaljettae.data.repository.DataStoreThemeRepository
import com.geunho.jenaljettae.data.repository.LocalCouponImageRepository
import com.geunho.jenaljettae.data.repository.RoomManagedItemRepository
import com.geunho.jenaljettae.data.repository.SampleManagedItemRepository
import com.geunho.jenaljettae.data.repository.SystemNotificationPermissionRepository
import com.geunho.jenaljettae.domain.model.AppThemeMode
import com.geunho.jenaljettae.domain.model.ManagedItem
import com.geunho.jenaljettae.domain.repository.ThemeRepository
import com.geunho.jenaljettae.domain.usecase.SaveCouponUseCase
import com.geunho.jenaljettae.domain.usecase.SavePassUseCase
import com.geunho.jenaljettae.domain.usecase.SaveSubscriptionUseCase
import com.geunho.jenaljettae.domain.usecase.SaveScheduleUseCase
import com.geunho.jenaljettae.features.home.HomeScreen
import com.geunho.jenaljettae.features.home.HomeViewModel
import com.geunho.jenaljettae.features.home.detail.ManagedItemDetailScreen
import com.geunho.jenaljettae.features.home.detail.ManagedItemDetailViewModel
import com.geunho.jenaljettae.features.home.registration.LocalOnCouponSaved
import com.geunho.jenaljettae.features.home.registration.LocalOnPassSaved
import com.geunho.jenaljettae.features.home.registration.LocalOnSubscriptionSaved
import com.geunho.jenaljettae.features.home.registration.LocalOnScheduleSaved
import com.geunho.jenaljettae.features.home.registration.LocalSaveCouponUseCase
import com.geunho.jenaljettae.features.home.registration.LocalSavePassUseCase
import com.geunho.jenaljettae.features.home.registration.LocalSaveSubscriptionUseCase
import com.geunho.jenaljettae.features.home.registration.LocalSaveScheduleUseCase
import com.geunho.jenaljettae.features.home.registration.RegistrationNavHost
import com.geunho.jenaljettae.features.library.LibraryScreen
import com.geunho.jenaljettae.features.library.LibraryViewModel
import com.geunho.jenaljettae.features.settings.SettingsScreen
import com.geunho.jenaljettae.ui.theme.AppIcon
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.JENALJETTAETheme
import kotlinx.coroutines.flow.MutableStateFlow
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
    // MARK: - Theme Repository | 화면 테마 저장소 생성
    val context = LocalContext.current.applicationContext
    val repository = remember(context) { DataStoreThemeRepository(context) }
    val selectedTheme by repository.selectedTheme.collectAsStateWithLifecycle(initialValue = AppThemeMode.SYSTEM)
    val systemDarkTheme = isSystemInDarkTheme()

    val darkTheme = when (selectedTheme) {
        AppThemeMode.SYSTEM -> systemDarkTheme
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    // MARK: - System Bars | 화면 테마에 따라 상태바와 내비게이션 바 아이콘 명암 적용
    val activity = LocalContext.current.findActivity()

    SideEffect {
        activity?.window?.let { window ->
            WindowInsetsControllerCompat(window, window.decorView).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    JENALJETTAETheme(darkTheme = darkTheme) {
        MainTabContent(themeRepository = repository, homeViewModel = homeViewModel)
    }
}

// MARK: - Main Tab Content | 홈·보관함·설정 탭 및 하위 화면
@Composable
private fun MainTabContent(themeRepository: ThemeRepository, homeViewModel: HomeViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    var showsRegistrationTypeSelection by rememberSaveable { mutableStateOf(false) }
    var selectedItemId by rememberSaveable { mutableStateOf<String?>(null) }

    val colors = AppTheme.colors
    val appContext = LocalContext.current.applicationContext
    val notificationPermissionRepository = remember(appContext) { SystemNotificationPermissionRepository(appContext) }

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
            // MARK: - Bottom Navigation | 상세화면에서는 탭바 숨김
            if (selectedItemId == null) {
                Column {
                    NavigationBar(containerColor = colors.card) {
                        MainTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab; selectedItemId = null },
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
        }
    ) { innerPadding ->
        // MARK: - Detail Destination | 홈·보관함 항목 상세화면, 하단 탭 숨김
        val itemId = selectedItemId

        if (itemId != null) {
            ManagedItemDetailDestination(
                itemId = itemId,
                modifier = Modifier.padding(innerPadding),
                onBack = { selectedItemId = null }
            )
        } else {
            // MARK: - Selected Tab | 홈·보관함·설정 탭
            when (selectedTab) {
                MainTab.HOME -> HomeScreen(
                    viewModel = homeViewModel,
                    modifier = Modifier.padding(innerPadding),
                    onAdd = { showsRegistrationTypeSelection = true },
                    onOpenDetail = { selectedItemId = it }
                )

                MainTab.LIBRARY -> LibraryDestination(
                    modifier = Modifier.padding(innerPadding),
                    onOpenDetail = { selectedItemId = it }
                )

                MainTab.SETTINGS -> SettingsScreen(
                    themeRepository = themeRepository,
                    notificationPermissionRepository = notificationPermissionRepository,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

// MARK: - Library Destination | Room을 보관함 ViewModel에 주입
@Composable
private fun LibraryDestination(onOpenDetail: (String) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current.applicationContext
    val repository = remember(context) { RoomManagedItemRepository(AppDatabase.getInstance(context).managedItemDao()) }

    val libraryViewModel: LibraryViewModel = viewModel(
        factory = viewModelFactory { initializer { LibraryViewModel(repository) } }
    )

    LibraryScreen(viewModel = libraryViewModel, onOpenDetail = onOpenDetail, modifier = modifier)
}

// MARK: - Detail Destination | iOS NavigationStack과 동일하게 항목 ID로 조회
@Composable
private fun ManagedItemDetailDestination(itemId: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current.applicationContext
    val repository = remember(context) { RoomManagedItemRepository(AppDatabase.getInstance(context).managedItemDao()) }

    val detailViewModel: ManagedItemDetailViewModel = viewModel(
        key = "managed_item_detail:$itemId",
        factory = viewModelFactory { initializer { ManagedItemDetailViewModel(itemId, repository) } }
    )

    ManagedItemDetailScreen(viewModel = detailViewModel, onBack = onBack, modifier = modifier)
}

// MARK: - Preview Theme Repository | DataStore와 분리된 미리보기용 저장소
private class PreviewThemeRepository(initialTheme: AppThemeMode) : ThemeRepository {
    private val state = MutableStateFlow(initialTheme)

    override val selectedTheme = state

    override suspend fun saveTheme(theme: AppThemeMode) {
        state.value = theme
    }
}

// MARK: - Previews | 라이트·다크 모드
@Preview(name = "Light", showBackground = true)
@Composable
private fun MainTabLightPreview() {
    JENALJETTAETheme(darkTheme = false) {
        MainTabContent(
            themeRepository = remember { PreviewThemeRepository(AppThemeMode.LIGHT) },
            homeViewModel = remember { HomeViewModel(SampleManagedItemRepository()) }
        )
    }
}

@Preview(name = "Dark", showBackground = true)
@Composable
private fun MainTabDarkPreview() {
    JENALJETTAETheme(darkTheme = true) {
        MainTabContent(
            themeRepository = remember { PreviewThemeRepository(AppThemeMode.DARK) },
            homeViewModel = remember { HomeViewModel(SampleManagedItemRepository()) }
        )
    }
}

// MARK: - Activity Context | ContextThemeWrapper에서도 Activity 탐색
private fun Context.findActivity(): Activity? {
    var current: Context = this

    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }

    return current as? Activity
}
