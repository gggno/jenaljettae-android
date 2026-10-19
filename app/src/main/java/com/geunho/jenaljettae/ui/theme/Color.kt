package com.geunho.jenaljettae.ui.theme

import androidx.compose.ui.graphics.Color

// MARK: - App Color Model

data class AppColors(
    val primary: Color,
    val primaryPressed: Color,
    val onPrimary: Color,
    val primaryForeground: Color,
    val primaryTint: Color,
    val background: Color,
    val card: Color,
    val surface: Color,
    val inputSurface: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val disabledSurface: Color,
    val disabledText: Color,
    val blackSurface: Color,
    val destructiveBackground: Color,
    val onBlackAccent: Color,
    val onBlackSecondary: Color,
    val secondaryPressed: Color
)

// MARK: - App Colors

object AppColor {

    // Light Mode
    val light = AppColors(
        primary = Color(0xFF78273C),
        primaryPressed = Color(0xFF5C1D2D),
        onPrimary = Color(0xFFFFFFFF),
        primaryForeground = Color(0xFF78273C),
        primaryTint = Color(0xFFF6ECEF),

        background = Color(0xFFFFFFFF),
        card = Color(0xFFFFFFFF),
        surface = Color(0xFFF7F5F6),
        inputSurface = Color(0xFFF7F5F6),
        border = Color(0xFFE6E0E3),

        textPrimary = Color(0xFF171417),
        textSecondary = Color(0xFF6B6268),

        disabledSurface = Color(0xFFE6E0E3),
        disabledText = Color(0xFF766B72),

        blackSurface = Color(0xFF171417),
        destructiveBackground = Color(0xFF171417),
        onBlackAccent = Color(0xFFF0B5C8),
        onBlackSecondary = Color(0xFFD3C8CE),
        secondaryPressed = Color(0xFFF7F5F6)
    )

    // Dark Mode
    val dark = AppColors(
        primary = Color(0xFF94334F),
        primaryPressed = Color(0xFF78273C),
        onPrimary = Color(0xFFFFFFFF),
        primaryForeground = Color(0xFFF0B5C8),
        primaryTint = Color(0xFF39242D),

        background = Color(0xFF141214),
        card = Color(0xFF201B1E),
        surface = Color(0xFF201B1E),
        inputSurface = Color(0xFF211D20),
        border = Color(0xFF4F3B44),

        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFC3B7BF),

        disabledSurface = Color(0xFF383036),
        disabledText = Color(0xFFB1A1AB),

        blackSurface = Color(0xFF0C0A0C),
        destructiveBackground = Color(0xFF686368),
        onBlackAccent = Color(0xFFF0B5C8),
        onBlackSecondary = Color(0xFFD3C8CE),
        secondaryPressed = Color(0xFF383036)
    )
}

// MARK: - Default Template Colors (임시 유지)

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650A4)
val PurpleGrey40 = Color(0xFF625B71)
val Pink40 = Color(0xFF7D5260)
