package com.example.sample2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sample2.data.DailyReflection
import com.example.sample2.ui.components.AppCard
import com.example.sample2.ui.components.AppCardVariant
import com.example.sample2.ui.theme.MonoTypography
import com.example.sample2.ui.theme.SemanticColors
import com.example.sample2.ui.theme.appColors
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.WeekFields
import java.util.Locale

/* ===========================================================================
 * 振り返り画面（リデザイン版）
 *
 * - 週固定: 月曜始まりの7日範囲をヘッダー内の ‹ › で前後切り替え。
 * - Weekly Overview: 曜日別スタックバー + 4指標サマリー。
 * - 日々の記録: 7日分の DayCard を新しい順で表示。
 *   - 4色メーター: wins / difficulties / insights / summary の入力有無を表示。
 *   - 入力済み項目: 読み取り表示の行全体タップで TextField 展開。
 *   - 空欄項目: 「+ 記録を追加」プレースホルダ → タップで TextField 展開。
 *   - フォーカスアウトで自動保存（差分があれば upsert）。
 * - tomorrowFirstAction はこの画面では表示・編集しない（既存データは保持）。
 * =========================================================================== */

private enum class ReflectionField(val label: String) {
    WINS("うまくいったこと"),
    DIFFICULTIES("しんどかったこと"),
    INSIGHTS("気づき"),
    SUMMARY("ひとことまとめ")
}

private data class FieldEditorKey(val date: String, val field: ReflectionField)

private data class WeekStats(
    val winsCount: Int,
    val difficultiesCount: Int,
    val insightsCount: Int,
    val summaryCount: Int,
    val recordedDays: Int,
    val totalDays: Int,
    val totalEntries: Int
)

@Composable
fun ReflectionTimelineScreen(
    reflections: List<DailyReflection>,
    onUpsertReflection: (DailyReflection) -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reflectionMap = remember(reflections) { reflections.associateBy { it.date } }

    var selectedWeekStart by rememberSaveable(stateSaver = LocalDateSaver) {
        mutableStateOf(weekStartMonday(LocalDate.now()))
    }

    // 編集中のフィールド（同時に1つだけ）
    var editingKey by remember { mutableStateOf<FieldEditorKey?>(null) }

    val weekDates = remember(selectedWeekStart) {
        (0L..6L).map { selectedWeekStart.plusDays(it) }
    }

    val weekStats = remember(reflectionMap, weekDates) {
        computeWeekStats(reflectionMap, weekDates)
    }

    val today = remember { LocalDate.now() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 「今週」または未来週にいる間は、次の週へのナビゲーションを隠す
        val canGoNext = selectedWeekStart.isBefore(weekStartMonday(today))
        ReflectionTopBar(
            weekStart = selectedWeekStart,
            canGoNext = canGoNext,
            onMenuClick = onMenuClick,
            onPrev = { selectedWeekStart = selectedWeekStart.minusWeeks(1) },
            onNext = { selectedWeekStart = selectedWeekStart.plusWeeks(1) },
            onSearchClick = { /* 検索は今後の拡張枠 */ },
            onAddTodayClick = {
                selectedWeekStart = weekStartMonday(today)
                editingKey = FieldEditorKey(today.toString(), ReflectionField.WINS)
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                WeeklyOverviewCard(
                    weekDates = weekDates,
                    reflectionMap = reflectionMap,
                    stats = weekStats,
                    today = today
                )
            }
            item {
                DaysSectionTitle()
            }

            // 未来日はカード化しない。今日以前の日のみ新しい順で表示。
            val orderedDates = weekDates.filter { !it.isAfter(today) }.sortedDescending()
            items(orderedDates, key = { it.toString() }) { date ->
                val reflection = reflectionMap[date.toString()]
                DayCard(
                    date = date,
                    reflection = reflection,
                    isToday = date == today,
                    editingKey = editingKey,
                    onStartEdit = { field ->
                        editingKey = FieldEditorKey(date.toString(), field)
                    },
                    onCancelEdit = {
                        if (editingKey?.date == date.toString()) editingKey = null
                    },
                    onSaveField = { field, newText ->
                        val current = reflectionMap[date.toString()]
                        val needsUpdate = current == null || !sameField(current, field, newText)
                        if (needsUpdate) {
                            val updated = updateField(
                                base = current ?: DailyReflection(date = date.toString()),
                                field = field,
                                value = newText
                            ).copy(updatedAt = System.currentTimeMillis())
                            onUpsertReflection(updated)
                        }
                        if (editingKey?.date == date.toString() && editingKey?.field == field) {
                            editingKey = null
                        }
                    }
                )
            }
        }
    }
}

