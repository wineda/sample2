package com.example.sample2.domain.analytics.service

import com.example.sample2.analytics.PersonalityScoreModel
import com.example.sample2.data.DailyRecord
import com.example.sample2.data.MessageV2
import java.time.LocalDate
import java.time.ZoneId

typealias DailyEmotionAverages = com.example.sample2.analytics.DailyEmotionAverages
typealias DailyFlagCounts = com.example.sample2.analytics.DailyFlagCounts
typealias DailyStructuredFeatures = com.example.sample2.analytics.DailyStructuredFeatures
typealias DailyPersonalityScore = com.example.sample2.analytics.DailyPersonalityScore
typealias WeeklyPersonalityScore = com.example.sample2.analytics.WeeklyPersonalityScore
typealias DailyFlagAverages = com.example.sample2.analytics.DailyFlagAverages
typealias PersonalBaseline = com.example.sample2.analytics.PersonalBaseline
typealias IntradayPersonalityPoint = com.example.sample2.analytics.IntradayPersonalityPoint
typealias PersonalityState = com.example.sample2.analytics.PersonalityState

object PersonalityScoreService {
    fun analyzeAllDaysRaw(
        messages: List<MessageV2>,
        dailyRecords: List<DailyRecord> = emptyList(),
        zoneId: ZoneId = ZoneId.of("Asia/Tokyo")
    ) = PersonalityScoreModel.analyzeAllDaysRaw(messages, dailyRecords, zoneId)

    fun analyzeDayRaw(
        date: LocalDate,
        messages: List<MessageV2>,
        dailyRecord: DailyRecord? = null,
        baseline: PersonalBaseline? = null
    ) = PersonalityScoreModel.analyzeDayRaw(date, messages, dailyRecord, baseline)

    fun buildWeeklyScores(dailyScores: List<DailyPersonalityScore>) =
        PersonalityScoreModel.buildWeeklyScores(dailyScores)
}
