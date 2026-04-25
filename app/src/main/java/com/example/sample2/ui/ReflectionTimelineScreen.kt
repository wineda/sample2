package com.example.sample2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.sample2.data.DailyReflection
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private enum class ReflectionFieldFilter(
    val label: String,
    val icon: ImageVector
) {
    SUMMARY("ひとこと", Icons.Default.Chat),
    WINS("うまくいった", Icons.Default.ThumbUp),
    DIFFICULTIES("しんどかった", Icons.Default.SentimentDissatisfied),
    TOMORROW_FIRST_ACTION("明日まずやる", Icons.Default.TaskAlt)
}

private data class ReflectionListUiState(
    val query: String = "",
    val fieldFilter: ReflectionFieldFilter? = null
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

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ReflectionFieldFilter.entries
                        .filter { it != ReflectionFieldFilter.TOMORROW_FIRST_ACTION }
                        .forEach { filter ->
                            FilterChip(
                                selected = filter == uiState.fieldFilter,
                                onClick = {
                                    uiState = uiState.copy(
                                        fieldFilter = if (uiState.fieldFilter == filter) null else filter
                                    )
                                },
                                label = {
                                    Text(
                                        text = filter.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = filter.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            )
                        }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val filter = ReflectionFieldFilter.TOMORROW_FIRST_ACTION
                    FilterChip(
                        selected = filter == uiState.fieldFilter,
                        onClick = {
                            uiState = uiState.copy(
                                fieldFilter = if (uiState.fieldFilter == filter) null else filter
                            )
                        },
                        label = {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = filter.icon,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
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
                                    fieldFilter = uiState.fieldFilter,
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
    fieldFilter: ReflectionFieldFilter?,
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

            val fullText = fieldFilter != null
            if (fieldFilter == null || fieldFilter == ReflectionFieldFilter.SUMMARY) {
                TimelineSnippetLine(
                    label = ReflectionFieldFilter.SUMMARY.label,
                    icon = ReflectionFieldFilter.SUMMARY.icon,
                    text = reflection.summary,
                    fullText = fullText
                )
            }
            if (fieldFilter == null || fieldFilter == ReflectionFieldFilter.WINS) {
                TimelineSnippetLine(
                    label = ReflectionFieldFilter.WINS.label,
                    icon = ReflectionFieldFilter.WINS.icon,
                    text = reflection.wins,
                    fullText = fullText
                )
            }
            if (fieldFilter == null || fieldFilter == ReflectionFieldFilter.DIFFICULTIES) {
                TimelineSnippetLine(
                    label = ReflectionFieldFilter.DIFFICULTIES.label,
                    icon = ReflectionFieldFilter.DIFFICULTIES.icon,
                    text = reflection.difficulties,
                    fullText = fullText
                )
            }
            if (fieldFilter == null || fieldFilter == ReflectionFieldFilter.TOMORROW_FIRST_ACTION) {
                TimelineSnippetLine(
                    label = ReflectionFieldFilter.TOMORROW_FIRST_ACTION.label,
                    icon = ReflectionFieldFilter.TOMORROW_FIRST_ACTION.icon,
                    text = reflection.tomorrowFirstAction,
                    fullText = fullText
                )
            }
        }
    }
}

@Composable
private fun TimelineSnippetLine(
    label: String,
    icon: ImageVector,
    text: String,
    fullText: Boolean
) {
    val displayText = text.ifBlank { "-" }
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = displayText,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (fullText) Int.MAX_VALUE else 1,
            overflow = if (fullText) TextOverflow.Clip else TextOverflow.Ellipsis
        )
    }
}

private fun buildTimelineItems(
    reflections: List<DailyReflection>,
    uiState: ReflectionListUiState
): List<ReflectionTimelineItem> {
    val filtered = reflections
        .asSequence()
        .filter { it.hasAnyContent() }
        .sortedByDescending { it.date }
        .filter { it.date.toLocalDateOrNull() != null }
        .filter { reflection ->
            if (uiState.query.isBlank()) {
                true
            } else {
                val q = uiState.query.trim().lowercase(Locale.JAPAN)
                reflection.searchableText().contains(q)
            }
        }
        .filter { reflection ->
            when (uiState.fieldFilter) {
                null -> true
                ReflectionFieldFilter.SUMMARY -> reflection.summary.isNotBlank()
                ReflectionFieldFilter.WINS -> reflection.wins.isNotBlank()
                ReflectionFieldFilter.DIFFICULTIES -> reflection.difficulties.isNotBlank()
                ReflectionFieldFilter.TOMORROW_FIRST_ACTION -> reflection.tomorrowFirstAction.isNotBlank()
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
