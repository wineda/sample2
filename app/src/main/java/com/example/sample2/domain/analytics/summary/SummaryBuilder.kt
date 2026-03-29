package com.example.sample2.domain.analytics.summary

import com.example.sample2.domain.analytics.service.DailyPersonalityScore

object SummaryBuilder {
    fun latestSummary(scores: List<DailyPersonalityScore>): String = scores.maxByOrNull { it.date }?.summary.orEmpty()
}
