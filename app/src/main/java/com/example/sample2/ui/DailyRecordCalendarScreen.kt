package com.example.sample2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sample2.data.DailyRecord
import com.example.sample2.data.DailyReflection
import com.example.sample2.ui.theme.MonoTypography
import com.example.sample2.ui.theme.appColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

private val SleepColor = Color(0xFF6366F1)
private val StepsColor = Color(0xFF43A047)
private val ReflectionColor = Color(0xFFFB8C00)
private val SaturdayColor = Color(0xFF1976D2)
private val SundayColor = Color(0xFFC62828)

/**
 * 日次記録のメイン画面。
 * 月カレンダーで全体を俯瞰し、マスをタップして詳細ダイアログを開く。
 *
 * 各マスには以下を表示:
 *  - Bedtime: 睡眠時間（h、小数1桁、未入力は "-"）
 *  - DirectionsWalk: 歩数（4桁未満は生値、4桁以上は k 表記、未入力は "-"）
 *  - HistoryEdu: 振り返り項目数（0-5、未入力は "-"）
 */
@Composable
fun DailyRecordCalendarScreen(
    records: List<DailyRecord>,
    reflections: List<DailyReflection>,
    onMenuClick: () -> Unit,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    var visibleMonth by rememberSaveable(stateSaver = YearMonthSaver) {
        mutableStateOf(YearMonth.now())
    }

    val recordMap = remember(records) {
        records.mapNotNull { record ->
            runCatching { LocalDate.parse(record.date) }.getOrNull()?.let { it to record }
        }.toMap()
    }
    val reflectionMap = remember(reflections) {
        reflections.mapNotNull { reflection ->
            runCatching { LocalDate.parse(reflection.date) }.getOrNull()?.let { it to reflection }
        }.toMap()
    }

    val monthRecordCount = remember(visibleMonth, recordMap, reflectionMap) {
        var count = 0
        var date = visibleMonth.atDay(1)
        val end = visibleMonth.atEndOfMonth()
        while (!date.isAfter(end)) {
            if (recordMap.containsKey(date) || reflectionMap.containsKey(date)) count++
            date = date.plusDays(1)
        }
        count
    }
    val monthDayCount = visibleMonth.lengthOfMonth()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DailyRecordTopBar(onMenuClick = onMenuClick)
        MonthNav(
            month = visibleMonth,
            monthRecordCount = monthRecordCount,
            monthDayCount = monthDayCount,
            canGoNext = visibleMonth.isBefore(YearMonth.from(today)),
            onPrev = { visibleMonth = visibleMonth.minusMonths(1) },
            onNext = { visibleMonth = visibleMonth.plusMonths(1) }
        )
        MonthCalendar(
            month = visibleMonth,
            today = today,
            recordMap = recordMap,
            reflectionMap = reflectionMap,
            onDayClick = onDayClick,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .weight(1f, fill = false)
        )
        Legend(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun DailyRecordTopBar(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompactHeaderIconButton(
            selected = false,
            onClick = onMenuClick,
            icon = Icons.Outlined.Menu,
            contentDescription = "メニュー"
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "日次記録",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.appColors.inkPrimary,
            modifier = Modifier.weight(1f)
        )
        CompactHeaderIconButton(
            selected = false,
            onClick = { /* 検索は今後の拡張枠 */ },
            icon = Icons.Outlined.Search,
            contentDescription = "検索（準備中）"
        )
    }
}

@Composable
private fun MonthNav(
    month: YearMonth,
    monthRecordCount: Int,
    monthDayCount: Int,
    canGoNext: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        MonthNavButton(
            icon = Icons.Outlined.ChevronLeft,
            contentDescription = "前の月",
            onClick = onPrev
        )
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${month.year}年 ${month.monthValue}月",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.inkPrimary
            )
            Text(
                text = "$monthRecordCount / $monthDayCount 日 記録",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.appColors.inkTertiary,
                    letterSpacing = 0.5.sp
                )
            )
        }
        Spacer(Modifier.width(8.dp))
        if (canGoNext) {
            MonthNavButton(
                icon = Icons.Outlined.ChevronRight,
                contentDescription = "次の月",
                onClick = onNext
            )
        } else {
            Spacer(Modifier.size(28.dp))
        }
    }
}

