package com.example.sample2.domain.analytics.model

import com.example.sample2.domain.analytics.service.DailyEmotionAverages
import com.example.sample2.domain.analytics.service.DailyFlagAverages
import com.example.sample2.domain.analytics.service.DailyFlagCounts
import com.example.sample2.domain.analytics.service.DailyPersonalityScore
import com.example.sample2.domain.analytics.service.DailyStructuredFeatures
import com.example.sample2.domain.analytics.service.IntradayPersonalityPoint
import com.example.sample2.domain.analytics.service.PersonalBaseline
import com.example.sample2.domain.analytics.service.PersonalityState
import com.example.sample2.domain.analytics.service.WeeklyPersonalityScore

typealias PersonalityDailyScore = DailyPersonalityScore
typealias PersonalityWeeklyScore = WeeklyPersonalityScore
typealias PersonalityBaseline = PersonalBaseline
typealias PersonalityDailyFeatures = DailyStructuredFeatures
typealias PersonalityDailyEmotionAverages = DailyEmotionAverages
typealias PersonalityDailyFlagCounts = DailyFlagCounts
typealias PersonalityDailyFlagAverages = DailyFlagAverages
typealias PersonalityIntradayPoint = IntradayPersonalityPoint
typealias PersonalityStatus = PersonalityState
