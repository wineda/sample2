package com.example.sample2.ui

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sample2.data.DailyRecord
import com.example.sample2.data.SleepData
import com.example.sample2.model.JournalJsonStorage
import com.example.sample2.ui.theme.AppShapeTokens
import com.example.sample2.ui.theme.SemanticColors
import com.example.sample2.ui.theme.appColors
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TokyoZone: ZoneId = ZoneId.of("Asia/Tokyo")
enum class SleepQuality { BAD, SLIGHTLY_BAD, NORMAL, GOOD, VERY_GOOD }

internal fun computeDurationMinutes(bed: LocalTime, wake: LocalTime): Int {
    val wakeMin = wake.hour * 60 + wake.minute
    val bedMin = bed.hour * 60 + bed.minute
    return if (bedMin <= wakeMin) wakeMin - bedMin else (24 * 60 - bedMin) + wakeMin
}

internal fun applyStepDelta(current: Int, delta: Int): Int = (current + delta).coerceIn(0, 999_999)

@Composable
fun DailyRecordScreen(onClose: () -> Unit, initialDate: String = todayDateString(), onOpenReflection: (String) -> Unit = {}) {
    val context = LocalContext.current
    BackHandler(onBack = onClose)

    val today = remember { LocalDate.now(TokyoZone) }
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.parse(initialDate)) }
    var bedTime by rememberSaveable { mutableStateOf<LocalTime?>(null) }
    var wakeTime by rememberSaveable { mutableStateOf<LocalTime?>(null) }
    var quality by rememberSaveable { mutableStateOf<SleepQuality?>(null) }
    var steps by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(selectedDate) {
        JournalJsonStorage.findDailyRecordOrNull(context, selectedDate.toString())?.let { r ->
            bedTime = r.sleep.startTimestamp?.let { Instant.ofEpochMilli(it).atZone(TokyoZone).toLocalTime() }
            wakeTime = r.sleep.endTimestamp?.let { Instant.ofEpochMilli(it).atZone(TokyoZone).toLocalTime() }
            quality = r.sleep.quality.toUiQuality()
            steps = r.steps
        } ?: run {
            bedTime = null; wakeTime = null; quality = null; steps = 0
        }
    }

    val duration = if (bedTime != null && wakeTime != null) computeDurationMinutes(bedTime!!, wakeTime!!) else null
    val enabled = bedTime != null || wakeTime != null || quality != null || steps > 0
    val filledCount = listOf(bedTime != null || wakeTime != null, quality != null, steps > 0).count { it }
    val dailyRecords = remember { JournalJsonStorage.readJournalData(context).dailyRecords }
    val recordedDates = remember(dailyRecords) { dailyRecords.mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }.toSet() }

    Scaffold(containerColor = MaterialTheme.colorScheme.background, contentWindowInsets = WindowInsets(0, 0, 0, 0), bottomBar = {
        FooterActions(onCancel = onClose, onSave = {
            val pair = buildSleepTimestamps(selectedDate, bedTime, wakeTime)
            JournalJsonStorage.upsertDailyRecord(context, DailyRecord(selectedDate.toString(), SleepData(duration ?: 0, quality.toModelQuality(), pair?.first, pair?.second), steps))
            Toast.makeText(context, "日次記録を保存しました", Toast.LENGTH_SHORT).show(); onClose()
        }, enabled = enabled)
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            JournalTopHeader(
                title = "日次記録",
                titleStyle = JournalHeaderTitleStyle.Medium,
                subtitle = selectedDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd(E)", Locale.JAPANESE)),
                navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack,
                navigationContentDescription = "戻る",
                onNavigationClick = onClose,
                trailing = { HeaderProgressStack(current = filledCount, total = 3, label = "FILLED") }
            )
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DateStepper(
                    selectedDate = selectedDate,
                    onDateChange = { selectedDate = it },
                    maxDate = today,
                    datesWithRecord = recordedDates,
                    quickOptions = listOf(
                        DateQuickOption("7日前") { LocalDate.now(TokyoZone).minusDays(7) },
                        DateQuickOption("昨日") { LocalDate.now(TokyoZone).minusDays(1) },
                        DateQuickOption("今日") { LocalDate.now(TokyoZone) }
                    )
                )
                TextButton(onClick = { onOpenReflection(selectedDate.toString()) }) { Text("振り返りを書く") }
                SleepCard(bedTime, wakeTime, quality, duration, { showTimePicker(context, bedTime) { bedTime = it } }, { showTimePicker(context, wakeTime) { wakeTime = it } }) { quality = if (quality == it) null else it }
                StepCard(steps, onSetSteps = { steps = it.coerceIn(0, 999_999) }, onDelta = { steps = applyStepDelta(steps, it) })
            }
        }
    }
}

