package com.example.sample2.domain.analytics.feature

import com.example.sample2.data.DailyRecord
import com.example.sample2.data.MessageV2
import com.example.sample2.domain.analytics.service.DailyPersonalityScore
import com.example.sample2.domain.analytics.service.PersonalityScoreService
import java.time.ZoneId

object FeatureExtractor {
    fun extract(messages: List<MessageV2>, dailyRecords: List<DailyRecord>, zoneId: ZoneId): List<DailyPersonalityScore> =
        PersonalityScoreService.analyzeAllDaysRaw(messages, dailyRecords, zoneId)
}
