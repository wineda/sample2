package com.example.sample2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val AppDarkColors = darkColorScheme(
    primary = Color(0xFF2A3440),
    onPrimary = Color(0xFFECECF1),
    primaryContainer = Color(0xFF2A2D35),
    onPrimaryContainer = Color(0xFFECECF1),
    secondary = Color(0xFF4C5964),
    onSecondary = Color(0xFFECECF1),
    tertiary = Color(0xFF60707B),
    onTertiary = Color(0xFFECECF1),
    background = Color(0xFF14181E),
    onBackground = Color(0xFFECECF1),
    surface = Color(0xFF1A1C22),
    onSurface = Color(0xFFECECF1),
    surfaceVariant = Color(0xFF2E3138),
    onSurfaceVariant = Color(0xFFB5BBC4),
    outline = Color(0xFF5A6068),
    scrim = Color(0x7A000000),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
)

private val AppLightColors = lightColorScheme(
    primary = Color(0xFF2A3440),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE5EC),
    onPrimaryContainer = Color(0xFF24323D),
    secondary = Color(0xFF4C5964),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE4EBF0),
    onSecondaryContainer = Color(0xFF2C3740),
    tertiary = Color(0xFF60707B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEAF0F4),
    onTertiaryContainer = Color(0xFF2F3A42),
    background = Color(0xFFF7F7F8),
    onBackground = Color(0xFF202123),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF202123),
    surfaceVariant = Color(0xFFF0F0F0),
    onSurfaceVariant = Color(0xFF666666),
    outline = Color(0xFFD0D0D0),
    outlineVariant = Color(0xFFE3E3E3),
    inverseSurface = Color(0xFF2A2A2A),
    inverseOnSurface = Color(0xFFF5F5F5),
    inversePrimary = Color(0xFFBFCAD4),
    surfaceTint = Color(0xFF2A3440),
    scrim = Color(0x66000000),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
)

@Composable
fun ChatGptTheme(useDarkTheme: Boolean = false, content: @Composable () -> Unit) {
    val colorScheme = if (useDarkTheme) AppDarkColors else AppLightColors
    val appColorScheme = if (useDarkTheme) DarkAppColors else LightAppColors
    CompositionLocalProvider(LocalAppColors provides appColorScheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content,
        )
    }
}
