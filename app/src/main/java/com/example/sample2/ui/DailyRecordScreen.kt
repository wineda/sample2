package com.example.sample2.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sample2.data.DailyRecord
import com.example.sample2.data.SleepData
import com.example.sample2.model.JournalJsonStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyRecordScreen(
    onClose: () -> Unit,
    initialDate: String = todayDateString(),
    onOpenReflection: (String) -> Unit = {}
) {
    val context = LocalContext.current

    BackHandler {
        onClose()
    }

    var selectedDate by rememberSaveable { mutableStateOf(initialDate) }
    var sleepStartMinutes by rememberSaveable { mutableStateOf<Int?>(null) }
    var sleepEndMinutes by rememberSaveable { mutableStateOf<Int?>(null) }
    var sleepQuality by rememberSaveable { mutableIntStateOf(0) }
    var stepsText by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(selectedDate) {
        val record = JournalJsonStorage.findDailyRecordOrNull(context, selectedDate)
        if (record != null) {
            sleepStartMinutes = record.sleep.startTimestamp?.let(::extractMinutesOfDay)
            sleepEndMinutes = record.sleep.endTimestamp?.let(::extractMinutesOfDay)
            sleepQuality = record.sleep.quality
            stepsText = if (record.steps > 0) record.steps.toString() else ""
        } else {
            sleepStartMinutes = null
            sleepEndMinutes = null
            sleepQuality = 0
            stepsText = ""
        }
    }

    val sleepDurationMinutes = calculateSleepDurationMinutes(
        startMinutes = sleepStartMinutes,
        endMinutes = sleepEndMinutes
    )

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onClose,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("キャンセル")
                    }

                    Button(
                        onClick = {
                            val steps = stepsText.toIntOrNull()?.coerceAtLeast(0) ?: 0
                            val sleepTimestamps = buildSleepTimestamps(
                                date = selectedDate,
                                startMinutes = sleepStartMinutes,
                                endMinutes = sleepEndMinutes
                            )

                            val record = DailyRecord(
                                date = selectedDate,
                                sleep = SleepData(
                                    durationMinutes = sleepDurationMinutes ?: 0,
                                    quality = sleepQuality,
                                    startTimestamp = sleepTimestamps?.first,
                                    endTimestamp = sleepTimestamps?.second
                                ),
                                steps = steps
                            )

                            JournalJsonStorage.upsertDailyRecord(context, record)
                            Toast.makeText(context, "日次記録を保存しました", Toast.LENGTH_SHORT).show()
                            onClose()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DateHeaderCard(
                date = selectedDate,
                onPrev = { selectedDate = shiftDate(selectedDate, -1) },
                onNext = { selectedDate = shiftDate(selectedDate, 1) },
                onToday = { selectedDate = todayDateString() },
                onOpenReflection = { onOpenReflection(selectedDate) },
                onPickDate = {
                    showDatePicker(
                        context = context,
                        contextDate = selectedDate,
                        onDateSelected = { selectedDate = it }
                    )
                }
            )

            SleepCard(
                startMinutes = sleepStartMinutes,
                endMinutes = sleepEndMinutes,
                durationMinutes = sleepDurationMinutes,
                quality = sleepQuality,
                onStartClick = {
                    showTimePicker(
                        context = context,
                        initialMinutes = sleepStartMinutes,
                        onSelected = { sleepStartMinutes = it }
                    )
                },
                onEndClick = {
                    showTimePicker(
                        context = context,
                        initialMinutes = sleepEndMinutes,
                        onSelected = { sleepEndMinutes = it }
                    )
                },
                onQualitySelected = { sleepQuality = it }
            )

            StepsCard(
                stepsText = stepsText,
                onStepsChange = { value ->
                    stepsText = value.filter { it.isDigit() }
                },
                onPresetClick = { preset ->
                    stepsText = preset.toString()
                },
                onAddClick = { add ->
                    val current = stepsText.toIntOrNull() ?: 0
                    stepsText = (current + add).toString()
                }
            )

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun DateHeaderCard(
    date: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onOpenReflection: () -> Unit,
    onPickDate: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniNavButton(
                    icon = Icons.Default.ChevronLeft,
                    contentDescription = "prev",
                    onClick = onPrev
                )

                Surface(
                    onClick = onPickDate,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatDisplayDate(date),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                MiniNavButton(
                    icon = Icons.Default.ChevronRight,
                    contentDescription = "next",
                    onClick = onNext
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = false,
                    onClick = onToday,
                    label = { Text("今日") },
                    leadingIcon = {
                        Icon(Icons.Default.Today, contentDescription = null)
                    },
                    colors = actionChipColors()
                )

                FilterChip(
                    selected = false,
                    onClick = onOpenReflection,
                    label = { Text("振り返り") },
                    colors = actionChipColors()
                )
            }
        }
    }
}

@Composable
private fun MiniNavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(42.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription)
        }
    }
}