@Composable private fun SleepCard(bed: LocalTime?, wake: LocalTime?, quality: SleepQuality?, duration: Int?, onBed: () -> Unit, onWake: () -> Unit, onQuality: (SleepQuality) -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.appColors.dividerCool)) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Bedtime, null); Spacer(Modifier.width(6.dp)); Text("睡眠", fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); StatusBadge(bed != null || wake != null || quality != null) }
        Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).background(Brush.linearGradient(listOf(MaterialTheme.appColors.sleepGradientStart, MaterialTheme.appColors.sleepGradientEnd))).padding(14.dp), verticalAlignment = Alignment.CenterVertically) { TimeCell("就寝", bed, Modifier.weight(1f), onBed); Text("→", color = MaterialTheme.appColors.inkTertiary); TimeCell("起床", wake, Modifier.weight(1f), onWake) }
        if (duration != null) { HorizontalDivider(color = MaterialTheme.appColors.dividerCool); Text("睡眠時間 ${formatDuration(duration)}") }
        Text("睡眠の質", color = MaterialTheme.appColors.inkSecondary, style = MaterialTheme.typography.bodySmall)
        val items = listOf("😣" to "悪い", "😕" to "やや悪", "😐" to "普通", "🙂" to "良い", "😄" to "とても")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { SleepQuality.entries.forEachIndexed { i, q -> QualityButton(items[i].first, items[i].second, quality == q, Modifier.weight(1f), { onQuality(q) }) } }
    } }
}
@Composable private fun TimeCell(label: String, time: LocalTime?, modifier: Modifier, onClick: () -> Unit) { Column(modifier.clickable(onClickLabel = "$label 時刻入力", onClick = onClick).padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(label, color = MaterialTheme.appColors.inkTertiary, style = MaterialTheme.typography.labelMedium); Text(time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "未設定", style = if (time == null) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.headlineLarge, color = if (time == null) MaterialTheme.appColors.inkTertiary else MaterialTheme.colorScheme.onSurface) } }
@Composable private fun QualityButton(emoji: String, label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) { Surface(onClick = onClick, modifier = modifier.height(72.dp), shape = MaterialTheme.shapes.small, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.appColors.dividerCool), contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.appColors.inkSecondary) { Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { Text(emoji); Text(label, style = MaterialTheme.typography.labelMedium) } } }

