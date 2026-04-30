package com.example.sample2.ui.analytics.components
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sample2.analytics.PersonalityState
import com.example.sample2.ui.analytics.state.AnalyticsUiState
@Composable fun StatusSummaryCard(state: AnalyticsUiState){ Card{ Column(Modifier.padding(12.dp)){ Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){ Text(state.state.label); Text("${"%.1f".format(state.stateTrend)}%") }; LazyVerticalGrid(columns=GridCells.Fixed(4), userScrollEnabled=false, modifier=Modifier.height(120.dp)){ items(state.kpis){ KpiTile(it) } } } } }
