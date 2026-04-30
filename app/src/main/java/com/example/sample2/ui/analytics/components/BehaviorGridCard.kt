package com.example.sample2.ui.analytics.components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sample2.ui.analytics.state.BehaviorData
@Composable fun BehaviorGridCard(items: List<BehaviorData>){ Card{ Column{ items.chunked(2).forEach{ row-> Row{ row.forEach{ b-> Column(Modifier.weight(1f).padding(12.dp)){ Text(b.label); Text("${b.count} 回"); Box(Modifier.fillMaxWidth().height(4.dp).background(b.color.copy(alpha=0.3f))) } } } } } } }
