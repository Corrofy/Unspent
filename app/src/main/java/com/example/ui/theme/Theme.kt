package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val TokyoExtendedColors = AppExtendedColors(
    greenIncome = TokyoGreen,
    redSpend = TokyoRed,
    yellowPending = TokyoYellow,
    glassBorder = TokyoBorder,
    glassGlow = TokyoGlassGlow,
    elevatedSurface = TokyoSurfaceElevated,
    mutedText = TokyoTextSecondary
)

val LocalAppExtendedColors = staticCompositionLocalOf {
    TokyoExtendedColors
}

private val TokyoNightColorScheme = darkColorScheme(
    primary = TokyoPrimary,
    onPrimary = TokyoTextDark,
    primaryContainer = TokyoSurfaceElevated,
    onPrimaryContainer = TokyoPrimary,
    secondary = TokyoSecondary,
    onSecondary = TokyoTextDark,
    secondaryContainer = TokyoSurfaceElevated,
    onSecondaryContainer = TokyoSecondary,
    tertiary = TokyoTertiary,
    onTertiary = TokyoTextDark,
    background = TokyoBackground,
    onBackground = TokyoTextPrimary,
    surface = TokyoSurface,
    onSurface = TokyoTextPrimary,
    surfaceVariant = TokyoSurfaceElevated,
    onSurfaceVariant = TokyoTextSecondary,
    outline = TokyoBorder,
    outlineVariant = TokyoBorderSubtle,
    error = TokyoRed,
    onError = TokyoTextDark
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAppExtendedColors provides TokyoExtendedColors
    ) {
        MaterialTheme(
            colorScheme = TokyoNightColorScheme,
            typography = Typography,
            content = content
        )
    }
}

