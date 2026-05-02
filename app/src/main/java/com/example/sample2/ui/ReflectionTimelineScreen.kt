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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sample2.data.DailyReflection
import com.example.sample2.ui.theme.AppShapeTokens
import com.example.sample2.ui.theme.MonoTypography
import com.example.sample2.ui.theme.SemanticColors
import com.example.sample2.ui.theme.appColors
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.Locale


private enum class ReflectionFieldFilter(
    val label: String,
    val timelineLabel: String
) {
    SUMMARY("Note", "NOTE"),
    WINS("Good", "GOOD"),
    DIFFICULTIES("気づき", "気づき"),
    INSIGHTS("気づき", "気づき"),
    TOMORROW_FIRST_ACTION("Next", "NEXT")
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
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val totalCount = reflections.count { it.hasAnyContent() }
            val today = LocalDate.now()
            val contentDates = reflections.filter { it.hasAnyContent() }.mapNotNull { it.date.toLocalDateOrNull() }.toSet()
            var streakDays = 0
            var cursor = today
            while (contentDates.contains(cursor)) {
                streakDays += 1
                cursor = cursor.minusDays(1)
            }
            val thisMonthCount = reflections.count {
                it.hasAnyContent() && it.date.toLocalDateOrNull()?.let { d -> d.year == today.year && d.month == today.month } == true
            }
            JournalTopHeader(
                title = "振り返り",
                showLiveDot = reflections.any { it.date == LocalDate.now().toString() && it.hasAnyContent() },
                navigationIcon = Icons.Outlined.Menu,
                navigationContentDescription = "メニュー",
                onNavigationClick = {},
                actions = {
                    CompactHeaderIconButton(selected = false, onClick = { isSearchExpanded = true }, icon = Icons.Default.Search, contentDescription = "検索")
                    CompactHeaderIconButton(selected = uiState.fieldFilters.isNotEmpty(), onClick = { isFilterSheetOpen = true }, icon = Icons.Default.FilterList, contentDescription = "フィルター", showNotificationDot = uiState.fieldFilters.isNotEmpty())
                    CompactHeaderIconButton(selected = false, onClick = onCreateToday, icon = Icons.Default.Add, contentDescription = "今日を入力")
                },
                bottomSlot = {
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        HeaderStatCell(value = "$totalCount", label = "ENTRIES")
                        HeaderStatCell(value = "$streakDays", label = "STREAK · DAYS", delta = if (streakDays > 0) "▲" else null)
                        HeaderStatCell(value = "$thisMonthCount", label = "THIS MONTH")
                    }
                }
            )

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
            }

            if (timelineItems.isEmpty()) {
                Surface(modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.appColors.dividerSubtle, MaterialTheme.shapes.large), color = MaterialTheme.appColors.surfaceElevated, shape = MaterialTheme.shapes.large) {
                    Text("まだ振り返りがありません", modifier = Modifier.padding(16.dp), color = MaterialTheme.appColors.inkTertiary)
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
        Text(text = label.uppercase(Locale.US), style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 2.sp), color = MaterialTheme.appColors.inkTertiary)
        Box(modifier = Modifier.padding(start = 10.dp).weight(1f).height(1.dp).background(MaterialTheme.appColors.dividerSubtle))
    }
}

@Composable
private fun TimelineRow(reflection: DailyReflection, fieldFilters: Set<ReflectionFieldFilter>, onClick: () -> Unit) {
    val date = reflection.date.toLocalDateOrNull()
    val isToday = date == LocalDate.now()
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.width(56.dp), horizontalAlignment = Alignment.End) {
            Text(text = date?.dayOfMonth?.toString() ?: "-", style = MaterialTheme.typography.displayMedium, color = if (isToday) SemanticColors.InfoMain else MaterialTheme.appColors.inkStrongAlt)
            Text(text = date?.dayOfWeekLabel() ?: "", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.appColors.inkTertiary)
            if (isToday) {
                Text(text = "TODAY", style = MonoTypography.Micro.copy(color = SemanticColors.InfoMain, letterSpacing = 1.sp), modifier = Modifier.padding(top = 2.dp))
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

    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        contentItems.forEachIndexed { index, (filter, text) ->
            TimelineEntry(filter, text, showDivider = index < contentItems.lastIndex)
        }
    }
}

@Composable
private fun filterColor(filter: ReflectionFieldFilter) = when (filter) {
    ReflectionFieldFilter.SUMMARY -> MaterialTheme.appColors.inkTertiary
    ReflectionFieldFilter.WINS -> SemanticColors.PositiveMain
    ReflectionFieldFilter.DIFFICULTIES, ReflectionFieldFilter.INSIGHTS -> SemanticColors.WarningMain
    ReflectionFieldFilter.TOMORROW_FIRST_ACTION -> SemanticColors.InfoMain
}

@Composable
private fun TimelineEntry(filter: ReflectionFieldFilter, text: String, showDivider: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp).width(3.dp).height(88.dp).clip(AppShapeTokens.Tech).background(filterColor(filter)))
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp, top = 18.dp, bottom = 18.dp)) {
            Text(text = filter.timelineLabel, color = filterColor(filter), style = MonoTypography.Micro.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.8.sp))
            Text(text = text, color = if (filter == ReflectionFieldFilter.TOMORROW_FIRST_ACTION) SemanticColors.InfoMain else if (filter == ReflectionFieldFilter.SUMMARY) MaterialTheme.appColors.inkTertiary else MaterialTheme.appColors.inkStrongAlt, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 25.sp), fontStyle = if (filter == ReflectionFieldFilter.SUMMARY) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal, modifier = Modifier.padding(top = 8.dp))
        }
    }
    if (showDivider) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.appColors.dividerSubtle))
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
    Surface(modifier = Modifier.clip(MaterialTheme.shapes.medium).clickable(onClick = onClick), color = if (selected) MaterialTheme.appColors.inkStrongAlt else MaterialTheme.colorScheme.surface) {
        Text(text = label, color = if (selected) MaterialTheme.appColors.inkOnInk else MaterialTheme.appColors.inkPrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp), style = MaterialTheme.typography.bodySmall)
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
