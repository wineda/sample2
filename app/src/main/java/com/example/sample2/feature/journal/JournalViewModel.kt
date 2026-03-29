package com.example.sample2.feature.journal

import androidx.lifecycle.ViewModel
import com.example.sample2.data.MessageV2
import com.example.sample2.data.repository.JournalRepository
import java.util.UUID

class JournalViewModel(
    private val repository: JournalRepository
) : ViewModel() {
    val uiState = JournalUiState()

    init {
        reload()
    }

    fun dispatch(action: JournalAction) {
        when (action) {
            is JournalAction.InputChanged -> uiState.inputText = action.value
            JournalAction.AddMessage -> addMessage()
            is JournalAction.DeleteMessage -> deleteMessage(action.message)
            is JournalAction.UpdateMessage -> updateMessage(action.message)
            is JournalAction.ToggleSingleLineMode -> {
                uiState.isSingleLineMode = action.enabled ?: !uiState.isSingleLineMode
            }
            is JournalAction.SelectMessage -> uiState.selectedMessage = action.message
            is JournalAction.SetRestoreDialogVisible -> uiState.showRestoreDialog = action.visible
            is JournalAction.SetDeleteTarget -> uiState.deleteTarget = action.message
            JournalAction.Reload -> reload()
        }
    }

    private fun reload() {
        uiState.messages.clear()
        uiState.messages.addAll(repository.loadMessages())
    }

    private fun addMessage() {
        val text = uiState.inputText.trim()
        if (text.isBlank()) return
        val message = MessageV2(id = UUID.randomUUID().toString(), timestamp = System.currentTimeMillis(), text = text)
        uiState.messages.add(message)
        uiState.inputText = ""
        repository.saveMessages(uiState.messages.toList())
    }

    private fun deleteMessage(message: MessageV2) {
        uiState.messages.removeAll { it.id == message.id }
        repository.saveMessages(uiState.messages.toList())
    }

    private fun updateMessage(updated: MessageV2) {
        val index = uiState.messages.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            uiState.messages[index] = updated
            repository.saveMessages(uiState.messages.toList())
        }
    }
}