// ============================================================================
// ヘッダー周辺
// ============================================================================

/**
 * 振り返り画面専用のトップバー。
 * - 左端: ≡ メニューアイコン
 * - 中央: 範囲ナビ ‹ M月D日 — D日 / 第N週 ›（2行ラベル + 前後ボタン）
 * - 右端: 検索 + 追加 アイコン
 * 「振り返り」というタイトル文字は持たない（ボトムナビで現在地が分かるため）。
 */
@Composable
private fun ReflectionTopBar(
    weekStart: LocalDate,
    canGoNext: Boolean,
    onMenuClick: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSearchClick: () -> Unit,
    onAddTodayClick: () -> Unit
) {
    val borderColor = MaterialTheme.appColors.dividerSoft

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 左: メニュー
        CompactHeaderIconButton(
            selected = false,
            onClick = onMenuClick,
            icon = Icons.Outlined.Menu,
            contentDescription = "メニュー"
        )

        // 中央: 範囲ナビ（残りスペースを weight(1f) で確保し、内側で中央寄せ）
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            RangeNavButton(
                icon = Icons.Outlined.ChevronLeft,
                contentDescription = "前の週",
                onClick = onPrev
            )
            Spacer(Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f, fill = false),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val end = weekStart.plusDays(6)
                Text(
                    text = formatRangeMain(weekStart, end),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.inkPrimary,
                    maxLines = 1
                )
                Text(
                    text = formatRangeSub(weekStart),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.appColors.inkTertiary,
                        letterSpacing = 0.5.sp
                    ),
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(8.dp))
            // 未来週へは進めない仕様。今週以降は › ボタンを透明な領域として確保する
            // （位置がズレないようにするため、サイズだけ維持）
            if (canGoNext) {
                RangeNavButton(
                    icon = Icons.Outlined.ChevronRight,
                    contentDescription = "次の週",
                    onClick = onNext
                )
            } else {
                Spacer(modifier = Modifier.size(28.dp))
            }
        }

        // 右: 検索 + 追加
        CompactHeaderIconButton(
            selected = false,
            onClick = onSearchClick,
            icon = Icons.Outlined.Search,
            contentDescription = "検索（準備中）"
        )
        CompactHeaderIconButton(
            selected = false,
            onClick = onAddTodayClick,
            icon = Icons.Outlined.Add,
            contentDescription = "今日の記録を追加"
        )
    }
}

@Composable
private fun RangeNavButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.appColors.dividerSoft, RoundedCornerShape(4.dp))
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

// ============================================================================
// Weekly Overview
// ============================================================================

@Composable
private fun WeeklyOverviewCard(
    weekDates: List<LocalDate>,
    reflectionMap: Map<String, DailyReflection>,
    stats: WeekStats,
    today: LocalDate
) {
    AppCard(
        variant = AppCardVariant.Outlined,
        contentPadding = PaddingValues(0.dp),
        verticalSpacing = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "今週の概要",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.appColors.inkSecondary,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp
                )
            )
            Text(
                "${stats.recordedDays} / ${stats.totalDays}日 ・ ${stats.totalEntries}件",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.appColors.inkTertiary)
            )
        }

        DividerLine()

        WeekBars(
            weekDates = weekDates,
            reflectionMap = reflectionMap,
            today = today
        )

        DividerLine()

        StatsRow(stats = stats)
    }
}

