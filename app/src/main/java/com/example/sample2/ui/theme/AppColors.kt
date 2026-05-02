package com.example.sample2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColorScheme(
    val inkPrimary: Color,
    val inkSecondary: Color,
    val inkTertiary: Color,
    val inkDisabled: Color,
    val inkStrongAlt: Color,
    val inkOnInk: Color,
    val surfaceSubtle: Color,
    val surfaceSubtleDeep: Color,
    val surfaceSubtleAlt: Color,
    val surfaceElevated: Color,
    val surfaceCool: Color,
    val surfaceMuted: Color,
    val surfaceLight: Color,
    val borderStrong: Color,
    val dividerSoft: Color,
    val dividerMid: Color,
    val dividerSubtle: Color,
    val dividerCool: Color,
    val dividerNeutral: Color,
    val sleepGradientStart: Color,
    val sleepGradientEnd: Color,
    val scrimDim: Color,
)

val LightAppColors = AppColorScheme(
    inkPrimary = Color(0xFF1A1F26),
    inkSecondary = Color(0xFF5A6371),
    inkTertiary = Color(0xFF8E97A3),
    inkDisabled = Color(0xFFC5CBD3),
    inkStrongAlt = Color(0xFF14181E),
    inkOnInk = Color(0xFFFFFFFF),
    surfaceSubtle = Color(0xFFEEF1F5),
    surfaceSubtleDeep = Color(0xFFE5E9EF),
    surfaceSubtleAlt = Color(0xFFECEFF4),
    surfaceElevated = Color(0xFFFAFBFC),
    surfaceCool = Color(0xFFF1F3F7),
    surfaceMuted = Color(0xFFFAFAFB),
    surfaceLight = Color(0xFFF3F4F6),
    borderStrong = Color(0xFFB8C0CC),
    dividerSoft = Color(0xFFE5E7EB),
    dividerMid = Color(0xFFD1D5DB),
    dividerSubtle = Color(0xFFE0E5EC),
    dividerCool = Color(0xFFE8EBF0),
    dividerNeutral = Color(0xFFE0E2E6),
    sleepGradientStart = Color(0xFFF8FAFD),
    sleepGradientEnd = Color(0xFFF1F4F9),
    scrimDim = Color(0x52000000),
)

val DarkAppColors = AppColorScheme(
    inkPrimary = Color(0xFFECECF1),
    inkSecondary = Color(0xFFB5BBC4),
    inkTertiary = Color(0xFF8A929C),
    inkDisabled = Color(0xFF5A5F66),
    inkStrongAlt = Color(0xFFF1F1F5),
    inkOnInk = Color(0xFF14181E),
    surfaceSubtle = Color(0xFF2A2D35),
    surfaceSubtleDeep = Color(0xFF353841),
    surfaceSubtleAlt = Color(0xFF2E3138),
    surfaceElevated = Color(0xFF1A1C22),
    surfaceCool = Color(0xFF2A2D35),
    surfaceMuted = Color(0xFF1F2128),
    surfaceLight = Color(0xFF2D3038),
    borderStrong = Color(0xFF5A6068),
    dividerSoft = Color(0xFF383B43),
    dividerMid = Color(0xFF4A4E57),
    dividerSubtle = Color(0xFF383B43),
    dividerCool = Color(0xFF383B43),
    dividerNeutral = Color(0xFF383B43),
    sleepGradientStart = Color(0xFF1F2128),
    sleepGradientEnd = Color(0xFF181A1F),
    scrimDim = Color(0x7A000000),
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

val MaterialTheme.appColors: AppColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current
