package com.example.sample2.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * メニュー項目の機能を識別するためのアイコン色。
 * 各項目に固有の色を割り当てて視覚的に区別する。
 * tint はアイコン本体、background はアイコン背景の淡色。
 */
data class MenuIconColors(val tint: Color, val background: Color)

object MenuIconPalette {
    // コピー（紫系）
    val Copy = MenuIconColors(tint = Color(0xFF4F46E5), background = Color(0xFFE0E7FF))

    // 共有（緑系）
    val Share = MenuIconColors(tint = Color(0xFF16A34A), background = Color(0xFFDCFCE7))

    // バックアップ（青系）
    val Backup = MenuIconColors(tint = Color(0xFF2563EB), background = Color(0xFFDBEAFE))

    // リストア（オレンジ系）
    val Restore = MenuIconColors(tint = Color(0xFFD97706), background = Color(0xFFFEF3C7))
}
