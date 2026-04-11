package com.example.sample2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.sample2.analytics.buildActionHeatmap
import com.example.sample2.analytics.buildEmotionHeatmap
import com.example.sample2.analytics.getMessagesForActionSlot
import com.example.sample2.analytics.getMessagesForEmotionSlot
import com.example.sample2.data.ActionType
import com.example.sample2.data.EmotionType
import com.example.sample2.data.MessageV2
import com.example.sample2.ui.filter.PeriodPreset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class HeatmapCategory(val label: String) {
    EMOTION("感情"),
    ACTION("行動")
}

@Composable
fun HeatmapScreen(
    state: ChatState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages = state.messages

    val initialPreset = remember { PeriodPreset.values().first() }
    val initialRange = remember { initialPreset.resolveRange() }

    var selectedCategory by rememberSaveable {
        mutableStateOf(HeatmapCategory.EMOTION)
    }

    var selectedPeriodName by rememberSaveable {
        mutableStateOf(initialPreset.name)
    }

    var fromDate by rememberSaveable {
        mutableStateOf(initialRange.first)
    }

    var toDate by rememberSaveable {
        mutableStateOf(initialRange.second)
    }

    val selectedPeriod = remember(selectedPeriodName) {
        PeriodPreset.valueOf(selectedPeriodName)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        PeriodPresetRow(
            selectedPeriod = selectedPeriod,
            onSelectPreset = { preset ->
                selectedPeriodName = preset.name
                val (from, to) = preset.resolveRange()
                fromDate = from
                toDate = to
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${formatDate(fromDate)} - ${formatDate(toDate)}",
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        HeatmapCategorySwitch(
            selected = selectedCategory,
            onSelected = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (selectedCategory) {
                HeatmapCategory.EMOTION -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(EmotionType.values().toList()) { emotion ->
                            Surface(
                                tonalElevation = 2.dp,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                EmotionHeatmapBlock(
                                    emotion = emotion,
                                    messages = messages,
                                    fromDate = fromDate,
                                    toDate = toDate
                                )
                            }
                        }
                    }
                }

                HeatmapCategory.ACTION -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(ActionType.values().toList()) { action ->
                            Surface(
                                tonalElevation = 2.dp,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ActionHeatmapBlock(
                                    action = action,
                                    messages = messages,
                                    fromDate = fromDate,
                                    toDate = toDate
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapCategorySwitch(
    selected: HeatmapCategory,
    onSelected: (HeatmapCategory) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        HeatmapCategory.values().forEachIndexed { index, category ->
            SegmentedButton(
                selected = selected == category,
                onClick = { onSelected(category) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = HeatmapCategory.values().size
                ),
                label = {
                    Text(category.label)
                }
            )
        }
    }
}

@Composable
fun EmotionHeatmapBlock(
    emotion: EmotionType,
    messages: List<MessageV2>,
    fromDate: Long?,
    toDate: Long?
) {
    var selectedPair by remember { mutableStateOf<Pair<String, String>?>(null) }
    val uiSpec = emotion.toUiSpec()

    val heatmapData = buildEmotionHeatmap(
        messages = messages,
        emotion = emotion,
        fromDate = fromDate,
        toDate = toDate
    )

    val days = listOf("月", "火", "水", "木", "金", "土", "日")
    val slots = listOf("朝", "昼", "夕", "夜")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = uiSpec.iconRes),
                contentDescription = emotion.label,
                tint = uiSpec.color,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = emotion.label,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row {
            Spacer(modifier = Modifier.width(16.dp))

            days.forEach { day ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(day, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        slots.forEach { slot ->
            Row {
                Box(
                    modifier = Modifier.width(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(slot, style = MaterialTheme.typography.labelSmall)
                }

                days.forEach { day ->
                    val value = heatmapData[day to slot] ?: 0.0

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(1.dp)
                            .background(colorForCount(uiSpec.color, value))
                            .clickable {
                                selectedPair = day to slot
                            }
                    )
                }
            }
        }
    }

    selectedPair?.let { (day, slot) ->
        val slotMessages = getMessagesForEmotionSlot(
            messages = messages,
            day = day,
            slot = slot,
            emotion = emotion,
            fromDate = fromDate,
            toDate = toDate
        )

        AlertDialog(
            onDismissRequest = { selectedPair = null },
            confirmButton = {
                TextButton(onClick = { selectedPair = null }) {
                    Text("閉じる")
                }
            },
            title = {
                Text("$day / $slot (${slotMessages.size}件)")
            },
            text = {
                if (slotMessages.isEmpty()) {
                    Text("データなし")
                } else {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        slotMessages.forEach {
                            val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.JAPAN)
                            Text("${sdf.format(Date(it.timestamp))} ${it.text}")
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ActionHeatmapBlock(
    action: ActionType,
    messages: List<MessageV2>,
    fromDate: Long?,
    toDate: Long?
) {
    var selectedPair by remember { mutableStateOf<Pair<String, String>?>(null) }
    val uiSpec = action.toUiSpec()

    val heatmapData = buildActionHeatmap(
        messages = messages,
        action = action,
        fromDate = fromDate,
        toDate = toDate
    )

    val days = listOf("月", "火", "水", "木", "金", "土", "日")
    val slots = listOf("朝", "昼", "夕", "夜")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = uiSpec.iconRes),
                contentDescription = action.label,
                tint = uiSpec.color,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = action.label,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row {
            Spacer(modifier = Modifier.width(16.dp))

            days.forEach { day ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(day, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        slots.forEach { slot ->
            Row {
                Box(
                    modifier = Modifier.width(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(slot, style = MaterialTheme.typography.labelSmall)
                }

                days.forEach { day ->
                    val value = heatmapData[day to slot] ?: 0.0

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(1.dp)
                            .background(colorForCount(uiSpec.color, value))
                            .clickable {
                                selectedPair = day to slot
                            }
                    )
                }
            }
        }
    }

    selectedPair?.let { (day, slot) ->
        val slotMessages = getMessagesForActionSlot(
            messages = messages,
            day = day,
            slot = slot,
            action = action,
            fromDate = fromDate,
            toDate = toDate
        )

        AlertDialog(
            onDismissRequest = { selectedPair = null },
            confirmButton = {
                TextButton(onClick = { selectedPair = null }) {
                    Text("閉じる")
                }
            },
            title = {
                Text("$day / $slot (${slotMessages.size}件)")
            },
            text = {
                if (slotMessages.isEmpty()) {
                    Text("データなし")
                } else {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        slotMessages.forEach {
                            val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.JAPAN)
                            Text("${sdf.format(Date(it.timestamp))} ${it.text}")
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun PeriodPresetRow(
    selectedPeriod: PeriodPreset,
    onSelectPreset: (PeriodPreset) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PeriodPreset.values().forEach { preset ->
            FilterChip(
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.surface
                ),
                selected = selectedPeriod == preset,
                onClick = {
                    onSelectPreset(preset)
                },
                label = { Text(preset.label) }
            )
        }
    }
}

fun colorForCount(baseColor: Color, count: Double): Color {
    return when {
        count == 0.0 -> Color.LightGray
        count == 1.0 -> baseColor.copy(alpha = 0.3f)
        count == 2.0 -> baseColor.copy(alpha = 0.6f)
        else -> baseColor.copy(alpha = 1.0f)
    }
}

fun formatDate(ts: Long?): String {
    if (ts == null) return "未設定"
    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
    return sdf.format(Date(ts))
}
