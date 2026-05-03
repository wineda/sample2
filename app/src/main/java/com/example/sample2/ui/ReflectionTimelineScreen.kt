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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import java.time.YearMonth
import java.time.format.DateTimeParseException
import java.util.Locale

private enum class ReflectionFieldFilter(val label: String, val timelineLabel: String) {
    SUMMARY("ひとことまとめ", "ひとことまとめ"),
    WINS("うまくいったこと", "うまくいったこと"),
    DIFFICULTIES("しんどかったこと", "しんどかったこと"),
    INSIGHTS("気づき", "気づき"),
    TOMORROW_FIRST_ACTION("明日まずやること", "明日まずやること")
}

private data class ReflectionListUiState(
    val query: String = "",
    val fieldFilters: Set<ReflectionFieldFilter> = emptySet()
)

private data class ReflectionDayGroup(
    val date: LocalDate,
    val entries: List<Pair<ReflectionFieldFilter, String>>,
    val reflectionDateKey: String
)

private data class ReflectionMonthGroup(
    val yearMonth: YearMonth,
    val days: List<ReflectionDayGroup>
)

@Composable
fun ReflectionTimelineScreen(
    reflections: List<DailyReflection>,
    onOpenReflection: (String) -> Unit,
    onCreateToday: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var uiState by remember { mutableStateOf(ReflectionListUiState()) }
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    var isFilterSheetOpen by rememberSaveable { mutableStateOf(false) }
    val searchFocusRequester = remember { FocusRequester() }

    val monthGroups = remember(reflections, uiState) {
        buildMonthGroups(reflections = reflections, uiState = uiState)
    }

    Scaffold(modifier = modifier, containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            JournalTopHeader(
                title = "振り返り",
                showLiveDot = reflections.any { it.date == LocalDate.now().toString() && it.hasAnyContent() },
                navigationIcon = Icons.Outlined.Menu,
                navigationContentDescription = "メニュー",
                onNavigationClick = onMenuClick,
                actions = {
                    CompactHeaderIconButton(selected = false, onClick = { isSearchExpanded = true }, icon = Icons.Outlined.Search, contentDescription = "検索")
                    CompactHeaderIconButton(selected = uiState.fieldFilters.isNotEmpty(), onClick = { isFilterSheetOpen = true }, icon = Icons.Outlined.FilterList, contentDescription = "フィルター", showNotificationDot = uiState.fieldFilters.isNotEmpty())
                    CompactHeaderIconButton(selected = false, onClick = onCreateToday, icon = Icons.Outlined.Add, contentDescription = "追加")
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

            if (monthGroups.isEmpty()) {
                Surface(modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.appColors.dividerSubtle, MaterialTheme.shapes.large), color = MaterialTheme.appColors.surfaceElevated, shape = MaterialTheme.shapes.large) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("まだ振り返りがありません", color = MaterialTheme.appColors.inkTertiary)
                        Text("今日の気づきや良かったことを追加してみましょう", color = MaterialTheme.appColors.inkTertiary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    monthGroups.forEach { group ->
                        item { ReflectionMonthHeader(group.yearMonth) }
                        items(group.days) { day ->
                            ReflectionDayCard(day = day, onClick = { onOpenReflection(day.reflectionDateKey) })
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
private fun ReflectionMonthHeader(yearMonth: YearMonth, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "%04d ・ %s".format(yearMonth.year, yearMonth.month.name),
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 4.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(modifier = Modifier.padding(start = 12.dp).weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun ReflectionDayCard(day: ReflectionDayGroup, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp)) {
            ReflectionDateColumn(date = day.date)
            Column(modifier = Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                day.entries.forEachIndexed { index, entry ->
                    ReflectionEntryItem(filter = entry.first, text = entry.second)
                    if (index < day.entries.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReflectionDateColumn(date: LocalDate) {
    Row(modifier = Modifier.width(88.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(
            modifier = Modifier.weight(1f).padding(end = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = date.dayOfMonth.toString(), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.appColors.inkStrongAlt)
            Text(text = date.dayOfWeekLabel(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.appColors.inkTertiary)
        }
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

@Composable
private fun ReflectionEntryItem(filter: ReflectionFieldFilter, text: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.width(4.dp).height(58.dp).clip(AppShapeTokens.Tech).background(filterColor(filter)))
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text = filter.timelineLabel, color = filterColor(filter), style = MonoTypography.Micro.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp))
            Text(text = text, color = MaterialTheme.appColors.inkStrongAlt, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun filterColor(filter: ReflectionFieldFilter) = when (filter) {
    ReflectionFieldFilter.SUMMARY -> MaterialTheme.appColors.inkTertiary
    ReflectionFieldFilter.WINS -> SemanticColors.PositiveMain
    ReflectionFieldFilter.DIFFICULTIES -> SemanticColors.WarningMain
    ReflectionFieldFilter.INSIGHTS -> SemanticColors.InfoMain
    ReflectionFieldFilter.TOMORROW_FIRST_ACTION -> SemanticColors.NegativeMain
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

private fun buildMonthGroups(reflections: List<DailyReflection>, uiState: ReflectionListUiState): List<ReflectionMonthGroup> {
    val filteredByReflection = reflections.asSequence()
        .filter { it.hasAnyContent() }
        .filter { reflection -> if (uiState.query.isBlank()) true else reflection.searchableText().contains(uiState.query.trim().lowercase(Locale.JAPAN)) }
        .mapNotNull { reflection ->
            val date = reflection.date.toLocalDateOrNull() ?: return@mapNotNull null
            val entries = reflection.entries(uiState.fieldFilters)
            if (entries.isEmpty()) null else date to ReflectionDayGroup(date = date, entries = entries, reflectionDateKey = reflection.date)
        }
        .toList()

    return filteredByReflection
        .groupBy { YearMonth.from(it.first) }
        .toSortedMap(compareByDescending { it })
        .map { (yearMonth, dayPairs) ->
            ReflectionMonthGroup(
                yearMonth = yearMonth,
                days = dayPairs.map { it.second }.sortedByDescending { it.date }
            )
        }
}

private fun DailyReflection.entries(selectedFilters: Set<ReflectionFieldFilter>): List<Pair<ReflectionFieldFilter, String>> {
    val all = listOf(
        ReflectionFieldFilter.WINS to wins,
        ReflectionFieldFilter.DIFFICULTIES to difficulties,
        ReflectionFieldFilter.INSIGHTS to insights,
        ReflectionFieldFilter.TOMORROW_FIRST_ACTION to tomorrowFirstAction,
        ReflectionFieldFilter.SUMMARY to summary
    )
    return all.filter { (filter, text) -> text.isNotBlank() && (selectedFilters.isEmpty() || filter in selectedFilters) }
}

private fun DailyReflection.hasAnyContent(): Boolean = wins.isNotBlank() || difficulties.isNotBlank() || insights.isNotBlank() || tomorrowFirstAction.isNotBlank() || summary.isNotBlank()
private fun DailyReflection.searchableText(): String = listOf(date, wins, difficulties, insights, tomorrowFirstAction, summary).joinToString("\n").lowercase(Locale.JAPAN)
private fun LocalDate.dayOfWeekLabel(): String = when (dayOfWeek.value) { 1 -> "月"; 2 -> "火"; 3 -> "水"; 4 -> "木"; 5 -> "金"; 6 -> "土"; else -> "日" }
private fun String.toLocalDateOrNull(): LocalDate? = try { LocalDate.parse(this) } catch (_: DateTimeParseException) { null }
