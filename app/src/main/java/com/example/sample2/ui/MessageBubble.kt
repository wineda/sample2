package com.example.sample2.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.sample2.util.formatDate
import com.example.sample2.util.formatTime
import java.util.Calendar

private val MessageRowHorizontalPadding = 16.dp

private val BubbleRightPadding = 12.dp
private val BubbleTextHorizontalPadding = 14.dp
private val BubbleStartIndent =
    MessageRowHorizontalPadding +
            46.dp

private val BubbleTextVerticalPadding = 10.dp
private val BubbleTextVerticalPaddingCompact = 4.dp
private val ChildBubbleIndent: Dp = MessageRowHorizontalPadding + 20.dp
private val ChildTimeColumnWidth = 44.dp
private val ChildStatusColumnWidth = 28.dp
private val ChildBubbleRightPadding = 20.dp
private val CategoryBarWidth = 4.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: MessageV2,
    state: JournalViewModel,
    onDelete: () -> Unit,
    onUpdate: (MessageV2) -> Unit,
    onDoubleClick: (MessageV2) -> Unit = {}
) {
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
            .padding(horizontal = MessageRowHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val context = LocalContext.current

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = BubbleRightPadding),
            verticalAlignment = Alignment.CenterVertically
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
    val dateText = formatDate(timestamp)
    val relative = getRelativeLabel(timestamp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = MessageRowHorizontalPadding,
                end = MessageRowHorizontalPadding,
                top = 8.dp,
                bottom = 8.dp
            ),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            color = Color(0xFFE0E0E0),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "$dateText - $relative",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

fun getRelativeLabel(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val days = diff / (1000L * 60 * 60 * 24)

    return when {
        days < 1 -> "今日"
        days < 14 -> "${days}D"
        days < 60 -> "${days / 7}W"
        else -> "${days / 30}M"
    }
}

@Composable
fun MessageActionOverlay(
    message: MessageV2,
    state: JournalViewModel,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (MessageV2) -> Unit
) {
    var editingEmotions by remember(message.id) { mutableStateOf(message.emotions) }
    var editingFlags by remember(message.id) { mutableStateOf(message.flags) }

    val scrollState = rememberScrollState()
    val blockClicks = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.14f))
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
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BubbleColor.copy(alpha = 0.96f),
                tonalElevation = 2.dp,
                shadowElevation = 6.dp
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(12.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = TextColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                tonalElevation = 2.dp,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionType.entries.chunked(4).forEach { rowTypes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowTypes.forEach { type ->
                                val uiSpec = type.toUiSpec()
                                ActionFlagIconButton(
                                    modifier = Modifier.weight(1f),
                                    iconRes = uiSpec.iconRes,
                                    label = type.label,
                                    checked = type.matches(editingFlags),
                                    activeColor = uiSpec.color,
                                    onClick = {
                                        editingFlags = editingFlags.toggle(type)
                                    }
                                )
                            }

                            repeat(4 - rowTypes.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                tonalElevation = 2.dp,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    EmotionType.entries.forEach { type ->
                        val uiSpec = type.toUiSpec()
                        EmotionSliderRow(
                            iconRes = uiSpec.iconRes,
                            value = type.scoreOf(editingEmotions).toFloat(),
                            label = type.label,
                            onChange = { newValue ->
                                editingEmotions =
                                    editingEmotions.withScore(type, newValue.toInt())
                            }
                        )
                    }
                }
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
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(
                        onClick = {
                            onUpdate(
                                message.copy(
                                    emotions = editingEmotions,
                                    flags = editingFlags
                                )
                            )
                            onDismiss()
                        }
                    ) {
                        Text("保存")
                    }

                    TextButton(
                        onClick = {
                            onDelete()
                            onDismiss()
                        }
                    ) {
                        Text("削除", color = Color.Red)
                    }

                    TextButton(
                        onClick = { onDismiss() }
                    ) {
                        Text("閉じる")
                    }
                }
            }
        }
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