@Composable private fun StepCard(steps: Int, onSetSteps: (Int) -> Unit, onDelta: (Int) -> Unit) { Card(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.appColors.dividerCool)) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.DirectionsWalk, null); Spacer(Modifier.width(6.dp)); Text("歩数", fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); StatusBadge(steps > 0) }
    val progress = (steps.toFloat()/10000f).coerceIn(0f,1f)
    Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).background(Brush.linearGradient(listOf(MaterialTheme.appColors.sleepGradientStart, MaterialTheme.appColors.sleepGradientEnd))).padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) { Row(verticalAlignment = Alignment.Bottom) { Text(String.format("%,d", steps), style = MaterialTheme.typography.displayLarge.copy(letterSpacing = (-1).sp)); Text("歩", color = MaterialTheme.appColors.inkTertiary, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)) }
    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(4.dp), color = SemanticColors.PositiveMain, trackColor = MaterialTheme.appColors.dividerCool)
    Text(if (progress < 1f) "目標 10,000歩 まで あと ${String.format("%,d", 10_000-steps.coerceAtMost(10_000))}歩" else "目標達成！(${String.format("%,d", steps - 10_000)}歩)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.appColors.inkTertiary)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf(3000,5000,8000,10000).forEach { v -> Surface(onClick={onSetSteps(v)}, modifier=Modifier.weight(1f).height(46.dp), shape=MaterialTheme.shapes.small, color=if(steps==v) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface, border=androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.appColors.dividerCool), contentColor=if(steps==v) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface){Box(contentAlignment=Alignment.Center){Text(String.format("%,d",v), fontWeight=FontWeight.SemiBold)}} } }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { Text("微調整", color = MaterialTheme.appColors.inkTertiary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(32.dp)); listOf(-1000,-500,500,1000).forEach { d -> Surface(onClick={onDelta(d)}, modifier=Modifier.weight(1f).height(44.dp), shape=MaterialTheme.shapes.small, color=MaterialTheme.colorScheme.surface, border=androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.appColors.dividerCool)) { Box(contentAlignment=Alignment.Center){Text((if(d>0) "+" else "−") + String.format("%,d", kotlin.math.abs(d)), color=if(d<0) SemanticColors.NegativeMain else SemanticColors.PositiveMain, fontWeight=FontWeight.SemiBold)} } }
    }
} } }

@Composable fun StatusBadge(filled: Boolean) { val (bg, fg, label) = if (filled) Triple(SemanticColors.PositiveSoft, SemanticColors.PositiveMain, "入力済み") else Triple(MaterialTheme.appColors.surfaceCool, MaterialTheme.appColors.inkTertiary, "未入力"); Surface(shape = AppShapeTokens.Pill, color = bg) { Text(label, color = fg, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)) } }

@Composable fun FooterActions(onCancel: () -> Unit, onSave: () -> Unit, enabled: Boolean) { Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.appColors.dividerCool).padding(10.dp)) { Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) { TextButton(onClick = onCancel, modifier = Modifier.weight(0.35f).height(52.dp), colors = ButtonDefaults.textButtonColors(containerColor = MaterialTheme.appColors.surfaceCool, contentColor = MaterialTheme.appColors.inkSecondary)) { Text("キャンセル") }; Button(onClick = onSave, enabled = enabled, modifier = Modifier.weight(1f).height(52.dp)) { Text("保存") } } } }

private fun buildSleepTimestamps(date: LocalDate, bed: LocalTime?, wake: LocalTime?): Pair<Long, Long>? { if (bed == null || wake == null) return null; val wakeDate = date.atTime(wake).atZone(TokyoZone).toInstant().toEpochMilli(); val bedDate = (if (bed <= wake) date else date.minusDays(1)).atTime(bed).atZone(TokyoZone).toInstant().toEpochMilli(); return bedDate to wakeDate }
private fun SleepQuality?.toModelQuality(): Int = when (this) { SleepQuality.BAD -> 1; SleepQuality.SLIGHTLY_BAD -> 2; SleepQuality.NORMAL -> 3; SleepQuality.GOOD -> 4; SleepQuality.VERY_GOOD -> 5; null -> 0 }
private fun Int.toUiQuality(): SleepQuality? = when (this) { 1 -> SleepQuality.BAD; 2 -> SleepQuality.SLIGHTLY_BAD; 3 -> SleepQuality.NORMAL; 4 -> SleepQuality.GOOD; 5 -> SleepQuality.VERY_GOOD; else -> null }
private fun formatDuration(minutes: Int): String = if (minutes / 60 == 0) "${minutes % 60}分" else "${minutes / 60}時間${minutes % 60}分"
private fun todayDateString(): String = LocalDate.now(TokyoZone).toString()
private fun showTimePicker(context: android.content.Context, initial: LocalTime?, onSelected: (LocalTime) -> Unit) { TimePickerDialog(context, { _, h, m -> onSelected(LocalTime.of(h, m)) }, initial?.hour ?: 23, initial?.minute ?: 0, true).show() }
