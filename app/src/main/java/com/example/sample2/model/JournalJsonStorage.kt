package com.example.sample2.model

import android.content.Context
import android.net.Uri
import com.example.sample2.data.ActionFlags
import com.example.sample2.data.DailyRecord
import com.example.sample2.data.DailyReflection
import com.example.sample2.data.EmotionMetrics
import com.example.sample2.data.MessageV2
import com.example.sample2.data.SleepData
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

object JournalJsonStorage {

    private const val MESSAGES_FILE_NAME = "messages_v2.json"
    private const val DAILY_RECORDS_FILE_NAME = "daily_records.json"
    private const val DAILY_REFLECTIONS_FILE_NAME = "daily_reflections.json"

    // daily_reflections を追加
    private const val BACKUP_VERSION = 4

    private val DATE_REGEX = Regex("""\d{4}-\d{2}-\d{2}""")

    data class RestoreResult(
        val messageCount: Int,
        val dailyRecordCount: Int,
        val dailyReflectionCount: Int
    )

    fun loadMessages(context: Context): List<MessageV2> {
        val file = File(context.filesDir, MESSAGES_FILE_NAME)
        if (!file.exists()) return emptyList()

        return try {
            val text = file.readText(Charsets.UTF_8)
            val array = JSONArray(text)
            val items = buildList {
                for (i in 0 until array.length()) {
                    add(parseMessage(array.getJSONObject(i)))
                }
            }
            normalizeMessages(items)
        } catch (e: Exception) {
            throw IllegalStateException("messages_v2.json の読込に失敗しました", e)
        }
    }

    fun saveMessages(context: Context, messages: List<MessageV2>) {
        val normalized = normalizeMessages(messages)
        val array = JSONArray()
        normalized.forEach { array.put(it.toJson()) }

        val file = File(context.filesDir, MESSAGES_FILE_NAME)
        writeAtomically(file, array.toString(2))
    }

    fun loadDailyRecords(context: Context): List<DailyRecord> {
        val file = File(context.filesDir, DAILY_RECORDS_FILE_NAME)
        if (!file.exists()) return emptyList()

        return try {
            val text = file.readText(Charsets.UTF_8)
            val array = JSONArray(text)
            val items = buildList {
                for (i in 0 until array.length()) {
                    add(parseDailyRecord(array.getJSONObject(i)))
                }
            }
            normalizeDailyRecords(items)
        } catch (e: Exception) {
            throw IllegalStateException("daily_records.json の読込に失敗しました", e)
        }
    }

    fun saveDailyRecords(context: Context, records: List<DailyRecord>) {
        val normalized = normalizeDailyRecords(records)
        val array = JSONArray()
        normalized.forEach { array.put(it.toJson()) }

        val file = File(context.filesDir, DAILY_RECORDS_FILE_NAME)
        writeAtomically(file, array.toString(2))
    }

    fun upsertDailyRecord(context: Context, record: DailyRecord) {
        val current = loadDailyRecords(context).associateBy { it.date }.toMutableMap()
        current[record.date] = normalizeDailyRecord(record)
        saveDailyRecords(context, current.values.toList())
    }

    fun findDailyRecordOrNull(context: Context, date: String): DailyRecord? {
        return loadDailyRecords(context).firstOrNull { it.date == date }
    }

    fun loadDailyReflections(context: Context): List<DailyReflection> {
        val file = File(context.filesDir, DAILY_REFLECTIONS_FILE_NAME)
        if (!file.exists()) return emptyList()

        return try {
            val text = file.readText(Charsets.UTF_8)
            val array = JSONArray(text)
            val items = buildList {
                for (i in 0 until array.length()) {
                    add(parseDailyReflection(array.getJSONObject(i)))
                }
            }
            normalizeDailyReflections(items)
        } catch (e: Exception) {
            throw IllegalStateException("daily_reflections.json の読込に失敗しました", e)
        }
    }

    fun saveDailyReflections(context: Context, reflections: List<DailyReflection>) {
        val normalized = normalizeDailyReflections(reflections)
        val array = JSONArray()
        normalized.forEach { array.put(it.toJson()) }

        val file = File(context.filesDir, DAILY_REFLECTIONS_FILE_NAME)
        writeAtomically(file, array.toString(2))
    }

