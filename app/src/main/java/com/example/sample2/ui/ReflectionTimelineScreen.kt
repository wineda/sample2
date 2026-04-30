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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sample2.data.DailyReflection
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.Locale

private val ScreenBg = Color(0xFFFAF9F6)
private val BodyText = Color(0xFF1F1D1A)
private val MutedText = Color(0xFF4A4640)
private val LightText = Color(0xFF8A8278)
private val DividerColor = Color(0xFFE6E0D4)

private enum class ReflectionFieldFilter(
    val label: String,
    val timelineLabel: String,
    val color: Color
) {
    SUMMARY("Note", "NOTE", Color(0xFF8A8278)),
    WINS("Good", "GOOD", Color(0xFF4A8A5C)),
    DIFFICULTIES("気づき", "気づき", Color(0xFFC89232)),
    INSIGHTS("気づき", "気づき", Color(0xFFC89232)),
    TOMORROW_FIRST_ACTION("Next", "NEXT", Color(0xFF4A6FA5))
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
        buildTimelineItems(reflections = reflections, uiState = uiState)
    }

    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBg)
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Reflections", fontSize = 32.sp, fontWeight = FontWeight.Medium, color = BodyText)
                    Text(text = "振 り 返 り", fontSize = 11.sp, letterSpacing = 2.sp, color = LightText)
                }
                Surface(shape = RoundedCornerShape(999.dp), color = Color(0xFF1F1D1A), modifier = Modifier.clickable(onClick = onCreateToday)) {
                    Text("＋ 今日を入力", color = Color.White, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), fontSize = 12.sp)
                }
            }

            if (isSearchExpanded) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { value -> uiState = uiState.copy(query = value) },
                        modifier = Modifier.weight(1f).focusRequester(searchFocusRequester),
                        singleLine = true,
                        label = { Text("キーワードで探す") }
                    )
                    TextButton(onClick = { isSearchExpanded = false }) { Text("キャンセル") }
                }
                LaunchedEffect(Unit) { searchFocusRequester.requestFocus() }
            } else {
                ReflectionCompactBar(uiState.query, uiState.fieldFilters, { isSearchExpanded = true }, { isFilterSheetOpen = true })
            }

            if (timelineItems.isEmpty()) {
                Surface(modifier = Modifier.fillMaxWidth().border(1.dp, DividerColor, RoundedCornerShape(16.dp)), color = ScreenBg, shape = RoundedCornerShape(16.dp)) {
                    Text("まだ振り返りがありません", modifier = Modifier.padding(16.dp), color = LightText)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 12.dp)) {
                    items(timelineItems) { item ->
                        when (item) {
                            is ReflectionTimelineItem.MonthHeader -> MonthHeader(item.label)
                            is ReflectionTimelineItem.ReflectionCard -> TimelineRow(item.reflection, uiState.fieldFilters) { onOpenReflection(item.reflection.date) }
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
            onToggleFilter = { filter -> uiState = uiState.copy(fieldFilters = if (filter in uiState.fieldFilters) uiState.fieldFilters - filter else uiState.fieldFilters + filter) }
        )
    }
}

@Composable
private fun MonthHeader(label: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label.uppercase(Locale.US), fontSize = 13.sp, color = LightText, letterSpacing = 2.sp)
        Box(modifier = Modifier.padding(start = 10.dp).weight(1f).height(1.dp).background(DividerColor))
    }
}

@Composable
private fun TimelineRow(reflection: DailyReflection, fieldFilters: Set<ReflectionFieldFilter>, onClick: () -> Unit) {
    val date = reflection.date.toLocalDateOrNull()
    val isToday = date == LocalDate.now()
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.width(56.dp), horizontalAlignment = Alignment.End) {
            Text(text = date?.dayOfMonth?.toString() ?: "-", fontSize = 36.sp, color = if (isToday) Color(0xFF4A6FA5) else BodyText)
            Text(text = date?.dayOfWeekLabel() ?: "", fontSize = 11.sp, color = LightText)
            if (isToday) {
                Text(text = "TODAY", fontSize = 9.sp, color = Color(0xFF4A6FA5), letterSpacing = 1.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
        Box(modifier = Modifier.width(16.dp))
        ReflectionTimelineEntries(reflection, fieldFilters, onClick)
    }
}

@Composable
private fun ReflectionTimelineEntries(reflection: DailyReflection, fieldFilters: Set<ReflectionFieldFilter>, onClick: () -> Unit) {
    val contentItems = listOf(
        ReflectionFieldFilter.SUMMARY to reflection.summary,
        ReflectionFieldFilter.WINS to reflection.wins,
        ReflectionFieldFilter.DIFFICULTIES to reflection.difficulties,
        ReflectionFieldFilter.INSIGHTS to reflection.insights,
        ReflectionFieldFilter.TOMORROW_FIRST_ACTION to reflection.tomorrowFirstAction
    ).filter { (filter, text) -> text.isNotBlank() && (fieldFilters.isEmpty() || filter in fieldFilters) }

    Column(modifier = Modifier.weight(1f).clickable(onClick = onClick)) {
        contentItems.forEachIndexed { index, (filter, text) ->
            TimelineEntry(filter, text, showDivider = index < contentItems.lastIndex)
        }
    }
}

@Composable
private fun TimelineEntry(filter: ReflectionFieldFilter, text: String, showDivider: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp).width(3.dp).height(88.dp).clip(RoundedCornerShape(3.dp)).background(filter.color))
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp, top = 18.dp, bottom = 18.dp)) {
            Text(text = filter.timelineLabel, color = filter.color, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.8.sp)
            Text(text = text, color = if (filter == ReflectionFieldFilter.TOMORROW_FIRST_ACTION) Color(0xFF4A6FA5) else if (filter == ReflectionFieldFilter.SUMMARY) LightText else BodyText, fontSize = 14.sp, lineHeight = 25.sp, fontStyle = if (filter == ReflectionFieldFilter.SUMMARY) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal, modifier = Modifier.padding(top = 8.dp))
        }
    }
    if (showDivider) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
}

