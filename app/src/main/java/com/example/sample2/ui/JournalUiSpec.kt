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
    val orange = Color(0xFFFB8C00)
    val green = Color(0xFF43A047)
    val purple = Color(0xFF5E35B1)

    return when (this) {
        ActionType.EXERCISED -> ActionUiSpec(R.drawable.ic_directions_run, orange)
        ActionType.SOCIALIZED -> ActionUiSpec(R.drawable.ic_form, orange)
        ActionType.DELEGATE -> ActionUiSpec(android.R.drawable.ic_menu_share, green)
        ActionType.CHALLENGE -> ActionUiSpec(R.drawable.ic_rocket, Color(0xFF8BC34A))
        ActionType.BREAKDOWN -> ActionUiSpec(android.R.drawable.ic_menu_sort_by_size, green)
        ActionType.INSTRUCT -> ActionUiSpec(R.drawable.ic_wb_incandescent, green)
        ActionType.QUICK_ACTION -> ActionUiSpec(R.drawable.ic_sprint, green)
        ActionType.PENDING_TASK -> ActionUiSpec(R.drawable.ic_hourglass, purple)
        ActionType.MEETING_STRESS -> ActionUiSpec(android.R.drawable.ic_dialog_alert, purple)
        ActionType.SMARTPHONE_DRIFT -> ActionUiSpec(R.drawable.ic_clock_loader_40, purple)
        ActionType.ALCOHOL -> ActionUiSpec(R.drawable.ic_beer_meal, purple)
        ActionType.HANGOVER -> ActionUiSpec(android.R.drawable.ic_delete, purple)
    }
}
