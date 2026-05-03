package com.example.sample2.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sample2.analytics.PersonalityScoreModel
import com.example.sample2.analytics.PersonalityState
import com.example.sample2.data.DailyReflection
import com.example.sample2.ui.theme.AppShapeTokens
import com.example.sample2.ui.theme.MonoTypography
import com.example.sample2.ui.theme.SemanticColors
import com.example.sample2.ui.theme.appColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyReflectionScreen(state: JournalViewModel, initialDate: String = todayDateString(), onClose: () -> Unit, onSaved: () -> Unit = {}) {
    val context = LocalContext.current
    BackHandler { onClose() }
    var selectedDate by rememberSaveable { mutableStateOf(initialDate) }
    var wins by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var difficulties by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var insights by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var tomorrow by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var summary by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }

    val dailyRecords = remember(state.messages.size) { state.loadDailyRecords() }
    val currentReflection = remember(selectedDate, state.messages.size) { state.findDailyReflectionOrNull(selectedDate) }
    val hints = remember(selectedDate, state.messages.size, dailyRecords, currentReflection) {
        ReflectionHintBuilder.build(selectedDate, state.messages, dailyRecords, currentReflection)
    }
    val score = remember(selectedDate, state.messages.size, dailyRecords) {
        runCatching {
            PersonalityScoreModel.analyzeDay(
                date = java.time.LocalDate.parse(selectedDate),
                messages = state.messages.filter { formatDate(it.timestamp) == selectedDate },
                dailyRecord = dailyRecords.firstOrNull { it.date == selectedDate }
            )
        }.getOrNull()
    }

    LaunchedEffect(selectedDate) {
        val r = state.findDailyReflectionOrNull(selectedDate)
        wins = TextFieldValue(r?.wins.orEmpty())
        difficulties = TextFieldValue(r?.difficulties.orEmpty())
        insights = TextFieldValue(r?.insights.orEmpty())
        tomorrow = TextFieldValue(r?.tomorrowFirstAction.orEmpty())
        summary = TextFieldValue(r?.summary.orEmpty())
    }

    val fields = listOf(wins, difficulties, insights, tomorrow, summary)
    val filled = fields.count { it.text.isNotBlank() }
    val hasBreakdown = remember(selectedDate, state.messages.size) {
        state.messages.any { formatDate(it.timestamp) == selectedDate && it.flags.breakdown }
    }

    fun saveReflection() {
        state.upsertDailyReflection(DailyReflection(selectedDate, wins.text, difficulties.text, insights.text, tomorrow.text, summary.text, System.currentTimeMillis()))
        Toast.makeText(context, "振り返りを保存しました", Toast.LENGTH_SHORT).show()
        onSaved(); onClose()
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            SaveBar(onCancel = onClose, onSave = { saveReflection() })
        }
    ) { p ->
        Column(Modifier.fillMaxSize().padding(p)) {
            JournalTopHeader(
                title = "振り返り",
                titleStyle = JournalHeaderTitleStyle.Medium,
                subtitle = "入力 ・ ${formatDisplayDate(selectedDate)}",
                navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack,
                navigationContentDescription = "戻る",
                onNavigationClick = onClose,
                trailing = { HeaderProgressStack(current = filled, total = 5, label = "FILLED") }
            )
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummarySection(hints, score, state = score?.state, hasBreakdown = hasBreakdown)
                InputHead(filled)
                ReflectionItem("01", "うまくいったこと", "どんな小さなことでも。", wins, { wins = it }, false, null)
                ReflectionItem("02", "しんどかったこと", "しんどかった瞬間や場面を、思い出せる範囲で。", difficulties, { difficulties = it }, false, if (hasBreakdown) "体調不良フラグあり" else null)
                ReflectionItem("03", "気づき", "今日学んだこと、再発見したこと。", insights, { insights = it }, false, null)
                ReflectionItem("04", "明日まずやること", "ひとつだけ。具体的なアクションを。", tomorrow, { tomorrow = it }, true, null, hints.analysisSummaryText)
                ReflectionItem("05", "ひとことまとめ", "今日を一行で言うと。", summary, { summary = it }, false, null)
            }
        }
    }
}

@Composable
private fun SummarySection(h: ReflectionHints, s: com.example.sample2.analytics.DailyPersonalityScore?, state: PersonalityState?, hasBreakdown: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.appColors.dividerCool)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("今日の状態", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.appColors.inkPrimary)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ステータス", style = MonoTypography.Caption.copy(color = MaterialTheme.appColors.inkTertiary))
                StatusBadge(state)
            }
            Row(Modifier.fillMaxWidth().border(1.dp, MaterialTheme.appColors.dividerNeutral).background(MaterialTheme.appColors.surfaceMuted)) {
                MetricCell("STABLE", s?.stability?.toInt()?.toString() ?: "--", true)
                MetricCell("ANXIETY", s?.anxiety?.let { "%.1f".format(it) } ?: "--", true)
                MetricCell("ENERGY", s?.energy?.toInt()?.toString() ?: "--", true)
                MetricCell("CONTROL", s?.control?.toInt()?.toString() ?: "--", false)
            }
            Text("メッセージ  ${h.messageCountText.filter { it.isDigit() }}件", style = MonoTypography.Caption.copy(color = MaterialTheme.appColors.inkTertiary))
            Text("睡眠  ${h.dailyRecordText.substringBefore(" /")}", style = MonoTypography.Caption.copy(color = MaterialTheme.appColors.inkTertiary))
            Text("感情傾向  ${h.emotionTrendText.removePrefix("感情傾向: ")}", style = MonoTypography.Caption.copy(color = MaterialTheme.appColors.inkTertiary))
            Text("体調フラグ  ${if (hasBreakdown) "あり" else "なし"}", style = MonoTypography.Caption.copy(color = MaterialTheme.appColors.inkTertiary))
            Box(Modifier.fillMaxWidth().background(MaterialTheme.appColors.surfaceMuted).border(1.dp, MaterialTheme.appColors.dividerNeutral).padding(10.dp)) {
                Text(s?.summary ?: h.analysisSummaryText, style = MaterialTheme.typography.labelMedium.copy(lineHeight = 17.sp), color = MaterialTheme.appColors.inkSecondary)
            }
        }
    }
}

