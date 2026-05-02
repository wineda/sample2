package com.example.sample2.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 意味を持つアクセント色。
 * 「ポジティブ」「ネガティブ」「警告」「情報」を画面横断で統一する。
 * main は前景（テキスト・アイコン・枠線）、soft は背景（バッジ等）。
 */
object SemanticColors {
    // ポジティブ（達成・良好・入力済み）
    val PositiveMain = Color(0xFF4A8A5C)
    val PositiveSoft = Color(0xFFE5F0EA)

    // ネガティブ（減少・体調不良・エラー）
    val NegativeMain = Color(0xFFC25450)
    val NegativeSoft = Color(0xFFF4E8E6)

    // 警告・注意（DIFFICULTIES、warn）
    val WarningMain = Color(0xFFC89232)
    val WarningSoft = Color(0xFFF7EFE0)

    // 情報・アクセント（青）
    val InfoMain = Color(0xFF4A6FA5)
    val InfoSoft = Color(0xFFE8EEF7)
}