@Composable
private fun ReflectionCompactBar(query: String, selectedFilters: Set<ReflectionFieldFilter>, onTapSearch: () -> Unit, onTapFilter: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF4A8A5C)))
            Text(text = compactLabel(query, selectedFilters), fontSize = 11.sp, color = MutedText, modifier = Modifier.padding(start = 6.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onTapSearch, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Search, contentDescription = "検索") }
            Box {
                IconButton(onClick = onTapFilter, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.FilterList, contentDescription = "フィルター") }
                if (selectedFilters.isNotEmpty()) {
                    Badge(modifier = Modifier.align(Alignment.TopEnd).size(7.dp), containerColor = Color(0xFFD97757)) {}
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReflectionFilterSheet(selectedFilters: Set<ReflectionFieldFilter>, onDismiss: () -> Unit, onClear: () -> Unit, onSelectAll: () -> Unit, onToggleFilter: (ReflectionFieldFilter) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onClear) { Text("クリア") }
            TextButton(onClick = onDismiss) { Text("完了") }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterOptionChip("すべて", selectedFilters.isEmpty(), onSelectAll)
            ReflectionFieldFilter.entries.forEach { filter ->
                FilterOptionChip(filter.label, filter in selectedFilters) { onToggleFilter(filter) }
            }
        }
    }
}

@Composable
private fun FilterOptionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick), color = if (selected) Color(0xFF1A1A1A) else Color.White) {
        Text(text = label, color = if (selected) Color.White else Color(0xFF1A1A1A), modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), fontSize = 12.sp)
    }
}

private fun buildTimelineItems(reflections: List<DailyReflection>, uiState: ReflectionListUiState): List<ReflectionTimelineItem> {
    val filtered = reflections.asSequence().filter { it.hasAnyContent() }.sortedByDescending { it.date }.filter { it.date.toLocalDateOrNull() != null }
        .filter { reflection -> if (uiState.query.isBlank()) true else reflection.searchableText().contains(uiState.query.trim().lowercase(Locale.JAPAN)) }
        .filter { reflection ->
            val selected = uiState.fieldFilters
            if (selected.isEmpty()) true else selected.any { filter ->
                when (filter) {
                    ReflectionFieldFilter.SUMMARY -> reflection.summary.isNotBlank()
                    ReflectionFieldFilter.WINS -> reflection.wins.isNotBlank()
                    ReflectionFieldFilter.DIFFICULTIES -> reflection.difficulties.isNotBlank() || reflection.insights.isNotBlank()
                    ReflectionFieldFilter.INSIGHTS -> reflection.insights.isNotBlank() || reflection.difficulties.isNotBlank()
                    ReflectionFieldFilter.TOMORROW_FIRST_ACTION -> reflection.tomorrowFirstAction.isNotBlank()
                }
            }
        }.toList()
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

private fun DailyReflection.hasAnyContent(): Boolean = wins.isNotBlank() || difficulties.isNotBlank() || insights.isNotBlank() || tomorrowFirstAction.isNotBlank() || summary.isNotBlank()
private fun DailyReflection.searchableText(): String = listOf(date, wins, difficulties, insights, tomorrowFirstAction, summary).joinToString("\n").lowercase(Locale.JAPAN)
private fun monthHeaderLabel(dateText: String): String {
    val date = dateText.toLocalDateOrNull() ?: return dateText
    return "${date.year} · ${date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}"
}
private fun LocalDate.dayOfWeekLabel(): String = when (dayOfWeek.value) { 1 -> "月"; 2 -> "火"; 3 -> "水"; 4 -> "木"; 5 -> "金"; 6 -> "土"; else -> "日" }
private fun String.toLocalDateOrNull(): LocalDate? = try { LocalDate.parse(this) } catch (_: DateTimeParseException) { null }
private fun compactLabel(query: String, selectedFilters: Set<ReflectionFieldFilter>): String {
    if (query.isNotBlank()) return "「${query.trim()}」で絞り込み中"
    if (selectedFilters.isEmpty()) return "すべて表示中"
    return selectedFilters.joinToString("・", postfix = " のみ表示中") { it.label }
}
