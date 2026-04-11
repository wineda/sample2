package com.example.sample2.ui

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.sample2.data.DailyReflection
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReflectionScreen(
    state: JournalViewModel,
    initialDate: String = todayDateString(),
    onClose: () -> Unit
) {
    val context = LocalContext.current

    BackHandler { onClose() }

    var selectedDate by rememberSaveable { mutableStateOf(initialDate) }
    var wins by rememberSaveable { mutableStateOf("") }
    var difficulties by rememberSaveable { mutableStateOf("") }
    var insights by rememberSaveable { mutableStateOf("") }
    var tomorrowFirstAction by rememberSaveable { mutableStateOf("") }
    var summary by rememberSaveable { mutableStateOf("") }

    val dailyRecords = remember(state.messages.size) { state.loadDailyRecords() }
    val currentReflection = remember(selectedDate, state.messages.size) {
        state.findDailyReflectionOrNull(selectedDate)
    }

    LaunchedEffect(selectedDate) {
        val reflection = state.findDailyReflectionOrNull(selectedDate)
        wins = reflection?.wins.orEmpty()
        difficulties = reflection?.difficulties.orEmpty()
        insights = reflection?.insights.orEmpty()
        tomorrowFirstAction = reflection?.tomorrowFirstAction.orEmpty()
        summary = reflection?.summary.orEmpty()
    }

    val hints = remember(selectedDate, state.messages.size, dailyRecords, currentReflection) {
        ReflectionHintBuilder.build(
            date = selectedDate,
            allMessages = state.messages,
            allDailyRecords = dailyRecords,
            reflection = currentReflection
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
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
                        Spacer(modifier = Modifier.padding(2.dp))
                        Text("閉じる")
                    }

                    Button(
                        onClick = {
                            state.upsertDailyReflection(
                                DailyReflection(
                                    date = selectedDate,
                                    wins = wins,
                                    difficulties = difficulties,
                                    insights = insights,
                                    tomorrowFirstAction = tomorrowFirstAction,
                                    summary = summary,
                                    updatedAt = System.currentTimeMillis()
                                )
                            )
                            Toast.makeText(context, "振り返りを保存しました", Toast.LENGTH_SHORT).show()
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
            ReflectionDateCard(
                date = selectedDate,
                onPickDate = {
                    showDatePicker(context, selectedDate) { selectedDate = it }
                }
            )

            ReflectionHintsCard(hints = hints)

            ReflectionInputCard(
                wins = wins,
                onWinsChange = { wins = it },
                difficulties = difficulties,
                onDifficultiesChange = { difficulties = it },
                insights = insights,
                onInsightsChange = { insights = it },
                tomorrowFirstAction = tomorrowFirstAction,
                onTomorrowFirstActionChange = { tomorrowFirstAction = it },
                summary = summary,
                onSummaryChange = { summary = it }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ReflectionDateCard(
    date: String,
    onPickDate: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Surface(
            onClick = onPickDate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null)
                Spacer(modifier = Modifier.padding(4.dp))
                Text(
                    text = "${formatDisplayDate(date)} の振り返り",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ReflectionHintsCard(hints: ReflectionHints) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("ヒント", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(hints.messageCountText)
            Text(hints.emotionTrendText)
            Text(hints.actionFlagsText)
            Text(hints.dailyRecordText)
            Text(
                hints.analysisSummaryText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReflectionInputCard(
    wins: String,
    onWinsChange: (String) -> Unit,
    difficulties: String,
    onDifficultiesChange: (String) -> Unit,
    insights: String,
    onInsightsChange: (String) -> Unit,
    tomorrowFirstAction: String,
    onTomorrowFirstActionChange: (String) -> Unit,
    summary: String,
    onSummaryChange: (String) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("入力", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            ReflectionField("今日うまくいったこと", wins, onWinsChange)
            ReflectionField("今日しんどかったこと", difficulties, onDifficultiesChange)
            ReflectionField("今日の気づき", insights, onInsightsChange)
            ReflectionField("明日まずやること", tomorrowFirstAction, onTomorrowFirstActionChange)
            ReflectionField("ひとことまとめ", summary, onSummaryChange)
        }
    }
}

@Composable
private fun ReflectionField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2
    )
}

private fun showDatePicker(
    context: android.content.Context,
    contextDate: String,
    onDateSelected: (String) -> Unit
) {
    val cal = parseDate(contextDate)
    DatePickerDialog(
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
            onDateSelected(SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(selected.time))
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    ).show()
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

private fun todayDateString(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date())
}

private fun formatDisplayDate(date: String): String {
    val cal = parseDate(date)
    return SimpleDateFormat("yyyy/MM/dd (E)", Locale.JAPAN).format(cal.time)
}
