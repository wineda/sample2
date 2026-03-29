package com.example.sample2.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sample2.data.EmotionType
import com.example.sample2.data.MessageV2
import java.util.Calendar

data class JournalFilterState(
    val weekday: WeekdayFilter = WeekdayFilter.ALL,
    val emotions: Set<EmotionType> = emptySet(),
    val emotionMatchMode: EmotionMatchMode = EmotionMatchMode.ANY
) {
    val isActive: Boolean
        get() = weekday != WeekdayFilter.ALL || emotions.isNotEmpty()

    fun summaryText(): String {
        if (!isActive) return "絞り込みなし"

        val parts = mutableListOf<String>()

        if (weekday != WeekdayFilter.ALL) {
            parts += weekday.label
        }

        if (emotions.isNotEmpty()) {
            parts += emotions
                .sortedBy { it.ordinal }
                .joinToString("・") { it.label }
        }

        return parts.joinToString(" / ")
    }

    fun matches(message: MessageV2): Boolean {
        val weekdayMatched = when (weekday) {
            WeekdayFilter.ALL -> true
            else -> {
                val cal = Calendar.getInstance().apply {
                    timeInMillis = message.timestamp
                }
                cal.get(Calendar.DAY_OF_WEEK) == weekday.calendarDay
            }
        }

        if (!weekdayMatched) return false
        if (emotions.isEmpty()) return true

        val emotionMatches = emotions.map { emotion ->
            message.hasEmotion(emotion, threshold = 1)
        }

        return when (emotionMatchMode) {
            EmotionMatchMode.ANY -> emotionMatches.any { it }
            EmotionMatchMode.ALL -> emotionMatches.all { it }
        }
    }
}

enum class EmotionMatchMode(val label: String) {
    ANY("いずれか"),
    ALL("すべて")
}

enum class WeekdayFilter(
    val label: String,
    val calendarDay: Int?
) {
    ALL("すべて", null),
    MON("月曜", Calendar.MONDAY),
    TUE("火曜", Calendar.TUESDAY),
    WED("水曜", Calendar.WEDNESDAY),
    THU("木曜", Calendar.THURSDAY),
    FRI("金曜", Calendar.FRIDAY),
    SAT("土曜", Calendar.SATURDAY),
    SUN("日曜", Calendar.SUNDAY)
}

@Composable
fun JournalFilterHeader(
    filterState: JournalFilterState,
    resultCount: Int,
    onOpenSheet: () -> Unit,
    onClearWeekday: () -> Unit,
    onRemoveEmotion: (EmotionType) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSheet() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "絞り込み",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = filterState.summaryText(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${resultCount}件",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "open filter",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (filterState.isActive) {
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filterState.weekday != WeekdayFilter.ALL) {
                    AssistChip(
                        onClick = onClearWeekday,
                        label = { Text("曜日: ${filterState.weekday.label} ×") }
                    )
                }

                filterState.emotions
                    .sortedBy { it.ordinal }
                    .forEach { emotion ->
                        AssistChip(
                            onClick = { onRemoveEmotion(emotion) },
                            label = { Text("${emotion.label} ×") }
                        )
                    }

                AssistChip(
                    onClick = onClearAll,
                    label = { Text("クリア") }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun JournalFilterBottomSheet(
    current: JournalFilterState,
    onDismiss: () -> Unit,
    onApply: (JournalFilterState) -> Unit
) {
    var draftWeekday by remember(current) {
        mutableStateOf(current.weekday)
    }
    var draftEmotions by remember(current) {
        mutableStateOf(current.emotions)
    }
    var draftMode by remember(current) {
        mutableStateOf(current.emotionMatchMode)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "絞り込み",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "曜日",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeekdayFilter.entries.forEach { weekday ->
                    FilterChip(
                        selected = draftWeekday == weekday,
                        onClick = { draftWeekday = weekday },
                        label = { Text(weekday.label) }
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

            Text(
                text = "感情",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EmotionType.entries.forEach { emotion ->
                    val selected = emotion in draftEmotions

                    FilterChip(
                        selected = selected,
                        onClick = {
                            draftEmotions = if (selected) {
                                draftEmotions - emotion
                            } else {
                                draftEmotions + emotion
                            }
                        },
                        label = { Text(emotion.label) }
                    )
                }
            }

            if (draftEmotions.isNotEmpty()) {
                Text(
                    text = "一致条件",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
                )

                Column {
                    EmotionMatchMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { draftMode = mode }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = draftMode == mode,
                                onClick = { draftMode = mode }
                            )
                            Text(
                                text = mode.label,
                                modifier = Modifier.padding(start = 6.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        draftWeekday = WeekdayFilter.ALL
                        draftEmotions = emptySet()
                        draftMode = EmotionMatchMode.ANY
                    }
                ) {
                    Text("クリア")
                }

                Button(
                    onClick = {
                        onApply(
                            JournalFilterState(
                                weekday = draftWeekday,
                                emotions = draftEmotions,
                                emotionMatchMode = draftMode
                            )
                        )
                        onDismiss()
                    }
                ) {
                    Text("適用")
                }
            }
        }
    }
}

private fun MessageV2.hasEmotion(
    emotionType: EmotionType,
    threshold: Int = 1
): Boolean {
    val score = when (emotionType) {
        EmotionType.ANXIETY -> emotions.anxiety
        EmotionType.ANGRY -> emotions.angry
        EmotionType.SAD -> emotions.sad
        EmotionType.HAPPY -> emotions.happy
        EmotionType.CALM -> emotions.calm
    }
    return score >= threshold
}