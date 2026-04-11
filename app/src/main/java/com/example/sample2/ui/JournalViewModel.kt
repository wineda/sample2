package com.example.sample2.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.sample2.data.DailyRecord
import com.example.sample2.data.JournalRepository
import com.example.sample2.data.MessageV2
import com.example.sample2.model.JournalJsonStorage
import java.util.UUID

class JournalViewModel(
    private val repository: JournalRepository
) {
    val messages = mutableStateListOf<MessageV2>()

    var inputText by mutableStateOf("")
    var showRestoreDialog by mutableStateOf(false)
    var selectedMessage by mutableStateOf<MessageV2?>(null)
    var deleteTarget by mutableStateOf<MessageV2?>(null)
    var isSingleLineMode by mutableStateOf(false)

    init {
        messages.addAll(repository.loadMessages())
    }

    fun addMessage() {
        val text = inputText.trim()
        if (text.isBlank()) return

        messages.add(
            MessageV2(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                text = text
            )
        )
        inputText = ""
        persistMessages()
    }

    fun deleteMessage(message: MessageV2) {
        messages.removeAll { it.id == message.id }
        persistMessages()
    }

    fun updateMessage(updated: MessageV2) {
        val index = messages.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            messages[index] = updated
            persistMessages()
        }
    }

    fun loadDailyRecords(): List<DailyRecord> = repository.loadDailyRecords()

    fun upsertDailyRecord(record: DailyRecord) {
        repository.upsertDailyRecord(record)
    }

    fun exportBackupToUri(uri: android.net.Uri) {
        repository.exportBackupToUri(uri)
    }

    fun restoreBackupFromUri(uri: android.net.Uri): JournalJsonStorage.RestoreResult {
        val result = repository.restoreBackupFromUri(uri)
        messages.clear()
        messages.addAll(repository.loadMessages())
        return result
    }

    private fun persistMessages() {
        repository.saveMessages(messages.toList())
    }
}
