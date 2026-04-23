package com.example.sample2.ui

import androidx.compose.ui.graphics.Color
import com.example.sample2.R
import com.example.sample2.data.ActionType
import com.example.sample2.data.EmotionType

data class EmotionUiSpec(
    val iconRes: Int,
    val color: Color
)

data class ActionUiSpec(
    val iconRes: Int,
    val color: Color
)

fun EmotionType.toUiSpec(): EmotionUiSpec {
    return when (this) {
        EmotionType.ANXIETY -> EmotionUiSpec(R.drawable.ic_anxiety, Color(0xFF9C27B0))
        EmotionType.ANGRY -> EmotionUiSpec(R.drawable.ic_angry, Color(0xFFE53935))
        EmotionType.SAD -> EmotionUiSpec(R.drawable.ic_sad, Color(0xFF1E88E5))
        EmotionType.HAPPY -> EmotionUiSpec(R.drawable.ic_happy, Color(0xFFFB8C00))
        EmotionType.CALM -> EmotionUiSpec(R.drawable.ic_satisfied, Color(0xFF43A047))
    }
}

fun ActionType.toUiSpec(): ActionUiSpec {
    return when (this) {
        ActionType.EXERCISED -> ActionUiSpec(R.drawable.ic_directions_run, Color(0xFF8D6E63))
        ActionType.SOCIALIZED -> ActionUiSpec(R.drawable.ic_form, Color(0xFF8D6E63))
        ActionType.DELEGATE -> ActionUiSpec(android.R.drawable.ic_menu_share, Color(0xFF8D6E63))
        ActionType.INTENT -> ActionUiSpec(R.drawable.ic_rocket, Color(0xFF8BC34A))
        ActionType.QUICK_ACTION -> ActionUiSpec(R.drawable.ic_sprint, Color(0xFF8BC34A))
        ActionType.PENDING_TASK -> ActionUiSpec(R.drawable.ic_hourglass, Color(0xFF5E35B1))
        ActionType.MEETING_STRESS -> ActionUiSpec(android.R.drawable.ic_dialog_alert, Color(0xFF5E35B1))
        ActionType.SMARTPHONE_DRIFT -> ActionUiSpec(R.drawable.ic_clock_loader_40, Color(0xFF5E35B1))
        ActionType.ALCOHOL -> ActionUiSpec(R.drawable.ic_beer_meal, Color(0xFF5E35B1))
        ActionType.HANGOVER -> ActionUiSpec(android.R.drawable.ic_delete, Color(0xFF5E35B1))
    }
}
