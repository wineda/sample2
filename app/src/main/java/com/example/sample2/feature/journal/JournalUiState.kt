package com.example.sample2.feature.journal

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.sample2.data.MessageV2

class JournalUiState {
    val messages = mutableStateListOf<MessageV2>()
    var inputText by mutableStateOf("")
    var showRestoreDialog by mutableStateOf(false)
    var selectedMessage by mutableStateOf<MessageV2?>(null)
    var deleteTarget by mutableStateOf<MessageV2?>(null)
    var isSingleLineMode by mutableStateOf(true)
}
