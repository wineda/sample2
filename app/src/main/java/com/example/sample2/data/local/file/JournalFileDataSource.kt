package com.example.sample2.data.local.file

import android.content.Context
import java.io.File

private const val MESSAGES_FILE_NAME = "messages_v2.json"
private const val DAILY_RECORDS_FILE_NAME = "daily_records.json"

class JournalFileDataSource(
    private val context: Context
) {
    fun messageFile(): File = File(context.filesDir, MESSAGES_FILE_NAME)
    fun dailyRecordFile(): File = File(context.filesDir, DAILY_RECORDS_FILE_NAME)

    fun readText(file: File): String = file.readText(Charsets.UTF_8)

    fun writeAtomically(target: File, text: String) {
        val parent = target.parentFile ?: throw IllegalStateException("保存先ディレクトリが不正です")
        if (!parent.exists()) parent.mkdirs()

        val temp = File(parent, "${target.name}.tmp")
        temp.writeText(text, Charsets.UTF_8)

        if (target.exists() && !target.delete()) {
            throw IllegalStateException("既存ファイルの置き換えに失敗しました: ${target.name}")
        }

        if (!temp.renameTo(target)) {
            temp.copyTo(target, overwrite = true)
            temp.delete()
        }
    }
}
