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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.sample2.analytics.DailyPersonalityScore
import com.example.sample2.analytics.PersonalityScoreModel
import com.example.sample2.analytics.PersonalityState
import com.example.sample2.data.DailyRecord
import com.example.sample2.data.MessageV2
import com.example.sample2.data.SleepData
import com.example.sample2.util.formatTime
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class AnalyticsDisplayMode {
    DETAIL,
    CHARTS,
    CARDS
}

private enum class AnalyticsInfoKind {
    DETAIL,
    CHARTS
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
    PREVIOUS_DAY("先日と比較"),
    PREVIOUS_WEEK("先週と比較")
}

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

private val StabilityChartColor = Color(0xFF8D6E63)
private val AnxietyChartColor = Color(0xFF8E24AA)
private val EnergyChartColor = Color(0xFFFB8C00)
private val ControlChartColor = Color(0xFF43A047)

@Composable
fun PersonalityAnalyticsScreen(
    messages: List<MessageV2>,
    dailyRecords: List<DailyRecord>,
    onUpdateDailyRecord: (DailyRecord) -> Unit,
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

    var displayModeName by rememberSaveable {
        mutableStateOf(AnalyticsDisplayMode.DETAIL.name)
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
    var infoDialogName by rememberSaveable {
        mutableStateOf<String?>(null)
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

    val filteredRawScoresDesc = remember(allRawScores, selectedPeriod) {
        filterScoresByPeriod(
            scores = allRawScores.sortedByDescending { it.date },
            period = selectedPeriod
        )
    }

    val filteredRawScoresAsc = remember(filteredRawScoresDesc) {
        filteredRawScoresDesc.sortedBy { it.date }
    }

    val selectedDate = remember(filteredRawScoresDesc, selectedDateText) {
        val requested = selectedDateText?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        requested?.takeIf { date -> filteredRawScoresDesc.any { it.date == date } }
            ?: filteredRawScoresDesc.firstOrNull()?.date
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

    infoDialogName?.let { name ->
        AnalyticsInfoDialog(
            kind = AnalyticsInfoKind.valueOf(name),
            onDismiss = { infoDialogName = null }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        AnalyticsDisplayModeToggle(
            current = displayMode,
            onChange = { displayModeName = it.name },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )

        if (displayMode != AnalyticsDisplayMode.DETAIL) {
            AnalyticsPeriodSelector(
                current = selectedPeriod,
                onChange = { selectedPeriodName = it.name },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        } else {
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
                            dates = filteredRawScoresDesc.map { it.date },
                            selectedDate = selectedDate,
                            onSelectDate = { selectedDateText = it.toString() }
                        )
                    }

                    item {
                        DailyMessagePseudoTrendCard(
                            date = selectedDate,
                            messages = selectedDayMessages,
                            dailyRecord = selectedDayRecord,
                            summaryScore = selectedRawScore,
                            compareMode = detailCompareMode,
                            onCompareModeChange = { detailCompareModeName = it.name },
                            comparisonDate = comparisonDate,
                            comparisonMessages = comparisonDayMessages,
                            comparisonDailyRecord = comparisonDayRecord,
                            onInfoClick = {
                                infoDialogName = AnalyticsInfoKind.DETAIL.name
                            },
                            onSwipeToOlderDate = {
                                val currentIndex = filteredRawScoresDesc.indexOfFirst { it.date == selectedDate }
                                if (currentIndex in 0 until filteredRawScoresDesc.lastIndex) {
                                    selectedDateText = filteredRawScoresDesc[currentIndex + 1].date.toString()
                                }
                            },
                            onSwipeToNewerDate = {
                                val currentIndex = filteredRawScoresDesc.indexOfFirst { it.date == selectedDate }
                                if (currentIndex > 0) {
                                    selectedDateText = filteredRawScoresDesc[currentIndex - 1].date.toString()
                                }
                            }
                        )
                    }

                    item {
                        DetailQuickMetaRow(
                            selectedDate = selectedDate,
                            score = selectedRawScore,
                            messageCount = selectedDayMessages.size
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
                            onInfoClick = {
                                infoDialogName = AnalyticsInfoKind.CHARTS.name
                            },
                            onSwipeToBroaderPeriod = {
                                selectedPeriodName = nextAnalyticsPeriod(selectedPeriod).name
                            },
                            onSwipeToNarrowerPeriod = {
                                selectedPeriodName = previousAnalyticsPeriod(selectedPeriod).name
                            }
                        )
                    }
                }
            }

            AnalyticsDisplayMode.CARDS -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = filteredRawScoresDesc,
                        key = { it.date.toString() }
                    ) { score ->
                        DailyPersonalityScoreCard(
                            score = score,
                            dailyRecord = dailyRecordMap[score.date.toString()],
                            onUpdateDailyRecord = onUpdateDailyRecord,
                            selected = score.date == selectedDate,
                            onClick = {
                                selectedDateText = score.date.toString()
                                displayModeName = AnalyticsDisplayMode.DETAIL.name
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsInfoDialog(
    kind: AnalyticsInfoKind,
    onDismiss: () -> Unit
) {
    val title: String
    val body: String

    when (kind) {
        AnalyticsInfoKind.DETAIL -> {
            title = "詳細"
            body = "選択した1日の中で、メッセージごとの変化を見ます。前日の影響は混ぜません。"
        }

        AnalyticsInfoKind.CHARTS -> {
            title = "グラフ"
            body = "日ごとの raw スコアの全体推移です。日内の細かい変化は含みません。"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("閉じる")
            }
        }
    )
}

@Composable
private fun DetailQuickMetaRow(
    selectedDate: LocalDate?,
    score: DailyPersonalityScore?,
    messageCount: Int,
    modifier: Modifier = Modifier
) {
    val dateText = selectedDate?.let {
        DateTimeFormatter.ofPattern("M月d日(E)", Locale.JAPAN).format(it)
    } ?: "未選択"

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateText,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(8.dp))

        score?.let {
            StateBadge(
                label = it.state.label,
                state = it.state
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "メッセージ $messageCount 件",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OverallTrendChartCard(
    scores: List<DailyPersonalityScore>,
    currentPeriod: AnalyticsPeriod,
    onInfoClick: () -> Unit,
    onSwipeToBroaderPeriod: () -> Unit,
    onSwipeToNarrowerPeriod: () -> Unit,
    modifier: Modifier = Modifier
) {
    val swipeThresholdPx = with(LocalDensity.current) { 36.dp.toPx() }
    var accumulatedDrag by remember(currentPeriod) { mutableStateOf(0f) }
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    text = "全体推移",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onInfoClick) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "グラフ説明"
                    )
                }
            }

            CompactChartsGrid(
                scores = scores,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
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

@Composable
private fun CompactChartsGrid(
    scores: List<DailyPersonalityScore>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CompactMetricChartCard(
                title = "安定度",
                values = scores.map { it.stability.toFloat() },
                labels = scores.map { it.date.toShortLabel() },
                absoluteMin = 0f,
                absoluteMax = 100f,
                lineColor = StabilityChartColor,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CompactMetricChartCard(
                title = "不安",
                values = scores.map { it.anxiety.toFloat() },
                labels = scores.map { it.date.toShortLabel() },
                absoluteMin = 0f,
                absoluteMax = 10f,
                lineColor = AnxietyChartColor,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CompactMetricChartCard(
                title = "活力",
                values = scores.map { it.energy.toFloat() },
                labels = scores.map { it.date.toShortLabel() },
                absoluteMin = 0f,
                absoluteMax = 100f,
                lineColor = EnergyChartColor,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            CompactMetricChartCard(
                title = "制御感",
                values = scores.map { it.control.toFloat() },
                labels = scores.map { it.date.toShortLabel() },
                absoluteMin = 0f,
                absoluteMax = 100f,
                lineColor = ControlChartColor,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
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
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            CompactChartLabelRow(labels = labels)
        }
    }
}

@Composable
private fun AnalyticsDisplayModeToggle(
    current: AnalyticsDisplayMode,
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
            ToggleChipLikeButton(
                modifier = Modifier.weight(1f),
                selected = current == AnalyticsDisplayMode.DETAIL,
                label = "詳細",
                icon = { Icon(Icons.Default.Today, contentDescription = null) },
                onClick = { onChange(AnalyticsDisplayMode.DETAIL) }
            )
            ToggleChipLikeButton(
                modifier = Modifier.weight(1f),
                selected = current == AnalyticsDisplayMode.CHARTS,
                label = "グラフ",
                icon = { Icon(Icons.Default.ShowChart, contentDescription = null) },
                onClick = { onChange(AnalyticsDisplayMode.CHARTS) }
            )
            ToggleChipLikeButton(
                modifier = Modifier.weight(1f),
                selected = current == AnalyticsDisplayMode.CARDS,
                label = "カード",
                icon = { Icon(Icons.Default.ViewAgenda, contentDescription = null) },
                onClick = { onChange(AnalyticsDisplayMode.CARDS) }
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
    val listState = rememberLazyListState()
    val selectedIndex = remember(dates, selectedDate) {
        dates.indexOf(selectedDate).takeIf { it >= 0 } ?: 0
    }
    val swipeThresholdPx = with(LocalDensity.current) { 28.dp.toPx() }
    var accumulatedDrag by remember(dates, selectedDate) { mutableStateOf(0f) }

    LaunchedEffect(selectedIndex, dates) {
        if (dates.isNotEmpty()) {
            listState.animateScrollToItem(selectedIndex.coerceIn(0, dates.lastIndex))
        }
    }

    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(dates, selectedDate) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        accumulatedDrag += dragAmount
                    },
                    onDragEnd = {
                        val currentIndex = dates.indexOf(selectedDate).takeIf { it >= 0 } ?: 0
                        when {
                            accumulatedDrag <= -swipeThresholdPx && currentIndex < dates.lastIndex -> {
                                onSelectDate(dates[currentIndex + 1])
                            }
                            accumulatedDrag >= swipeThresholdPx && currentIndex > 0 -> {
                                onSelectDate(dates[currentIndex - 1])
                            }
                        }
                        accumulatedDrag = 0f
                    },
                    onDragCancel = {
                        accumulatedDrag = 0f
                    }
                )
            },
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = dates,
            key = { _, date -> date.toString() }
        ) { _, date ->
            FilterChip(
                selected = date == selectedDate,
                onClick = { onSelectDate(date) },
                label = { Text(date.toShortLabel()) },
                colors = selectionChipColors()
            )
        }
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
    summaryScore: DailyPersonalityScore?,
    compareMode: DetailCompareMode,
    onCompareModeChange: (DetailCompareMode) -> Unit,
    comparisonDate: LocalDate?,
    comparisonMessages: List<MessageV2>,
    comparisonDailyRecord: DailyRecord?,
    onInfoClick: () -> Unit,
    onSwipeToOlderDate: () -> Unit,
    onSwipeToNewerDate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val points = remember(messages, dailyRecord) {
        PersonalityScoreModel.buildIntradayScoreSeries(
            messages = messages,
            dailyRecord = dailyRecord
        )
    }

    val timeLabels = remember(points) {
        points.map { formatTime(it.timestamp) }
    }
    val comparisonPoints = remember(compareMode, comparisonMessages, comparisonDailyRecord) {
        if (compareMode == DetailCompareMode.NONE || comparisonMessages.isEmpty()) {
            emptyList()
        } else {
            PersonalityScoreModel.buildIntradayScoreSeries(
                messages = comparisonMessages,
                dailyRecord = comparisonDailyRecord
            )
        }
    }
    var sharedSelectedIndex by remember(points) { mutableStateOf<Int?>(null) }
    val selectedTimeLabel = sharedSelectedIndex?.let { timeLabels.getOrNull(it) }
    val selectedMessagesAtTime = remember(sharedSelectedIndex, points, messages) {
        val selectedPoint = sharedSelectedIndex?.let { points.getOrNull(it) }
        if (selectedPoint == null) {
            emptyList()
        } else {
            messages
                .filter { formatTime(it.timestamp) == formatTime(selectedPoint.timestamp) }
                .sortedBy { it.timestamp }
        }
    }
    val comparisonLabel = remember(compareMode, comparisonDate, comparisonMessages) {
        when (compareMode) {
            DetailCompareMode.NONE -> null
            DetailCompareMode.PREVIOUS_DAY -> {
                comparisonDate?.let {
                    if (comparisonMessages.isNotEmpty()) {
                        "比較対象: ${it.monthValue}/${it.dayOfMonth}"
                    } else {
                        "先日データなし"
                    }
                } ?: "先日データなし"
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
    val swipeThresholdPx = with(LocalDensity.current) { 36.dp.toPx() }
    var accumulatedDrag by remember(date) { mutableStateOf(0f) }

    Card(
        modifier = modifier.fillMaxWidth(),
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
                    text = date?.let { "${it.monthValue}月${it.dayOfMonth}日 の日内推移" } ?: "日内推移",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onInfoClick) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "詳細グラフの説明"
                    )
                }
            }

            summaryScore?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StateBadge(
                        label = it.state.label,
                        state = it.state
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "メッセージ ${messages.size}件",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    selectedTimeLabel?.let { label ->
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactMetricChartCard(
                        title = "安定度",
                        values = points.map { it.stability },
                        comparisonValues = comparisonPoints.map { it.stability },
                        labels = timeLabels,
                        absoluteMin = 0f,
                        absoluteMax = 100f,
                        lineColor = StabilityChartColor,
                        selectedIndex = sharedSelectedIndex,
                        onSelectedIndexChange = { sharedSelectedIndex = it },
                        showSelectionLabel = false,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    CompactMetricChartCard(
                        title = "不安",
                        values = points.map { it.anxiety },
                        comparisonValues = comparisonPoints.map { it.anxiety },
                        labels = timeLabels,
                        absoluteMin = 0f,
                        absoluteMax = 10f,
                        lineColor = AnxietyChartColor,
                        selectedIndex = sharedSelectedIndex,
                        onSelectedIndexChange = { sharedSelectedIndex = it },
                        showSelectionLabel = false,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactMetricChartCard(
                        title = "活力",
                        values = points.map { it.energy },
                        comparisonValues = comparisonPoints.map { it.energy },
                        labels = timeLabels,
                        absoluteMin = 0f,
                        absoluteMax = 100f,
                        lineColor = EnergyChartColor,
                        selectedIndex = sharedSelectedIndex,
                        onSelectedIndexChange = { sharedSelectedIndex = it },
                        showSelectionLabel = false,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    CompactMetricChartCard(
                        title = "制御感",
                        values = points.map { it.control },
                        comparisonValues = comparisonPoints.map { it.control },
                        labels = timeLabels,
                        absoluteMin = 0f,
                        absoluteMax = 100f,
                        lineColor = ControlChartColor,
                        selectedIndex = sharedSelectedIndex,
                        onSelectedIndexChange = { sharedSelectedIndex = it },
                        showSelectionLabel = false,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }

                if (selectedMessagesAtTime.isNotEmpty()) {
                    SelectedMessagesAtTimeCard(
                        timeLabel = selectedTimeLabel.orEmpty(),
                        messages = selectedMessagesAtTime,
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
    modifier: Modifier = Modifier
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val comparisonColor = Color(0xFF9E9E9E)
    val density = LocalDensity.current

    var chartSize by remember { mutableStateOf(IntSize.Zero) }
    var internalSelectedIndex by remember(values, labels) { mutableStateOf<Int?>(null) }
    val activeSelectedIndex = selectedIndex ?: internalSelectedIndex

    val leftPadPx = with(density) { 6.dp.toPx() }
    val rightPadPx = with(density) { 6.dp.toPx() }
    val topPadPx = with(density) { 10.dp.toPx() }
    val bottomPadPx = with(density) { 10.dp.toPx() }

    Box(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .onSizeChanged { chartSize = it }
                .pointerInput(values, labels, chartSize) {
                    detectTapGestures { tapOffset ->
                        if (values.isEmpty()) return@detectTapGestures

                        val chartWidth = chartSize.width - leftPadPx - rightPadPx
                        if (chartWidth <= 0f) return@detectTapGestures

                        val clampedX = (tapOffset.x - leftPadPx).coerceIn(0f, chartWidth)
                        val rawIndex = if (values.size <= 1) {
                            0f
                        } else {
                            (clampedX / chartWidth) * values.lastIndex.toFloat()
                        }

                        val newIndex = rawIndex.roundToInt().coerceIn(0, values.lastIndex)
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

            fun buildPoints(targetValues: List<Float>): List<Offset> {
                val normalized = targetValues.map { value ->
                    val ratio = if (abs(maxValue - minValue) < 0.0001f) {
                        0.5f
                    } else {
                        ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
                    }
                    1f - ratio
                }

                return normalized.mapIndexed { index, yRatio ->
                    val x = if (targetValues.size == 1) {
                        leftPadPx + chartWidth / 2f
                    } else {
                        leftPadPx + chartWidth * (index.toFloat() / targetValues.lastIndex.toFloat())
                    }
                    val y = topPadPx + chartHeight * yRatio
                    Offset(x, y)
                }
            }

            fun drawSeries(points: List<Offset>, color: Color, strokeWidthDp: Int) {
                if (points.size >= 2) {
                    val path = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(
                            width = strokeWidthDp.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )
                }
            }

            val comparisonPointsOnChart = buildPoints(comparisonValues)
            if (comparisonPointsOnChart.isNotEmpty()) {
                drawSeries(
                    points = comparisonPointsOnChart,
                    color = comparisonColor.copy(alpha = 0.95f),
                    strokeWidthDp = 2
                )
            }

            val points = buildPoints(values)
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
private fun CompactChartLabelRow(
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val first = labels.firstOrNull().orEmpty()
    val last = labels.lastOrNull().orEmpty()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = first,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = last,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End
        )
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

private fun LocalDate.toShortLabel(): String {
    return format(DateTimeFormatter.ofPattern("M/d(E)", Locale.JAPAN))
}
