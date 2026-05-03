package com.example.sample2.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import com.example.sample2.analytics.DailyPersonalityScore
import com.example.sample2.analytics.PersonalityScoreModel
import com.example.sample2.analytics.PersonalityState
import com.example.sample2.data.ActionType
import com.example.sample2.data.DailyRecord
import com.example.sample2.data.MessageV2
import com.example.sample2.data.SleepData
import com.example.sample2.data.EmotionType
import com.example.sample2.ui.ActionHeatmapBlock
import com.example.sample2.ui.EmotionHeatmapBlock
import com.example.sample2.ui.JournalTopHeader
import com.example.sample2.ui.filter.PeriodPreset
import com.example.sample2.ui.formatDate
import androidx.compose.material.icons.outlined.Menu
import com.example.sample2.util.formatTime
import com.example.sample2.ui.theme.ActionPalette
import com.example.sample2.ui.theme.ScorePalette
import java.time.Instant
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private const val AnalyticsLogTag = "PersonalityAnalytics"

enum class AnalyticsDisplayMode {
    DETAIL,
    CHARTS,
    MAP
}

private enum class AnalyticsPeriod(
    val label: String
) {
    DAYS_7("7日"),
    DAYS_14("14日"),
    DAYS_30("30日"),
    ALL("全期間")
}

private enum class DetailCompareMode(
    val label: String
) {
    NONE("比較なし"),
    PREVIOUS_DAY("前日と比較"),
    PREVIOUS_WEEK("先週と比較")
}

private data class LineSeries(
    val label: String,
    val color: Color,
    val values: List<Float>
)

private data class TimedLineSeries(
    val label: String,
    val color: Color,
    val values: List<Float>,
    val xValues: List<Float>
)

private data class GraphEntry(
    val date: LocalDate,
    val label: String,
    val value: Float
)

private fun nextAnalyticsPeriod(period: AnalyticsPeriod): AnalyticsPeriod {
    val periods = AnalyticsPeriod.entries
    val index = periods.indexOf(period)
    return periods[(index + 1).coerceAtMost(periods.lastIndex)]
}

private fun previousAnalyticsPeriod(period: AnalyticsPeriod): AnalyticsPeriod {
    val periods = AnalyticsPeriod.entries
    val index = periods.indexOf(period)
    return periods[(index - 1).coerceAtLeast(0)]
}


private val TokyoZoneId: ZoneId = ZoneId.of("Asia/Tokyo")
private const val DetailChartStartMinutes = 7 * 60f
private const val DetailChartEndMinutes = 22 * 60f

private fun minutesOfDay(timestamp: Long): Float {
    val time = Instant.ofEpochMilli(timestamp)
        .atZone(TokyoZoneId)
        .toLocalTime()
    return time.hour * 60f + time.minute + (time.second / 60f)
}

private fun isDetailChartVisibleTime(timestamp: Long): Boolean {
    val minutes = minutesOfDay(timestamp)
    return minutes in DetailChartStartMinutes..DetailChartEndMinutes
}

private fun formatMinuteLabel(minutes: Float): String {
    val totalMinutes = minutes.roundToInt().coerceIn(0, 24 * 60)
    val hour = totalMinutes / 60
    val minute = totalMinutes % 60
    return String.format(Locale.JAPAN, "%02d:%02d", hour, minute)
}

