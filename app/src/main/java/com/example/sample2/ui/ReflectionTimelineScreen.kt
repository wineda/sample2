package com.example.sample2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.WbIncandescent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.sample2.data.DailyReflection
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.Locale

private enum class ReflectionFieldFilter(
    val label: String,
    val emoji: String,
    val icon: ImageVector,
    val textColor: Color,
    val bgColor: Color
) {
    SUMMARY("コメント", "💬", Icons.Default.Chat, Color(0xFF6B6B6B), Color(0xFFEFEDEA)),
    WINS("いいね", "👍", Icons.Default.ThumbUp, Color(0xFF4A7C4A), Color(0xFFEAF3E8)),
    DIFFICULTIES("顔", "😐", Icons.Default.SentimentDissatisfied, Color(0xFFB85C4A), Color(0xFFF7E9E4)),
    INSIGHTS("電球", "💡", Icons.Default.WbIncandescent, Color(0xFFC79410), Color(0xFFFAF0D8)),
    TOMORROW_FIRST_ACTION("チェック", "✓", Icons.Default.TaskAlt, Color(0xFF3A6EA5), Color(0xFFE6EEF7))
}

private data class ReflectionListUiState(
    val query: String = "",
    val fieldFilters: Set<ReflectionFieldFilter> = emptySet()
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
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    var isFilterSheetOpen by rememberSaveable { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

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
                .background(Color(0xFFFAF8F4))
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "振り返り",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onCreateToday) {
                    Text("＋ 今日を入力")
                }
            }
            if (isSearchExpanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { value -> uiState = uiState.copy(query = value) },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(searchFocusRequester),
                        singleLine = true,
                        label = { Text("キーワードで探す") }
                    )
                    TextButton(onClick = { isSearchExpanded = false }) {
                        Text("キャンセル")
                    }
                }
                LaunchedEffect(Unit) { searchFocusRequester.requestFocus() }
            } else {
                ReflectionCompactBar(
                    query = uiState.query,
                    selectedFilters = uiState.fieldFilters,
                    onTapSearch = { isSearchExpanded = true },
                    onTapFilter = { isFilterSheetOpen = true }
                )
            }

            if (timelineItems.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE8E4DC), RoundedCornerShape(16.dp)),
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
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
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(timelineItems) { item ->
                        when (item) {
                            is ReflectionTimelineItem.MonthHeader -> {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }

                            is ReflectionTimelineItem.ReflectionCard -> {
                                TimelineRow(
                                    reflection = item.reflection,
                                    fieldFilters = uiState.fieldFilters,
                                    onClick = { onOpenReflection(item.reflection.date) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (isFilterSheetOpen) {
        ReflectionFilterSheet(
            selectedFilters = uiState.fieldFilters,
            onDismiss = { isFilterSheetOpen = false },
            onClear = { uiState = uiState.copy(fieldFilters = emptySet()) },
            onSelectAll = { uiState = uiState.copy(fieldFilters = emptySet()) },
            onToggleFilter = { filter ->
                uiState = uiState.copy(
                    fieldFilters = if (filter in uiState.fieldFilters) {
                        uiState.fieldFilters - filter
                    } else {
                        uiState.fieldFilters + filter
                    }
                )
            }
        )
    }
}

@Composable
private fun ReflectionCompactBar(
    query: String,
    selectedFilters: Set<ReflectionFieldFilter>,
    onTapSearch: () -> Unit,
    onTapFilter: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = Color(0xFFF2EEE7),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = compactLabel(query, selectedFilters),
                fontSize = 11.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = onTapSearch,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = "検索")
            }
            Box {
                IconButton(
                    onClick = onTapFilter,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "フィルター")
                }
                if (selectedFilters.isNotEmpty()) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(7.dp),
                        containerColor = Color(0xFFD97757)
                    ) {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ReflectionFilterSheet(
    selectedFilters: Set<ReflectionFieldFilter>,
    onDismiss: () -> Unit,
    onClear: () -> Unit,
    onSelectAll: () -> Unit,
    onToggleFilter: (ReflectionFieldFilter) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onClear) { Text("クリア") }
            TextButton(onClick = onDismiss) { Text("完了") }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterOptionChip(
                label = "すべて",
                selected = selectedFilters.isEmpty(),
                onClick = onSelectAll
            )
            ReflectionFieldFilter.entries.forEach { filter ->
                FilterOptionChip(
                    label = "${filter.emoji} ${filter.label}",
                    selected = filter in selectedFilters,
                    onClick = { onToggleFilter(filter) }
                )
            }
        }
    }
}

