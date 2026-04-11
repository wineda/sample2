package com.example.sample2.data

import android.net.Uri
import com.example.sample2.model.JournalJsonStorage
import java.io.OutputStream

class DefaultJournalRepository(
    private val localDataSource: JournalLocalDataSource,
    private val backupService: JournalBackupService
) : JournalRepository {
    override fun loadMessages(): List<MessageV2> = localDataSource.loadMessages()

    override fun saveMessages(messages: List<MessageV2>) {
        localDataSource.saveMessages(messages)
    }

    override fun loadDailyRecords(): List<DailyRecord> = localDataSource.loadDailyRecords()

    override fun upsertDailyRecord(record: DailyRecord) {
        localDataSource.upsertDailyRecord(record)
    }

    override fun findDailyRecordOrNull(date: String): DailyRecord? {
        return localDataSource.findDailyRecordOrNull(date)
    }

    override fun exportBackupToUri(uri: Uri) {
        backupService.exportToUri(uri)
    }

    override fun restoreBackupFromUri(uri: Uri): JournalJsonStorage.RestoreResult {
        return backupService.restoreFromUri(uri)
    }


    fun exportBackup(output: OutputStream) {
        backupService.export(output)
    }

    fun reloadMessagesAfterRestore(): List<MessageV2> {
        return backupService.reloadMessagesAfterRestore()
    }
}
