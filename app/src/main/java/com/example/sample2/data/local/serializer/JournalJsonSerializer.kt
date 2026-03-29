package com.example.sample2.data.local.serializer

import com.example.sample2.data.ActionFlags
import com.example.sample2.data.DailyRecord
import com.example.sample2.data.EmotionMetrics
import com.example.sample2.data.MessageV2
import com.example.sample2.data.SleepData
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val DATE_REGEX = Regex("""\d{4}-\d{2}-\d{2}""")

object JournalJsonSerializer {
    fun parseMessages(text: String): List<MessageV2> {
        val array = JSONArray(text)
        val items = buildList {
            for (i in 0 until array.length()) add(parseMessage(array.getJSONObject(i)))
        }
        return normalizeMessages(items)
    }

    fun toMessageJson(messages: List<MessageV2>): String {
        val array = JSONArray()
        normalizeMessages(messages).forEach { array.put(it.toJson()) }
        return array.toString(2)
    }

    fun parseDailyRecords(text: String): List<DailyRecord> {
        val array = JSONArray(text)
        val items = buildList {
            for (i in 0 until array.length()) add(parseDailyRecord(array.getJSONObject(i)))
        }
        return normalizeDailyRecords(items)
    }

    fun toDailyRecordJson(records: List<DailyRecord>): String {
        val array = JSONArray()
        normalizeDailyRecords(records).forEach { array.put(it.toJson()) }
        return array.toString(2)
    }

    fun normalizeDailyRecord(record: DailyRecord): DailyRecord = record.copy(
        steps = record.steps.coerceAtLeast(0),
        sleep = record.sleep.copy(
            durationMinutes = record.sleep.durationMinutes.coerceAtLeast(0),
            quality = record.sleep.quality.coerceIn(0, 5)
        )
    )

    private fun parseMessage(obj: JSONObject): MessageV2 = MessageV2(
        id = obj.optString("id").ifBlank { UUID.randomUUID().toString() },
        timestamp = obj.optLong("timestamp", 0L),
        text = obj.optString("text", ""),
        emotions = obj.optJSONObject("emotions")?.let(::parseEmotionMetrics) ?: EmotionMetrics(),
        flags = obj.optJSONObject("flags")?.let(::parseActionFlags) ?: ActionFlags()
    )

    private fun parseEmotionMetrics(obj: JSONObject): EmotionMetrics = EmotionMetrics(
        anxiety = obj.optInt("anxiety", 0).coerceAtLeast(0),
        angry = obj.optInt("angry", 0).coerceAtLeast(0),
        sad = obj.optInt("sad", 0).coerceAtLeast(0),
        happy = obj.optInt("happy", 0).coerceAtLeast(0),
        calm = obj.optInt("calm", 0).coerceAtLeast(0)
    )

    private fun parseActionFlags(obj: JSONObject): ActionFlags = ActionFlags(
        exercised = obj.optBoolean("exercised", false),
        socialized = obj.optBoolean("socialized", false),
        intent = obj.optBoolean("intent", false),
        insight = obj.optBoolean("insight", false),
        reflection = obj.optBoolean("reflection", false),
        pendingTask = obj.optBoolean("pendingTask", false),
        meetingStress = obj.optBoolean("meetingStress", false),
        smartphoneDrift = obj.optBoolean("smartphoneDrift", false),
        alcohol = obj.optBoolean("alcohol", false),
        hangover = obj.optBoolean("hangover", false)
    )

    private fun parseDailyRecord(obj: JSONObject): DailyRecord = DailyRecord(
        date = obj.optString("date", ""),
        sleep = obj.optJSONObject("sleep")?.let(::parseSleepData) ?: SleepData(),
        steps = obj.optInt("steps", 0).coerceAtLeast(0)
    )

    private fun parseSleepData(obj: JSONObject): SleepData = SleepData(
        durationMinutes = obj.optInt("durationMinutes", 0).coerceAtLeast(0),
        quality = obj.optInt("quality", 0).coerceIn(0, 5),
        startTimestamp = obj.optLongOrNull("startTimestamp"),
        endTimestamp = obj.optLongOrNull("endTimestamp")
    )

    private fun MessageV2.toJson(): JSONObject = JSONObject().apply {
        put("id", id); put("timestamp", timestamp); put("text", text)
        put("emotions", emotions.toJson()); put("flags", flags.toJson())
    }
    private fun EmotionMetrics.toJson(): JSONObject = JSONObject().apply {
        put("anxiety", anxiety); put("angry", angry); put("sad", sad); put("happy", happy); put("calm", calm)
    }
    private fun ActionFlags.toJson(): JSONObject = JSONObject().apply {
        put("exercised", exercised); put("socialized", socialized); put("intent", intent); put("insight", insight); put("reflection", reflection)
        put("pendingTask", pendingTask); put("meetingStress", meetingStress); put("smartphoneDrift", smartphoneDrift); put("alcohol", alcohol); put("hangover", hangover)
    }
    private fun DailyRecord.toJson(): JSONObject = JSONObject().apply { put("date", date); put("sleep", sleep.toJson()); put("steps", steps) }
    private fun SleepData.toJson(): JSONObject = JSONObject().apply { put("durationMinutes", durationMinutes); put("quality", quality); put("startTimestamp", startTimestamp); put("endTimestamp", endTimestamp) }

    private fun normalizeMessages(messages: List<MessageV2>): List<MessageV2> = messages
        .map { m -> m.copy(id = m.id.ifBlank { UUID.randomUUID().toString() }, emotions = normalizeEmotionMetrics(m.emotions), flags = m.flags.copy()) }
        .sortedWith(compareBy<MessageV2> { it.timestamp }.thenBy { it.id })

    private fun normalizeEmotionMetrics(e: EmotionMetrics): EmotionMetrics = e.copy(
        anxiety = e.anxiety.coerceAtLeast(0), angry = e.angry.coerceAtLeast(0), sad = e.sad.coerceAtLeast(0), happy = e.happy.coerceAtLeast(0), calm = e.calm.coerceAtLeast(0)
    )

    private fun normalizeDailyRecords(records: List<DailyRecord>): List<DailyRecord> = records
        .asSequence()
        .filter { DATE_REGEX.matches(it.date) }
        .map(::normalizeDailyRecord)
        .groupBy { it.date }
        .map { (_, grouped) -> grouped.last() }
        .sortedBy { it.date }

    private fun JSONObject.optLongOrNull(name: String): Long? = if (has(name) && !isNull(name)) optLong(name) else null
}
