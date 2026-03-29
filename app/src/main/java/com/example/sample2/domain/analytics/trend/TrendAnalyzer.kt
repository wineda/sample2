package com.example.sample2.domain.analytics.trend

import com.example.sample2.domain.analytics.service.DailyPersonalityScore
import com.example.sample2.domain.analytics.service.PersonalityScoreService

object TrendAnalyzer {
    fun weekly(scores: List<DailyPersonalityScore>) = PersonalityScoreService.buildWeeklyScores(scores)
}
