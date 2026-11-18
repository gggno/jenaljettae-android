package com.geunho.jenaljettae.domain.repository

import com.geunho.jenaljettae.domain.model.AppThemeMode
import kotlinx.coroutines.flow.Flow

// MARK: - Theme Repository | 테마 조회 및 저장 인터페이스
interface ThemeRepository {
    val selectedTheme: Flow<AppThemeMode>
    suspend fun saveTheme(theme: AppThemeMode)
}
