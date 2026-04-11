package com.example.sample2.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sample2.data.DailyReflection
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ReflectionRangeFilter(val label: String) {
    ALL("すべて"),
    LAST_7_DAYS("最近7日"),
    THIS_MONTH("今月")
}

private sealed interface ReflectionTimelineRow {
    data class MonthHeader(val title: String) : ReflectionTimelineRow
    data class ReflectionItem(val data: DailyReflection) : ReflectionTimelineRow
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionTimelineScreen(
    reflections: List<DailyReflection>,
    onOpenReflection: (DailyReflection) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(ReflectionRangeFilter.ALL) }
    var query by remember { mutableStateOf("") }

    val filteredReflections = remember(reflections, selectedFilter, query) {
        val monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val sevenDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -6)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        reflections
            .asSequence()
            .sortedByDescending { it.date }
            .filter { reflection ->
                when (selectedFilter) {
                    ReflectionRangeFilter.ALL -> true
                    ReflectionRangeFilter.LAST_7_DAYS -> reflection.date >= dateKey(sevenDaysAgo.timeInMillis)
                    ReflectionRangeFilter.THIS_MONTH -> reflection.date >= dateKey(monthStart.timeInMillis)
                }
            }
            .filter { reflection ->
                if (query.isBlank()) return@filter true
                val keyword = query.trim()
                listOf(
                    reflection.summary,
                    reflection.wins,
                    reflection.difficulties,
                    reflection.tomorrowFirstAction
                ).any { it.contains(keyword, ignoreCase = true) }
            }
            .toList()
    }

    val timelineRows = remember(filteredReflections) {
        buildList {
            var previousMonth = ""
            filteredReflections.forEach { reflection ->
                val monthTitle = formatMonthTitle(reflection.date)
                if (monthTitle != previousMonth) {
                    add(ReflectionTimelineRow.MonthHeader(monthTitle))
                    previousMonth = monthTitle
                }
                add(ReflectionTimelineRow.ReflectionItem(reflection))
            }
        }
    }

    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = "振り返りタイムライン",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ReflectionRangeFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("キーワード検索") },
                placeholder = { Text("summary / wins / difficulties / tomorrow") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (timelineRows.isEmpty()) {
                ReflectionEmptyState(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = timelineRows,
                        key = { row ->
                            when (row) {
                                is ReflectionTimelineRow.MonthHeader -> "month-${row.title}"
                                is ReflectionTimelineRow.ReflectionItem -> "reflection-${row.data.date}"
                            }
                        }
                    ) { row ->
                        when (row) {
                            is ReflectionTimelineRow.MonthHeader -> {
                                Text(
                                    text = row.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                )
                            }

                            is ReflectionTimelineRow.ReflectionItem -> {
                                ReflectionCard(
                                    reflection = row.data,
                                    onClick = { onOpenReflection(row.data) }
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
private fun ReflectionCard(
    reflection: DailyReflection,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = formatDateLabel(reflection.date),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ReflectionSnippet(label = "ひとこと", text = reflection.summary)
            ReflectionSnippet(label = "うまくいったこと", text = reflection.wins)
            ReflectionSnippet(label = "しんどかったこと", text = reflection.difficulties)
            ReflectionSnippet(label = "明日まずやること", text = reflection.tomorrowFirstAction)
        }
    }
}

@Composable
private fun ReflectionSnippet(
    label: String,
    text: String
) {
    val value = text.ifBlank { "（未入力）" }
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun ReflectionEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "まだ振り返りがありません",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "入力した振り返りがここに時系列で表示されます。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ReflectionDetailDialog(
    reflection: DailyReflection,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "${formatDateLabel(reflection.date)} の振り返り")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ReflectionDetailBlock(title = "ひとことまとめ", value = reflection.summary)
                ReflectionDetailBlock(title = "今日うまくいったこと", value = reflection.wins)
                ReflectionDetailBlock(title = "今日しんどかったこと", value = reflection.difficulties)
                ReflectionDetailBlock(title = "明日まずやること", value = reflection.tomorrowFirstAction)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
    )
}

@Composable
private fun ReflectionDetailBlock(title: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.ifBlank { "（未入力）" },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatDateLabel(date: String): String {
    return runCatching {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
        val formatter = SimpleDateFormat("yyyy年M月d日(E)", Locale.JAPAN)
        formatter.format(parser.parse(date) ?: return date)
    }.getOrDefault(date)
}

private fun formatMonthTitle(date: String): String {
    return runCatching {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
        val formatter = SimpleDateFormat("yyyy年M月", Locale.JAPAN)
        formatter.format(parser.parse(date) ?: return date)
    }.getOrDefault(date)
}

private fun dateKey(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date(timestamp))
}
