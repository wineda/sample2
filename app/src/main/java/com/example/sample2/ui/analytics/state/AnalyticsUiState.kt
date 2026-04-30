package com.example.sample2.ui.analytics.state

import androidx.compose.ui.graphics.Color
import com.example.sample2.analytics.PersonalityState
import java.time.LocalDate

enum class AnalyticsPeriod(val days: Int) { D7(7), D14(14), D30(30), ALL(-1) }

data class KpiData(val label: String, val value: Float, val maxValue: Float, val baseline: Float, val delta: Float, val color: Color)
data class TimelinePoint(val date: LocalDate, val stability: Float, val anxiety: Float, val energy: Float, val control: Float)
data class BehaviorData(val label: String, val count: Int, val progress: Float, val color: Color)
data class DailyBarData(val date: LocalDate, val value: Float, val isToday: Boolean = false)

data class AnalyticsUiState(
    val period: AnalyticsPeriod = AnalyticsPeriod.D7,
    val state: PersonalityState = PersonalityState.RECOVERING,
    val stateTrend: Float = 0f,
    val kpis: List<KpiData> = emptyList(),
    val baselineMap: Map<String, Float> = emptyMap(),
    val timeline: List<TimelinePoint> = emptyList(),
    val behaviors: List<BehaviorData> = emptyList(),
    val steps: List<DailyBarData> = emptyList(),
    val sleep: List<DailyBarData> = emptyList(),
    val aiInsight: String = "",
    val isLoading: Boolean = false,
)