@Composable
fun PersonalityAnalyticsScreen(
    messages: List<MessageV2>,
    dailyRecords: List<DailyRecord>,
    onUpdateDailyRecord: (DailyRecord) -> Unit,
    initialDisplayMode: AnalyticsDisplayMode = AnalyticsDisplayMode.DETAIL,
    displayModes: List<AnalyticsDisplayMode> = AnalyticsDisplayMode.entries,
    modifier: Modifier = Modifier
) {
    val allRawScores = remember(messages, dailyRecords) {
        PersonalityScoreModel.analyzeAllDaysRaw(
            messages = messages,
            dailyRecords = dailyRecords
        )
    }

    val dailyRecordMap = remember(dailyRecords) {
        dailyRecords.associateBy { it.date }
    }

    val messageMapByDate = remember(messages) {
        messages.groupBy {
            Instant.ofEpochMilli(it.timestamp)
                .atZone(ZoneId.of("Asia/Tokyo"))
                .toLocalDate()
        }
    }

    val availableDisplayModes = remember(displayModes) {
        displayModes.ifEmpty { listOf(AnalyticsDisplayMode.DETAIL) }.distinct()
    }
    val normalizedInitialDisplayMode = remember(initialDisplayMode, availableDisplayModes) {
        if (initialDisplayMode in availableDisplayModes) {
            initialDisplayMode
        } else {
            availableDisplayModes.first()
        }
    }

    var displayModeName by rememberSaveable(
        availableDisplayModes.joinToString(separator = ",") { it.name },
        normalizedInitialDisplayMode.name
    ) {
        mutableStateOf(normalizedInitialDisplayMode.name)
    }
    var selectedPeriodName by rememberSaveable {
        mutableStateOf(AnalyticsPeriod.DAYS_7.name)
    }
    var selectedDateText by rememberSaveable {
        mutableStateOf<String?>(null)
    }
    var detailCompareModeName by rememberSaveable {
        mutableStateOf(DetailCompareMode.NONE.name)
    }
    val initialPreset = remember { PeriodPreset.values().first() }
    val initialRange = remember { initialPreset.resolveRange() }
    var heatmapPeriodName by rememberSaveable {
        mutableStateOf(initialPreset.name)
    }
    var heatmapFromDate by rememberSaveable {
        mutableStateOf(initialRange.first)
    }
    var heatmapToDate by rememberSaveable {
        mutableStateOf(initialRange.second)
    }

    val displayMode = remember(displayModeName) {
        AnalyticsDisplayMode.valueOf(displayModeName)
    }
    val selectedPeriod = remember(selectedPeriodName) {
        AnalyticsPeriod.valueOf(selectedPeriodName)
    }
    val detailCompareMode = remember(detailCompareModeName) {
        DetailCompareMode.valueOf(detailCompareModeName)
    }
    val heatmapPeriod = remember(heatmapPeriodName) {
        PeriodPreset.valueOf(heatmapPeriodName)
    }

    val allRawScoresDesc = remember(allRawScores) {
        allRawScores.sortedByDescending { it.date }
    }

    val filteredRawScoresDesc = remember(allRawScoresDesc, selectedPeriod) {
        filterScoresByPeriod(
            scores = allRawScoresDesc,
            period = selectedPeriod
        )
    }

    val filteredRawScoresAsc = remember(filteredRawScoresDesc) {
        filteredRawScoresDesc.sortedBy { it.date }
    }

    val filteredDateSet = remember(filteredRawScoresDesc) {
        filteredRawScoresDesc.mapTo(linkedSetOf()) { it.date }
    }

    val filteredMessages = remember(messages, filteredDateSet) {
        messages.filter { message ->
            val messageDate = Instant.ofEpochMilli(message.timestamp)
                .atZone(ZoneId.of("Asia/Tokyo"))
                .toLocalDate()
            messageDate in filteredDateSet
        }
    }

    val targetActionTypes = remember {
        listOf(
            ActionType.DELEGATE,
            ActionType.CHALLENGE,
            ActionType.BREAKDOWN,
            ActionType.INSTRUCT,
            ActionType.QUICK_ACTION
        )
    }

    val chartDates = remember(allRawScoresDesc, selectedPeriod) {
        buildChartDatesByPeriod(
            allDatesDesc = allRawScoresDesc.map { it.date },
            period = selectedPeriod
        )
    }

    val dailyRecordByLocalDate = remember(dailyRecords) {
        dailyRecords.mapNotNull { record ->
            runCatching { LocalDate.parse(record.date) }.getOrNull()?.let { date ->
                date to record
            }
        }.toMap()
    }

    val actionFlagSeries = remember(filteredMessages, targetActionTypes, chartDates) {
        val messageCountByDate = filteredMessages.groupBy { message ->
            Instant.ofEpochMilli(message.timestamp)
                .atZone(TokyoZoneId)
                .toLocalDate()
        }
        targetActionTypes.mapIndexed { index, type ->
            LineSeries(
                label = type.label,
                color = ActionPalette.ChartCategories[index % ActionPalette.ChartCategories.size],
                values = chartDates.map { date ->
                    messageCountByDate[date].orEmpty().count { message ->
                        type.matches(message.flags)
                    }.toFloat()
                }
            )
        }
    }

    val chartXAxisLabels = remember(chartDates, selectedPeriod) {
        buildChartXAxisLabels(
            dates = chartDates,
            period = selectedPeriod
        )
    }

    val stepsValueByDate = remember(dailyRecordByLocalDate) {
        dailyRecordByLocalDate.mapValues { (_, record) ->
            (record.steps / 1000f).coerceAtLeast(0f)
        }
    }

    val sleepValueByDate = remember(dailyRecordByLocalDate) {
        dailyRecordByLocalDate.mapValues { (_, record) ->
            (record.sleep.durationMinutes / 60f).coerceAtLeast(0f)
        }
    }

    val stepGraphEntries = remember(chartDates, chartXAxisLabels, stepsValueByDate) {
        buildGraphEntries(
            dates = chartDates,
            labels = chartXAxisLabels,
            valueByDate = stepsValueByDate
        )
    }

    val sleepGraphEntries = remember(chartDates, chartXAxisLabels, sleepValueByDate) {
        buildGraphEntries(
            dates = chartDates,
            labels = chartXAxisLabels,
            valueByDate = sleepValueByDate
        )
    }

    LaunchedEffect(chartDates, stepGraphEntries, sleepGraphEntries) {
        Log.d(AnalyticsLogTag, "displayDates=${chartDates.joinToString()}")
        stepGraphEntries.forEach { entry ->
            Log.d(AnalyticsLogTag, "steps entry date=${entry.date} label=${entry.label} value=${entry.value}")
        }
        sleepGraphEntries.forEach { entry ->
            Log.d(AnalyticsLogTag, "sleep entry date=${entry.date} label=${entry.label} value=${entry.value}")
        }
    }

    val moodAvgText = remember(filteredRawScoresDesc) {
        filteredRawScoresDesc.map { it.stability }.average().takeIf { !it.isNaN() }?.let { "%.1f".format(it) } ?: "--"
    }
    val sleepAvgText = remember(filteredDateSet, dailyRecordMap) {
        val hours = filteredDateSet.mapNotNull { d -> dailyRecordMap[d.toString()]?.sleep?.durationMinutes?.takeIf { it > 0 }?.div(60f) }
        if (hours.isEmpty()) "--" else "%.1fh".format(hours.average())
    }
    val entriesCountText = remember(filteredDateSet, messageMapByDate) {
        filteredDateSet.sumOf { d -> messageMapByDate[d].orEmpty().size }.toString()
    }

    val selectedDate = remember(allRawScoresDesc, selectedDateText) {
        val requested = selectedDateText?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        requested ?: allRawScoresDesc.firstOrNull()?.date
    }

    val selectedRawScore = remember(filteredRawScoresDesc, selectedDate) {
        filteredRawScoresDesc.firstOrNull { it.date == selectedDate }
    }

    val selectedDayMessages = remember(selectedDate, messageMapByDate) {
        selectedDate?.let { date ->
            messageMapByDate[date].orEmpty().sortedBy { it.timestamp }
        }.orEmpty()
    }

    val selectedDayRecord = remember(selectedDate, dailyRecordMap) {
        selectedDate?.let { dailyRecordMap[it.toString()] }
    }

    val comparisonDate = remember(selectedDate, detailCompareMode) {
        when (detailCompareMode) {
            DetailCompareMode.NONE -> null
            DetailCompareMode.PREVIOUS_DAY -> selectedDate?.minusDays(1)
            DetailCompareMode.PREVIOUS_WEEK -> selectedDate?.minusWeeks(1)
        }
    }

    val comparisonDayMessages = remember(comparisonDate, messageMapByDate) {
        comparisonDate?.let { date ->
            messageMapByDate[date].orEmpty().sortedBy { it.timestamp }
        }.orEmpty()
    }

    val comparisonDayRecord = remember(comparisonDate, dailyRecordMap) {
        comparisonDate?.let { dailyRecordMap[it.toString()] }
    }

    Column(modifier = modifier.fillMaxSize()) {
        JournalTopHeader(
            title = if (initialDisplayMode == AnalyticsDisplayMode.DETAIL) "詳細分析" else "分析",
            subtitle = if (initialDisplayMode == AnalyticsDisplayMode.DETAIL) "1日単位の内訳" else "感情・行動の推移",
            navigationIcon = Icons.Outlined.Menu,
            navigationContentDescription = "メニュー",
            onNavigationClick = {}
        )

        if (availableDisplayModes.size > 1) {
            AnalyticsDisplayModeToggle(
                current = displayMode,
                modes = availableDisplayModes,
                onChange = { displayModeName = it.name },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        if (displayMode == AnalyticsDisplayMode.CHARTS) {
            AnalyticsPeriodSelector(
                current = selectedPeriod,
                onChange = { selectedPeriodName = it.name },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        } else if (displayMode == AnalyticsDisplayMode.DETAIL) {
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (filteredRawScoresDesc.isEmpty()) {
            EmptyAnalyticsState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            )
            return@Column
        }

        when (displayMode) {
            AnalyticsDisplayMode.DETAIL -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        SelectedDateSelectorRow(
                            dates = allRawScoresDesc.map { it.date },
                            selectedDate = selectedDate,
                            onSelectDate = { selectedDateText = it.toString() }
                        )
                    }

                    item {
                        DailyMessagePseudoTrendCard(
                            date = selectedDate,
                            messages = selectedDayMessages,
                            dailyRecord = selectedDayRecord,
                            compareMode = detailCompareMode,
                            onCompareModeChange = { detailCompareModeName = it.name },
                            comparisonDate = comparisonDate,
                            comparisonMessages = comparisonDayMessages,
                            comparisonDailyRecord = comparisonDayRecord,
                            onSwipeToOlderDate = {
                                val currentIndex = allRawScoresDesc.indexOfFirst { it.date == selectedDate }
                                if (currentIndex in 0 until allRawScoresDesc.lastIndex) {
                                    selectedDateText = allRawScoresDesc[currentIndex + 1].date.toString()
                                }
                            },
                            onSwipeToNewerDate = {
                                val currentIndex = allRawScoresDesc.indexOfFirst { it.date == selectedDate }
                                if (currentIndex > 0) {
                                    selectedDateText = allRawScoresDesc[currentIndex - 1].date.toString()
                                }
                            }
                        )
                    }

                    item {
                        DetailQuickMetaRow(
                            selectedDate = selectedDate
                        )
                    }
                }
            }

            AnalyticsDisplayMode.CHARTS -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OverallTrendChartCard(
                            scores = filteredRawScoresAsc,
                            currentPeriod = selectedPeriod,
                            onSwipeToBroaderPeriod = {
                                selectedPeriodName = nextAnalyticsPeriod(selectedPeriod).name
                            },
                            onSwipeToNarrowerPeriod = {
                                selectedPeriodName = previousAnalyticsPeriod(selectedPeriod).name
                            }
                        )
                    }
                    item {
                        ActionFlagCountChartCard(
                            labels = chartXAxisLabels,
                            series = actionFlagSeries,
                            smoothLine = selectedPeriod == AnalyticsPeriod.ALL
                        )
                    }
                    item {
                        MetricBarChartCard(
                            entries = stepGraphEntries,
                            title = "運動推移",
                            seriesLabel = "歩数(千歩)",
                            color = ScorePalette.Steps
                        )
                    }
                    item {
                        MetricBarChartCard(
                            entries = sleepGraphEntries,
                            title = "睡眠推移",
                            seriesLabel = "睡眠時間(h)",
                            color = ScorePalette.Sleep
                        )
                    }
                }
            }
            AnalyticsDisplayMode.MAP -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PeriodPresetHeatmapSelector(
                        selectedPeriod = heatmapPeriod,
                        onSelectPreset = { preset ->
                            heatmapPeriodName = preset.name
                            val (from, to) = preset.resolveRange()
                            heatmapFromDate = from
                            heatmapToDate = to
                        }
                    )
                    Text(
                        text = "${formatDate(heatmapFromDate)} - ${formatDate(heatmapToDate)}",
                        style = MaterialTheme.typography.labelMedium
                    )
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
                                    fromDate = heatmapFromDate,
                                    toDate = heatmapToDate
                                )
                            }
                        }
                        items(ActionType.values().toList()) { action ->
                            Surface(
                                tonalElevation = 2.dp,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ActionHeatmapBlock(
                                    action = action,
                                    messages = messages,
                                    fromDate = heatmapFromDate,
                                    toDate = heatmapToDate
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
private fun ActionFlagCountChartCard(
    labels: List<String>,
    series: List<LineSeries>,
    smoothLine: Boolean,
    startPadding: Dp = 0.dp,
    endPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = startPadding, end = endPadding),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "仕事推移",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            MultiLineChart(
                labels = labels,
                series = series,
                minValue = 0f,
                maxValue = 10f,
                yAxisTicks = listOf(0f, 2f, 4f, 6f, 8f, 10f),
                toggleableLegend = true,
                smoothLine = smoothLine,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
        }
    }
}

@Composable
private fun MetricBarChartCard(
    entries: List<GraphEntry>,
    title: String,
    seriesLabel: String,
    color: Color,
    startPadding: Dp = 0.dp,
    endPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = startPadding, end = endPadding),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            SimpleBarChart(
                entries = entries,
                seriesLabel = seriesLabel,
                color = color,
                minValue = 0f,
                maxValue = 10f,
                yAxisTicks = listOf(0f, 2f, 4f, 6f, 8f, 10f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
        }
    }
}

