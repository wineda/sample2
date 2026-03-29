package com.example.sample2.ui.journal

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.sample2.model.JournalJsonStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun shareJournalBackup(context: Context) {
    val backupFile = File(
        context.cacheDir,
        "journal-share-${System.currentTimeMillis()}.json"
    )

    backupFile.outputStream().use { output ->
        JournalJsonStorage.exportBackup(context, output)
    }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        backupFile
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "journal_backup.json")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "バックアップを共有"))
}

fun buildJournalDateLabel(timestamp: Long): String {
    val dateText = SimpleDateFormat("M月d日(E)", Locale.JAPAN).format(Date(timestamp))

    val target = Calendar.getInstance().apply {
        timeInMillis = timestamp
    }
    val today = Calendar.getInstance()

    val isToday =
        target.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            target.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

    return if (isToday) {
        "$dateText・今日"
    } else {
        dateText
    }
}
