package com.example.sample2.ui

import com.example.sample2.analytics.PersonalityScoreModel
import com.example.sample2.data.DailyRecord
import com.example.sample2.data.DailyReflection
import com.example.sample2.data.MessageV2
import com.example.sample2.data.maxEmotionOrNull
import com.example.sample2.data.toItemList
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class ReflectionHints(
    val messageCountText: String,
    val emotionTrendText: String,
    val actionFlagsText: String,
    val dailyRecordText: String,
    val analysisSummaryText: String
)

object ReflectionHintBuilder {
    private val zoneId: ZoneId = ZoneId.of("Asia/Tokyo")

    fun build(
        date: String,
        allMessages: List<MessageV2>,
        allDailyRecords: List<DailyRecord>,
        reflection: DailyReflection?
    ): ReflectionHints {
        val targetDate = runCatching { LocalDate.parse(date) }.getOrNull()
        val messages = targetDate?.let { target ->
            allMessages.filter { message ->
                Instant.ofEpochMilli(message.timestamp).atZone(zoneId).toLocalDate() == target
            }
        }.orEmpty()

        val dailyRecord = allDailyRecords.firstOrNull { it.date == date }
        val dailyScore = targetDate?.let {
            PersonalityScoreModel.analyzeDay(
                date = it,
                messages = messages,
                dailyRecord = dailyRecord
            )
        }

        val dominantEmotion = messages
            .groupBy { it.emotions.maxEmotionOrNull() }
            .filterKeys { it != null }
            .maxByOrNull { it.value.size }
            ?.key
            ?.label

        val actionFlags = messages
            .flatMap { message ->
                message.flags.toItemList().filter { it.enabled }.map { it.label }
            }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .joinToString("、") { "${it.key}(${it.value})" }
            .ifBlank { "フラグなし" }

        val sleepText = dailyRecord?.sleep?.durationMinutes?.takeIf { it > 0 }?.let { minutes ->
            "睡眠 ${minutes / 60}時間${minutes % 60}分"
        } ?: "睡眠データなし"

        val stepsText = dailyRecord?.steps?.takeIf { it > 0 }?.let { "歩数 ${it}歩" } ?: "歩数データなし"

        return ReflectionHints(
            messageCountText = "メッセージ ${messages.size}件",
            emotionTrendText = dominantEmotion?.let { "感情傾向: $it が多め" } ?: "感情傾向: データ不足",
            actionFlagsText = "行動フラグ: $actionFlags",
            dailyRecordText = "$sleepText / $stepsText",
            analysisSummaryText = dailyScore?.summary ?: "分析サマリ: この日はまだ十分なデータがありません。"
        )
    }
}
