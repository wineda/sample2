package com.example.sample2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sample2.data.DailyReflection
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private enum class ReflectionRangeFilter(
    val label: String
) {
    LAST_7_DAYS("最近7日"),
    THIS_MONTH("今月"),
    ALL("すべて")
}

data class ReflectionListUiState(
    val query: String = "",
    val rangeFilter: ReflectionRangeFilter = ReflectionRangeFilter.ALL
)

private sealed interface ReflectionTimelineItem {
    data class MonthHeader(val label: String) : ReflectionTimelineItem
    data class ReflectionCard(val reflection: DailyReflection) : ReflectionTimelineItem
}

@Composable
fun ReflectionTimelineScreen(
    reflections: List<DailyReflection>,
    onOpenReflection: (String) -> Unit,
    onCreateToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    var uiState by remember { mutableStateOf(ReflectionListUiState()) }

    val timelineItems = remember(reflections, uiState) {
        buildTimelineItems(
            reflections = reflections,
            uiState = uiState
        )
    }

    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "振り返りタイムライン",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onCreateToday) {
                    Text("今日を入力")
                }
            }

            OutlinedTextField(
                value = uiState.query,
                onValueChange = { value ->
                    uiState = uiState.copy(query = value)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("キーワード検索") }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ReflectionRangeFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = filter == uiState.rangeFilter,
                        onClick = {
                            uiState = uiState.copy(rangeFilter = filter)
                        },
                        label = { Text(filter.label) }
                    )
                }
            }

            if (timelineItems.isEmpty()) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = "まだ振り返りがありません",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(
                        items = timelineItems,
                        key = { item ->
                            when (item) {
                                is ReflectionTimelineItem.MonthHeader -> "header_${item.label}"
                                is ReflectionTimelineItem.ReflectionCard -> "reflection_${item.reflection.date}"
                            }
                        }
                    ) { item ->
                        when (item) {
                            is ReflectionTimelineItem.MonthHeader -> {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            is ReflectionTimelineItem.ReflectionCard -> {
                                ReflectionTimelineCard(
                                    reflection = item.reflection,
                                    onClick = { onOpenReflection(item.reflection.date) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReflectionTimelineCard(
    reflection: DailyReflection,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = formatReflectionDate(reflection.date),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TimelineSnippetLine("ひとこと", reflection.summary)
            TimelineSnippetLine("うまくいった", reflection.wins)
            TimelineSnippetLine("しんどかった", reflection.difficulties)
            TimelineSnippetLine("明日まずやる", reflection.tomorrowFirstAction)
        }
    }
}

@Composable
private fun TimelineSnippetLine(
    label: String,
    text: String
) {
    val displayText = text.ifBlank { "-" }
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = displayText,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun buildTimelineItems(
    reflections: List<DailyReflection>,
    uiState: ReflectionListUiState
): List<ReflectionTimelineItem> {
    val today = LocalDate.now()
    val filtered = reflections
        .asSequence()
        .filter { it.hasAnyContent() }
        .sortedByDescending { it.date }
        .filter { reflection ->
            val date = reflection.date.toLocalDateOrNull() ?: return@filter false
            when (uiState.rangeFilter) {
                ReflectionRangeFilter.LAST_7_DAYS -> !date.isBefore(today.minusDays(6))
                ReflectionRangeFilter.THIS_MONTH ->
                    date.year == today.year && date.monthValue == today.monthValue

                ReflectionRangeFilter.ALL -> true
            }
        }
        .filter { reflection ->
            if (uiState.query.isBlank()) {
                true
            } else {
                val q = uiState.query.trim().lowercase(Locale.JAPAN)
                reflection.searchableText().contains(q)
            }
        }
        .toList()

    if (filtered.isEmpty()) return emptyList()

    val items = mutableListOf<ReflectionTimelineItem>()
    var currentHeader: String? = null
    filtered.forEach { reflection ->
        val header = monthHeaderLabel(reflection.date)
        if (header != currentHeader) {
            currentHeader = header
            items += ReflectionTimelineItem.MonthHeader(header)
        }
        items += ReflectionTimelineItem.ReflectionCard(reflection)
    }
    return items
}

private fun DailyReflection.hasAnyContent(): Boolean {
    return wins.isNotBlank() ||
            difficulties.isNotBlank() ||
            insights.isNotBlank() ||
            tomorrowFirstAction.isNotBlank() ||
            summary.isNotBlank()
}

private fun DailyReflection.searchableText(): String {
    return listOf(
        date,
        wins,
        difficulties,
        insights,
        tomorrowFirstAction,
        summary
    ).joinToString("\n").lowercase(Locale.JAPAN)
}

private fun monthHeaderLabel(dateText: String): String {
    val date = dateText.toLocalDateOrNull() ?: return dateText
    return "${date.year}年${date.monthValue}月"
}

private fun formatReflectionDate(dateText: String): String {
    val date = dateText.toLocalDateOrNull() ?: return dateText
    return date.format(DateTimeFormatter.ofPattern("yyyy年M月d日(E)", Locale.JAPAN))
}

private fun String.toLocalDateOrNull(): LocalDate? {
    return try {
        LocalDate.parse(this)
    } catch (_: DateTimeParseException) {
        null
    }
}
