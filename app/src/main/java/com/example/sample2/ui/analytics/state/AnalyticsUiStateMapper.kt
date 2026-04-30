package com.example.sample2.ui.analytics.state

import com.example.sample2.analytics.DailyPersonalityScore
import com.example.sample2.analytics.PersonalityState
import com.example.sample2.data.ActionType
import com.example.sample2.data.DailyRecord
import com.example.sample2.data.MessageV2
import com.example.sample2.ui.theme.AnalyticsTokens
import java.time.Instant
import java.time.ZoneId

object AnalyticsUiStateMapper {
    private val zoneId = ZoneId.of("Asia/Tokyo")

    fun map(scores: List<DailyPersonalityScore>, messages: List<MessageV2>, records: List<DailyRecord>, period: AnalyticsPeriod): AnalyticsUiState {
        val filtered = if (period.days < 0) scores else scores.takeLast(period.days)
        if (filtered.isEmpty()) return AnalyticsUiState(period = period)
        val latest = filtered.last()
        val previous = filtered.dropLast(1).lastOrNull()
        val trend = previous?.let { ((latest.stability - it.stability) / it.stability * 100.0).toFloat() } ?: 0f
        val baselines = mapOf(
            "stability" to filtered.map { it.stability }.average().toFloat(),
            "anxiety" to filtered.map { it.anxiety }.average().toFloat(),
            "energy" to filtered.map { it.energy }.average().toFloat(),
            "control" to filtered.map { it.control }.average().toFloat()
        )
        val timeline = filtered.map { TimelinePoint(it.date, it.stability.toFloat(), it.anxiety.toFloat() * 10f, it.energy.toFloat(), it.control.toFloat()) }

        val maxCount = filtered.size.coerceAtLeast(1)
        fun count(type: ActionType): Int {
            val dates = filtered.map { it.date }.toSet()
            return messages.count { m -> Instant.ofEpochMilli(m.timestamp).atZone(zoneId).toLocalDate() in dates && type.matches(m.flags) }
        }
        val behaviors = listOf(
            BehaviorData("チャレンジ", count(ActionType.CHALLENGE), count(ActionType.CHALLENGE) / maxCount.toFloat(), AnalyticsTokens.green),
            BehaviorData("すぐやる", count(ActionType.QUICK_ACTION), count(ActionType.QUICK_ACTION) / maxCount.toFloat(), AnalyticsTokens.violet),
            BehaviorData("委譲", count(ActionType.DELEGATE), count(ActionType.DELEGATE) / maxCount.toFloat(), AnalyticsTokens.blue),
            BehaviorData("細分化", count(ActionType.BREAKDOWN), count(ActionType.BREAKDOWN) / maxCount.toFloat(), AnalyticsTokens.amber)
        )
        val endDate = latest.date
        val rec = records.mapNotNull { runCatching { java.time.LocalDate.parse(it.date) to it }.getOrNull() }
            .filter { (d, _) -> period.days < 0 || !d.isBefore(endDate.minusDays((period.days - 1).toLong())) }
            .sortedBy { it.first }
        val steps = rec.map { (d, r) -> DailyBarData(d, r.steps.toFloat(), d == endDate) }
        val sleep = rec.map { (d, r) -> DailyBarData(d, r.sleep.durationMinutes / 60f, d == endDate) }
        val kpis = listOf(
            kpi("安定度", latest.stability.toFloat(), 100f, baselines.getValue("stability"), ((latest.stability - (previous?.stability ?: latest.stability))).toFloat(), AnalyticsTokens.blue),
            kpi("不安", latest.anxiety.toFloat(), 10f, baselines.getValue("anxiety"), ((latest.anxiety - (previous?.anxiety ?: latest.anxiety))).toFloat(), AnalyticsTokens.red),
            kpi("活力", latest.energy.toFloat(), 100f, baselines.getValue("energy"), ((latest.energy - (previous?.energy ?: latest.energy))).toFloat(), AnalyticsTokens.amber),
            kpi("制御感", latest.control.toFloat(), 100f, baselines.getValue("control"), ((latest.control - (previous?.control ?: latest.control))).toFloat(), AnalyticsTokens.teal)
        )
        return AnalyticsUiState(period, latest.state, trend, kpis, baselines, timeline, behaviors, steps, sleep, latest.summary, false)
    }

    private fun kpi(label: String, value: Float, max: Float, baseline: Float, delta: Float, color: androidx.compose.ui.graphics.Color) =
        KpiData(label, value, max, baseline, delta, color)
}