@Composable
private fun SleepCard(
    startMinutes: Int?,
    endMinutes: Int?,
    durationMinutes: Int?,
    quality: Int,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit,
    onQualitySelected: (Int) -> Unit
) {
    val qualityLabels = remember {
        listOf("未設定", "悪い", "やや悪い", "普通", "良い", "かなり良い")
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "睡眠",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = formatDuration(durationMinutes),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "起床日ベースで保存。就寝が起床より遅い場合は前日就寝として扱います。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TimeFieldButton(
                    label = "就寝",
                    timeText = minutesToTimeText(startMinutes),
                    modifier = Modifier.weight(1f),
                    onClick = onStartClick
                )
                TimeFieldButton(
                    label = "起床",
                    timeText = minutesToTimeText(endMinutes),
                    modifier = Modifier.weight(1f),
                    onClick = onEndClick
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "睡眠の質",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    qualityLabels.forEachIndexed { index, label ->
                        FilterChip(
                            selected = quality == index,
                            onClick = { onQualitySelected(index) },
                            label = { Text(label) },
                            colors = selectionChipColors()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeFieldButton(
    label: String,
    timeText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = timeText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun StepsCard(
    stepsText: String,
    onStepsChange: (String) -> Unit,
    onPresetClick: (Int) -> Unit,
    onAddClick: (Int) -> Unit
) {
    val presets = remember { listOf(3000, 5000, 8000, 10000) }
    val addValues = remember { listOf(1000, 3000, 5000) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "歩数",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = stepsText,
                onValueChange = onStepsChange,
                label = { Text("歩数") },
                suffix = { Text("歩") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "よく使う値",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { preset ->
                        FilterChip(
                            selected = false,
                            onClick = { onPresetClick(preset) },
                            label = { Text("${preset}歩") },
                            colors = actionChipColors()
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "加算",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    addValues.forEach { add ->
                        FilterChip(
                            selected = false,
                            onClick = { onAddClick(add) },
                            label = { Text("+${add}") },
                            colors = actionChipColors()
                        )
                    }
                }
            }
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

@Composable
private fun actionChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.secondaryContainer,
    labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
)

private fun todayDateString(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date())
}

private fun formatDisplayDate(date: String): String {
    val cal = parseDate(date)
    return SimpleDateFormat("yyyy/MM/dd (E)", Locale.JAPAN).format(cal.time)
}

private fun shiftDate(date: String, days: Int): String {
    val cal = parseDate(date)
    cal.add(Calendar.DAY_OF_MONTH, days)
    return SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(cal.time)
}

private fun parseDate(date: String): Calendar {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN)
    val parsed = sdf.parse(date) ?: Date()
    return Calendar.getInstance().apply {
        time = parsed
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}

private fun showDatePicker(
    context: android.content.Context,
    contextDate: String,
    onDateSelected: (String) -> Unit
) {
    val cal = parseDate(contextDate)
    val dialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selected = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onDateSelected(
                SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(selected.time)
            )
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    )
    dialog.show()
}

private fun showTimePicker(
    context: android.content.Context,
    initialMinutes: Int?,
    onSelected: (Int) -> Unit
) {
    val hour = initialMinutes?.div(60) ?: 23
    val minute = initialMinutes?.rem(60) ?: 0
    val dialog = TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            onSelected(selectedHour * 60 + selectedMinute)
        },
        hour,
        minute,
        true
    )
    dialog.show()
}

private fun minutesToTimeText(minutes: Int?): String {
    if (minutes == null) return "未設定"
    val hour = minutes / 60
    val minute = minutes % 60
    return "%02d:%02d".format(hour, minute)
}

private fun calculateSleepDurationMinutes(
    startMinutes: Int?,
    endMinutes: Int?
): Int? {
    if (startMinutes == null || endMinutes == null) return null
    return if (endMinutes >= startMinutes) {
        endMinutes - startMinutes
    } else {
        24 * 60 - startMinutes + endMinutes
    }
}

private fun formatDuration(durationMinutes: Int?): String {
    if (durationMinutes == null) return "未設定"
    val hour = durationMinutes / 60
    val minute = durationMinutes % 60
    return "${hour}時間${minute}分"
}

private fun extractMinutesOfDay(timestamp: Long): Int {
    val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
}

private fun buildSleepTimestamps(
    date: String,
    startMinutes: Int?,
    endMinutes: Int?
): Pair<Long, Long>? {
    if (startMinutes == null || endMinutes == null) return null

    val endCal = parseDate(date).apply {
        set(Calendar.HOUR_OF_DAY, endMinutes / 60)
        set(Calendar.MINUTE, endMinutes % 60)
    }

    val startCal = parseDate(date).apply {
        if (startMinutes > endMinutes) {
            add(Calendar.DAY_OF_MONTH, -1)
        }
        set(Calendar.HOUR_OF_DAY, startMinutes / 60)
        set(Calendar.MINUTE, startMinutes % 60)
    }

    return startCal.timeInMillis to endCal.timeInMillis
}
