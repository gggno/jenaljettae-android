package com.geunho.jenaljettae.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object AppTypography {

    // MARK: - Title
    val screenTitle = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold)
    val detailTitle = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
    val navigationTitle = TextStyle(fontSize = 17.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold)
    val sectionTitle = TextStyle(fontSize = 17.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold)

    // MARK: - Content
    val itemTitle = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold)
    val inputValue = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal)
    val body = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal)
    val button = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold)

    // MARK: - Supporting Text
    val label = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
    val caption = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal)
    val status = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)

    // MARK: - Number
    val number = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium, fontFeatureSettings = "tnum")
}

// MARK: - Material 3 Typography

val Typography = Typography(
    headlineMedium = AppTypography.screenTitle,
    headlineSmall = AppTypography.detailTitle,
    titleLarge = AppTypography.navigationTitle,
    titleMedium = AppTypography.itemTitle,
    titleSmall = AppTypography.sectionTitle,
    bodyLarge = AppTypography.inputValue,
    bodyMedium = AppTypography.body,
    bodySmall = AppTypography.caption,
    labelLarge = AppTypography.button,
    labelMedium = AppTypography.label,
    labelSmall = AppTypography.status
)
