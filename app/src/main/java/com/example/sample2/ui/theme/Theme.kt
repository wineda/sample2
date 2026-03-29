package com.example.sample2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val ChatGptDarkColors = darkColorScheme(
    primary = Color(0xFF10A37F),        // ChatGPTっぽい緑
    onPrimary = Color.White,
    background = Color(0xFF343541),     // メイン背景
    onBackground = Color(0xFFECECF1),
    surface = Color(0xFF444654),        // カード・ダイアログ
    onSurface = Color(0xFFECECF1),
    surfaceVariant = Color(0xFF3E3F4B), // 入力欄・微妙に違う面
    onSurfaceVariant = Color(0xFFC5C5D2),
    outline = Color(0xFF565869)         // 枠線
)

private val ChatGptLightColors = lightColorScheme(
    // 選択状態や強調に使う主色：低彩度の青みグレー
    primary = Color(0xFF2A3440),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE5EC),
    onPrimaryContainer = Color(0xFF24323D),

    // 補助色：少しだけ柔らかい青みグレー
    secondary = Color(0xFF4C5964),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE4EBF0),
    onSecondaryContainer = Color(0xFF2C3740),

    // 選択ヘッダや別種の選択面向け
    tertiary = Color(0xFF60707B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEAF0F4),
    onTertiaryContainer = Color(0xFF2F3A42),

    // 画面全体は落ち着いたニュートラル
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
    onErrorContainer = Color(0xFF410E0B)
)

@Composable
fun ChatGptTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ChatGptLightColors,
        typography = Typography(), // 必要ならカスタム
        content = content
    )
}
