package com.example.sample2.ui


import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.sample2.ui.theme.AppShapeTokens
import com.example.sample2.ui.theme.SemanticColors
import com.example.sample2.ui.theme.appColors
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sample2.ui.theme.Spacing



@Composable
fun VerticalScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {

    val layoutInfo = listState.layoutInfo
    val totalItems = layoutInfo.totalItemsCount
    val visibleItems = layoutInfo.visibleItemsInfo

    if (totalItems == 0 || visibleItems.isEmpty()) return

    val firstVisible = visibleItems.first().index
    val visibleCount = visibleItems.size

    val proportion = visibleCount.toFloat() / totalItems
    val offset = firstVisible.toFloat() / totalItems

    @Suppress("UnusedBoxWithConstraintsScope")
    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(4.dp)
    ) {
        val height = maxHeight

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height * proportion)
                .offset(y = height * offset)
                .background(MaterialTheme.appColors.inkTertiary, AppShapeTokens.Tech)
        )
    }
}

enum class JournalHeaderTitleStyle { Default, Medium }

@Composable
fun HeaderProgressStack(current: Int, total: Int, label: String, large: Boolean = false) {
    Column(horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = "$current", fontFamily = FontFamily.Monospace, fontSize = if (large) 24.sp else 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.appColors.inkPrimary)
            Text(text = " / $total", fontFamily = FontFamily.Monospace, fontSize = if (large) 14.sp else 12.sp, color = MaterialTheme.appColors.inkTertiary)
        }
        Text(text = label.uppercase(), fontFamily = FontFamily.Monospace, fontSize = 9.sp, letterSpacing = 1.5.sp, color = MaterialTheme.appColors.inkTertiary)
    }
}

@Composable
fun JournalTopHeader(
    title: String,
    subtitle: String? = null,
    showLiveDot: Boolean = false,
    titleStyle: JournalHeaderTitleStyle = JournalHeaderTitleStyle.Default,
    navigationIcon: ImageVector,
    navigationContentDescription: String,
    onNavigationClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    trailing: (@Composable () -> Unit)? = null,
    strongBottomBorder: Boolean = false,
    modifier: Modifier = Modifier
) {
    val borderColor = if (strongBottomBorder) {
        MaterialTheme.appColors.inkStrongAlt
    } else {
        MaterialTheme.appColors.dividerCool
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = androidx.compose.ui.geometry.Offset(0f, size.height),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompactHeaderIconButton(
            selected = false,
            onClick = onNavigationClick,
            icon = navigationIcon,
            contentDescription = navigationContentDescription
        )
        Spacer(modifier = Modifier.size(8.dp))
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
            if (showLiveDot) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(SemanticColors.PositiveMain))
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = title,
                fontSize = if (titleStyle == JournalHeaderTitleStyle.Default) 22.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp,
                color = MaterialTheme.appColors.inkStrongAlt
            )
            subtitle?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(start = 6.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.appColors.inkTertiary
                )
            }
        }
        if (trailing != null) {
            trailing()
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}

@Composable
fun CompactHeaderIconButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    showNotificationDot: Boolean = false,
    modifier: Modifier = Modifier
) {
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        modifier = modifier.size(40.dp),
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent,
        contentColor = contentColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(22.dp)
            )
            if (showNotificationDot) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = Spacing.sm, end = 8.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(SemanticColors.NegativeMain)
                )
            }
        }
    }
}


data class DateQuickOption(
    val label: String,
    val resolveDate: (current: LocalDate) -> LocalDate
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalDatePickerDialog(
    initialDate: LocalDate,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = LocalDate.now(),
    datesWithRecord: Set<LocalDate> = emptySet(),
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val zoneId = remember { ZoneId.systemDefault() }
    val _unused = datesWithRecord
    val selectableDates = remember(minDate, maxDate, zoneId) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant.ofEpochMilli(utcTimeMillis).atZone(zoneId).toLocalDate()
                val afterMin = minDate?.let { !date.isBefore(it) } ?: true
                val beforeMax = maxDate?.let { !date.isAfter(it) } ?: true
                return afterMin && beforeMax
            }
        }
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(zoneId).toInstant().toEpochMilli(),
        selectableDates = selectableDates,
    )
    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = {
            val ms = datePickerState.selectedDateMillis ?: return@TextButton
            onConfirm(Instant.ofEpochMilli(ms).atZone(zoneId).toLocalDate())
        }, enabled = datePickerState.selectedDateMillis != null) { Text("選択") }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } }) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun StepperCircleNav(icon: androidx.compose.ui.graphics.vector.ImageVector, cd: String, onClick: () -> Unit, enabled: Boolean = true) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = MaterialTheme.appColors.surfaceCool,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(icon, cd, tint = MaterialTheme.appColors.inkPrimary)
        }
    }
}

