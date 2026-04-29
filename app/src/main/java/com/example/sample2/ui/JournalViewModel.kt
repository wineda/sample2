package com.example.sample2.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.sample2.data.DailyRecord
import com.example.sample2.data.DailyReflection
import com.example.sample2.data.EmotionResponse
import com.example.sample2.data.JournalRepository
import com.example.sample2.data.JournalEntryType
import com.example.sample2.data.MessageV2
import com.example.sample2.data.ActionType
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

    fun addEmotionResponse(
        parent: MessageV2,
        targetEmotionKey: String,
        actionKey: String,
        effectScore: Int,
        note: String
    ) {
        val now = System.currentTimeMillis()
        val trimmedNote = note.trim()
        val actionLabel = ActionType.entries.firstOrNull { it.key == actionKey }?.label
        val displayText = if (trimmedNote.isNotEmpty()) {
            trimmedNote
        } else {
            actionLabel ?: actionKey
        }

        messages.add(
            MessageV2(
                id = UUID.randomUUID().toString(),
                timestamp = now,
                text = displayText,
                parentId = parent.id,
                entryType = JournalEntryType.EMOTION_RESPONSE,
                response = EmotionResponse(
                    targetEmotionKey = targetEmotionKey,
                    actionKey = actionKey,
                    effectScore = effectScore.coerceIn(0, 3),
                    note = note,
                    createdAt = now
                )
            )
        )
        persistMessages()
    }

    fun childrenOf(parent: MessageV2): List<MessageV2> {
        return messages
            .filter { it.parentId == parent.id }
            .sortedBy { it.timestamp }
    }

    fun rootMessages(): List<MessageV2> {
        return messages
            .filter { it.parentId == null && it.entryType == JournalEntryType.MEMO }
            .sortedBy { it.timestamp }
    }

    fun deleteMessage(message: MessageV2) {
        if (message.parentId == null) {
            messages.removeAll { it.id == message.id || it.parentId == message.id }
        } else {
            messages.removeAll { it.id == message.id }
        }
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

    fun loadDailyReflections(): List<DailyReflection> = repository.loadDailyReflections()

    fun upsertDailyReflection(reflection: DailyReflection) {
        repository.upsertDailyReflection(reflection)
    }

    fun findDailyReflectionOrNull(date: String): DailyReflection? = repository.findDailyReflectionOrNull(date)

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
