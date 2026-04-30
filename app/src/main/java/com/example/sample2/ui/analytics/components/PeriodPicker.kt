package com.example.sample2.ui.analytics.components
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.sample2.ui.analytics.state.AnalyticsPeriod
@Composable fun PeriodPicker(selected: AnalyticsPeriod, onSelect:(AnalyticsPeriod)->Unit){ Row { AnalyticsPeriod.entries.forEach { p -> FilterChip(selected=selected==p,onClick={onSelect(p)},label={Text(when(p){AnalyticsPeriod.D7->"7日";AnalyticsPeriod.D14->"14日";AnalyticsPeriod.D30->"30日";AnalyticsPeriod.ALL->"全期間"})}) } } }
