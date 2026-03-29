package com.example.sample2.ui

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.sample2.data.MessageV2
import com.example.sample2.model.JournalJsonStorage
import java.util.UUID

class ChatState(
    private val context: Context
) {
    val messages = mutableStateListOf<MessageV2>()

    var inputText by mutableStateOf("")
    var showRestoreDialog by mutableStateOf(false)
    var selectedMessage by mutableStateOf<MessageV2?>(null)
    var deleteTarget by mutableStateOf<MessageV2?>(null)
    var isSingleLineMode by mutableStateOf(false)

    init {
        messages.addAll(JournalJsonStorage.loadMessages(context))
    }

    fun addMessage() {
        val text = inputText.trim()
        if (text.isBlank()) return

        val message = MessageV2(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            text = text
        )

        messages.add(message)
        inputText = ""
        saveMessages()
    }

    fun deleteMessage(message: MessageV2) {
        messages.removeAll { it.id == message.id }
        saveMessages()
    }

    fun updateMessage(updated: MessageV2) {
        val index = messages.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            messages[index] = updated
            saveMessages()
        }
    }

    private fun saveMessages() {
        JournalJsonStorage.saveMessages(context, messages.toList())
    }
}