package com.example.sample2.ui.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sample2.ui.analytics.state.KpiData
import com.example.sample2.ui.theme.AnalyticsTypography

@Composable fun KpiTile(data: KpiData) { Column(Modifier.padding(8.dp)) { Text(data.label, style = AnalyticsTypography.kpiLabel); Text(data.value.toInt().toString(), style = AnalyticsTypography.kpiValue); Text("Δ ${"%.1f".format(data.delta)}", style = AnalyticsTypography.metaLabel); androidx.compose.foundation.layout.Box(Modifier.fillMaxWidth().height(3.dp).background(data.color.copy(alpha = 0.4f))) } }
