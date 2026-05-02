package com.example.sample2.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.sample2.data.EmotionType

data class EmotionColorSpec(
    val main: Color,    // 強調・枠線・アイコン色に使用
    val tint: Color,    // 背景・チップ色に使用
    val onMain: Color = Color.White
)

object EmotionPalette {
    val Anxiety = EmotionColorSpec(main = Color(0xFF9333EA), tint = Color(0xFFF3E8FF))
    val Angry = EmotionColorSpec(main = Color(0xFFDC2626), tint = Color(0xFFFEE2E2))
    val Sad = EmotionColorSpec(main = Color(0xFF6366F1), tint = Color(0xFFE0E7FF))
    val Happy = EmotionColorSpec(main = Color(0xFFB45309), tint = Color(0xFFFEF3C7))
    val Calm = EmotionColorSpec(main = Color(0xFF3B82F6), tint = Color(0xFFDBEAFE))
    val Neutral = EmotionColorSpec(main = Color(0xFF78716C), tint = Color(0xFFF5F5F4))
}

fun EmotionType.colorSpec(): EmotionColorSpec = when (this) {
    EmotionType.ANXIETY -> EmotionPalette.Anxiety
    EmotionType.ANGRY -> EmotionPalette.Angry
    EmotionType.SAD -> EmotionPalette.Sad
    EmotionType.HAPPY -> EmotionPalette.Happy
    EmotionType.CALM -> EmotionPalette.Calm
}