    fun upsertDailyReflection(context: Context, reflection: DailyReflection) {
        val current = loadDailyReflections(context).associateBy { it.date }.toMutableMap()
        current[reflection.date] = normalizeDailyReflection(reflection)
        saveDailyReflections(context, current.values.toList())
    }

    fun findDailyReflectionOrNull(context: Context, date: String): DailyReflection? {
        return loadDailyReflections(context).firstOrNull { it.date == date }
    }

    fun exportBackupToUri(context: Context, uri: Uri) {
        context.contentResolver.openOutputStream(uri)?.use { output ->
            exportBackup(context, output)
        } ?: throw IllegalStateException("バックアップ出力先を開けませんでした")
    }

    fun restoreBackupFromUri(context: Context, uri: Uri): RestoreResult {
        context.contentResolver.openInputStream(uri)?.use { input ->
            return restoreBackup(context, input)
        }
        throw IllegalStateException("バックアップ入力元を開けませんでした")
    }

    fun exportBackup(context: Context, outputStream: OutputStream) {
        val backup = buildBackupJson(
            messages = loadMessages(context),
            dailyRecords = loadDailyRecords(context),
            dailyReflections = loadDailyReflections(context)
        )
        outputStream.writer(Charsets.UTF_8).use { writer ->
            writer.write(backup.toString(2))
        }
    }

    fun restoreBackup(context: Context, inputStream: InputStream): RestoreResult {
        val text = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }

        val root = try {
            JSONObject(text)
        } catch (e: Exception) {
            throw IllegalArgumentException("バックアップJSONの形式が不正です", e)
        }

        val version = root.optInt("version", 0)
        if (version <= 0) {
            throw IllegalArgumentException("バックアップ version が不正です")
        }

        val messages = parseMessagesFromBackup(root)
        val dailyRecords = parseDailyRecordsFromBackup(root)
        val dailyReflections = parseDailyReflectionsFromBackup(root)

        saveMessages(context, messages)
        saveDailyRecords(context, dailyRecords)
        saveDailyReflections(context, dailyReflections)

