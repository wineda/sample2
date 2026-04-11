package com.example.sample2.data

import android.content.Context
import com.example.sample2.model.JournalJsonStorage

class JournalLocalDataSource(
    private val context: Context
) {
    fun loadMessages(): List<MessageV2> = JournalJsonStorage.loadMessages(context)

    fun saveMessages(messages: List<MessageV2>) {
        JournalJsonStorage.saveMessages(context, messages)
    }

    fun loadDailyRecords(): List<DailyRecord> = JournalJsonStorage.loadDailyRecords(context)

    fun saveDailyRecords(records: List<DailyRecord>) {
        JournalJsonStorage.saveDailyRecords(context, records)
    }

    fun upsertDailyRecord(record: DailyRecord) {
        JournalJsonStorage.upsertDailyRecord(context, record)
    }

    fun findDailyRecordOrNull(date: String): DailyRecord? {
        return JournalJsonStorage.findDailyRecordOrNull(context, date)
    }

    fun loadDailyReflections(): List<DailyReflection> = JournalJsonStorage.loadDailyReflections(context)

    fun upsertDailyReflection(reflection: DailyReflection) {
        JournalJsonStorage.upsertDailyReflection(context, reflection)
    }

    fun findDailyReflectionOrNull(date: String): DailyReflection? {
        return JournalJsonStorage.findDailyReflectionOrNull(context, date)
    }
}