@Composable
private fun WeekBars(
    weekDates: List<LocalDate>,
    reflectionMap: Map<String, DailyReflection>,
    today: LocalDate
) {
    val countsPerDay = weekDates.map { date ->
        reflectionMap[date.toString()]?.let { categoryFlags(it) } ?: emptyList()
    }
    val maxCount = (countsPerDay.maxOfOrNull { it.size } ?: 0).coerceAtLeast(1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        weekDates.forEachIndexed { index, date ->
            val flags = countsPerDay[index]
            DayBar(
                date = date,
                segments = flags,
                maxCount = maxCount,
                isToday = date == today,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DayBar(
    date: LocalDate,
    segments: List<ReflectionField>,
    maxCount: Int,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val barTotalHeight = 60.dp
        val emptyColor = MaterialTheme.appColors.dividerSoft
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barTotalHeight)
                .clip(RoundedCornerShape(2.dp))
                .background(emptyColor)
        ) {
            if (segments.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    val ratio = segments.size.toFloat() / maxCount.toFloat()
                    val filledHeight = barTotalHeight * ratio
                    val segHeight = filledHeight / segments.size
                    segments.forEach { f ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(segHeight)
                                .background(fieldColor(f))
                        )
                    }
                }
            }
        }
        Text(
            text = dayOfWeekShort(date.dayOfWeek),
            style = MonoTypography.Micro.copy(
                color = MaterialTheme.appColors.inkTertiary,
                fontSize = 9.sp
            )
        )
        if (isToday) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.appColors.inkStrongAlt),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.appColors.inkOnInk,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        } else {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.appColors.inkPrimary,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
private fun StatsRow(stats: WeekStats) {
    Row(modifier = Modifier.fillMaxWidth()) {
        StatCell(
            label = "うまく",
            value = stats.winsCount.toString(),
            valueColor = fieldColor(ReflectionField.WINS),
            drawDivider = true,
            modifier = Modifier.weight(1f)
        )
        StatCell(
            label = "しんどい",
            value = stats.difficultiesCount.toString(),
            valueColor = fieldColor(ReflectionField.DIFFICULTIES),
            drawDivider = true,
            modifier = Modifier.weight(1f)
        )
        StatCell(
            label = "気づき",
            value = stats.insightsCount.toString(),
            valueColor = fieldColor(ReflectionField.INSIGHTS),
            drawDivider = true,
            modifier = Modifier.weight(1f)
        )
        StatCell(
            label = "記録日",
            value = "${stats.recordedDays}/${stats.totalDays}",
            valueColor = MaterialTheme.appColors.inkPrimary,
            drawDivider = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    valueColor: Color,
    drawDivider: Boolean,
    modifier: Modifier = Modifier
) {
    val dividerColor = MaterialTheme.appColors.dividerSoft
    Box(
        modifier = modifier
            .padding(vertical = 10.dp, horizontal = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MonoTypography.Numeric.copy(
                    color = valueColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.appColors.inkSecondary
                )
            )
        }
        if (drawDivider) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(1.dp)
                    .height(28.dp)
                    .background(dividerColor)
            )
        }
    }
}

// ============================================================================
// 日々の記録セクション
// ============================================================================

@Composable
private fun DaysSectionTitle() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            "日々の記録",
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.appColors.inkSecondary,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp
            )
        )
        Text(
            "↓ 新しい順",
            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.appColors.inkTertiary)
        )
    }
}

@Composable
private fun DayCard(
    date: LocalDate,
    reflection: DailyReflection?,
    isToday: Boolean,
    editingKey: FieldEditorKey?,
    onStartEdit: (ReflectionField) -> Unit,
    onCancelEdit: () -> Unit,
    onSaveField: (ReflectionField, String) -> Unit
) {
    val hasContent = reflection?.let { hasAnyOf4(it) } == true
    AppCard(
        variant = AppCardVariant.Outlined,
        contentPadding = PaddingValues(0.dp),
        verticalSpacing = 0.dp
    ) {
        // 日付ヘッダ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.appColors.surfaceQuiet)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.appColors.inkPrimary
                    )
                )
                Text(
                    text = "${date.monthValue}月",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.appColors.inkSecondary
                    ),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = dayOfWeekShort(date.dayOfWeek) + (if (isToday) " · TODAY" else ""),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isToday) SemanticColors.InfoMain else MaterialTheme.appColors.inkTertiary,
                        letterSpacing = 0.8.sp
                    ),
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                ReflectionField.entries.forEach { f ->
                    val filled = reflection?.let { fieldText(it, f) }?.isNotBlank() == true
                    Box(
                        modifier = Modifier
                            .size(width = 16.dp, height = 5.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(if (filled) fieldColor(f) else MaterialTheme.appColors.dividerSoft)
                    )
                }
            }
        }

        DividerLine()

        if (!hasContent && editingKey?.date != date.toString()) {
            // 完全に記録なし & 編集中でもない日 → 「記録なし · タップして追加」表示
            DayEmptyRow(
                onClick = { onStartEdit(ReflectionField.WINS) }
            )
        } else {
            ReflectionField.entries.forEachIndexed { index, field ->
                val text = reflection?.let { fieldText(it, field) }.orEmpty()
                val isEditing = editingKey?.date == date.toString() && editingKey?.field == field
                ReflectionFieldRow(
                    field = field,
                    text = text,
                    isEditing = isEditing,
                    onStartEdit = { onStartEdit(field) },
                    onCancelEdit = onCancelEdit,
                    onSave = { newText -> onSaveField(field, newText) }
                )
                if (index < ReflectionField.entries.lastIndex) {
                    DividerLine()
                }
            }
        }
    }
}

