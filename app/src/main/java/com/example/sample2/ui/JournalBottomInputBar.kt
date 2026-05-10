package com.example.sample2.ui

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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Adjust
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
 * 設計上の重要な原則:
 *   - TextField は展開エリア内の 1 つだけにする
 *   - ボトムバー本体の中央エリアは TextField ではなく Box + Text
 *   - タップ受付・プレースホルダ表示・プレビュー表示はすべて Text + Box で扱う
 *   - フォーカスの一意性を保つことで、IME 入力が確実に展開エリアに届く
 *
 * 構成:
 *   通常状態 (focused=false):
 *     [きっかけアイコン] [タップ受付Box]                    [送信]
 *
 *   入力中状態 (focused=true):
 *     ┌── 唯一の TextField (複数行) ──┐
 *     [きっかけアイコン] [入力中プレビュー(Text)]            [送信]
 */
@Composable
fun JournalBottomInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    trigger: TriggerKind?,
    onTriggerSelected: (TriggerKind?) -> Unit,
    focused: Boolean,
    onFocusedChange: (Boolean) -> Unit,
    triggerPopoverVisible: Boolean,
    onTriggerPopoverVisibleChange: (Boolean) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val canSend = text.isNotBlank()

    // focused=true になったら展開エリアの TextField に確実にフォーカスを移す
    // ※ if (focused) { ... } で展開 TextField が composition に追加された直後では
    //   まだ FocusRequester が node に attach されていない可能性がある。
    //   1 フレーム待つことで attach 完了を保証する。
    LaunchedEffect(focused) {
        if (focused) {
            withFrameNanos {}
            focusRequester.requestFocus()
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Column {
            // ───── 展開エリア (フォーカス時のみ) ─────
            // AnimatedVisibility は使わない。理由:
            //   AnimatedVisibility は内部 composable の attach が遅延するため、
            //   その直後に呼ぶ FocusRequester.requestFocus() が失敗する。
            //   アニメーションは諦めて、確実な動作を優先する。
            if (focused) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.appColors.dividerColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    // ★ アプリ全体で唯一の TextField (フォーカス時のみ存在)
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 220.dp)
                            .padding(14.dp)
                            .focusRequester(focusRequester)
                            .onFocusChanged { fs ->
                                // 外タップ等でフォーカスを失ったら折りたたみ
                                if (!fs.isFocused && focused) {
                                    onFocusedChange(false)
                                }
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
                        cursorBrush = SolidColor(MaterialTheme.appColors.inkPrimary),
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

            // ───── ボトムバー本体 ─────
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.appColors.dividerColor
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // きっかけアイコン
                    TriggerIconButton(
                        trigger = trigger,
                        onClick = { onTriggerPopoverVisibleChange(!triggerPopoverVisible) }
                    )

                    // ★ 中央は TextField ではなく Box + Text
                    //   タップで focused=true にする
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.appColors.surfaceInactive)
                            .clickable { onFocusedChange(true) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        val displayText = when {
                            text.isEmpty() -> "いま、何があった？"
                            focused -> "入力中…"
                            else -> text
                        }
                        val displayColor = if (text.isEmpty() || focused) {
                            MaterialTheme.appColors.inkTertiary
                        } else {
                            MaterialTheme.appColors.inkPrimary
                        }
                        Text(
                            text = displayText,
                            color = displayColor,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }

                    // 送信ボタン
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
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "送信",
                            tint = if (canSend) Color.White else MaterialTheme.appColors.inkTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // ───── きっかけポップオーバー ─────
        if (triggerPopoverVisible) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.appColors.dividerColor),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp)
                    .offset(y = (-200).dp)
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
                    val triggers = TriggerKind.entries.toList()
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (i in triggers.indices step 2) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                TriggerTile(
                                    kind = triggers[i],
                                    isSelected = triggers[i] == trigger,
                                    onClick = {
                                        onTriggerSelected(
                                            if (triggers[i] == trigger) null else triggers[i]
                                        )
                                        onTriggerPopoverVisibleChange(false)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                if (i + 1 < triggers.size) {
                                    TriggerTile(
                                        kind = triggers[i + 1],
                                        isSelected = triggers[i + 1] == trigger,
                                        onClick = {
                                            onTriggerSelected(
                                                if (triggers[i + 1] == trigger) null else triggers[i + 1]
                                            )
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
