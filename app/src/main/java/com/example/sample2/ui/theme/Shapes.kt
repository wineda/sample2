package com.example.sample2.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * アプリ全体の形状（角丸）トークン。
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** MaterialTheme.shapes に乗らないアプリ独自の形状。 */
object AppShapeTokens {
    /** 完全な丸/ピル（チップ・FAB・ステータスバッジ） */
    val Pill = RoundedCornerShape(999.dp)
    /** Reflection 系のテクニカル/レポート風の角（極小） */
    val Tech = RoundedCornerShape(2.dp)
}