@Composable
private fun StatusBadge(state: PersonalityState?) {
    val (fg, bg) = when (state) {
        PersonalityState.STABLE -> SemanticColors.InfoMain to SemanticColors.InfoSoft
        PersonalityState.RECOVERING -> SemanticColors.WarningMain to SemanticColors.WarningSoft
        PersonalityState.TENSE -> SemanticColors.WarningMain to SemanticColors.WarningSoft
        PersonalityState.EXHAUSTED -> SemanticColors.NegativeMain to SemanticColors.NegativeSoft
        null -> MaterialTheme.appColors.inkTertiary to MaterialTheme.appColors.surfaceMuted
    }
    Row(Modifier.background(bg, AppShapeTokens.Tech).padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(6.dp).height(6.dp).background(fg, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(state?.label ?: "不明", color = fg, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}

@Composable
private fun RowScope.MetricCell(label: String, value: String, divider: Boolean) {
    Column(Modifier.weight(1f).then(if (divider) Modifier.border(0.5.dp, MaterialTheme.appColors.dividerNeutral) else Modifier).padding(8.dp)) {
        Text(label, style = MonoTypography.Micro.copy(color = MaterialTheme.appColors.inkTertiary))
        Text(value, fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.appColors.inkPrimary)
    }
}

@Composable
private fun InputHead(filled: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("入力項目", style = MonoTypography.Caption.copy(color = MaterialTheme.appColors.inkTertiary))
        Text("$filled/5 入力済み", style = MonoTypography.Caption.copy(color = MaterialTheme.appColors.inkTertiary))
    }
}

@Composable
private fun ReflectionItem(number: String, title: String, placeholder: String, value: TextFieldValue, onValue: (TextFieldValue) -> Unit, priority: Boolean, sub: String?, hint: String? = null) {
    val done = value.text.isNotBlank()
    var focused by remember { mutableStateOf(false) }
    val border by animateColorAsState(
        if (focused) SemanticColors.InfoMain else MaterialTheme.appColors.dividerNeutral,
        label = ""
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.appColors.dividerCool)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(number, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = MaterialTheme.appColors.inkTertiary, modifier = Modifier.widthIn(min = 24.dp))
                Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                val label = if (done) "入力済" else if (priority) "優先" else "未入力"
                val lBg = if (done) SemanticColors.InfoSoft else if (priority) SemanticColors.WarningSoft else MaterialTheme.appColors.surfaceMuted
                val lColor = if (done) SemanticColors.InfoMain else if (priority) SemanticColors.WarningMain else MaterialTheme.appColors.inkTertiary
                Text(label, style = MonoTypography.Micro.copy(color = lColor), modifier = Modifier.background(lBg, AppShapeTokens.Tech).padding(6.dp, 2.dp))
            }
            if (priority && !done && hint != null) {
                Text(
                    "ANALYSIS: $hint",
                    style = MonoTypography.Body.copy(color = MaterialTheme.appColors.inkSecondary),
                    modifier = Modifier.fillMaxWidth().background(SemanticColors.WarningSoft).padding(8.dp)
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValue,
                minLines = 2,
                textStyle = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.appColors.inkPrimary, lineHeight = 22.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (done || focused) MaterialTheme.colorScheme.surface else MaterialTheme.appColors.surfaceMuted, AppShapeTokens.Tech)
                    .border(1.dp, border, AppShapeTokens.Tech)
                    .padding(10.dp, 8.dp),
                decorationBox = { inner ->
                    if (value.text.isBlank()) Text(placeholder, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.appColors.inkDisabled)
                    inner()
                }
            )
            if (!sub.isNullOrBlank()) {
                Text(sub, fontSize = 10.sp, color = MaterialTheme.appColors.inkTertiary, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
private fun SaveBar(onCancel: () -> Unit, onSave: () -> Unit) {
    val dividerColor = MaterialTheme.appColors.dividerCool
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = dividerColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(10.dp)
            .imePadding()
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.weight(0.35f).height(52.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.appColors.surfaceCool,
                    contentColor = MaterialTheme.appColors.inkSecondary
                )
            ) {
                Text("キャンセル")
            }
            Button(onClick = onSave, modifier = Modifier.weight(1f).height(52.dp)) {
                Text("保存")
            }
        }
    }
}

private fun todayDateString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date())
private fun formatDisplayDate(date: String): String = runCatching { SimpleDateFormat("yyyy/MM/dd (E)", Locale.JAPAN).format(SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).parse(date) ?: Date()) }.getOrDefault(date)
private fun formatDate(ts: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date(ts))
