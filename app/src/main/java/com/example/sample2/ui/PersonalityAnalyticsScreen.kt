package com.example.sample2.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sample2.data.DailyRecord
import com.example.sample2.data.MessageV2
import com.example.sample2.analytics.PersonalityScoreModel
import com.example.sample2.ui.analytics.components.*
import com.example.sample2.ui.analytics.state.AnalyticsPeriod

import com.example.sample2.ui.analytics.state.AnalyticsUiStateMapper
import com.example.sample2.ui.theme.AnalyticsTokens
import com.example.sample2.ui.theme.AnalyticsTypography

enum class AnalyticsDisplayMode { DETAIL, CHARTS, MAP }

@Composable
fun PersonalityAnalyticsScreen(messages: List<MessageV2>, dailyRecords: List<DailyRecord>, onUpdateDailyRecord: (DailyRecord) -> Unit, initialDisplayMode: AnalyticsDisplayMode = AnalyticsDisplayMode.CHARTS, displayModes: List<AnalyticsDisplayMode> = AnalyticsDisplayMode.entries, modifier: Modifier = Modifier) {
    val ui = AnalyticsUiStateMapper.map(PersonalityScoreModel.analyzeAllDays(messages, dailyRecords), messages, dailyRecords, AnalyticsPeriod.D7)
    Column(modifier.fillMaxSize().background(AnalyticsTokens.bg).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("分析", style = AnalyticsTypography.screenTitle); Text("LAST 7 DAYS", style = AnalyticsTypography.metaLabel) }; Row { IconButton(onClick = {}) { Icon(Icons.Default.FileUpload, null) }; IconButton(onClick = {}) { Icon(Icons.Default.FilterAlt, null) } } }
        ViewToggle()
        PeriodPicker(selected = ui.period, onSelect = {})
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { StatusSummaryCard(ui) }
            item { AiInsightCard(ui.aiInsight) }
            item { EmotionTimelineCard(ui.timeline) }
            item { BehaviorGridCard(ui.behaviors) }
            item { BodyMetricsCard(ui.steps, ui.sleep) }
        }
    }
}
