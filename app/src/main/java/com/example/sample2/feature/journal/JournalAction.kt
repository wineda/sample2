package com.example.sample2.feature.journal

import com.example.sample2.data.MessageV2

sealed interface JournalAction {
    data class InputChanged(val value: String) : JournalAction
    data object AddMessage : JournalAction
    data class DeleteMessage(val message: MessageV2) : JournalAction
    data class UpdateMessage(val message: MessageV2) : JournalAction
    data class ToggleSingleLineMode(val enabled: Boolean? = null) : JournalAction
    data class SelectMessage(val message: MessageV2?) : JournalAction
    data class SetRestoreDialogVisible(val visible: Boolean) : JournalAction
    data class SetDeleteTarget(val message: MessageV2?) : JournalAction
    data object Reload : JournalAction
}
