package com.geunho.jenaljettae.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

// MARK: - Light Theme

private val LightColorScheme = lightColorScheme(
    primary = AppColor.light.primary,
    onPrimary = AppColor.light.onPrimary,
    primaryContainer = AppColor.light.primaryTint,
    onPrimaryContainer = AppColor.light.primaryForeground,

    secondary = AppColor.light.primaryForeground,
    onSecondary = AppColor.light.background,
    tertiary = AppColor.light.primary,
    onTertiary = AppColor.light.onPrimary,

    background = AppColor.light.background,
    onBackground = AppColor.light.textPrimary,
    surface = AppColor.light.card,
    onSurface = AppColor.light.textPrimary,
    surfaceVariant = AppColor.light.surface,
    onSurfaceVariant = AppColor.light.textSecondary,
    outline = AppColor.light.border
)

// MARK: - Dark Theme

private val DarkColorScheme = darkColorScheme(
    primary = AppColor.dark.primary,
    onPrimary = AppColor.dark.onPrimary,
    primaryContainer = AppColor.dark.primaryTint,
    onPrimaryContainer = AppColor.dark.primaryForeground,

    secondary = AppColor.dark.primaryForeground,
    onSecondary = AppColor.dark.background,
    tertiary = AppColor.dark.primary,
    onTertiary = AppColor.dark.onPrimary,

    background = AppColor.dark.background,
    onBackground = AppColor.dark.textPrimary,
    surface = AppColor.dark.card,
    onSurface = AppColor.dark.textPrimary,
    surfaceVariant = AppColor.dark.surface,
    onSurfaceVariant = AppColor.dark.textSecondary,
    outline = AppColor.dark.border
)

// MARK: - App Theme Colors

private val LocalAppColors = staticCompositionLocalOf { AppColor.light }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

// MARK: - App Theme

@Composable
fun JENALJETTAETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) AppColor.dark else AppColor.light
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
