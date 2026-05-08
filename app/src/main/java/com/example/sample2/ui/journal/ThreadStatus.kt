package com.example.sample2.ui.journal

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.sample2.data.MessageV2
import com.example.sample2.ui.theme.SemanticColors
import com.example.sample2.ui.theme.appColors

enum class ThreadStatus(val label: String) {
    Unresolved("未対応"),
    InProgress("進行中"),
    RecordOnly("記録のみ"),
}

@Composable
fun ThreadStatus.frontColor(): Color = when (this) {
    ThreadStatus.Unresolved -> SemanticColors.NegativeMain
    ThreadStatus.InProgress -> SemanticColors.WarningMain
    ThreadStatus.RecordOnly -> MaterialTheme.appColors.inkTertiary
}

@Composable
fun ThreadStatus.backColor(): Color = when (this) {
    ThreadStatus.Unresolved -> SemanticColors.NegativeSoft
    ThreadStatus.InProgress -> SemanticColors.WarningSoft
    ThreadStatus.RecordOnly -> MaterialTheme.appColors.surfaceInactive
}

fun deriveThreadStatus(parent: MessageV2, children: List<MessageV2>): ThreadStatus {
    val isNegative = parent.emotions.angry > 0 ||
            parent.emotions.anxiety > 0 ||
            parent.emotions.sad > 0

    return when {
        isNegative && children.isEmpty() -> ThreadStatus.Unresolved
        isNegative -> ThreadStatus.InProgress
        else -> ThreadStatus.RecordOnly
    }
}