/** 記録なしの日のプレースホルダ行（斜線パターン背景） */
@Composable
private fun DayEmptyRow(onClick: () -> Unit) {
    val stripeBase = MaterialTheme.colorScheme.surface
    val stripeAccent = MaterialTheme.appColors.surfaceQuiet
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .drawBehind {
                drawRect(stripeBase)
                val stripeWidth = 12.dp.toPx()
                var x = -size.height
                while (x < size.width) {
                    drawLine(
                        color = stripeAccent,
                        start = Offset(x, size.height),
                        end = Offset(x + size.height, 0f),
                        strokeWidth = stripeWidth / 2,
                        cap = StrokeCap.Square
                    )
                    x += stripeWidth
                }
            }
            .padding(vertical = 18.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "記録なし · タップして追加",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.appColors.inkTertiary
            )
        )
    }
}

// ============================================================================
// インライン編集行
// ============================================================================

@Composable
private fun ReflectionFieldRow(
    field: ReflectionField,
    text: String,
    isEditing: Boolean,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSave: (String) -> Unit
) {
    val accentColor = fieldColor(field)
    val isEmpty = text.isBlank()

    // 編集中以外は、行全体タップで編集モードに入れるようにする。
    // 鉛筆アイコンは廃止し、テキストエリア全体をタップ領域にする。
    val rowModifier = if (!isEditing) {
        Modifier.clickable { onStartEdit() }
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(rowModifier)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(if (isEditing) 60.dp else 36.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isEmpty && !isEditing) MaterialTheme.appColors.dividerSoft else accentColor)
        )
        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = field.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (isEmpty && !isEditing) MaterialTheme.appColors.inkTertiary else accentColor,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            )
            if (isEditing) {
                EditingField(
                    initialText = text,
                    onCancel = onCancelEdit,
                    onSave = onSave,
                    accentColor = accentColor
                )
            } else if (isEmpty) {
                Text(
                    text = "+ 記録を追加",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.appColors.inkTertiary
                    )
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.appColors.inkPrimary,
                        lineHeight = 20.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun EditingField(
    initialText: String,
    onCancel: () -> Unit,
    onSave: (String) -> Unit,
    accentColor: Color
) {
    var value by remember(initialText) {
        mutableStateOf(TextFieldValue(initialText, selection = TextRange(initialText.length)))
    }
    val focusRequester = remember { FocusRequester() }
    var hasFocused by remember { mutableStateOf(false) }
    var hasFiredSave by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BasicTextField(
        value = value,
        onValueChange = { value = it },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { state ->
                if (state.isFocused) {
                    hasFocused = true
                } else if (hasFocused && !hasFiredSave) {
                    hasFiredSave = true
                    val newText = value.text
                    if (newText != initialText) {
                        onSave(newText)
                    } else {
                        onCancel()
                    }
                }
            }
            .background(MaterialTheme.appColors.surfaceQuiet, RoundedCornerShape(4.dp))
            .border(1.dp, accentColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.appColors.inkPrimary,
            lineHeight = 20.sp
        ),
        cursorBrush = SolidColor(accentColor),
        keyboardOptions = KeyboardOptions.Default,
        decorationBox = { inner ->
            if (value.text.isEmpty()) {
                Text(
                    "ここに入力 — 別の場所をタップで保存",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.appColors.inkDisabled
                    )
                )
            }
            inner()
        }
    )
}

// ============================================================================
// 共通パーツ
// ============================================================================

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.appColors.dividerSoft)
    )
}

// ============================================================================
// データユーティリティ
// ============================================================================

private fun fieldText(r: DailyReflection, field: ReflectionField): String = when (field) {
    ReflectionField.WINS -> r.wins
    ReflectionField.DIFFICULTIES -> r.difficulties
    ReflectionField.INSIGHTS -> r.insights
    ReflectionField.SUMMARY -> r.summary
}

private fun updateField(base: DailyReflection, field: ReflectionField, value: String): DailyReflection = when (field) {
    ReflectionField.WINS -> base.copy(wins = value)
    ReflectionField.DIFFICULTIES -> base.copy(difficulties = value)
    ReflectionField.INSIGHTS -> base.copy(insights = value)
    ReflectionField.SUMMARY -> base.copy(summary = value)
}

