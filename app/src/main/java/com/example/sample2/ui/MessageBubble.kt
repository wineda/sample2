package com.example.sample2.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import com.example.sample2.BubbleColor
import com.example.sample2.TextColor
import com.example.sample2.TimeColor
import com.example.sample2.data.ActionFlags
import com.example.sample2.data.ActionType
import com.example.sample2.data.EmotionMetrics
import com.example.sample2.data.EmotionType
import com.example.sample2.data.JournalEntryType
import com.example.sample2.data.MessageV2
import com.example.sample2.data.firstEnabledActionOrNull
import com.example.sample2.data.maxEmotionOrNull
import com.example.sample2.ui.theme.CategoryEmotionNegative
import com.example.sample2.ui.theme.CategoryEmotionPositive
import com.example.sample2.ui.theme.CategoryExerciseBody
import com.example.sample2.ui.theme.CategoryMorningHabit
import com.example.sample2.ui.theme.CategorySleep
import com.example.sample2.ui.theme.CategoryWork
import com.example.sample2.ui.theme.emotionCategoryToColor
import com.example.sample2.util.formatTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val MessageRowHorizontalPadding = 16.dp

private val BubbleRightPadding = 12.dp
private val BubbleTextHorizontalPadding = 14.dp
private val BubbleStartIndent =
    MessageRowHorizontalPadding +
            46.dp

private val BubbleTextVerticalPadding = 10.dp
private val BubbleTextVerticalPaddingCompact = 4.dp
private val MessageRowVerticalPadding = 4.dp
private val MessageRowVerticalPaddingCompact = 1.5.dp
private val ChildBubbleRightPadding = 20.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: MessageV2,
    state: JournalViewModel,
    isConnectedToPreviousInDay: Boolean,
    isConnectedToNextInDay: Boolean,
    onDelete: () -> Unit,
    onUpdate: (MessageV2) -> Unit,
    onDoubleClick: (MessageV2) -> Unit = {}
) {
    val rowVerticalPadding =
        if (state.isSingleLineMode) MessageRowVerticalPaddingCompact else MessageRowVerticalPadding

    val displayText = if (state.isSingleLineMode) {
        message.text
            .replace("\r\n", " ")
            .replace("\n", " ")
            .replace("\r", " ")
    } else {
        message.text
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = MessageRowHorizontalPadding, vertical = rowVerticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val context = LocalContext.current

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = BubbleRightPadding)
                .combinedClickable(
                    onClick = {},
                    onDoubleClick = { onDoubleClick(message) },
                    onLongClick = { state.selectedMessage = message }
                )
        ) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = emotionCategoryToColor(message.emotions.maxEmotionOrNull()).border
                ),
                modifier = Modifier
                    .weight(1f)
                    .combinedClickable(
                        onClick = {},
                        onDoubleClick = { onDoubleClick(message) },
                        onLongClick = { state.selectedMessage = message }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    StatusIconBox(
                        message = message,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val categoryLabel = message.flags.firstEnabledActionOrNull()?.label
                            ?: message.emotions.maxEmotionOrNull()?.label
                            ?: "メモ"
                        Text(
                            text = categoryLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = categoryColorFor(message)
                        )
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.16).sp
                            ),
                            color = TextColor,
                            maxLines = if (state.isSingleLineMode) 1 else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatTime(message.timestamp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = TimeColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmotionResponseChildBubble(
    message: MessageV2,
    onLongClick: (MessageV2) -> Unit,
    modifier: Modifier = Modifier
) {
    if (message.entryType != JournalEntryType.EMOTION_RESPONSE) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 36.dp, end = BubbleRightPadding),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(18.dp)
                .height(54.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .background(Color(0xFFD1D5DB))
            )
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(8.dp)
                    .background(color = Color(0xFF9CA3AF), shape = CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = ChildBubbleRightPadding, top = 4.dp, bottom = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = {}, onLongClick = { onLongClick(message) }),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextColor,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatTime(message.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = Color(0xFF9CA3AF)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFE5E7EB))
            )
        }
    }
}


private fun categoryColorFor(message: MessageV2): Color {
    return emotionCategoryToColor(message.emotions.maxEmotionOrNull()).border
}

