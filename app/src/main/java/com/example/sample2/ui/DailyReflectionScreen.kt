package com.example.sample2.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sample2.analytics.PersonalityScoreModel
import com.example.sample2.analytics.PersonalityState
import com.example.sample2.data.ActionFlags
import com.example.sample2.data.DailyReflection
import com.example.sample2.ui.theme.AppColors
import com.example.sample2.ui.theme.SemanticColors
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
    val progress = filled / 5f
    val hasBreakdown = remember(selectedDate, state.messages.size) {
        state.messages.any { formatDate(it.timestamp) == selectedDate && it.flags.breakdown }
    }

    fun saveDraft() {
        state.upsertDailyReflection(DailyReflection(selectedDate, wins.text, difficulties.text, insights.text, tomorrow.text, summary.text, System.currentTimeMillis()))
        Toast.makeText(context, "振り返りを保存しました", Toast.LENGTH_SHORT).show()
        onSaved(); onClose()
    }

    Scaffold(contentWindowInsets = WindowInsets.safeDrawing, containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { SaveBar(progress = progress, filled = filled, onDraft = { saveDraft() }, onSave = { saveDraft() }) }) { p ->
        Column(Modifier.fillMaxSize().padding(p)) {
            Header(date = selectedDate, filled = filled)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
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

@Composable private fun Header(date: String, filled: Int) { Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).border(1.dp, AppColors.DividerNeutral).padding(12.dp,10.dp), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("DAILY REFLECTION", style = MonoTypography.Caption.copy(color = AppColors.InkTertiary, letterSpacing = 1.sp)); Text(formatDisplayDate(date), style = MaterialTheme.typography.titleMedium, color = AppColors.InkPrimary) }; Row(verticalAlignment = Alignment.Bottom) { Text("$filled", style = MonoTypography.Numeric.copy(fontWeight = FontWeight.SemiBold, color = AppColors.InkPrimary)); Text(" / 5", fontFamily = FontFamily.Monospace, color = AppColors.InkSecondary) } } }

@Composable private fun SummarySection(h: ReflectionHints, s: com.example.sample2.analytics.DailyPersonalityScore?, state: PersonalityState?, hasBreakdown: Boolean) { Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).border(1.dp,AppColors.DividerNeutral).padding(16.dp,12.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("STATUS", style = MonoTypography.Caption.copy(color = AppColors.InkTertiary)); StatusBadge(state) }; Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth().border(1.dp,AppColors.DividerNeutral).background(AppColors.SurfaceMuted)) { MetricCell("STABLE", s?.stability?.toInt()?.toString() ?: "--", true); MetricCell("ANXIETY", s?.anxiety?.let { "%.1f".format(it) } ?: "--", true); MetricCell("ENERGY", s?.energy?.toInt()?.toString() ?: "--", true); MetricCell("CONTROL", s?.control?.toInt()?.toString() ?: "--", false) }; Spacer(Modifier.height(8.dp)); Text("MSG  ${h.messageCountText.filter { it.isDigit() }}件", style = MonoTypography.Caption.copy(color = AppColors.InkTertiary)); Text("SLEEP  ${h.dailyRecordText.substringBefore(" /").replace("睡眠 ","").replace("時間","h ").replace("分","m")}", style = MonoTypography.Caption.copy(color = AppColors.InkTertiary)); Text("EMOTION  ${h.emotionTrendText.removePrefix("感情傾向: ").replace(" が多め", "+")}", style = MonoTypography.Caption.copy(color = AppColors.InkTertiary)); Text("FLAGS  ${if (hasBreakdown) "体×1" else "--"}", style = MonoTypography.Caption.copy(color = AppColors.InkTertiary)); Spacer(Modifier.height(8.dp)); Box(Modifier.fillMaxWidth().background(AppColors.SurfaceMuted).border(1.dp, AppColors.DividerNeutral).padding(10.dp)) { Text(s?.summary ?: h.analysisSummaryText, style = MaterialTheme.typography.labelMedium.copy(lineHeight = 17.sp), color = AppColors.InkSecondary) } } }
@Composable private fun StatusBadge(state: PersonalityState?) { val (fg,bg) = when(state){PersonalityState.STABLE->SemanticColors.InfoMain to SemanticColors.InfoSoft; PersonalityState.RECOVERING->SemanticColors.WarningMain to SemanticColors.WarningSoft; PersonalityState.TENSE->SemanticColors.WarningMain to SemanticColors.WarningSoft; PersonalityState.EXHAUSTED->SemanticColors.NegativeMain to SemanticColors.NegativeSoft; null->AppColors.InkTertiary to AppColors.SurfaceMuted}; Row(Modifier.background(bg, AppShapeTokens.Tech).padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.width(6.dp).height(6.dp).background(fg, CircleShape)); Spacer(Modifier.width(6.dp)); Text(state?.label ?: "不明", color = fg, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)) } }
@Composable private fun RowScope.MetricCell(label:String,value:String,divider:Boolean){ Column(Modifier.weight(1f).then(if(divider) Modifier.border(0.5.dp,AppColors.DividerNeutral) else Modifier).padding(8.dp)) { Text(label,style=MonoTypography.Micro.copy(color=AppColors.InkTertiary)); Text(value,fontFamily=FontFamily.Monospace,fontSize=18.sp,fontWeight=FontWeight.Medium,color=AppColors.InkPrimary) } }

