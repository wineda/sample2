package com.example.sample2.domain.analytics.scoring

import com.example.sample2.data.DailyRecord
import com.example.sample2.data.MessageV2
import com.example.sample2.domain.analytics.service.PersonalityScoreService
import java.time.LocalDate

object ScoringEngine {
    fun scoreDay(date: LocalDate, messages: List<MessageV2>, dailyRecord: DailyRecord?) =
        PersonalityScoreService.analyzeDayRaw(date, messages, dailyRecord)
}
