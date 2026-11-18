package com.geunho.jenaljettae.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geunho.jenaljettae.domain.model.AppThemeMode
import com.geunho.jenaljettae.domain.model.NotificationPermissionStatus
import com.geunho.jenaljettae.domain.repository.NotificationPermissionRepository
import com.geunho.jenaljettae.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// MARK: - Settings UI State | 설정 화면 표시 상태
data class SettingsUiState(
    val selectedTheme: AppThemeMode = AppThemeMode.SYSTEM,
    val notificationStatus: NotificationPermissionStatus? = null,
    val appVersion: String = "v-"
) {
    val notificationStatusText: String
        get() = when (notificationStatus) {
            null -> "확인 중"
            NotificationPermissionStatus.AUTHORIZED -> "허용됨"
            NotificationPermissionStatus.DENIED -> "허용 안 됨"
            NotificationPermissionStatus.NOT_DETERMINED -> "미설정"
            NotificationPermissionStatus.UNKNOWN -> "확인 필요"
        }
}

// MARK: - Settings ViewModel | 화면 테마 및 알림 권한 상태 관리
class SettingsViewModel(
    private val themeRepository: ThemeRepository,
    private val notificationPermissionRepository: NotificationPermissionRepository,
    appVersion: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState(appVersion = appVersion))
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            themeRepository.selectedTheme.collect { theme ->
                _uiState.update { it.copy(selectedTheme = theme) }
            }
        }
    }

    // MARK: - Select Theme | 변경된 테마 저장
    fun selectTheme(theme: AppThemeMode) {
        if (_uiState.value.selectedTheme == theme) return
        viewModelScope.launch { themeRepository.saveTheme(theme) }
    }

    // MARK: - Refresh Notification Status | 시스템 알림 허용 여부 갱신
    fun refreshNotificationStatus() {
        _uiState.update { it.copy(notificationStatus = notificationPermissionRepository.authorizationStatus()) }
    }
}
