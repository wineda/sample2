package com.example.sample2.data

import android.net.Uri
import com.example.sample2.model.JournalJsonStorage

interface JournalRepository {
    fun loadMessages(): List<MessageV2>
    fun saveMessages(messages: List<MessageV2>)
    fun loadDailyRecords(): List<DailyRecord>
    fun upsertDailyRecord(record: DailyRecord)
    fun findDailyRecordOrNull(date: String): DailyRecord?
    fun exportBackupToUri(uri: Uri)
    fun restoreBackupFromUri(uri: Uri): JournalJsonStorage.RestoreResult
}