@Composable
private fun SimpleBarChart(
    entries: List<GraphEntry>,
    seriesLabel: String,
    color: Color,
    minValue: Float,
    maxValue: Float,
    yAxisTicks: List<Float>,
    modifier: Modifier = Modifier
) {
    MultiLineChart(
        labels = entries.map { it.label },
        series = listOf(LineSeries(seriesLabel, color, entries.map { it.value })),
        minValue = minValue,
        maxValue = maxValue,
        yAxisTicks = yAxisTicks,
        toggleableLegend = false,
        smoothLine = false,
        drawStyle = ChartDrawStyle.BAR,
        modifier = modifier
    )
}

@Composable
private fun DetailQuickMetaRow(
    selectedDate: LocalDate?,
    startPadding: Dp = 0.dp,
    endPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val dateText = selectedDate?.let {
        DateTimeFormatter.ofPattern("M月d日(E)", Locale.JAPAN).format(it)
    } ?: "未選択"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = startPadding, end = endPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateText,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun OverallTrendChartCard(
    scores: List<DailyPersonalityScore>,
    currentPeriod: AnalyticsPeriod,
    onSwipeToBroaderPeriod: () -> Unit,
    onSwipeToNarrowerPeriod: () -> Unit,
    startPadding: Dp = 0.dp,
    endPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val swipeThresholdPx = with(LocalDensity.current) { 36.dp.toPx() }
    var accumulatedDrag by remember(currentPeriod) { mutableStateOf(0f) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = startPadding, end = endPadding),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .pointerInput(currentPeriod, scores.size) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            accumulatedDrag += dragAmount
                        },
                        onDragEnd = {
                            when {
                                accumulatedDrag <= -swipeThresholdPx -> onSwipeToBroaderPeriod()
                                accumulatedDrag >= swipeThresholdPx -> onSwipeToNarrowerPeriod()
                            }
                            accumulatedDrag = 0f
                        },
                        onDragCancel = {
                            accumulatedDrag = 0f
                        }
                    )
                },
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "感情推移",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            CombinedEmotionChart(
                scores = scores,
                period = currentPeriod,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }
    }
}

