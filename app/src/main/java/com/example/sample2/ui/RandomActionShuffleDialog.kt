package com.example.sample2.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sample2.model.RandomActionsSource
import com.example.sample2.model.RandomActionsSource.RandomAction
import com.example.sample2.ui.theme.MonoTypography
import com.example.sample2.ui.theme.appColors
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * 「明日やること」をランダム抽選するダイアログ。
 *
 * 表示直後と「もう一回」タップで、短いシャッフル演出（候補が高速に切り替わる）の後に
 * 結果が確定する。「これにする」で確定値を返す。
 */
@Composable
fun RandomActionShuffleDialog(
    usedIds: Set<Int>,
    onConfirm: (RandomAction) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val all = remember { RandomActionsSource.load(context) }
    val accent = Color(0xFFE53935) // 「明日やること」と同じ赤

    // 抽選トリガー: 値が変わるたびにシャッフル演出をやり直す
    var shuffleNonce by remember { mutableIntStateOf(0) }
    // 演出中に表示するダミー候補
    var displayedAction by remember { mutableStateOf<RandomAction?>(null) }
    // 演出が終わって確定した結果
    var finalAction by remember { mutableStateOf<RandomAction?>(null) }
    var isShuffling by remember { mutableStateOf(true) }

    LaunchedEffect(shuffleNonce) {
        isShuffling = true
        finalAction = null
        // ソシャゲ風のシャッフル演出: 約1.2秒間、徐々に減速しながら候補を切り替える
        val frames = listOf(50L, 50L, 60L, 70L, 90L, 120L, 160L, 220L, 320L)
        repeat(frames.size) { i ->
            displayedAction = all[Random.nextInt(all.size)]
            delay(frames[i])
        }
        // 最後に正式な抽選（使用済み除外）を行う
        val picked = RandomActionsSource.pickRandom(context, usedIds)
        displayedAction = picked
        finalAction = picked
        isShuffling = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { finalAction?.let { onConfirm(it) } },
                enabled = !isShuffling && finalAction != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("これにする")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onDismiss) {
                    Text("キャンセル")
                }
                TextButton(
                    onClick = { shuffleNonce++ },
                    enabled = !isShuffling
                ) {
                    Icon(Icons.Outlined.Casino, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("もう一回")
                }
            }
        },
        title = {
            Column {
                Text(
                    text = "明日やること",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = accent,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "365個からランダム",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            ResultBox(
                action = displayedAction,
                isShuffling = isShuffling,
                accent = accent
            )
        }
    )
}

@Composable
private fun ResultBox(
    action: RandomAction?,
    isShuffling: Boolean,
    accent: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.06f))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (action == null) {
            Text(
                text = "...",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.appColors.inkTertiary
            )
        } else {
            AnimatedContent(
                targetState = action,
                transitionSpec = {
                    fadeIn(animationSpec = tween(120)) togetherWith fadeOut(animationSpec = tween(80))
                },
                label = "shuffleResult"
            ) { current ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "#${current.id}",
                        style = MonoTypography.Numeric.copy(
                            color = accent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = current.text,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 24.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = if (isShuffling) {
                            MaterialTheme.appColors.inkSecondary
                        } else {
                            MaterialTheme.appColors.inkPrimary
                        }
                    )
                }
            }
        }
    }
}