@Composable
private fun MonthNavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.appColors.inkSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun MonthCalendar(
    month: YearMonth,
    today: LocalDate,
    recordMap: Map<LocalDate, DailyRecord>,
    reflectionMap: Map<LocalDate, DailyReflection>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstOfMonth = month.atDay(1)
    val lastOfMonth = month.atEndOfMonth()
    val leadingBlank = (firstOfMonth.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val totalCells = leadingBlank + lastOfMonth.dayOfMonth
    val rowCount = (totalCells + 6) / 7

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.appColors.dividerSoft, RoundedCornerShape(14.dp))
            .padding(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            val dows = listOf(
                "月" to MaterialTheme.appColors.inkSecondary,
                "火" to MaterialTheme.appColors.inkSecondary,
                "水" to MaterialTheme.appColors.inkSecondary,
                "木" to MaterialTheme.appColors.inkSecondary,
                "金" to MaterialTheme.appColors.inkSecondary,
                "土" to SaturdayColor,
                "日" to SundayColor
            )
            dows.forEach { (label, color) ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = color,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        HorizontalDivider(
            color = MaterialTheme.appColors.dividerSoft,
            thickness = 1.dp
        )

        for (row in 0 until rowCount) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayOfMonth = cellIndex - leadingBlank + 1
                    if (dayOfMonth in 1..lastOfMonth.dayOfMonth) {
                        val date = month.atDay(dayOfMonth)
                        DayCell(
                            date = date,
                            today = today,
                            record = recordMap[date],
                            reflection = reflectionMap[date],
                            onClick = { if (!date.isAfter(today)) onDayClick(date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    today: LocalDate,
    record: DailyRecord?,
    reflection: DailyReflection?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isToday = date == today
    val isFuture = date.isAfter(today)
    val numColor = when {
        isToday -> Color.White
        isFuture -> MaterialTheme.appColors.inkTertiary
        date.dayOfWeek == DayOfWeek.SATURDAY -> SaturdayColor
        date.dayOfWeek == DayOfWeek.SUNDAY -> SundayColor
        else -> MaterialTheme.appColors.inkPrimary
    }

    Column(
        modifier = modifier
            .padding(1.dp)
            .clip(RoundedCornerShape(6.dp))
            .then(if (!isFuture) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isToday) MaterialTheme.appColors.inkPrimary else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = numColor,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        Spacer(Modifier.height(3.dp))

        if (!isFuture) {
            DayValueRow(
                icon = Icons.Outlined.Bedtime,
                value = formatSleepHours(record),
                color = SleepColor
            )
            DayValueRow(
                icon = Icons.Outlined.DirectionsWalk,
                value = formatSteps(record),
                color = StepsColor
            )
            DayValueRow(
                icon = Icons.Default.HistoryEdu,
                value = formatReflectionCount(reflection),
                color = ReflectionColor
            )
        }
    }
}

@Composable
private fun DayValueRow(icon: ImageVector, value: String, color: Color) {
    val isEmpty = value == "-"
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(10.dp)
                .alpha(if (isEmpty) 0.5f else 1f),
            tint = if (isEmpty) MaterialTheme.appColors.inkTertiary else color
        )
        Spacer(Modifier.width(2.dp))
        Text(
            text = value,
            style = MonoTypography.Micro.copy(
                color = if (isEmpty) MaterialTheme.appColors.inkTertiary else color,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun Legend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
            .border(1.dp, MaterialTheme.appColors.dividerSoft, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(icon = Icons.Outlined.Bedtime, label = "睡眠 (h)", color = SleepColor)
        LegendItem(icon = Icons.Outlined.DirectionsWalk, label = "歩数", color = StepsColor)
        LegendItem(icon = Icons.Default.HistoryEdu, label = "振り返り", color = ReflectionColor)
    }
}

@Composable
private fun LegendItem(icon: ImageVector, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

private fun formatSleepHours(record: DailyRecord?): String {
    val mins = record?.sleep?.durationMinutes ?: 0
    if (mins <= 0) return "-"
    val hours = mins / 60.0
    return String.format(Locale.US, "%.1f", hours)
}

private fun formatSteps(record: DailyRecord?): String {
    val steps = record?.steps ?: 0
    return when {
        steps <= 0 -> "-"
        steps < 1000 -> steps.toString()
        steps < 10_000 -> String.format(Locale.US, "%.1fk", steps / 1000.0)
        else -> "${steps / 1000}k"
    }
}

private fun formatReflectionCount(reflection: DailyReflection?): String {
    if (reflection == null) return "-"
    val count = listOf(
        reflection.wins,
        reflection.difficulties,
        reflection.insights,
        reflection.summary,
        reflection.tomorrowFirstAction
    ).count { it.isNotBlank() }
    return if (count == 0) "-" else count.toString()
}

private val YearMonthSaver = Saver<YearMonth, String>(
    save = { it.toString() },
    restore = { YearMonth.parse(it) }
)