private fun sameField(r: DailyReflection, field: ReflectionField, value: String): Boolean =
    fieldText(r, field) == value

private fun categoryFlags(r: DailyReflection): List<ReflectionField> =
    ReflectionField.entries.filter { fieldText(r, it).isNotBlank() }

private fun hasAnyOf4(r: DailyReflection): Boolean =
    r.wins.isNotBlank() || r.difficulties.isNotBlank() || r.insights.isNotBlank() || r.summary.isNotBlank()

/**
 * 振り返り4項目の色。記録画面（ChatScreen）の ActionGroup カラーと意味的に揃えてある:
 *  - WINS         → ActionGroup.RECOVER（立て直し）の緑
 *  - DIFFICULTIES → ActionGroup.LOAD（負荷）の紫
 *  - INSIGHTS     → ActionGroup.FORWARD（前進）のオレンジ
 *  - SUMMARY      → どのグループにも該当しないニュートラル紫（既存維持）
 *
 * SemanticColors.* はアプリ全体で共有されている色のため、振り返り画面だけで配色を
 * 変えたい今回のケースでは、ここに直接ハードコードする方が波及範囲が小さくて安全。
 */
private fun fieldColor(field: ReflectionField): Color = when (field) {
    ReflectionField.WINS -> Color(0xFF43A047)
    ReflectionField.DIFFICULTIES -> Color(0xFF8E24AA)
    ReflectionField.INSIGHTS -> Color(0xFFFB8C00)
    ReflectionField.SUMMARY -> SemanticColors.SummaryMain
}

private fun computeWeekStats(
    reflectionMap: Map<String, DailyReflection>,
    weekDates: List<LocalDate>
): WeekStats {
    var wins = 0
    var diff = 0
    var ins = 0
    var sum = 0
    var recordedDays = 0
    weekDates.forEach { date ->
        val r = reflectionMap[date.toString()] ?: return@forEach
        var any = false
        if (r.wins.isNotBlank()) { wins++; any = true }
        if (r.difficulties.isNotBlank()) { diff++; any = true }
        if (r.insights.isNotBlank()) { ins++; any = true }
        if (r.summary.isNotBlank()) { sum++; any = true }
        if (any) recordedDays++
    }
    return WeekStats(
        winsCount = wins,
        difficultiesCount = diff,
        insightsCount = ins,
        summaryCount = sum,
        recordedDays = recordedDays,
        totalDays = weekDates.size,
        totalEntries = wins + diff + ins + sum
    )
}

// ============================================================================
// 日付ユーティリティ
// ============================================================================

/** 月曜始まりでその週の月曜日を返す */
private fun weekStartMonday(date: LocalDate): LocalDate {
    val dow = date.dayOfWeek.value // Mon=1 ... Sun=7
    return date.minusDays((dow - 1).toLong())
}

private fun dayOfWeekShort(d: DayOfWeek): String = when (d) {
    DayOfWeek.MONDAY -> "月"
    DayOfWeek.TUESDAY -> "火"
    DayOfWeek.WEDNESDAY -> "水"
    DayOfWeek.THURSDAY -> "木"
    DayOfWeek.FRIDAY -> "金"
    DayOfWeek.SATURDAY -> "土"
    DayOfWeek.SUNDAY -> "日"
}

private fun formatRangeMain(start: LocalDate, end: LocalDate): String {
    return if (start.month == end.month) {
        "${start.monthValue}月${start.dayOfMonth}日 — ${end.dayOfMonth}日"
    } else {
        "${start.monthValue}月${start.dayOfMonth}日 — ${end.monthValue}月${end.dayOfMonth}日"
    }
}

private fun formatRangeSub(start: LocalDate): String {
    val weekFields = WeekFields.of(Locale.JAPAN)
    val weekNum = start.get(weekFields.weekOfWeekBasedYear())
    val year = start.get(weekFields.weekBasedYear())
    return "${year}年 第${weekNum}週"
}

// ============================================================================
// LocalDate を rememberSaveable に保存するためのカスタム Saver
// ============================================================================

private val LocalDateSaver: Saver<LocalDate, String> = Saver(
    save = { it.toString() },
    restore = {
        try {
            LocalDate.parse(it)
        } catch (_: DateTimeParseException) {
            LocalDate.now()
        }
    }
)
