package com.example.sample2.ui.analytics

import com.example.sample2.analytics.PersonalityScoreModel
import com.example.sample2.data.DailyRecord
import com.example.sample2.data.MessageV2
import com.example.sample2.ui.analytics.state.AnalyticsPeriod
import com.example.sample2.ui.analytics.state.AnalyticsUiStateMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsUiStateMapperTest {
    @Test
    fun mapsToUiState() {
        val msgs = listOf(MessageV2("1", System.currentTimeMillis(), "a"))
        val records = listOf(DailyRecord(date = java.time.LocalDate.now().toString(), steps = 3200))
        val scores = PersonalityScoreModel.analyzeAllDays(msgs, records)
        val ui = AnalyticsUiStateMapper.map(scores, msgs, records, AnalyticsPeriod.D7)
        assertEquals(4, ui.kpis.size)
        assertTrue(ui.aiInsight.isNotBlank())
    }
}
