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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val bgPage = Color(0xFFF5F5F5)
private val bgCard = Color(0xFFFFFFFF)
private val bgSubtle = Color(0xFFFAFAFA)
private val inkStrong = Color(0xFF1A1A1A)
private val inkMid = Color(0xFF4A4A4A)
private val inkSoft = Color(0xFF757575)
private val inkFaint = Color(0xFFB0B0B0)
private val line = Color(0xFFE0E0E0)
private val lineStrong = Color(0xFFC8C8C8)
private val accent = Color(0xFF1A4A8A)
private val accentSoft = Color(0xFFE8EEF7)
private val warn = Color(0xFF8A5A1A)
private val warnSoft = Color(0xFFF7EFE0)

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

    Scaffold(contentWindowInsets = WindowInsets.safeDrawing, containerColor = bgPage,
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

@Composable private fun Header(date: String, filled: Int) { Row(Modifier.fillMaxWidth().background(bgCard).border(1.dp, line).padding(12.dp,10.dp), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("DAILY REFLECTION", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = inkSoft, letterSpacing = 1.sp); Text(formatDisplayDate(date), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = inkStrong) }; Row(verticalAlignment = Alignment.Bottom) { Text("$filled", fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = inkStrong); Text(" / 5", fontFamily = FontFamily.Monospace, color = inkMid) } } }