@Composable
private fun StateBadge(
    label: String,
    state: PersonalityState,
    modifier: Modifier = Modifier
) {
    val containerColor = when (state) {
        PersonalityState.STABLE -> MaterialTheme.colorScheme.primaryContainer
        PersonalityState.RECOVERING -> MaterialTheme.colorScheme.secondaryContainer
        PersonalityState.TENSE -> MaterialTheme.colorScheme.tertiaryContainer
        PersonalityState.EXHAUSTED -> MaterialTheme.colorScheme.errorContainer
    }

    val contentColor = when (state) {
        PersonalityState.STABLE -> MaterialTheme.colorScheme.onPrimaryContainer
        PersonalityState.RECOVERING -> MaterialTheme.colorScheme.onSecondaryContainer
        PersonalityState.TENSE -> MaterialTheme.colorScheme.onTertiaryContainer
        PersonalityState.EXHAUSTED -> MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

private enum class ChartDrawStyle { LINE, BAR }

@Composable
private fun CombinedEmotionChart(
    scores: List<DailyPersonalityScore>,
    period: AnalyticsPeriod,
    modifier: Modifier = Modifier
) {
    val labels = remember(scores, period) {
        buildChartXAxisLabels(
            dates = scores.map { it.date },
            period = period
        )
    }
    val series = listOf(
        LineSeries("安定度", ScorePalette.Stability, scores.map { it.stability.toFloat() }),
        LineSeries("不安", ScorePalette.Anxiety, scores.map { (it.anxiety * 10.0).coerceIn(0.0, 100.0).toFloat() }),
        LineSeries("活力", ScorePalette.Energy, scores.map { it.energy.toFloat() }),
        LineSeries("制御感", ScorePalette.Control, scores.map { it.control.toFloat() })
    )
    MultiLineChart(
        labels = labels,
        series = series,
        minValue = 0f,
        maxValue = 100f,
        yAxisTicks = listOf(0f, 25f, 50f, 75f, 100f),
        toggleableLegend = true,
        smoothLine = period == AnalyticsPeriod.ALL,
        modifier = modifier
    )
}

@Composable
private fun MultiLineChart(
    labels: List<String>,
    series: List<LineSeries>,
    comparisonSeries: List<LineSeries>? = null,
    seriesXValues: Map<String, List<Float>> = emptyMap(),
    comparisonSeriesXValues: Map<String, List<Float>> = emptyMap(),
    selectedXAxisValue: Float? = null,
    onSelectedXAxisValueChange: ((Float) -> Unit)? = null,
    xAxisMin: Float? = null,
    xAxisMax: Float? = null,
    minValue: Float,
    maxValue: Float,
    yAxisTicks: List<Float>? = null,
    toggleableLegend: Boolean = false,
    smoothLine: Boolean = false,
    drawStyle: ChartDrawStyle = ChartDrawStyle.LINE,
    modifier: Modifier = Modifier
) {
    var hiddenSeriesLabels by remember(series) {
        mutableStateOf(setOf<String>())
    }
    val displayedSeries = remember(series, hiddenSeriesLabels) {
        series.filterNot { it.label in hiddenSeriesLabels }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                series.forEach { item ->
                    Row(
                        modifier = if (toggleableLegend) {
                            Modifier.clickable {
                                hiddenSeriesLabels = if (item.label in hiddenSeriesLabels) {
                                    hiddenSeriesLabels - item.label
                                } else {
                                    hiddenSeriesLabels + item.label
                                }
                            }
                        } else {
                            Modifier
                        },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(8.dp)
                                .background(
                                    if (item.label in hiddenSeriesLabels) {
                                        item.color.copy(alpha = 0.3f)
                                    } else {
                                        item.color
                                    },
                                    RoundedCornerShape(999.dp)
                                )
                        )
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (item.label in hiddenSeriesLabels) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
            SimpleMultiLineChart(
                series = displayedSeries,
                comparisonSeries = comparisonSeries.orEmpty().filter { compared ->
                    compared.label !in hiddenSeriesLabels
                },
                seriesXValues = seriesXValues,
                comparisonSeriesXValues = comparisonSeriesXValues,
                selectedXAxisValue = selectedXAxisValue,
                onSelectedXAxisValueChange = onSelectedXAxisValueChange,
                xAxisMin = xAxisMin,
                xAxisMax = xAxisMax,
                minValue = minValue,
                maxValue = maxValue,
                yAxisTicks = yAxisTicks,
                smoothLine = smoothLine,
                drawStyle = drawStyle,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            CompactChartTickLabelRow(
                labels = labels,
                startPadding = 34.dp + 4.dp + 6.dp,
                endPadding = 6.dp,
                centerAllLabels = drawStyle == ChartDrawStyle.BAR
            )
        }
    }
}

@Composable
private fun SimpleMultiLineChart(
    series: List<LineSeries>,
    comparisonSeries: List<LineSeries> = emptyList(),
    seriesXValues: Map<String, List<Float>> = emptyMap(),
    comparisonSeriesXValues: Map<String, List<Float>> = emptyMap(),
    selectedXAxisValue: Float? = null,
    onSelectedXAxisValueChange: ((Float) -> Unit)? = null,
    xAxisMin: Float? = null,
    xAxisMax: Float? = null,
    minValue: Float,
    maxValue: Float,
    yAxisTicks: List<Float>? = null,
    smoothLine: Boolean = false,
    drawStyle: ChartDrawStyle = ChartDrawStyle.LINE,
    modifier: Modifier = Modifier
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val guideValues = remember(minValue, maxValue, yAxisTicks) {
        yAxisTicks
            ?.takeIf { it.isNotEmpty() }
            ?.distinct()
            ?.sortedDescending()
            ?: List(4) { index ->
                maxValue - ((maxValue - minValue) * (index / 3f))
            }
    }
    val selectionLineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)

    Row(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(34.dp)
                .padding(top = 10.dp, bottom = 10.dp, end = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            guideValues.forEach { value ->
                Text(
                    text = if (value % 1f == 0f) {
                        value.toInt().toString()
                    } else {
                        String.format(Locale.JAPAN, "%.1f", value)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
                .pointerInput(
                    series,
                    comparisonSeries,
                    seriesXValues,
                    xAxisMin,
                    xAxisMax
                ) {
                    detectTapGestures { tapOffset ->
                        val allAxisXs = buildList {
                            series.forEach { item ->
                                val custom = seriesXValues[item.label].orEmpty()
                                if (custom.size == item.values.size) {
                                    addAll(custom)
                                } else {
                                    addAll(item.values.indices.map { it.toFloat() })
                                }
                            }
                        }
                        if (allAxisXs.isEmpty()) return@detectTapGestures

                        val leftPad = 6.dp.toPx()
                        val rightPad = 6.dp.toPx()
                        val chartWidth = size.width - leftPad - rightPad
                        if (chartWidth <= 0f) return@detectTapGestures

                        val inferredMin = allAxisXs.minOrNull() ?: 0f
                        val inferredMax = allAxisXs.maxOrNull() ?: inferredMin + 1f
                        val axisMinResolved = xAxisMin ?: inferredMin
                        val axisMaxResolved = xAxisMax ?: inferredMax
                        if (abs(axisMaxResolved - axisMinResolved) < 0.0001f) {
                            return@detectTapGestures
                        }

                        val clampedX = (tapOffset.x - leftPad).coerceIn(0f, chartWidth)
                        val tappedAxisX = axisMinResolved +
                            (clampedX / chartWidth) * (axisMaxResolved - axisMinResolved)
                        val nearestAxisX = allAxisXs.minByOrNull { axisX ->
                            abs(axisX - tappedAxisX)
                        } ?: return@detectTapGestures
                        onSelectedXAxisValueChange?.invoke(nearestAxisX)
                    }
                }
        ) {
            val leftPad = 6.dp.toPx()
            val rightPad = 6.dp.toPx()
            val topPad = 10.dp.toPx()
            val bottomPad = 10.dp.toPx()
            val chartWidth = size.width - leftPad - rightPad
            val chartHeight = size.height - topPad - bottomPad
            if (chartWidth <= 0f || chartHeight <= 0f) return@Canvas

            val guideDenominator = (guideValues.lastIndex).coerceAtLeast(1).toFloat()
            val allMainAxisXs = buildList {
                series.forEach { item ->
                    val custom = seriesXValues[item.label].orEmpty()
                    if (custom.size == item.values.size) {
                        addAll(custom)
                    } else {
                        addAll(item.values.indices.map { it.toFloat() })
                    }
                }
            }
            val axisMinResolved = xAxisMin ?: allMainAxisXs.minOrNull() ?: 0f
            val axisMaxResolved = xAxisMax ?: allMainAxisXs.maxOrNull() ?: 1f
            repeat(guideValues.size) { i ->
                val y = topPad + chartHeight * (i / guideDenominator)
                drawLine(
                    color = gridColor,
                    start = Offset(leftPad, y),
                    end = Offset(size.width - rightPad, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            fun buildPoints(values: List<Float>): List<Offset> {
                if (values.isEmpty()) return emptyList()
                return values.mapIndexed { index, value ->
                    val xValue = index.toFloat()
                    val denominator = (values.lastIndex).coerceAtLeast(1).toFloat()
                    val xRatio = if (abs(axisMaxResolved - axisMinResolved) >= 0.0001f) {
                        ((xValue - axisMinResolved) / (axisMaxResolved - axisMinResolved)).coerceIn(0f, 1f)
                    } else {
                        index / denominator
                    }
                    val yRatio = if (abs(maxValue - minValue) < 0.0001f) {
                        0.5f
                    } else {
                        1f - ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
                    }
                    Offset(
                        x = leftPad + chartWidth * xRatio,
                        y = topPad + chartHeight * yRatio
                    )
                }
            }

            fun buildPoints(values: List<Float>, xValues: List<Float>): List<Offset> {
                if (values.isEmpty()) return emptyList()
                val denominator = (values.lastIndex).coerceAtLeast(1).toFloat()
                return values.mapIndexed { index, value ->
                    val xValue = xValues.getOrElse(index) { index.toFloat() }
                    val xRatio = if (abs(axisMaxResolved - axisMinResolved) >= 0.0001f) {
                        ((xValue - axisMinResolved) / (axisMaxResolved - axisMinResolved)).coerceIn(0f, 1f)
                    } else {
                        index / denominator
                    }
                    val yRatio = if (abs(maxValue - minValue) < 0.0001f) {
                        0.5f
                    } else {
                        1f - ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
                    }
                    Offset(
                        x = leftPad + chartWidth * xRatio,
                        y = topPad + chartHeight * yRatio
                    )
                }
            }

            comparisonSeries.forEach { item ->
                val points = buildPoints(item.values, comparisonSeriesXValues[item.label].orEmpty())
                if (points.size >= 2) {
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = item.color.copy(alpha = 0.4f),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            series.forEach { item ->
                val xValues = seriesXValues[item.label].orEmpty()
                val points = if (xValues.size == item.values.size) {
                    buildPoints(item.values, xValues)
                } else {
                    buildPoints(item.values)
                }
                if (drawStyle == ChartDrawStyle.BAR) {
                    val barWidth = (chartWidth / (points.size.coerceAtLeast(1) * 1.6f)).coerceAtLeast(3.dp.toPx())
                    points.forEach { point ->
                        drawRect(
                            color = item.color,
                            topLeft = Offset(point.x - barWidth / 2f, point.y),
                            size = androidx.compose.ui.geometry.Size(barWidth, size.height - bottomPad - point.y)
                        )
                    }
                } else if (points.size >= 2) {
                    val path = if (smoothLine && points.size >= 3) {
                        buildSmoothLinePath(points)
                    } else {
                        Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                    }
                    drawPath(
                        path = path,
                        color = item.color,
                        style = Stroke(
                            width = 2.2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
                if (drawStyle == ChartDrawStyle.LINE) {
                    points.forEach { point ->
                        drawCircle(
                            color = item.color,
                            radius = 2.dp.toPx(),
                            center = point
                        )
                    }
                }
            }

            if (selectedXAxisValue != null && abs(axisMaxResolved - axisMinResolved) >= 0.0001f) {
                val selectedXRatio = ((selectedXAxisValue - axisMinResolved) / (axisMaxResolved - axisMinResolved))
                    .coerceIn(0f, 1f)
                val selectedX = leftPad + chartWidth * selectedXRatio
                drawLine(
                    color = selectionLineColor,
                    start = Offset(selectedX, topPad),
                    end = Offset(selectedX, size.height - bottomPad),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
        }
    }
}

@Composable
private fun CompactMetricChartCard(
    title: String,
    values: List<Float>,
    comparisonValues: List<Float> = emptyList(),
    labels: List<String>,
    absoluteMin: Float,
    absoluteMax: Float,
    lineColor: Color,
    selectedIndex: Int? = null,
    onSelectedIndexChange: ((Int) -> Unit)? = null,
    showSelectionLabel: Boolean = true,
    xValues: List<Float> = emptyList(),
    comparisonXValues: List<Float> = emptyList(),
    axisStartLabel: String? = null,
    axisEndLabel: String? = null,
    xAxisMinOverride: Float? = null,
    xAxisMaxOverride: Float? = null,
    modifier: Modifier = Modifier
) {
    val latest = values.lastOrNull()

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                latest?.let {
                    Text(
                        text = formatMetricValue(title = title, value = it),
                        style = MaterialTheme.typography.labelMedium,
                        color = lineColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            SimpleLineChart(
                values = values,
                comparisonValues = comparisonValues,
                labels = labels,
                minValue = absoluteMin,
                maxValue = absoluteMax,
                lineColor = lineColor,
                selectedIndex = selectedIndex,
                onSelectedIndexChange = onSelectedIndexChange,
                showSelectionLabel = showSelectionLabel,
                xValues = xValues,
                comparisonXValues = comparisonXValues,
                xAxisMinOverride = xAxisMinOverride,
                xAxisMaxOverride = xAxisMaxOverride,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            CompactChartTickLabelRow(
                labels = listOf(
                    axisStartLabel ?: labels.firstOrNull().orEmpty(),
                    axisEndLabel ?: labels.lastOrNull().orEmpty()
                )
            )
        }
    }
}

@Composable
private fun AnalyticsDisplayModeToggle(
    current: AnalyticsDisplayMode,
    modes: List<AnalyticsDisplayMode>,
    onChange: (AnalyticsDisplayMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            modes.forEach { mode ->
                val label = when (mode) {
                    AnalyticsDisplayMode.DETAIL -> "詳細"
                    AnalyticsDisplayMode.CHARTS -> "グラフ"
                    AnalyticsDisplayMode.MAP -> "マップ"
                }
                val icon: @Composable () -> Unit = {
                    when (mode) {
                        AnalyticsDisplayMode.DETAIL -> {
                            Icon(Icons.Default.Today, contentDescription = null)
                        }
                        AnalyticsDisplayMode.CHARTS -> {
                            Icon(Icons.Default.ShowChart, contentDescription = null)
                        }
                        AnalyticsDisplayMode.MAP -> {
                            Icon(Icons.Default.GridView, contentDescription = null)
                        }
                    }
                }

                ToggleChipLikeButton(
                    modifier = Modifier.weight(1f),
                    selected = current == mode,
                    label = label,
                    icon = icon,
                    onClick = { onChange(mode) }
                )
            }
        }
    }
}

@Composable
private fun PeriodPresetHeatmapSelector(
    selectedPeriod: PeriodPreset,
    onSelectPreset: (PeriodPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PeriodPreset.values().forEach { preset ->
            FilterChip(
                selected = selectedPeriod == preset,
                onClick = { onSelectPreset(preset) },
                label = { Text(preset.label) },
                colors = selectionChipColors()
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnalyticsPeriodSelector(
    current: AnalyticsPeriod,
    onChange: (AnalyticsPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnalyticsPeriod.entries.forEach { period ->
            FilterChip(
                selected = current == period,
                onClick = { onChange(period) },
                label = { Text(period.label) },
                colors = selectionChipColors()
            )
        }
    }
}

@Composable
private fun ToggleChipLikeButton(
    selected: Boolean,
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SelectedDateSelectorRow(
    dates: List<LocalDate>,
    selectedDate: LocalDate?,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCalendarDialog by rememberSaveable(dates, selectedDate) { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = selectedDate?.let {
                DateTimeFormatter.ofPattern("M月d日(E)", Locale.JAPAN).format(it)
            } ?: "日付未選択",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        AssistChip(
            onClick = { showCalendarDialog = true },
            label = { Text("カレンダー") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null
                )
            }
        )
    }

    if (showCalendarDialog) {
        AvailableDatePickerDialog(
            initialDate = selectedDate,
            onDismiss = { showCalendarDialog = false },
            onConfirm = { pickedDate ->
                onSelectDate(pickedDate)
                showCalendarDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AvailableDatePickerDialog(
    initialDate: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val zoneId = remember { ZoneId.systemDefault() }
    val today = remember(zoneId) { LocalDate.now(zoneId) }
    val selectableDates = remember(today, zoneId) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant.ofEpochMilli(utcTimeMillis).atZone(zoneId).toLocalDate()
                return !date.isAfter(today)
            }
        }
    }
    val initialSelectedDateMillis = remember(initialDate, zoneId) {
        initialDate?.atStartOfDay(zoneId)?.toInstant()?.toEpochMilli()
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis,
        selectableDates = selectableDates
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis ?: return@TextButton
                    val selectedDate = Instant.ofEpochMilli(selectedMillis).atZone(zoneId).toLocalDate()
                    onConfirm(selectedDate)
                },
                enabled = datePickerState.selectedDateMillis != null
            ) {
                Text("選択")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyPersonalityScoreCard(
    score: DailyPersonalityScore,
    dailyRecord: DailyRecord?,
    onUpdateDailyRecord: (DailyRecord) -> Unit,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("M月d日(E)", Locale.JAPAN)
    }

    var showSleepDialog by rememberSaveable(score.date.toString()) { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .let { base -> if (onClick != null) base.clickable { onClick() } else base },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                contentColor = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = score.date.format(dateFormatter),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        val sleepText = score.features.sleepHours?.let {
                            "睡眠 ${"%.1f".format(Locale.JAPAN, it)}h"
                        } ?: "睡眠未入力"

                        Text(
                            text = sleepText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    TextButton(onClick = { showSleepDialog = true }) {
                        Icon(imageVector = Icons.Default.Bedtime, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("睡眠入力")
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StateBadge(
                    label = "日単体: ${score.state.label}",
                    state = score.state
                )

                ScoreRow(
                    label = "安定度",
                    value = score.stability.roundToInt().toString(),
                    progress = (score.stability / 100.0).toFloat()
                )

                ScoreRow(
                    label = "不安",
                    value = "%.1f".format(Locale.JAPAN, score.anxiety),
                    progress = (score.anxiety / 10.0).toFloat()
                )

                ScoreRow(
                    label = "活力",
                    value = score.energy.roundToInt().toString(),
                    progress = (score.energy / 100.0).toFloat()
                )

                ScoreRow(
                    label = "制御感",
                    value = score.control.roundToInt().toString(),
                    progress = (score.control / 100.0).toFloat()
                )

                HorizontalDivider()

                Text(
                    text = score.summary,
                    style = MaterialTheme.typography.bodyMedium
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    scoreFeatureChips(score).forEach { label ->
                        AssistChip(
                            onClick = {},
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    }

    if (showSleepDialog) {
        SleepInputDialog(
            date = score.date,
            initialMinutes = dailyRecord?.sleep?.durationMinutes ?: 0,
            initialQuality = dailyRecord?.sleep?.quality ?: 2,
            initialSteps = dailyRecord?.steps ?: 0,
            onDismiss = { showSleepDialog = false },
            onSave = { minutes, quality, steps ->
                onUpdateDailyRecord(
                    DailyRecord(
                        date = score.date.toString(),
                        sleep = SleepData(
                            durationMinutes = minutes,
                            quality = quality
                        ),
                        steps = steps
                    )
                )
                showSleepDialog = false
            }
        )
    }
}

@Composable
private fun DailyMessagePseudoTrendCard(
    date: LocalDate?,
    messages: List<MessageV2>,
    dailyRecord: DailyRecord?,
    compareMode: DetailCompareMode,
    onCompareModeChange: (DetailCompareMode) -> Unit,
    comparisonDate: LocalDate?,
    comparisonMessages: List<MessageV2>,
    comparisonDailyRecord: DailyRecord?,
    onSwipeToOlderDate: () -> Unit,
    onSwipeToNewerDate: () -> Unit,
    startPadding: Dp = 0.dp,
    endPadding: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val points = remember(messages, dailyRecord) {
        PersonalityScoreModel.buildIntradayScoreSeries(
            messages = messages,
            dailyRecord = dailyRecord
        ).filter { isDetailChartVisibleTime(it.timestamp) }
    }

    val detailSeries = remember(points) {
        listOf(
            TimedLineSeries(
                label = "安定度",
                color = ScorePalette.Stability,
                values = points.map { it.stability },
                xValues = points.map { minutesOfDay(it.timestamp) }
            ),
            TimedLineSeries(
                label = "不安",
                color = ScorePalette.Anxiety,
                values = points.map { (it.anxiety * 10f).coerceIn(0f, 100f) },
                xValues = points.map { minutesOfDay(it.timestamp) }
            ),
            TimedLineSeries(
                label = "活力",
                color = ScorePalette.Energy,
                values = points.map { it.energy },
                xValues = points.map { minutesOfDay(it.timestamp) }
            ),
            TimedLineSeries(
                label = "制御感",
                color = ScorePalette.Control,
                values = points.map { it.control },
                xValues = points.map { minutesOfDay(it.timestamp) }
            )
        )
    }
    val comparisonPoints = remember(comparisonMessages, comparisonDailyRecord) {
        PersonalityScoreModel.buildIntradayScoreSeries(
            messages = comparisonMessages,
            dailyRecord = comparisonDailyRecord
        ).filter { isDetailChartVisibleTime(it.timestamp) }
    }
    val comparisonSeries = remember(comparisonPoints) {
        listOf(
            TimedLineSeries(
                label = "安定度",
                color = ScorePalette.Stability,
                values = comparisonPoints.map { it.stability },
                xValues = comparisonPoints.map { minutesOfDay(it.timestamp) }
            ),
            TimedLineSeries(
                label = "不安",
                color = ScorePalette.Anxiety,
                values = comparisonPoints.map { (it.anxiety * 10f).coerceIn(0f, 100f) },
                xValues = comparisonPoints.map { minutesOfDay(it.timestamp) }
            ),
            TimedLineSeries(
                label = "活力",
                color = ScorePalette.Energy,
                values = comparisonPoints.map { it.energy },
                xValues = comparisonPoints.map { minutesOfDay(it.timestamp) }
            ),
            TimedLineSeries(
                label = "制御感",
                color = ScorePalette.Control,
                values = comparisonPoints.map { it.control },
                xValues = comparisonPoints.map { minutesOfDay(it.timestamp) }
            )
        )
    }
    val comparisonLabel = remember(compareMode, comparisonDate, comparisonMessages) {
        when (compareMode) {
            DetailCompareMode.NONE -> null
            DetailCompareMode.PREVIOUS_DAY -> {
                comparisonDate?.let {
                    if (comparisonMessages.isNotEmpty()) {
                        "比較対象: ${it.monthValue}/${it.dayOfMonth}"
                    } else {
                        "前日データなし"
                    }
                } ?: "前日データなし"
            }
            DetailCompareMode.PREVIOUS_WEEK -> {
                comparisonDate?.let {
                    if (comparisonMessages.isNotEmpty()) {
                        "比較対象: ${it.monthValue}/${it.dayOfMonth}"
                    } else {
                        "先週データなし"
                    }
                } ?: "先週データなし"
            }
        }
    }
    var selectedChartMinutes by remember(date, points) {
        mutableStateOf(points.lastOrNull()?.let { minutesOfDay(it.timestamp) })
    }
    LaunchedEffect(points) {
        if (points.isNotEmpty() && selectedChartMinutes == null) {
            selectedChartMinutes = minutesOfDay(points.last().timestamp)
        }
    }
    val selectedPoint = remember(points, selectedChartMinutes) {
        val selected = selectedChartMinutes ?: return@remember null
        points.minByOrNull { point ->
            abs(minutesOfDay(point.timestamp) - selected)
        }
    }
    val selectedTimeLabel = remember(selectedPoint) {
        selectedPoint?.let { formatMinuteLabel(minutesOfDay(it.timestamp)) }
    }
    val selectedMessages = remember(messages, selectedPoint) {
        val anchorTimestamp = selectedPoint?.timestamp ?: return@remember emptyList()
        val anchorMinutes = minutesOfDay(anchorTimestamp)
        val nearestDistance = messages.minOfOrNull { message ->
            abs(minutesOfDay(message.timestamp) - anchorMinutes)
        } ?: return@remember emptyList()
        messages.filter { message ->
            abs(minutesOfDay(message.timestamp) - anchorMinutes) <= nearestDistance
        }
    }
    val swipeThresholdPx = with(LocalDensity.current) { 36.dp.toPx() }
    var accumulatedDrag by remember(date) { mutableStateOf(0f) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = startPadding, end = endPadding),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date?.let {
                        "${DateTimeFormatter.ofPattern("M月d日(E)", Locale.JAPAN).format(it)} の日内推移"
                    } ?: "日内推移",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            DetailCompareModeSelector(
                current = compareMode,
                onChange = onCompareModeChange,
                modifier = Modifier.fillMaxWidth()
            )

            comparisonLabel?.let { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (messages.isEmpty()) {
                Text(
                    text = "この日にメッセージがありません",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            if (points.isEmpty()) {
                Text(
                    text = "07:00〜22:00 の範囲に表示できるメッセージがありません",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            Column(
                modifier = Modifier.pointerInput(date, points.size) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            accumulatedDrag += dragAmount
                        },
                        onDragEnd = {
                            when {
                                accumulatedDrag <= -swipeThresholdPx -> onSwipeToOlderDate()
                                accumulatedDrag >= swipeThresholdPx -> onSwipeToNewerDate()
                            }
                            accumulatedDrag = 0f
                        },
                        onDragCancel = {
                            accumulatedDrag = 0f
                        }
                    )
                },
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MultiLineChart(
                    labels = listOf("07:00", "10:00", "13:00", "16:00", "19:00", "22:00"),
                    series = detailSeries.map { LineSeries(it.label, it.color, it.values) },
                    comparisonSeries = comparisonSeries.map {
                        LineSeries(
                            label = it.label,
                            color = it.color.copy(alpha = 0.4f),
                            values = it.values
                        )
                    }.takeIf { compareMode != DetailCompareMode.NONE },
                    seriesXValues = detailSeries.associate { it.label to it.xValues },
                    comparisonSeriesXValues = comparisonSeries.associate { it.label to it.xValues },
                    selectedXAxisValue = selectedPoint?.let { minutesOfDay(it.timestamp) },
                    onSelectedXAxisValueChange = { tappedMinutes ->
                        selectedChartMinutes = tappedMinutes
                    },
                    xAxisMin = DetailChartStartMinutes,
                    xAxisMax = DetailChartEndMinutes,
                    minValue = 0f,
                    maxValue = 100f,
                    yAxisTicks = listOf(0f, 25f, 50f, 75f, 100f),
                    toggleableLegend = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )

                if (selectedMessages.isNotEmpty() && selectedTimeLabel != null) {
                    SelectedMessagesAtTimeCard(
                        timeLabel = selectedTimeLabel,
                        messages = selectedMessages,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailCompareModeSelector(
    current: DetailCompareMode,
    onChange: (DetailCompareMode) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DetailCompareMode.entries.forEach { mode ->
            FilterChip(
                selected = current == mode,
                onClick = { onChange(mode) },
                label = { Text(mode.label) },
                colors = selectionChipColors()
            )
        }
    }
}

@Composable
private fun SelectedMessagesAtTimeCard(
    timeLabel: String,
    messages: List<MessageV2>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "$timeLabel のメッセージ",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            messages.forEachIndexed { index, message ->
                if (index > 0) {
                    HorizontalDivider()
                }

                Column(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formatTime(message.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = message.extractDisplayText(),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    val enabledFlags = message.enabledActionFlagLabels()
                    val emotionDetails = message.emotionDetails()
                    if (enabledFlags.isNotEmpty()) {
                        Text(
                            text = "フラグ: ${enabledFlags.joinToString(" / ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (emotionDetails.isNotEmpty()) {
                        Text(
                            text = "感情: ${emotionDetails.joinToString(" / ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun MessageV2.extractDisplayText(): String {
    fun tryGet(name: String): String? {
        return runCatching {
            val getterName = "get" + name.replaceFirstChar { it.uppercase() }
            val getter = javaClass.methods.firstOrNull { method ->
                method.name == getterName && method.parameterCount == 0
            }
            val value = getter?.invoke(this) as? String
            value?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    return tryGet("message")
        ?: tryGet("text")
        ?: tryGet("content")
        ?: "（メッセージ本文を取得できませんでした）"
}

private fun MessageV2.enabledActionFlagLabels(): List<String> {
    return ActionType.entries
        .filter { action -> action.matches(flags) }
        .map { action -> action.label }
}

private fun MessageV2.emotionDetails(): List<String> {
    return EmotionType.entries.mapNotNull { emotion ->
        val score = emotion.scoreOf(emotions)
        if (score > 0) "${emotion.label} $score" else null
    }
}

@Composable
private fun ScoreRow(
    label: String,
    value: String,
    progress: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SimpleLineChart(
    values: List<Float>,
    comparisonValues: List<Float> = emptyList(),
    labels: List<String>,
    minValue: Float,
    maxValue: Float,
    lineColor: Color,
    selectedIndex: Int? = null,
    onSelectedIndexChange: ((Int) -> Unit)? = null,
    showSelectionLabel: Boolean = true,
    xValues: List<Float> = emptyList(),
    comparisonXValues: List<Float> = emptyList(),
    xAxisMinOverride: Float? = null,
    xAxisMaxOverride: Float? = null,
    modifier: Modifier = Modifier
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val comparisonColor = ScorePalette.Comparison
    val density = LocalDensity.current

    var chartSize by remember { mutableStateOf(IntSize.Zero) }
    var internalSelectedIndex by remember(values, labels) { mutableStateOf<Int?>(null) }
    val activeSelectedIndex = selectedIndex ?: internalSelectedIndex

    val leftPadPx = with(density) { 6.dp.toPx() }
    val rightPadPx = with(density) { 6.dp.toPx() }
    val topPadPx = with(density) { 10.dp.toPx() }
    val bottomPadPx = with(density) { 10.dp.toPx() }

    val useSharedXAxis = xValues.size == values.size
    val mainXValues = remember(values, xValues) {
        if (xValues.size == values.size) xValues else values.indices.map { it.toFloat() }
    }
    val mainComparisonXValues = remember(comparisonValues, comparisonXValues) {
        if (comparisonXValues.size == comparisonValues.size) {
            comparisonXValues
        } else {
            comparisonValues.indices.map { it.toFloat() }
        }
    }

    val axisMinX = xAxisMinOverride ?: if (useSharedXAxis) {
        DetailChartStartMinutes
    } else {
        0f
    }
    val axisMaxX = xAxisMaxOverride ?: if (useSharedXAxis) {
        DetailChartEndMinutes
    } else {
        maxOf(
            (values.lastIndex).coerceAtLeast(1),
            (comparisonValues.lastIndex).coerceAtLeast(1)
        ).toFloat()
    }

    Box(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .onSizeChanged { chartSize = it }
                .pointerInput(values, labels, chartSize, mainXValues, axisMinX, axisMaxX) {
                    detectTapGestures { tapOffset ->
                        if (values.isEmpty()) return@detectTapGestures

                        val chartWidth = chartSize.width - leftPadPx - rightPadPx
                        if (chartWidth <= 0f) return@detectTapGestures

                        val clampedX = (tapOffset.x - leftPadPx).coerceIn(0f, chartWidth)
                        val tappedAxisX = axisMinX + (clampedX / chartWidth) * (axisMaxX - axisMinX)

                        val newIndex = mainXValues.indices
                            .minByOrNull { index -> abs(mainXValues[index] - tappedAxisX) }
                            ?: 0

                        if (onSelectedIndexChange != null) {
                            onSelectedIndexChange(newIndex)
                        } else {
                            internalSelectedIndex = newIndex
                        }
                    }
                }
        ) {
            if (values.isEmpty()) return@Canvas

            val chartWidth = size.width - leftPadPx - rightPadPx
            val chartHeight = size.height - topPadPx - bottomPadPx
            if (chartWidth <= 0f || chartHeight <= 0f) return@Canvas

            repeat(4) { i ->
                val y = topPadPx + chartHeight * (i / 3f)
                drawLine(
                    color = gridColor,
                    start = Offset(leftPadPx, y),
                    end = Offset(size.width - rightPadPx, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            fun buildPoints(targetValues: List<Float>, targetXValues: List<Float>): List<Offset> {
                return targetValues.mapIndexed { index, value ->
                    val yRatio = if (abs(maxValue - minValue) < 0.0001f) {
                        0.5f
                    } else {
                        1f - ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
                    }

                    val axisX = targetXValues.getOrElse(index) { index.toFloat() }
                    val xRatio = if (abs(axisMaxX - axisMinX) < 0.0001f) {
                        0.5f
                    } else {
                        ((axisX - axisMinX) / (axisMaxX - axisMinX)).coerceIn(0f, 1f)
                    }

                    Offset(
                        x = leftPadPx + chartWidth * xRatio,
                        y = topPadPx + chartHeight * yRatio
                    )
                }
            }

            fun buildLinePath(points: List<Offset>): Path {
                return Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val point = points[i]
                        lineTo(point.x, point.y)
                    }
                }
            }

            fun drawSeries(points: List<Offset>, color: Color, strokeWidthDp: Int) {
                if (points.size >= 2) {
                    drawPath(
                        path = buildLinePath(points),
                        color = color,
                        style = Stroke(
                            width = strokeWidthDp.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            val comparisonPointsOnChart = buildPoints(comparisonValues, mainComparisonXValues)
            if (comparisonPointsOnChart.isNotEmpty()) {
                drawSeries(
                    points = comparisonPointsOnChart,
                    color = comparisonColor.copy(alpha = 0.95f),
                    strokeWidthDp = 2
                )
            }

            val points = buildPoints(values, mainXValues)
            drawSeries(
                points = points,
                color = lineColor,
                strokeWidthDp = 3
            )

            activeSelectedIndex?.let { index ->
                val selectedPoint = points.getOrNull(index)
                if (selectedPoint != null) {
                    drawLine(
                        color = lineColor.copy(alpha = 0.45f),
                        start = Offset(selectedPoint.x, topPadPx),
                        end = Offset(selectedPoint.x, size.height - bottomPadPx),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }

            comparisonPointsOnChart.forEach { point ->
                drawCircle(
                    color = comparisonColor.copy(alpha = 0.95f),
                    radius = 1.8.dp.toPx(),
                    center = point
                )
            }

            points.forEachIndexed { index, point ->
                val isSelected = index == activeSelectedIndex

                drawCircle(
                    color = lineColor,
                    radius = if (isSelected) 3.4.dp.toPx() else 2.4.dp.toPx(),
                    center = point
                )
            }
        }

        if (showSelectionLabel) {
            activeSelectedIndex?.let { index ->
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = labels.getOrElse(index) { "" },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactChartTickLabelRow(
    labels: List<String>,
    startPadding: Dp = 0.dp,
    endPadding: Dp = 0.dp,
    centerAllLabels: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (labels.isEmpty()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = startPadding, end = endPadding),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        labels.forEachIndexed { index, label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = if (centerAllLabels) {
                    TextAlign.Center
                } else {
                    when (index) {
                        0 -> TextAlign.Start
                        labels.lastIndex -> TextAlign.End
                        else -> TextAlign.Center
                    }
                },
                maxLines = 1
            )
        }
    }
}

@Composable
private fun selectionChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.surface,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepInputDialog(
    date: LocalDate,
    initialMinutes: Int,
    initialQuality: Int,
    initialSteps: Int,
    onDismiss: () -> Unit,
    onSave: (durationMinutes: Int, quality: Int, steps: Int) -> Unit
) {
    val initialHours = (initialMinutes / 60).coerceAtLeast(0)
    val initialRemainMinutes = (initialMinutes % 60).coerceAtLeast(0)

    var hoursText by rememberSaveable(date.toString()) {
        mutableStateOf(if (initialMinutes > 0) initialHours.toString() else "")
    }
    var minutesText by rememberSaveable("${date}_minutes") {
        mutableStateOf(if (initialMinutes > 0) initialRemainMinutes.toString() else "")
    }
    var stepsText by rememberSaveable("${date}_steps") {
        mutableStateOf(if (initialSteps > 0) initialSteps.toString() else "")
    }
    var qualityValue by rememberSaveable("${date}_quality") {
        mutableStateOf(initialQuality.coerceIn(0, 3).toFloat())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${date.monthValue}月${date.dayOfMonth}日の睡眠入力") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hoursText,
                        onValueChange = { hoursText = it.filter(Char::isDigit).take(2) },
                        label = { Text("時間") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = minutesText,
                        onValueChange = { minutesText = it.filter(Char::isDigit).take(2) },
                        label = { Text("分") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = stepsText,
                    onValueChange = { stepsText = it.filter(Char::isDigit).take(6) },
                    label = { Text("歩数") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text(
                        text = "睡眠品質 ${qualityValue.toInt()} / 3",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = qualityValue,
                        onValueChange = {
                            qualityValue = it.roundToInt().coerceIn(0, 3).toFloat()
                        },
                        valueRange = 0f..3f,
                        steps = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hours = hoursText.toIntOrNull()?.coerceAtLeast(0) ?: 0
                    val minutes = minutesText.toIntOrNull()?.coerceIn(0, 59) ?: 0
                    val totalMinutes = hours * 60 + minutes
                    val steps = stepsText.toIntOrNull()?.coerceAtLeast(0) ?: 0
                    onSave(totalMinutes, qualityValue.toInt(), steps)
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

@Composable
private fun EmptyAnalyticsState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "表示できる分析データがありません",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun scoreFeatureChips(score: DailyPersonalityScore): List<String> {
    val chips = mutableListOf<String>()
    val features = score.features
    val emotions = features.emotions

    features.sleepHours?.let { chips += "睡眠 ${"%.1f".format(Locale.JAPAN, it)}h" }
    features.sleepQuality?.let { chips += "睡眠品質 $it/3" }
    features.steps?.let { chips += "歩数 $it" }

    if (emotions.anxiety > 0) chips += "不安 ${"%.1f".format(Locale.JAPAN, emotions.anxiety)}"
    if (emotions.angry > 0) chips += "怒り ${"%.1f".format(Locale.JAPAN, emotions.angry)}"
    if (emotions.sad > 0) chips += "悲しみ ${"%.1f".format(Locale.JAPAN, emotions.sad)}"
    if (emotions.happy > 0) chips += "喜び ${"%.1f".format(Locale.JAPAN, emotions.happy)}"
    if (emotions.calm > 0) chips += "安心 ${"%.1f".format(Locale.JAPAN, emotions.calm)}"

    return chips
}

private fun formatMetricValue(
    title: String,
    value: Float
): String {
    return when (title) {
        "不安" -> "%.1f".format(Locale.JAPAN, value)
        else -> value.roundToInt().toString()
    }
}


private fun filterScoresByPeriod(
    scores: List<DailyPersonalityScore>,
    period: AnalyticsPeriod
): List<DailyPersonalityScore> {
    if (scores.isEmpty()) return emptyList()
    if (period == AnalyticsPeriod.ALL) return scores

    val latest = scores.maxByOrNull { it.date }?.date ?: return scores
    val from = when (period) {
        AnalyticsPeriod.DAYS_7 -> latest.minusDays(6)
        AnalyticsPeriod.DAYS_14 -> latest.minusDays(13)
        AnalyticsPeriod.DAYS_30 -> latest.minusDays(29)
        AnalyticsPeriod.ALL -> latest
    }

    return scores.filter { it.date >= from }
}


private fun buildChartDatesByPeriod(
    allDatesDesc: List<LocalDate>,
    period: AnalyticsPeriod
): List<LocalDate> {
    if (allDatesDesc.isEmpty()) return emptyList()

    val latest = allDatesDesc.maxOrNull() ?: return emptyList()
    return when (period) {
        AnalyticsPeriod.DAYS_7 -> (0L..6L).map { latest.minusDays(6 - it) }
        AnalyticsPeriod.DAYS_14 -> (0L..13L).map { latest.minusDays(13 - it) }
        AnalyticsPeriod.DAYS_30 -> (0L..29L).map { latest.minusDays(29 - it) }
        AnalyticsPeriod.ALL -> allDatesDesc.distinct().sorted()
    }
}

private fun LocalDate.toDayWeekLabel(): String {
    return format(DateTimeFormatter.ofPattern("d(E)", Locale.JAPAN))
}


private fun buildGraphEntries(
    dates: List<LocalDate>,
    labels: List<String>,
    valueByDate: Map<LocalDate, Float>
): List<GraphEntry> {
    return dates.mapIndexed { index, date ->
        GraphEntry(
            date = date,
            label = labels.getOrElse(index) { "" },
            value = valueByDate[date] ?: 0f
        )
    }
}

private fun buildSmoothLinePath(points: List<Offset>): Path {
    return Path().apply {
        moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            val previous = points[i - 1]
            val current = points[i]
            val controlX = (previous.x + current.x) / 2f
            cubicTo(
                controlX, previous.y,
                controlX, current.y,
                current.x, current.y
            )
        }
    }
}

private fun buildChartXAxisLabels(
    dates: List<LocalDate>,
    period: AnalyticsPeriod
): List<String> {
    return when (period) {
        AnalyticsPeriod.DAYS_7 -> dates.map { it.toDayWeekLabel() }
        AnalyticsPeriod.DAYS_14,
        AnalyticsPeriod.DAYS_30 -> dates.map { date ->
            if (date.dayOfWeek == DayOfWeek.MONDAY) date.toDayWeekLabel() else ""
        }
        AnalyticsPeriod.ALL -> dates.map { "" }.toMutableList().apply {
            if (isNotEmpty()) {
                this[0] = dates.first().toDayWeekLabel()
                this[lastIndex] = dates.last().toDayWeekLabel()
            }
        }
    }
}
