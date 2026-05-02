package com.example.sample2.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Personality Analytics の 4 スコア指標用の色。
 * stability / anxiety / energy / control を画面横断で統一するためのパレット。
 */
object ScorePalette {
    val Stability = Color(0xFF8D6E63)
    val Anxiety = Color(0xFF8E24AA)
    val Energy = Color(0xFFFB8C00)
    val Control = Color(0xFF43A047)

    // 補助系（睡眠・歩数）
    val Sleep = Color(0xFF3949AB)
    val Steps = Color(0xFF00897B)

    // 比較ライン
    val Comparison = Color(0xFF9E9E9E)
}
