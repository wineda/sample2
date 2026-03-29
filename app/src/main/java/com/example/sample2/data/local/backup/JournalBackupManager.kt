package com.example.sample2.data.local.backup

import com.example.sample2.data.DailyRecord
import com.example.sample2.data.MessageV2
import com.example.sample2.data.local.serializer.JournalJsonSerializer
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

private const val BACKUP_VERSION = 2

data class RestorePayload(
    val messages: List<MessageV2>,
    val dailyRecords: List<DailyRecord>
)

class JournalBackupManager {
    fun export(messages: List<MessageV2>, dailyRecords: List<DailyRecord>, output: OutputStream) {
        val backup = JSONObject().apply {
            put("version", BACKUP_VERSION)
            put("exportedAt", System.currentTimeMillis())
            put("messages", JSONArray(JournalJsonSerializer.toMessageJson(messages)))
            put("dailyRecords", JSONArray(JournalJsonSerializer.toDailyRecordJson(dailyRecords)))
        }
        output.writer(Charsets.UTF_8).use { it.write(backup.toString(2)) }
    }

    fun restore(input: InputStream): RestorePayload {
        val text = input.bufferedReader(Charsets.UTF_8).use { it.readText() }
        val root = try {
            JSONObject(text)
        } catch (e: Exception) {
            throw IllegalArgumentException("バックアップJSONの形式が不正です", e)
        }
        val version = root.optInt("version", 0)
        if (version <= 0) throw IllegalArgumentException("バックアップ version が不正です")

        val messages = JournalJsonSerializer.parseMessages((root.optJSONArray("messages") ?: JSONArray()).toString())
        val dailyRecords = JournalJsonSerializer.parseDailyRecords((root.optJSONArray("dailyRecords") ?: JSONArray()).toString())
        return RestorePayload(messages = messages, dailyRecords = dailyRecords)
    }
}
