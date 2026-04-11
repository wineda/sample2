package com.example.sample2.data

import android.content.Context
import android.net.Uri
import com.example.sample2.model.JournalJsonStorage
import java.io.OutputStream

class JournalBackupService(
    private val context: Context,
    private val localDataSource: JournalLocalDataSource
) {
    fun exportToUri(uri: Uri) {
        JournalJsonStorage.exportBackupToUri(context, uri)
    }

    fun restoreFromUri(uri: Uri): JournalJsonStorage.RestoreResult {
        return JournalJsonStorage.restoreBackupFromUri(context, uri)
    }

    fun export(outputStream: OutputStream) {
        JournalJsonStorage.exportBackup(context, outputStream)
    }

    fun reloadMessagesAfterRestore(): List<MessageV2> = localDataSource.loadMessages()
}
