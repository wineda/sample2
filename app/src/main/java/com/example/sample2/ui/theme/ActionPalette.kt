package com.example.sample2.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 行動（ActionType）の分類色。
 * 行動カテゴリの種別（ポジティブ行動 / 思考整理 / 負荷フラグ）を
 * 色で区別するためのパレット。
 */
object ActionPalette {
    // ポジティブ行動（運動・社交）
    val Positive = Color(0xFFFB8C00)

    // 思考整理・実行（委譲・チャレンジ・細分化・指示・即実行）
    val Execute = Color(0xFF43A047)

    // 負荷フラグ（保留・会議・スマホ・酒・体調）
    val Load = Color(0xFF5E35B1)

    /**
     * チャート用のカテゴリ識別色（5色）。
     * Personality Analytics の感情/行動チャートで複数系列を
     * 視覚的に区別するために使用。
     */
    val ChartCategories = listOf(
        Color(0xFF1E88E5),
        Color(0xFF43A047),
        Color(0xFFFB8C00),
        Color(0xFFE53935),
        Color(0xFF8E24AA)
    )
}
