package com.example.sample2.ui.analytics.components
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.sample2.ui.analytics.state.TimelinePoint
import com.example.sample2.ui.theme.AnalyticsTokens
@Composable fun EmotionTimelineCard(points:List<TimelinePoint>){ Card{ Canvas(Modifier.fillMaxWidth().height(180.dp)){ if(points.isEmpty()) return@Canvas; fun line(sel:(TimelinePoint)->Float, color: androidx.compose.ui.graphics.Color){ val step=size.width/(points.size-1).coerceAtLeast(1); for(i in 0 until points.lastIndex){ drawLine(color, Offset(i*step, size.height*(1f-sel(points[i])/100f)), Offset((i+1)*step, size.height*(1f-sel(points[i+1])/100f)), strokeWidth=3f) } }; line({it.stability}, AnalyticsTokens.blue); line({it.anxiety}, AnalyticsTokens.red); line({it.energy}, AnalyticsTokens.amber); line({it.control}, AnalyticsTokens.teal); drawLine(AnalyticsTokens.text3, Offset(0f,size.height*0.5f), Offset(size.width,size.height*0.5f), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f,8f)), strokeWidth = 2f) } } }
