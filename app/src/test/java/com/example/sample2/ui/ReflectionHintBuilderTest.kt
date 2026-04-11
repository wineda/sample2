package com.example.sample2.ui

import com.example.sample2.data.ActionFlags
import com.example.sample2.data.DailyRecord
import com.example.sample2.data.EmotionMetrics
import com.example.sample2.data.MessageV2
import com.example.sample2.data.SleepData
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ReflectionHintBuilderTest {

    @Test
    fun build_returnsFallbackTexts_whenNoDataForDate() {
        val hints = ReflectionHintBuilder.build(
            date = "2026-04-10",
            allMessages = emptyList(),
            allDailyRecords = emptyList(),
            reflection = null
        )

        assertTrue(hints.messageCountText.contains("0件"))
        assertTrue(hints.emotionTrendText.contains("データ不足"))
        assertTrue(hints.dailyRecordText.contains("睡眠データなし"))
        assertTrue(hints.dailyRecordText.contains("歩数データなし"))
    }

    @Test
    fun build_includesEmotionFlagsAndRecordHints_whenDataExists() {
        val zone = ZoneId.of("Asia/Tokyo")
        val timestamp = LocalDate.of(2026, 4, 10)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        val hints = ReflectionHintBuilder.build(
            date = "2026-04-10",
            allMessages = listOf(
                MessageV2(
                    id = "m1",
                    timestamp = timestamp,
                    text = "a",
                    emotions = EmotionMetrics(happy = 3),
                    flags = ActionFlags(exercised = true)
                ),
                MessageV2(
                    id = "m2",
                    timestamp = timestamp,
                    text = "b",
                    emotions = EmotionMetrics(happy = 2),
                    flags = ActionFlags(exercised = true, insight = true)
                )
            ),
            allDailyRecords = listOf(
                DailyRecord(
                    date = "2026-04-10",
                    sleep = SleepData(durationMinutes = 420, quality = 3),
                    steps = 8000
                )
            ),
            reflection = null
        )

        assertTrue(hints.messageCountText.contains("2件"))
        assertTrue(hints.emotionTrendText.contains("喜び"))
        assertTrue(hints.actionFlagsText.contains("運動"))
        assertTrue(hints.dailyRecordText.contains("7時間0分"))
        assertTrue(hints.dailyRecordText.contains("8000歩"))
    }
}