@Composable private fun SummarySection(h: ReflectionHints, s: com.example.sample2.analytics.DailyPersonalityScore?, state: PersonalityState?, hasBreakdown: Boolean) { Column(Modifier.fillMaxWidth().background(bgCard).border(1.dp,line).padding(16.dp,12.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("STATUS", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = inkSoft); StatusBadge(state) }; Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth().border(1.dp,line).background(bgSubtle)) { MetricCell("STABLE", s?.stability?.toInt()?.toString() ?: "--", true); MetricCell("ANXIETY", s?.anxiety?.let { "%.1f".format(it) } ?: "--", true); MetricCell("ENERGY", s?.energy?.toInt()?.toString() ?: "--", true); MetricCell("CONTROL", s?.control?.toInt()?.toString() ?: "--", false) }; Spacer(Modifier.height(8.dp)); Text("MSG  ${h.messageCountText.filter { it.isDigit() }}件", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = inkSoft); Text("SLEEP  ${h.dailyRecordText.substringBefore(" /").replace("睡眠 ","").replace("時間","h ").replace("分","m")}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = inkSoft); Text("EMOTION  ${h.emotionTrendText.removePrefix("感情傾向: ").replace(" が多め", "+")}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = inkSoft); Text("FLAGS  ${if (hasBreakdown) "体×1" else "--"}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = inkSoft); Spacer(Modifier.height(8.dp)); Box(Modifier.fillMaxWidth().background(bgSubtle).border(1.dp, line).padding(10.dp)) { Text(s?.summary ?: h.analysisSummaryText, fontSize = 11.sp, color = inkMid, lineHeight = 17.sp) } } }
@Composable private fun StatusBadge(state: PersonalityState?) { val (fg,bg) = when(state){PersonalityState.STABLE->accent to accentSoft; PersonalityState.RECOVERING->warn to warnSoft; PersonalityState.TENSE->Color(0xFF7A4A12) to warnSoft; PersonalityState.EXHAUSTED->Color(0xFF8A2A2A) to Color(0xFFF4E8E6); null->inkSoft to bgSubtle}; Row(Modifier.background(bg, RoundedCornerShape(3.dp)).padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.width(6.dp).height(6.dp).background(fg, CircleShape)); Spacer(Modifier.width(6.dp)); Text(state?.label ?: "不明", color = fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) } }
@Composable private fun MetricCell(label:String,value:String,divider:Boolean){ Column(Modifier.weight(1f).then(if(divider) Modifier.border(0.5.dp,line) else Modifier).padding(8.dp)) { Text(label,fontFamily=FontFamily.Monospace,fontSize=9.sp,color=inkSoft); Text(value,fontFamily=FontFamily.Monospace,fontSize=18.sp,fontWeight=FontWeight.Medium,color=inkStrong) } }

@Composable private fun InputHead(filled:Int){ Row(Modifier.fillMaxWidth().background(bgPage).padding(16.dp,14.dp,16.dp,6.dp), horizontalArrangement = Arrangement.SpaceBetween){ Text("INPUT — 5 ITEMS", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = inkSoft); Text("$filled OF 5 FILLED", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = inkSoft) } }

@Composable private fun ReflectionItem(number:String,title:String,placeholder:String,value:TextFieldValue,onValue:(TextFieldValue)->Unit,priority:Boolean,sub:String?,hint:String?=null){ val done=value.text.isNotBlank(); var focused by remember { mutableStateOf(false) }; val border by animateColorAsState(if (focused) accent else if (done) lineStrong else line, label = ""); Column(Modifier.fillMaxWidth().background(bgCard).padding(16.dp,12.dp).border(1.dp,line)) { Row(verticalAlignment = Alignment.CenterVertically) { Text(number,fontFamily=FontFamily.Monospace,fontSize=10.sp,color=inkSoft,modifier=Modifier.widthIn(min=24.dp)); Text(title,modifier=Modifier.weight(1f),fontSize=13.sp,fontWeight=FontWeight.SemiBold); val label=if(done)"DONE" else if(priority)"PRIORITY" else "EMPTY"; val lBg=if(done) accentSoft else if(priority) warnSoft else bgSubtle; val lColor=if(done) accent else if(priority) warn else inkSoft; Text(label,fontFamily=FontFamily.Monospace,fontSize=9.sp,color=lColor,modifier=Modifier.background(lBg,RoundedCornerShape(2.dp)).padding(6.dp,2.dp)) }
        if (priority && !done && hint != null) Text("ANALYSIS: $hint", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = inkMid, modifier = Modifier.fillMaxWidth().padding(top=8.dp).background(warnSoft).padding(8.dp))
        BasicTextField(value=value,onValueChange=onValue,textStyle=TextStyle(fontSize=13.sp,color=inkStrong,lineHeight=22.sp),modifier=Modifier.fillMaxWidth().padding(top=8.dp).background(if(done||focused) bgCard else bgSubtle, RoundedCornerShape(2.dp)).border(1.dp,border,RoundedCornerShape(2.dp)).padding(10.dp,8.dp),decorationBox={inner-> if(value.text.isBlank()) Text(placeholder,fontSize=13.sp,color=inkFaint); inner() })
        Row(Modifier.fillMaxWidth().padding(top=6.dp), horizontalArrangement = Arrangement.SpaceBetween){ Text(sub.orEmpty(),fontSize=10.sp,color=inkSoft,fontFamily=FontFamily.Monospace); Text("${value.text.length} chars",fontSize=10.sp,color=inkSoft,fontFamily=FontFamily.Monospace, textAlign = TextAlign.End) }
    } }

@Composable private fun SaveBar(progress:Float,filled:Int,onDraft:()->Unit,onSave:()->Unit){ Row(Modifier.fillMaxWidth().imePadding().background(bgCard).border(1.dp,line).padding(16.dp,10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) { Column(Modifier.weight(1f)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("PROGRESS", fontFamily=FontFamily.Monospace,fontSize=10.sp,color=inkSoft); Text("${(progress*100).toInt()}%", fontFamily=FontFamily.Monospace,fontSize=10.sp,color=inkSoft) }; Spacer(Modifier.height(4.dp)); Box(Modifier.fillMaxWidth().height(4.dp).background(line)) { Box(Modifier.fillMaxWidth(progress).height(4.dp).background(accent)) } }; Button(onClick=onDraft, colors = ButtonDefaults.buttonColors(containerColor=bgCard, contentColor=inkMid), modifier=Modifier.height(36.dp), shape=RoundedCornerShape(2.dp)) { Text("下書き") }; Button(onClick=onSave, colors = ButtonDefaults.buttonColors(containerColor=inkStrong, contentColor=Color.White), modifier=Modifier.height(36.dp), shape=RoundedCornerShape(2.dp)) { Text("保存", letterSpacing = 0.5.sp) } } }

private fun todayDateString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date())
private fun formatDisplayDate(date: String): String = runCatching { SimpleDateFormat("yyyy/MM/dd (E)", Locale.JAPAN).format(SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).parse(date) ?: Date()) }.getOrDefault(date)
private fun formatDate(ts: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(Date(ts))
