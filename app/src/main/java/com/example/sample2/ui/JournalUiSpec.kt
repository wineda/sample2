package com.example.sample2.ui

import androidx.compose.ui.graphics.Color
import com.example.sample2.R
import com.example.sample2.data.ActionType
import com.example.sample2.data.EmotionType
import com.example.sample2.ui.theme.ActionPalette
import com.example.sample2.ui.theme.colorSpec

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
        EmotionType.ANXIETY -> EmotionUiSpec(R.drawable.ic_anxiety, colorSpec().main)
        EmotionType.ANGRY -> EmotionUiSpec(R.drawable.ic_angry, colorSpec().main)
        EmotionType.SAD -> EmotionUiSpec(R.drawable.ic_sad, colorSpec().main)
        EmotionType.HAPPY -> EmotionUiSpec(R.drawable.ic_happy, colorSpec().main)
        EmotionType.CALM -> EmotionUiSpec(R.drawable.ic_satisfied, colorSpec().main)
    }
}

fun ActionType.toUiSpec(): ActionUiSpec {
    return when (this) {
        ActionType.EXERCISED -> ActionUiSpec(R.drawable.ic_directions_run, ActionPalette.Positive)
        ActionType.SOCIALIZED -> ActionUiSpec(R.drawable.ic_form, ActionPalette.Positive)
        ActionType.DELEGATE -> ActionUiSpec(R.drawable.ic_scroll_down, ActionPalette.Execute)
        ActionType.CHALLENGE -> ActionUiSpec(R.drawable.ic_rocket, ActionPalette.Execute)
        ActionType.BREAKDOWN -> ActionUiSpec(android.R.drawable.ic_menu_sort_by_size, ActionPalette.Execute)
        ActionType.INSTRUCT -> ActionUiSpec(R.drawable.ic_wb_incandescent, ActionPalette.Execute)
        ActionType.QUICK_ACTION -> ActionUiSpec(R.drawable.ic_sprint, ActionPalette.Execute)
        ActionType.PENDING_TASK -> ActionUiSpec(R.drawable.ic_hourglass, ActionPalette.Load)
        ActionType.MEETING_STRESS -> ActionUiSpec(android.R.drawable.ic_dialog_alert, ActionPalette.Load)
        ActionType.SMARTPHONE_DRIFT -> ActionUiSpec(R.drawable.ic_clock_loader_40, ActionPalette.Load)
        ActionType.ALCOHOL -> ActionUiSpec(R.drawable.ic_beer_meal, ActionPalette.Load)
        ActionType.HANGOVER -> ActionUiSpec(android.R.drawable.ic_delete, ActionPalette.Load)
    }
}
