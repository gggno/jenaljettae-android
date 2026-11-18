package com.geunho.jenaljettae.ui.theme

import androidx.annotation.DrawableRes
import com.geunho.jenaljettae.domain.model.AppThemeMode

// MARK: - Theme Title | 테마 표시 이름
val AppThemeMode.title: String
    get() = when (this) {
        AppThemeMode.SYSTEM -> "시스템 설정"
        AppThemeMode.LIGHT -> "라이트 모드"
        AppThemeMode.DARK -> "다크 모드"
    }

// MARK: - Theme Description | 테마 선택 안내
val AppThemeMode.description: String
    get() = when (this) {
        AppThemeMode.SYSTEM -> "기기 설정에 따라 자동으로 변경돼요."
        AppThemeMode.LIGHT -> "항상 밝은 화면으로 표시해요."
        AppThemeMode.DARK -> "항상 어두운 화면으로 표시해요."
    }

// MARK: - Theme Icon | 테마 아이콘 매핑
@get:DrawableRes
val AppThemeMode.iconRes: Int
    get() = when (this) {
        AppThemeMode.SYSTEM -> AppIcon.system
        AppThemeMode.LIGHT -> AppIcon.light
        AppThemeMode.DARK -> AppIcon.dark
    }