@Composable
private fun FilterOptionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick),
        color = if (selected) Color(0xFF1A1A1A) else Color.White
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else Color(0xFF1A1A1A),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun TimelineRow(
    reflection: DailyReflection,
    fieldFilters: Set<ReflectionFieldFilter>,
    onClick: () -> Unit
) {
    val date = reflection.date.toLocalDateOrNull()
    val isToday = date == LocalDate.now()

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = date?.dayOfMonth?.toString() ?: "-", fontWeight = FontWeight.Bold, fontSize = 30.sp)
            Text(text = date?.dayOfWeekLabel() ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(if (isToday) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (isToday) Color(0xFFD05A46) else Color(0xFFD0CBC2))
            )
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .width(1.dp)
                    .height(56.dp)
                    .background(Color(0xFFE2DDD4))
            )
        }
        ReflectionTimelineCard(reflection = reflection, fieldFilters = fieldFilters, onClick = onClick)
    }
}

@Composable
private fun ReflectionTimelineCard(
    reflection: DailyReflection,
    fieldFilters: Set<ReflectionFieldFilter>,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, Color(0xFFE8E4DC), RoundedCornerShape(16.dp)),
        color = Color.White,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            val contentItems = listOf(
                ReflectionFieldFilter.SUMMARY to reflection.summary,
                ReflectionFieldFilter.WINS to reflection.wins,
                ReflectionFieldFilter.DIFFICULTIES to reflection.difficulties,
                ReflectionFieldFilter.INSIGHTS to reflection.insights
            ).filter { (filter, text) ->
                text.isNotBlank() && (fieldFilters.isEmpty() || filter in fieldFilters)
            }

            contentItems.forEachIndexed { index, (filter, text) ->
                TimelineSnippetLine(
                    icon = filter.icon,
                    text = text,
                    textColor = filter.textColor,
                    bgColor = filter.bgColor,
                    drawDivider = index < contentItems.lastIndex
                )
            }

            if (reflection.tomorrowFirstAction.isNotBlank() &&
                (fieldFilters.isEmpty() || ReflectionFieldFilter.TOMORROW_FIRST_ACTION in fieldFilters)
            ) {
                TomorrowActionBlock(text = reflection.tomorrowFirstAction)
            }
        }
    }
}

@Composable
private fun TimelineSnippetLine(
    icon: ImageVector,
    text: String,
    textColor: Color,
    bgColor: Color,
    drawDivider: Boolean
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = textColor, modifier = Modifier.size(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF1F1F1F),
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Clip
            )
        }
        if (drawDivider) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1EEE7)))
        }
    }
}

@Composable
private fun TomorrowActionBlock(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE6EEF7))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.TaskAlt,
            contentDescription = null,
            tint = Color(0xFF3A6EA5),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            color = Color(0xFF3A6EA5),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = Int.MAX_VALUE,
            overflow = TextOverflow.Clip
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
            val selected = uiState.fieldFilters
            if (selected.isEmpty()) true else selected.any { filter ->
                when (filter) {
                    ReflectionFieldFilter.SUMMARY -> reflection.summary.isNotBlank()
                    ReflectionFieldFilter.WINS -> reflection.wins.isNotBlank()
                    ReflectionFieldFilter.DIFFICULTIES -> reflection.difficulties.isNotBlank()
                    ReflectionFieldFilter.INSIGHTS -> reflection.insights.isNotBlank()
                    ReflectionFieldFilter.TOMORROW_FIRST_ACTION -> reflection.tomorrowFirstAction.isNotBlank()
                }
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

private fun LocalDate.dayOfWeekLabel(): String {
    return when (dayOfWeek.value) {
        1 -> "月"
        2 -> "火"
        3 -> "水"
        4 -> "木"
        5 -> "金"
        6 -> "土"
        else -> "日"
    }
}

private fun String.toLocalDateOrNull(): LocalDate? {
    return try {
        LocalDate.parse(this)
    } catch (_: DateTimeParseException) {
        null
    }
}

private fun compactLabel(query: String, selectedFilters: Set<ReflectionFieldFilter>): String {
    if (query.isNotBlank()) return "「${query.trim()}」で絞り込み中"
    if (selectedFilters.isEmpty()) return "すべて表示中"
    return selectedFilters.joinToString("・", postfix = " のみ表示中") { it.label }
}