@Composable private fun InputHead(filled:Int){ Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(16.dp,14.dp,16.dp,6.dp), horizontalArrangement = Arrangement.SpaceBetween){ Text("INPUT — 5 ITEMS", style = MonoTypography.Caption.copy(color = AppColors.InkTertiary)); Text("$filled OF 5 FILLED", style = MonoTypography.Caption.copy(color = AppColors.InkTertiary)) } }

@Composable private fun ReflectionItem(number:String,title:String,placeholder:String,value:TextFieldValue,onValue:(TextFieldValue)->Unit,priority:Boolean,sub:String?,hint:String?=null){ val done=value.text.isNotBlank(); var focused by remember { mutableStateOf(false) }; val border by animateColorAsState(if (focused) SemanticColors.InfoMain else if (done) AppColors.DividerMid else AppColors.DividerNeutral, label = ""); Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(16.dp,12.dp).border(1.dp,AppColors.DividerNeutral)) { Row(verticalAlignment = Alignment.CenterVertically) { Text(number,fontFamily=FontFamily.Monospace,fontSize=10.sp,color=AppColors.InkTertiary,modifier=Modifier.widthIn(min=24.dp)); Text(title,modifier=Modifier.weight(1f),style=MaterialTheme.typography.titleSmall); val label=if(done)"DONE" else if(priority)"PRIORITY" else "EMPTY"; val lBg=if(done) SemanticColors.InfoSoft else if(priority) SemanticColors.WarningSoft else AppColors.SurfaceMuted; val lColor=if(done) SemanticColors.InfoMain else if(priority) SemanticColors.WarningMain else AppColors.InkTertiary; Text(label,style=MonoTypography.Micro.copy(color=lColor),modifier=Modifier.background(lBg,AppShapeTokens.Tech).padding(6.dp,2.dp)) }
        if (priority && !done && hint != null) Text("ANALYSIS: $hint", style = MonoTypography.Body.copy(color = AppColors.InkSecondary), modifier = Modifier.fillMaxWidth().padding(top=8.dp).background(SemanticColors.WarningSoft).padding(8.dp))
        BasicTextField(value=value,onValueChange=onValue,textStyle=MaterialTheme.typography.titleSmall.copy(color=AppColors.InkPrimary, lineHeight=22.sp),modifier=Modifier.fillMaxWidth().padding(top=8.dp).background(if(done||focused) MaterialTheme.colorScheme.surface else AppColors.SurfaceMuted, AppShapeTokens.Tech).border(1.dp,border,AppShapeTokens.Tech).padding(10.dp,8.dp),decorationBox={inner-> if(value.text.isBlank()) Text(placeholder,style=MaterialTheme.typography.titleSmall,color=AppColors.InkDisabled); inner() })
        Row(Modifier.fillMaxWidth().padding(top=6.dp), horizontalArrangement = Arrangement.SpaceBetween){ Text(sub.orEmpty(),fontSize=10.sp,color=AppColors.InkTertiary,fontFamily=FontFamily.Monospace); Text("${value.text.length} chars",fontSize=10.sp,color=AppColors.InkTertiary,fontFamily=FontFamily.Monospace, textAlign = TextAlign.End) }
    } }

@Composable private fun SaveBar(progress:Float,filled:Int,onDraft:()->Unit,onSave:()->Unit){ Row(Modifier.fillMaxWidth().imePadding().background(MaterialTheme.colorScheme.surface).border(1.dp,AppColors.DividerNeutral).padding(16.dp,10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) { Column(Modifier.weight(1f)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("PROGRESS", fontFamily=FontFamily.Monospace,fontSize=10.sp,color=AppColors.InkTertiary); Text("${(progress*100).toInt()}%", fontFamily=FontFamily.Monospace,fontSize=10.sp,color=AppColors.InkTertiary) }; Spacer(Modifier.height(4.dp)); Box(Modifier.fillMaxWidth().height(4.dp).background(AppColors.DividerNeutral)) { Box(Modifier.fillMaxWidth(progress).height(4.dp).background(SemanticColors.InfoMain)) } }; Button(onClick=onDraft, colors = ButtonDefaults.buttonColors(containerColor=MaterialTheme.colorScheme.surface, contentColor=AppColors.InkSecondary), modifier=Modifier.height(36.dp), shape=AppShapeTokens.Tech) { Text("下書き") }; Button(onClick=onSave, colors = ButtonDefaults.buttonColors(containerColor=AppColors.InkPrimary, contentColor=Color.White), modifier=Modifier.height(36.dp), shape=AppShapeTokens.Tech) { Text("保存", letterSpacing = 0.5.sp) } } }

private fun todayDateString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date())
private fun formatDisplayDate(date: String): String = runCatching { SimpleDateFormat("yyyy/MM/dd (E)", Locale.JAPAN).format(SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).parse(date) ?: Date()) }.getOrDefault(date)
private fun formatDate(ts: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date(ts))
