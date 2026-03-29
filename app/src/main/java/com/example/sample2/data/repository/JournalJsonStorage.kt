package com.example.sample2.data.repository

import android.content.Context
import android.net.Uri
import com.example.sample2.data.DailyRecord
import com.example.sample2.data.MessageV2
import java.io.InputStream
import java.io.OutputStream

object JournalJsonStorage {
    data class RestoreResult(
        val messageCount: Int,
        val dailyRecordCount: Int
    )

    private fun repo(context: Context) = JournalRepository(context)

    fun loadMessages(context: Context): List<MessageV2> = repo(context).loadMessages()
    fun saveMessages(context: Context, messages: List<MessageV2>) = repo(context).saveMessages(messages)
    fun loadDailyRecords(context: Context): List<DailyRecord> = repo(context).loadDailyRecords()
    fun saveDailyRecords(context: Context, records: List<DailyRecord>) = repo(context).saveDailyRecords(records)
    fun upsertDailyRecord(context: Context, record: DailyRecord) = repo(context).upsertDailyRecord(record)
    fun findDailyRecordOrNull(context: Context, date: String): DailyRecord? = repo(context).findDailyRecordOrNull(date)

    fun exportBackupToUri(context: Context, uri: Uri) = repo(context).exportBackupToUri(uri)
    fun exportBackup(context: Context, outputStream: OutputStream) = repo(context).exportBackup(outputStream)

    fun restoreBackupFromUri(context: Context, uri: Uri): RestoreResult {
        val result = repo(context).restoreBackupFromUri(uri)
        return RestoreResult(result.messageCount, result.dailyRecordCount)
    }

    fun restoreBackup(context: Context, inputStream: InputStream): RestoreResult {
        val result = repo(context).restoreBackup(inputStream)
        return RestoreResult(result.messageCount, result.dailyRecordCount)
    }
}
