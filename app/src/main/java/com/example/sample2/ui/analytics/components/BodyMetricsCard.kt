package com.example.sample2.ui.analytics.components
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.sample2.ui.analytics.state.DailyBarData
import com.example.sample2.ui.theme.AnalyticsTokens
@Composable fun BodyMetricsCard(steps: List<DailyBarData>, sleep: List<DailyBarData>){ Card{ Column{ Bars(steps,5000f,true); Bars(sleep,9f,false) } } }
@Composable private fun Bars(data:List<DailyBarData>, target:Float, steps:Boolean){ Canvas(Modifier.fillMaxWidth().height(120.dp).padding(12.dp)){ val n=data.size.coerceAtLeast(1); val w=size.width/n; data.forEachIndexed{i,d-> val h=(d.value/target).coerceIn(0f,1f)*size.height; val c= if(d.isToday) AnalyticsTokens.blue else if(!steps && d.value<4.5f) AnalyticsTokens.red else AnalyticsTokens.green; drawRoundRect(c, topLeft= androidx.compose.ui.geometry.Offset(i*w+4, size.height-h), size= androidx.compose.ui.geometry.Size(w-8,h), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f,8f)) } } }
