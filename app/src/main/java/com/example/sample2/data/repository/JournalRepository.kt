package com.example.sample2.data.repository

import android.content.Context
import android.net.Uri
import com.example.sample2.data.DailyRecord
import com.example.sample2.data.MessageV2
import com.example.sample2.data.local.backup.JournalBackupManager
import com.example.sample2.data.local.file.JournalFileDataSource
import com.example.sample2.data.local.serializer.JournalJsonSerializer

data class RestoreResult(
    val messageCount: Int,
    val dailyRecordCount: Int
)

class JournalRepository(private val context: Context) {
    private val fileDataSource = JournalFileDataSource(context)
    private val backupManager = JournalBackupManager()

    fun loadMessages(): List<MessageV2> {
        val file = fileDataSource.messageFile()
        if (!file.exists()) return emptyList()
        return try {
            JournalJsonSerializer.parseMessages(fileDataSource.readText(file))
        } catch (e: Exception) {
            throw IllegalStateException("messages_v2.json の読込に失敗しました", e)
        }
    }

    fun saveMessages(messages: List<MessageV2>) {
        fileDataSource.writeAtomically(
            fileDataSource.messageFile(),
            JournalJsonSerializer.toMessageJson(messages)
        )
    }

    fun loadDailyRecords(): List<DailyRecord> {
        val file = fileDataSource.dailyRecordFile()
        if (!file.exists()) return emptyList()
        return try {
            JournalJsonSerializer.parseDailyRecords(fileDataSource.readText(file))
        } catch (e: Exception) {
            throw IllegalStateException("daily_records.json の読込に失敗しました", e)
        }
    }

    fun saveDailyRecords(records: List<DailyRecord>) {
        fileDataSource.writeAtomically(
            fileDataSource.dailyRecordFile(),
            JournalJsonSerializer.toDailyRecordJson(records)
        )
    }

    fun upsertDailyRecord(record: DailyRecord) {
        val current = loadDailyRecords().associateBy { it.date }.toMutableMap()
        current[record.date] = JournalJsonSerializer.normalizeDailyRecord(record)
        saveDailyRecords(current.values.toList())
    }

    fun findDailyRecordOrNull(date: String): DailyRecord? = loadDailyRecords().firstOrNull { it.date == date }

    fun exportBackup(outputStream: java.io.OutputStream) {
        backupManager.export(loadMessages(), loadDailyRecords(), outputStream)
    }

    fun restoreBackup(inputStream: java.io.InputStream): RestoreResult {
        val restored = backupManager.restore(inputStream)
        saveMessages(restored.messages)
        saveDailyRecords(restored.dailyRecords)
        return RestoreResult(restored.messages.size, restored.dailyRecords.size)
    }

    fun exportBackupToUri(uri: Uri) {
        context.contentResolver.openOutputStream(uri)?.use { exportBackup(it) }
            ?: throw IllegalStateException("バックアップ出力先を開けませんでした")
    }

    fun restoreBackupFromUri(uri: Uri): RestoreResult {
        context.contentResolver.openInputStream(uri)?.use { return restoreBackup(it) }
        throw IllegalStateException("バックアップ入力元を開けませんでした")
    }
}