@Composable
fun DateStepper(selectedDate: LocalDate,onDateChange: (LocalDate) -> Unit,minDate: LocalDate? = null,maxDate: LocalDate = LocalDate.now(),datesWithRecord: Set<LocalDate> = emptySet(),quickOptions: List<DateQuickOption> = emptyList(),modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }
    val today = remember { LocalDate.now() }
    val showQuick = quickOptions.isNotEmpty()
    val dividerColor = MaterialTheme.appColors.dividerCool
    Column(modifier = modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().then(if(!showQuick) Modifier.drawBehind { drawLine(dividerColor, androidx.compose.ui.geometry.Offset(0f,size.height), androidx.compose.ui.geometry.Offset(size.width,size.height),1.dp.toPx()) } else Modifier).padding(horizontal=Spacing.md, vertical=Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            StepperCircleNav(Icons.Rounded.ChevronLeft, "前日", { onDateChange(selectedDate.minusDays(1)) }, enabled = minDate?.let { selectedDate>it } ?: true)
            Column(Modifier.weight(1f).clickable { showDialog=true }.semantics { contentDescription = "日付選択" }, horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(selectedDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.JAPAN)), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.appColors.inkPrimary)
                    if (selectedDate==today) Surface(shape=AppShapeTokens.Tech, color=SemanticColors.InfoMain){ Text("TODAY", color=Color.White, fontFamily=FontFamily.Monospace, fontSize=9.sp, letterSpacing=1.sp, modifier=Modifier.padding(horizontal=Spacing.xs, vertical=1.dp)) }
                    else if (selectedDate<today) Surface(shape=AppShapeTokens.Tech, color=MaterialTheme.appColors.surfaceCool){ Text("${ChronoUnit.DAYS.between(selectedDate,today)}日前", color=MaterialTheme.appColors.inkTertiary, fontFamily=FontFamily.Monospace, fontSize=9.sp, modifier=Modifier.padding(horizontal=Spacing.xs, vertical=1.dp)) }
                }
                Row(verticalAlignment=Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.CalendarMonth, null, modifier=Modifier.size(14.dp), tint=MaterialTheme.appColors.inkTertiary)
                    Text(selectedDate.format(DateTimeFormatter.ofPattern("E曜日", Locale.JAPAN)), fontSize=12.sp, color=MaterialTheme.appColors.inkTertiary)
                }
            }
            StepperCircleNav(Icons.Rounded.ChevronRight, "翌日", { onDateChange(selectedDate.plusDays(1)) }, enabled = selectedDate < maxDate)
        }
        if (showQuick) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).drawBehind { drawLine(dividerColor, androidx.compose.ui.geometry.Offset(0f,size.height), androidx.compose.ui.geometry.Offset(size.width,size.height),1.dp.toPx()) }.padding(horizontal=Spacing.md, vertical=8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                quickOptions.forEach { opt ->
                    val target = opt.resolveDate(selectedDate)
                    val selected = target == selectedDate
                    Surface(onClick = { onDateChange(target) }, shape = AppShapeTokens.Pill, color = if(selected) MaterialTheme.appColors.inkPrimary else MaterialTheme.appColors.surfaceCool, contentColor = if(selected) Color.White else MaterialTheme.appColors.inkSecondary) {
                        Text(opt.label, modifier=Modifier.padding(horizontal=12.dp, vertical=6.dp), fontSize=11.sp, fontWeight=FontWeight.Bold)
                    }
                }
            }
        }
    }
    if (showDialog) JournalDatePickerDialog(selectedDate, minDate, maxDate, datesWithRecord, { showDialog=false }) { onDateChange(it); showDialog=false }
}
