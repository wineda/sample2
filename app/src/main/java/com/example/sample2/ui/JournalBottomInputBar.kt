package com.example.sample2.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Adjust
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sample2.data.TriggerKind
import com.example.sample2.ui.theme.appColors

/**
 * 全画面で常時表示される、メモ作成用のボトムバー入力UI。
 *
 * 構成:
 *   通常状態 (折りたたみ):
 *     [きっかけアイコン] [本文プレースホルダー] [送信ボタン]
 *
 *   フォーカス状態 (展開):
 *     ┌── 複数行 TextField ──┐
 *     [きっかけアイコン] [入力中プレビュー] [送信ボタン]
 *
 *   きっかけアイコンタップ → 2x2 ポップオーバー (4種から排他選択)
 *
 * 親 (ChatRoute) は state を保持し、onSend で MessageV2 を組み立てる。
 */
@Composable
fun JournalBottomInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    trigger: TriggerKind?,
    onTriggerSelected: (TriggerKind?) -> Unit,
    focused: Boolean,
    onFocusedChange: (Boolean) -> Unit,
    onSend: () -> Unit,
    triggerPopoverVisible: Boolean,
    onTriggerPopoverVisibleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomFocusRequester = remember { FocusRequester() }
    val expandedFocusRequester = remember { FocusRequester() }
    val canSend = text.isNotBlank()

    LaunchedEffect(focused) {
        if (focused) {
            expandedFocusRequester.requestFocus()
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Column {
            AnimatedVisibility(
                visible = focused,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.appColors.dividerColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 220.dp)
                            .padding(14.dp)
                            .focusRequester(expandedFocusRequester)
                            .onFocusChanged { fs ->
                                if (fs.isFocused != focused) onFocusedChange(fs.isFocused)
                            },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            color = MaterialTheme.appColors.inkPrimary,
                            lineHeight = 22.sp
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Default
                        ),
                        decorationBox = { innerField ->
                            Box {
                                if (text.isEmpty()) {
                                    Text(
                                        text = "ここに本文を書く…",
                                        color = MaterialTheme.appColors.inkTertiary,
                                        fontSize = 14.sp
                                    )
                                }
                                innerField()
                            }
                        }
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.appColors.dividerColor,
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TriggerIconButton(
                        trigger = trigger,
                        onClick = { onTriggerPopoverVisibleChange(!triggerPopoverVisible) }
                    )

                    BasicTextField(
                        value = if (focused) text else "",
                        onValueChange = onTextChange,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.appColors.surfaceInactive)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .onFocusChanged { fs ->
                                if (fs.isFocused && !focused) onFocusedChange(true)
                            }
                            .focusRequester(bottomFocusRequester),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 13.sp,
                            color = MaterialTheme.appColors.inkPrimary
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Default
                        ),
                        decorationBox = { innerField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                val showPlaceholder = !focused && text.isEmpty()
                                if (showPlaceholder) {
                                    Text(
                                        text = "いま、何があった？",
                                        color = MaterialTheme.appColors.inkTertiary,
                                        fontSize = 13.sp
                                    )
                                } else if (!focused && text.isNotEmpty()) {
                                    Text(
                                        text = text,
                                        color = MaterialTheme.appColors.inkPrimary,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                }
                                innerField()
                            }
                        }
                    )

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (canSend) MaterialTheme.appColors.inkPrimary
                                else MaterialTheme.appColors.dividerSoft
                            )
                            .clickable(enabled = canSend) {
                                onSend()
                                onTriggerPopoverVisibleChange(false)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowUpward,
                            contentDescription = "送信",
                            tint = if (canSend) Color.White else MaterialTheme.appColors.inkTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        if (triggerPopoverVisible) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.appColors.dividerColor),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp)
                    .offset(y = (-132).dp)
                    .width(220.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "きっかけ",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.appColors.inkSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    val triggers = TriggerKind.values().toList()
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (i in triggers.indices step 2) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TriggerTile(
                                    kind = triggers[i],
                                    isSelected = triggers[i] == trigger,
                                    onClick = {
                                        onTriggerSelected(if (triggers[i] == trigger) null else triggers[i])
                                        onTriggerPopoverVisibleChange(false)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                if (i + 1 < triggers.size) {
                                    TriggerTile(
                                        kind = triggers[i + 1],
                                        isSelected = triggers[i + 1] == trigger,
                                        onClick = {
                                            onTriggerSelected(if (triggers[i + 1] == trigger) null else triggers[i + 1])
                                            onTriggerPopoverVisibleChange(false)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TriggerIconButton(
    trigger: TriggerKind?,
    onClick: () -> Unit
) {
    val color = trigger?.color ?: MaterialTheme.appColors.inkSecondary
    val bgColor = trigger?.color?.copy(alpha = 0.12f) ?: MaterialTheme.appColors.surfaceInactive
    val icon: ImageVector = trigger?.icon ?: Icons.Outlined.Adjust

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = if (trigger != null) color else MaterialTheme.appColors.dividerColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = trigger?.label ?: "きっかけを選ぶ",
            tint = color,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun TriggerTile(
    kind: TriggerKind,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) kind.color.copy(alpha = 0.15f)
                else MaterialTheme.appColors.surfaceInactive
            )
            .border(
                BorderStroke(
                    if (isSelected) 1.5.dp else 1.dp,
                    if (isSelected) kind.color else MaterialTheme.appColors.dividerColor
                ),
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = kind.icon,
            contentDescription = kind.label,
            tint = if (isSelected) kind.color else MaterialTheme.appColors.inkSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = kind.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.appColors.inkPrimary
        )
    }
}