@Composable
fun DateLabel(timestamp: Long) {
    val dateText = SimpleDateFormat("M月d日 E", Locale.JAPANESE).format(Date(timestamp))
    val relative = getRelativeLabel(timestamp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = MessageRowHorizontalPadding,
                end = MessageRowHorizontalPadding,
                top = 12.dp,
                bottom = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateText,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.01).sp
            ),
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            color = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = relative,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.1.sp
                ),
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(Color(0xFFE8E4D8))
        )
    }
}

fun getRelativeLabel(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val days = diff / (1000L * 60 * 60 * 24)

    return if (days < 1) {
        "TODAY"
    } else {
        "-${days}D"
    }
}

enum class EditorMode { CREATE, EDIT }

@Composable
fun MessageActionOverlay(
    message: MessageV2,
    mode: EditorMode,
    state: JournalViewModel,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (MessageV2) -> Unit,
    onCreate: (MessageV2) -> Unit = {}
) {
    var editingText by remember(message.id) { mutableStateOf(TextFieldValue(message.text)) }
    var editingTimestamp by remember(message.id) { mutableStateOf(message.timestamp) }
    var editingEmotions by remember(message.id) { mutableStateOf(message.emotions) }
    var editingFlags by remember(message.id) { mutableStateOf(message.flags) }
    var selectedActionType by remember(message.id) { mutableStateOf(message.flags.firstEnabledActionOrNull()) }
    var isActionTypeExpanded by remember(message.id) { mutableStateOf(false) }
    var showActionMenu by remember(message.id) { mutableStateOf(false) }
    var showDeleteConfirm by remember(message.id) { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val blockClicks = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val textFocusRequester = remember { FocusRequester() }
    var isTextFocused by remember { mutableStateOf(false) }

    LaunchedEffect(message.id, mode) {
        if (mode == EditorMode.CREATE) textFocusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.32f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onDismiss()
            }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 72.dp, bottom = 16.dp)
                .verticalScroll(scrollState)
                .clickable(
                    indication = null,
                    interactionSource = blockClicks
                ) {
                    // 中身タップ時は閉じない
                }
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.5.dp, if (isTextFocused) Color(0xFF1A1A1A) else Color(0xFFEFECE4)),
                tonalElevation = 0.dp,
                shadowElevation = 3.dp
            ) {
                Column(modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (mode == EditorMode.CREATE) "新しい記録" else "記録を編集",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (mode == EditorMode.CREATE) "NEW" else "EDIT",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.15.sp,
                            color = Color(0xFF888888)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "本文",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.12.sp,
                        color = Color(0xFF999999)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    BasicTextField(
                        value = editingText,
                        onValueChange = { editingText = it },
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = TextColor
                        ),
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(textFocusRequester)
                            .onFocusChanged { isTextFocused = it.isFocused },
                        decorationBox = { innerTextField ->
                            if (editingText.text.isBlank()) {
                                Text(
                                    text = "思ったことをひとこと…",
                                    color = Color(0xFFB8B3A8),
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showTimestampPicker(
                            context = context,
                            initialTimestamp = editingTimestamp,
                            onSelected = { editingTimestamp = it }
                        )
                    },
                shape = RoundedCornerShape(14.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF5F2EA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🕒", color = Color(0xFF9CA3AF), fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "時刻",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF999999),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatEditorTime(editingTimestamp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = (-0.02).sp,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("⌄", color = Color(0xFFCCCCCC), fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                tonalElevation = 2.dp,
                shadowElevation = 6.dp
            ) {
                if (selectedActionType == null) {
                    Column {
                        AddEmotionButton(
                            text = "種類を追加",
                            onClick = {
                                focusManager.clearFocus(force = true)
                                isActionTypeExpanded = !isActionTypeExpanded
                            }
                        )
                        if (isActionTypeExpanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(color = Color.White) {
                                ActionTypeGrid(
                                    selectedType = null,
                                    onSelected = { selected ->
                                        selectedActionType = selected
                                        editingFlags = ActionFlags().selectOnly(selected)
                                        isActionTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    CollapsibleActionTypeEditor(
                        selectedType = selectedActionType,
                        expanded = isActionTypeExpanded,
                        onToggleExpanded = {
                            focusManager.clearFocus(force = true)
                            isActionTypeExpanded = !isActionTypeExpanded
                        },
                        onSelected = { selected ->
                            selectedActionType = selected
                            editingFlags = ActionFlags().selectOnly(selected)
                            isActionTypeExpanded = false
                        },
                        onClear = {
                            selectedActionType = null
                            editingFlags = ActionFlags()
                            isActionTypeExpanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                tonalElevation = 2.dp,
                shadowElevation = 6.dp
            ) {
                AdditiveEmotionEditor(
                    emotions = editingEmotions,
                    onEmotionsChanged = { editingEmotions = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                tonalElevation = 2.dp,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6B6660)),
                        onClick = {
                            onDismiss()
                        }
                    ) {
                        Text("閉じる", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        modifier = Modifier.weight(1.6f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                        onClick = {
                            val payload = message.copy(
                                text = editingText.text,
                                timestamp = editingTimestamp,
                                emotions = editingEmotions,
                                flags = editingFlags
                            )
                            if (mode == EditorMode.CREATE) onCreate(payload) else onUpdate(payload)
                            onDismiss()
                        }
                    ) {
                        Text("保存", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    if (mode == EditorMode.EDIT) Box {
                        IconButton(
                            onClick = { showActionMenu = true },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "more",
                                tint = Color(0xFF888888)
                            )
                        }
                        DropdownMenu(
                            expanded = showActionMenu,
                            onDismissRequest = { showActionMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("削除", color = Color.Red) },
                                onClick = {
                                    showActionMenu = false
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("削除の確認") },
            text = { Text("このメッセージを削除しますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                        onDismiss()
                    }
                ) {
                    Text("削除", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("キャンセル")
                }
            }
        )
    }
}

private fun formatEditorTime(timestamp: Long): String {
    val selected = Calendar.getInstance().apply { timeInMillis = timestamp }
    val now = Calendar.getInstance()
    val isToday = selected.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            selected.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
    val hhmm = SimpleDateFormat("HH:mm", Locale.JAPANESE).format(Date(timestamp))
    return if (isToday) {
        "今日 $hhmm"
    } else {
        val md = SimpleDateFormat("M/d", Locale.JAPANESE).format(Date(timestamp))
        "$md $hhmm"
    }
}

@Composable
private fun AdditiveEmotionEditor(
    emotions: EmotionMetrics,
    onEmotionsChanged: (EmotionMetrics) -> Unit
) {
    var showPalette by remember { mutableStateOf(false) }
    val selectedEmotions = EmotionType.entries.filter { it.scoreOf(emotions) > 0 }
    val remainingEmotions = EmotionType.entries.filterNot { it in selectedEmotions }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (selectedEmotions.isEmpty()) {
            AddEmotionButton(
                text = "感情を追加",
                onClick = { showPalette = !showPalette }
            )
        } else {
            selectedEmotions.forEach { emotion ->
                EmotionSegmentRow(
                    emotion = emotion,
                    value = emotion.scoreOf(emotions),
                    onValueChanged = { score ->
                        onEmotionsChanged(emotions.withScore(emotion, score))
                    },
                    onRemove = {
                        onEmotionsChanged(emotions.withScore(emotion, 0))
                    }
                )
            }

            if (remainingEmotions.isNotEmpty()) {
                AddEmotionButton(
                    text = "もう1つ追加",
                    onClick = { showPalette = !showPalette }
                )
            }
        }

        if (showPalette && remainingEmotions.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "どの感情がありましたか?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EmotionType.entries.forEach { emotion ->
                            val isAdded = emotion in selectedEmotions
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF5F2EA))
                                    .clickable(enabled = !isAdded) {
                                        onEmotionsChanged(emotions.withScore(emotion, 1))
                                        showPalette = false
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                EmotionCircleBadge(
                                    emotion = emotion,
                                    size = 28.dp,
                                    alpha = if (isAdded) 0.4f else 1f
                                )
                                Text(
                                    text = emotion.label,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF666666).copy(alpha = if (isAdded) 0.4f else 1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddEmotionButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, Color(0xFFC8C2B0))
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "＋ ", fontWeight = FontWeight.Bold, color = Color(0xFF888888))
            Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF888888))
        }
    }
}

@Composable
private fun EmotionSegmentRow(
    emotion: EmotionType,
    value: Int,
    onValueChanged: (Int) -> Unit,
    onRemove: () -> Unit
) {
    val theme = emotionTheme(emotion)
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                EmotionCircleBadge(emotion = emotion, size = 32.dp)
            }
            Text(
                text = emotion.label,
                modifier = Modifier
                    .width(36.dp)
                    .padding(start = 8.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                (0..3).forEach { score ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onValueChanged(score) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (score == value) theme.main else Color(0xFFF5F2EA)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = score.toString(),
                                color = if (score == value) Color.White else Color(0xFF6B6660),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                Text("×", color = Color(0xFFCCCCCC), fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun CollapsibleActionTypeEditor(
    selectedType: ActionType?,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSelected: (ActionType) -> Unit,
    onClear: () -> Unit
) {
    val currentType = selectedType ?: ActionType.CHALLENGE
    val selectedSpec = currentType.toUiSpec()
    val chevronRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron_rotation")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpanded() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(selectedSpec.color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(selectedSpec.iconRes),
                    contentDescription = currentType.label,
                    tint = selectedSpec.color,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = "種類",
                    fontSize = 9.sp,
                    color = Color(0xFF999999),
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = currentType.label,
                    fontSize = 14.sp,
                    color = Color(0xFF1A1A1A),
                    fontWeight = FontWeight.Bold
                )
            }
            if (!expanded) {
                Text(
                    text = "×",
                    color = Color(0xFFCCCCCC),
                    fontSize = 18.sp,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onClear() },
                    textAlign = TextAlign.Center
                )
            }
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFF3F0E8))
            )
            Spacer(modifier = Modifier.height(14.dp))
            ActionTypeGrid(selectedType = currentType, onSelected = onSelected)
        }
    }
}

@Composable
private fun ActionTypeGrid(
    selectedType: ActionType?,
    onSelected: (ActionType) -> Unit
) {
    val allActionTypes = listOf(
        ActionType.EXERCISED,
        ActionType.SOCIALIZED,
        ActionType.DELEGATE,
        ActionType.CHALLENGE,
        ActionType.BREAKDOWN,
        ActionType.INSTRUCT,
        ActionType.QUICK_ACTION,
        ActionType.PENDING_TASK,
        ActionType.MEETING_STRESS,
        ActionType.SMARTPHONE_DRIFT,
        ActionType.ALCOHOL,
        ActionType.HANGOVER
    )
    allActionTypes.chunked(4).forEach { rowTypes ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rowTypes.forEach { type ->
                val uiSpec = type.toUiSpec()
                val isSelected = type == selectedType
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) uiSpec.color.copy(alpha = 0.18f) else Color(0xFFF5F2EA))
                        .border(
                            if (isSelected) BorderStroke(1.5.dp, uiSpec.color) else BorderStroke(0.dp, Color.Transparent),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelected(type) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = uiSpec.iconRes),
                        contentDescription = type.label,
                        modifier = Modifier.size(18.dp),
                        tint = if (isSelected) uiSpec.color else Color(0xFF6B6660)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = type.label,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3D3A34),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun EmotionSliderRow(
    iconRes: Int,
    value: Float,
    label: String,
    onChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.width(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }

        Slider(
            value = value,
            onValueChange = onChange,
            //valueRange = 0f..5f,
            valueRange = 0f..3f,
            steps = 2,
            modifier = Modifier
                .weight(1f)
                .scale(scaleX = 1f, scaleY = 0.7f)
        )

        Text(
            text = value.toInt().toString(),
            modifier = Modifier.width(24.dp),
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun ActionFlagIconButton(
    modifier: Modifier = Modifier,
    iconRes: Int,
    label: String,
    checked: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val bgColor = if (checked) {
        activeColor.copy(alpha = 0.18f)
    } else {
        Color(0xFFF3F3F3)
    }

    val contentColor = if (checked) {
        activeColor
    } else {
        Color.Gray
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatusIconBox(
    message: MessageV2,
    modifier: Modifier = Modifier
) {
    val display = message.flags.firstEnabledActionOrNull()?.toStatusUi()
        ?: message.emotions.maxEmotionOrNull()?.toStatusUi()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (display != null) {
            Surface(
                color = display.color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = display.iconRes),
                        contentDescription = display.label,
                        tint = display.color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChildStatusIconBox(
    message: MessageV2,
    modifier: Modifier = Modifier
) {
    val display = message.flags.firstEnabledActionOrNull()?.toStatusUi()
        ?: message.emotions.maxEmotionOrNull()?.toStatusUi()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (display != null) {
            Surface(
                color = display.color.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = display.iconRes),
                        contentDescription = display.label,
                        tint = display.color,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

private data class StatusUi(
    val label: String,
    val iconRes: Int,
    val color: Color
)

private fun ActionType.toStatusUi(): StatusUi {
    val uiSpec = toUiSpec()
    return StatusUi(
        label = label,
        iconRes = uiSpec.iconRes,
        color = uiSpec.color
    )
}

private fun EmotionType.toStatusUi(): StatusUi {
    val uiSpec = toUiSpec()
    return StatusUi(
        label = label,
        iconRes = uiSpec.iconRes,
        color = uiSpec.color
    )
}

private fun EmotionMetrics.withScore(type: EmotionType, score: Int): EmotionMetrics {
    return when (type) {
        EmotionType.ANXIETY -> copy(anxiety = score)
        EmotionType.ANGRY -> copy(angry = score)
        EmotionType.SAD -> copy(sad = score)
        EmotionType.HAPPY -> copy(happy = score)
        EmotionType.CALM -> copy(calm = score)
    }
}

private fun ActionFlags.toggle(type: ActionType): ActionFlags {
    return when (type) {
        ActionType.EXERCISED -> copy(exercised = !exercised)
        ActionType.SOCIALIZED -> copy(socialized = !socialized)
        ActionType.DELEGATE -> copy(delegate = !delegate)
        ActionType.CHALLENGE -> copy(challenge = !challenge)
        ActionType.BREAKDOWN -> copy(breakdown = !breakdown)
        ActionType.INSTRUCT -> copy(instruct = !instruct)
        ActionType.QUICK_ACTION -> copy(quickAction = !quickAction)

        ActionType.PENDING_TASK -> copy(pendingTask = !pendingTask)
        ActionType.MEETING_STRESS -> copy(meetingStress = !meetingStress)
        ActionType.SMARTPHONE_DRIFT -> copy(smartphoneDrift = !smartphoneDrift)
        ActionType.ALCOHOL -> copy(alcohol = !alcohol)
        ActionType.HANGOVER -> copy(hangover = !hangover)
    }
}

private fun ActionFlags.selectOnly(type: ActionType): ActionFlags {
    return ActionFlags().toggle(type)
}

private data class EmotionTheme(val main: Color, val light: Color)

private fun emotionTheme(type: EmotionType): EmotionTheme = when (type) {
    EmotionType.ANXIETY -> EmotionTheme(Color(0xFF9333EA), Color(0xFFF3E8FF))
    EmotionType.ANGRY -> EmotionTheme(Color(0xFFDC2626), Color(0xFFFEE2E2))
    EmotionType.SAD -> EmotionTheme(Color(0xFF6366F1), Color(0xFFE0E7FF))
    EmotionType.HAPPY -> EmotionTheme(Color(0xFFB45309), Color(0xFFFEF3C7))
    EmotionType.CALM -> EmotionTheme(Color(0xFF3B82F6), Color(0xFFDBEAFE))
}

@Composable
private fun EmotionCircleBadge(
    emotion: EmotionType,
    size: androidx.compose.ui.unit.Dp,
    alpha: Float = 1f
) {
    val ui = emotion.toUiSpec()
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(ui.color.copy(alpha = 0.16f * alpha)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = ui.iconRes),
            contentDescription = emotion.label,
            tint = ui.color.copy(alpha = alpha),
            modifier = Modifier.size((size.value * 0.56f).dp)
        )
    }
}

private fun showTimestampPicker(
    context: Context,
    initialTimestamp: Long,
    onSelected: (Long) -> Unit
) {
    val initialCal = Calendar.getInstance().apply {
        timeInMillis = initialTimestamp
    }

    val originalSecond = initialCal.get(Calendar.SECOND)
    val originalMillis = initialCal.get(Calendar.MILLISECOND)

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val resultCal = Calendar.getInstance().apply {
                timeInMillis = initialTimestamp
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }

            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    resultCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    resultCal.set(Calendar.MINUTE, minute)
                    resultCal.set(Calendar.SECOND, originalSecond)
                    resultCal.set(Calendar.MILLISECOND, originalMillis)

                    onSelected(resultCal.timeInMillis)
                },
                initialCal.get(Calendar.HOUR_OF_DAY),
                initialCal.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(context)
            ).show()
        },
        initialCal.get(Calendar.YEAR),
        initialCal.get(Calendar.MONTH),
        initialCal.get(Calendar.DAY_OF_MONTH)
    ).show()
}
