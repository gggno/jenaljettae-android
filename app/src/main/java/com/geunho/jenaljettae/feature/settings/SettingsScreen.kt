package com.geunho.jenaljettae.features.settings

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.geunho.jenaljettae.domain.repository.NotificationPermissionRepository
import com.geunho.jenaljettae.domain.repository.ThemeRepository
import com.geunho.jenaljettae.ui.theme.AppIcon
import com.geunho.jenaljettae.ui.theme.AppSpacing
import com.geunho.jenaljettae.ui.theme.AppTheme
import com.geunho.jenaljettae.ui.theme.AppTypography
import com.geunho.jenaljettae.ui.theme.title

private enum class SettingsPage { MAIN, NOTIFICATION, THEME }

// MARK: - Settings Screen | 설정 메인 및 하위 화면 관리
@Composable
fun SettingsScreen(
    themeRepository: ThemeRepository,
    notificationPermissionRepository: NotificationPermissionRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current.applicationContext
    val version = remember(context) { getVersionLabel(context) }
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = viewModelFactory { initializer { SettingsViewModel(themeRepository, notificationPermissionRepository, version) } }
    )
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    var page by rememberSaveable { mutableStateOf(SettingsPage.MAIN) }

    BackHandler(enabled = page != SettingsPage.MAIN) { page = SettingsPage.MAIN }
    LaunchedEffect(settingsViewModel) { settingsViewModel.refreshNotificationStatus() }

    // MARK: - Permission Refresh | 앱 시스템 설정에서 돌아올 때 권한 다시 확인
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, settingsViewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) settingsViewModel.refreshNotificationStatus()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when (page) {
        SettingsPage.MAIN -> SettingsMainContent(
            permissionText = uiState.notificationStatusText,
            themeText = uiState.selectedTheme.title,
            appVersion = uiState.appVersion,
            onNotification = { page = SettingsPage.NOTIFICATION },
            onTheme = { page = SettingsPage.THEME },
            modifier = modifier
        )
        SettingsPage.NOTIFICATION -> SettingsPlaceholderScreen(onBack = { page = SettingsPage.MAIN }, modifier = modifier)
        SettingsPage.THEME -> ThemeSettingsScreen(
            selectedTheme = uiState.selectedTheme,
            onSelectTheme = settingsViewModel::selectTheme,
            onBack = { page = SettingsPage.MAIN },
            modifier = modifier
        )
    }
}

// MARK: - Settings Main Content | 설정 메인 화면 UI
@Composable
private fun SettingsMainContent(
    permissionText: String,
    themeText: String,
    appVersion: String,
    onNotification: () -> Unit,
    onTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier.fillMaxSize().background(colors.background).verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.screenHorizontal)
            .padding(top = 16.dp, bottom = AppSpacing.large),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
    ) {
        Text("설정", style = AppTypography.screenTitle, color = colors.textPrimary)

        SettingsSection("알림") {
            SettingsMenuRow(AppIcon.bell, "알림 설정", permissionText, onClick = onNotification)
        }
        SettingsSection("화면") {
            SettingsMenuRow(AppIcon.theme, "화면 테마", themeText, onClick = onTheme)
        }
        SettingsSection("앱") {
            SettingsMenuRow(AppIcon.info, "앱 정보", appVersion)
        }
    }
}

// MARK: - Settings Section | 설정 구역 제목
@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
        Text(title, style = AppTypography.body, color = colors.textPrimary)
        content()
    }
}

// MARK: - Settings Menu Row | 설정 공통 메뉴 행
@Composable
private fun SettingsMenuRow(
    @DrawableRes iconRes: Int,
    title: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(16.dp)
    val clickableModifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)

    Row(
        modifier = Modifier.fillMaxWidth().clip(shape).background(colors.textPrimary.copy(alpha = 0.045f))
            .then(clickableModifier).heightIn(min = 52.dp).padding(AppSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        Icon(painterResource(iconRes), contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(22.dp))
        Text(title, style = AppTypography.body, color = colors.textPrimary)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        if (onClick != null) {
            Icon(painterResource(AppIcon.chevronRight), contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
        }
    }
}

// MARK: - Settings Placeholder Screen | 알림 설정 준비 화면
@Composable
private fun SettingsPlaceholderScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Column(modifier = modifier.fillMaxSize().background(colors.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.screenHorizontal), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(painterResource(AppIcon.back), contentDescription = "뒤로가기", tint = colors.textPrimary, modifier = Modifier.size(24.dp))
            }
            Text("알림 설정", style = AppTypography.body, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
        }
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("설정 화면 개발 예정", style = AppTypography.body, color = colors.textSecondary)
        }
    }
}

// MARK: - App Version | 설치된 앱 버전 조회
@Suppress("DEPRECATION")
private fun getVersionLabel(context: Context): String {
    val version = runCatching {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        packageInfo.versionName ?: "-"
    }.getOrDefault("-")
    return "v$version"
}