        return RestoreResult(
            messageCount = messages.size,
            dailyRecordCount = dailyRecords.size,
            dailyReflectionCount = dailyReflections.size
        )
    }

    private fun buildBackupJson(
        messages: List<MessageV2>,
        dailyRecords: List<DailyRecord>,
        dailyReflections: List<DailyReflection>
    ): JSONObject {
        val messageArray = JSONArray()
        normalizeMessages(messages).forEach { messageArray.put(it.toJson()) }

        val dailyRecordArray = JSONArray()
        normalizeDailyRecords(dailyRecords).forEach { dailyRecordArray.put(it.toJson()) }

        val dailyReflectionArray = JSONArray()
        normalizeDailyReflections(dailyReflections).forEach { dailyReflectionArray.put(it.toJson()) }

        return JSONObject().apply {
            put("version", BACKUP_VERSION)
            put("exportedAt", System.currentTimeMillis())
            put("messages", messageArray)
            put("dailyRecords", dailyRecordArray)
            put("dailyReflections", dailyReflectionArray)
        }
    }

    private fun parseMessagesFromBackup(root: JSONObject): List<MessageV2> {
        val array = root.optJSONArray("messages") ?: JSONArray()
        val items = buildList {
            for (i in 0 until array.length()) {
                add(parseMessage(array.getJSONObject(i)))
            }
        }
        return normalizeMessages(items)
    }

    private fun parseDailyRecordsFromBackup(root: JSONObject): List<DailyRecord> {
        val array = root.optJSONArray("dailyRecords") ?: JSONArray()
        val items = buildList {
            for (i in 0 until array.length()) {
                add(parseDailyRecord(array.getJSONObject(i)))
            }
        }
        return normalizeDailyRecords(items)
    }

    private fun parseDailyReflectionsFromBackup(root: JSONObject): List<DailyReflection> {
        val array = root.optJSONArray("dailyReflections") ?: JSONArray()
        val items = buildList {
            for (i in 0 until array.length()) {
                add(parseDailyReflection(array.getJSONObject(i)))
            }
        }
        return normalizeDailyReflections(items)
    }

    private fun parseMessage(obj: JSONObject): MessageV2 {
        return MessageV2(
            id = obj.optString("id").ifBlank { UUID.randomUUID().toString() },
            timestamp = obj.optLong("timestamp", 0L),
            text = obj.optString("text", ""),
            emotions = obj.optJSONObject("emotions")?.let(::parseEmotionMetrics) ?: EmotionMetrics(),
            flags = obj.optJSONObject("flags")?.let(::parseActionFlags) ?: ActionFlags()
        )
    }

    private fun parseEmotionMetrics(obj: JSONObject): EmotionMetrics {
        return EmotionMetrics(
            anxiety = obj.optInt("anxiety", 0).coerceAtLeast(0),
            angry = obj.optInt("angry", 0).coerceAtLeast(0),
            sad = obj.optInt("sad", 0).coerceAtLeast(0),
            happy = obj.optInt("happy", 0).coerceAtLeast(0),
            calm = obj.optInt("calm", 0).coerceAtLeast(0)
        )
    }

    private fun parseActionFlags(obj: JSONObject): ActionFlags {
        return ActionFlags(
            exercised = obj.optBoolean("exercised", false),
            socialized = obj.optBoolean("socialized", false),
            delegate = obj.optBoolean("delegate", false),
            intent = obj.optBoolean("intent", false),
            instruct = obj.optBoolean("instruct", false),

            // 旧JSONに存在しない項目は false 既定で後方互換
            pendingTask = obj.optBoolean("pendingTask", false),
            quickAction = obj.optBoolean("quickAction", false),
            meetingStress = obj.optBoolean("meetingStress", false),
            smartphoneDrift = obj.optBoolean("smartphoneDrift", false),
            alcohol = obj.optBoolean("alcohol", false),
            hangover = obj.optBoolean("hangover", false)
        )
    }

    private fun parseDailyRecord(obj: JSONObject): DailyRecord {
        return DailyRecord(
            date = obj.optString("date", ""),
            sleep = obj.optJSONObject("sleep")?.let(::parseSleepData) ?: SleepData(),
            steps = obj.optInt("steps", 0).coerceAtLeast(0)
        )
    }

    private fun parseDailyReflection(obj: JSONObject): DailyReflection {
        return DailyReflection(
            date = obj.optString("date", ""),
            wins = obj.optString("wins", ""),
            difficulties = obj.optString("difficulties", ""),
            insights = obj.optString("insights", ""),
            tomorrowFirstAction = obj.optString("tomorrowFirstAction", ""),
            summary = obj.optString("summary", ""),
            updatedAt = obj.optLong("updatedAt", 0L)
        )
    }

    private fun parseSleepData(obj: JSONObject): SleepData {
        return SleepData(
            durationMinutes = obj.optInt("durationMinutes", 0).coerceAtLeast(0),
            quality = obj.optInt("quality", 0).coerceIn(0, 5),
            startTimestamp = obj.optLongOrNull("startTimestamp"),
            endTimestamp = obj.optLongOrNull("endTimestamp")
        )
    }

    private fun MessageV2.toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("timestamp", timestamp)
            put("text", text)
            put("emotions", emotions.toJson())
            put("flags", flags.toJson())
        }
    }

    private fun EmotionMetrics.toJson(): JSONObject {
        return JSONObject().apply {
            put("anxiety", anxiety)
            put("angry", angry)
            put("sad", sad)
            put("happy", happy)
            put("calm", calm)
        }
    }

    private fun ActionFlags.toJson(): JSONObject {
        return JSONObject().apply {
            put("exercised", exercised)
            put("socialized", socialized)
            put("delegate", delegate)
            put("intent", intent)
            put("instruct", instruct)

            put("pendingTask", pendingTask)
            put("quickAction", quickAction)
            put("meetingStress", meetingStress)
            put("smartphoneDrift", smartphoneDrift)
            put("alcohol", alcohol)
            put("hangover", hangover)
        }
    }

    private fun DailyRecord.toJson(): JSONObject {
        return JSONObject().apply {
            put("date", date)
            put("sleep", sleep.toJson())
            put("steps", steps)
        }
    }

    private fun DailyReflection.toJson(): JSONObject {
        return JSONObject().apply {
            put("date", date)
            put("wins", wins)
            put("difficulties", difficulties)
            put("insights", insights)
            put("tomorrowFirstAction", tomorrowFirstAction)
            put("summary", summary)
            put("updatedAt", updatedAt)
        }
    }

    private fun SleepData.toJson(): JSONObject {
        return JSONObject().apply {
            put("durationMinutes", durationMinutes)
            put("quality", quality)
            put("startTimestamp", startTimestamp)
            put("endTimestamp", endTimestamp)
        }
    }

    private fun normalizeMessages(messages: List<MessageV2>): List<MessageV2> {
        return messages
            .map { message ->
                message.copy(
                    id = message.id.ifBlank { UUID.randomUUID().toString() },
                    emotions = normalizeEmotionMetrics(message.emotions),
                    flags = normalizeActionFlags(message.flags)
                )
            }
            .sortedWith(compareBy<MessageV2> { it.timestamp }.thenBy { it.id })
    }

    private fun normalizeEmotionMetrics(emotions: EmotionMetrics): EmotionMetrics {
        return emotions.copy(
            anxiety = emotions.anxiety.coerceAtLeast(0),
            angry = emotions.angry.coerceAtLeast(0),
            sad = emotions.sad.coerceAtLeast(0),
            happy = emotions.happy.coerceAtLeast(0),
            calm = emotions.calm.coerceAtLeast(0)
        )
    }

    /**
     * いまは Boolean だけなので実質そのまま返せば十分。
     * ただ normalizeMessages 側で入口を揃えておくと後で拡張しやすい。
     */
    private fun normalizeActionFlags(flags: ActionFlags): ActionFlags {
        return flags.copy()
    }

    private fun normalizeDailyRecords(records: List<DailyRecord>): List<DailyRecord> {
        return records
            .asSequence()
            .filter { DATE_REGEX.matches(it.date) }
            .map(::normalizeDailyRecord)
            .groupBy { it.date }
            .map { (_, grouped) -> grouped.last() }
            .sortedBy { it.date }
    }

    private fun normalizeDailyRecord(record: DailyRecord): DailyRecord {
        return record.copy(
            steps = record.steps.coerceAtLeast(0),
            sleep = normalizeSleepData(record.sleep)
        )
    }

    private fun normalizeSleepData(sleep: SleepData): SleepData {
        return sleep.copy(
            durationMinutes = sleep.durationMinutes.coerceAtLeast(0),
            quality = sleep.quality.coerceIn(0, 5)
        )
    }

    private fun normalizeDailyReflections(reflections: List<DailyReflection>): List<DailyReflection> {
        return reflections
            .asSequence()
            .filter { DATE_REGEX.matches(it.date) }
            .map(::normalizeDailyReflection)
            .groupBy { it.date }
            .map { (_, grouped) -> grouped.maxByOrNull { it.updatedAt } ?: grouped.last() }
            .sortedBy { it.date }
    }

    private fun normalizeDailyReflection(reflection: DailyReflection): DailyReflection {
        val normalizedUpdatedAt = reflection.updatedAt.takeIf { it > 0L } ?: System.currentTimeMillis()
        return reflection.copy(
            wins = reflection.wins,
            difficulties = reflection.difficulties,
            insights = reflection.insights,
            tomorrowFirstAction = reflection.tomorrowFirstAction,
            summary = reflection.summary,
            updatedAt = normalizedUpdatedAt
        )
    }

    private fun writeAtomically(target: File, text: String) {
        val parent = target.parentFile ?: throw IllegalStateException("保存先ディレクトリが不正です")
        if (!parent.exists()) {
            parent.mkdirs()
        }

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

    private fun JSONObject.optLongOrNull(name: String): Long? {
        return if (has(name) && !isNull(name)) optLong(name) else null
    }
}
