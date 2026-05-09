package com.example.sample2.ui

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.sample2.R
import com.example.sample2.data.ActionType
import com.example.sample2.data.EmotionType
import com.example.sample2.ui.theme.colorSpec

data class EmotionUiSpec(
    @DrawableRes val iconRes: Int,
    val color: Color
)

data class ActionUiSpec(
    @DrawableRes val iconRes: Int,
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
    val iconRes = when (this) {
        ActionType.PENDING_TASK -> R.drawable.ic_hourglass
        ActionType.RELUCTANCE -> R.drawable.ic_mood_bad
        ActionType.MEETING_STRESS -> android.R.drawable.ic_dialog_alert
        ActionType.RUMINATION -> R.drawable.ic_sync_problem
        ActionType.IDLE_DRIFT -> R.drawable.ic_clock_loader_40
        ActionType.ALCOHOL -> R.drawable.ic_beer_meal
        ActionType.HYPERFOCUS -> R.drawable.ic_visibility_off
        ActionType.NO_DRINK_CHOICE -> R.drawable.ic_no_drinks
        ActionType.QUICK_ACTION -> R.drawable.ic_sprint
        ActionType.BREAKDOWN -> android.R.drawable.ic_menu_sort_by_size
        ActionType.REST -> R.drawable.ic_bedtime
        ActionType.EXERCISED -> R.drawable.ic_directions_run
        ActionType.MINDFUL_ACTION -> R.drawable.ic_rocket
        ActionType.INSIGHT -> R.drawable.ic_lightbulb
        ActionType.TOMORROW_BATON -> R.drawable.ic_scroll_down
        ActionType.CONSULT_CONNECT -> R.drawable.ic_handshake
    }
    return ActionUiSpec(iconRes = iconRes, color = group.color)
}
