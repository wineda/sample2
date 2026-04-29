package com.example.sample2.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.sample2.data.EmotionType

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val CategoryWork = Color(0xFF2D6A4F)
val CategoryExerciseBody = Color(0xFFD97706)
val CategorySleep = Color(0xFF4338CA)
val CategoryMorningHabit = Color(0xFF7C3AED)
val CategoryEmotionNegative = Color(0xFFB91C1C)
val CategoryEmotionPositive = Color(0xFF15803D)

data class EmotionColor(
    val border: Color,
    val tint: Color
)

val EmotionAnxietyColor = EmotionColor(
    border = Color(0xFF9333EA),
    tint = Color(0xFFF3E8FF)
)
val EmotionAngryColor = EmotionColor(
    border = Color(0xFFDC2626),
    tint = Color(0xFFFEE2E2)
)
val EmotionTiredColor = EmotionColor(
    border = Color(0xFF78716C),
    tint = Color(0xFFF5F5F4)
)
val EmotionHappyColor = EmotionColor(
    border = Color(0xFFB45309),
    tint = Color(0xFFFEF3C7)
)
val EmotionCalmColor = EmotionColor(
    border = Color(0xFF3B82F6),
    tint = Color(0xFFDBEAFE)
)
val EmotionSadColor = EmotionColor(
    border = Color(0xFF6366F1),
    tint = Color(0xFFE0E7FF)
)
val EmotionNeutralColor = EmotionColor(
    border = Color(0xFF78716C),
    tint = Color(0xFFF5F5F4)
)

fun emotionCategoryToColor(emotionType: EmotionType?): EmotionColor {
    return when (emotionType) {
        EmotionType.ANXIETY -> EmotionAnxietyColor
        EmotionType.ANGRY -> EmotionAngryColor
        EmotionType.SAD -> EmotionSadColor
        EmotionType.HAPPY -> EmotionHappyColor
        EmotionType.CALM -> EmotionCalmColor
        null -> EmotionNeutralColor
    }
}
