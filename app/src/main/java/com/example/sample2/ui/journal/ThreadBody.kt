package com.example.sample2.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sample2.data.MessageV2
import com.example.sample2.ui.theme.Spacing
import com.example.sample2.ui.theme.appColors

/**
 * スレッド展開時の子要素一覧。
 *
 * - 上端にセパレータ
 * - 子要素間に 1px の薄いセパレータ
 * - 全体に薄い背景（surfaceQuiet）
 */
@Composable
fun ThreadBody(
    parent: MessageV2,
    children: List<MessageV2>,
    onLongPressChild: (MessageV2) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (children.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.appColors.surfaceQuiet),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.appColors.dividerSoft),
        )
        Spacer(Modifier.height(Spacing.xxs))

        children.forEachIndexed { index, child ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .padding(start = 64.dp, end = Spacing.sm)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.appColors.dividerSoft.copy(alpha = 0.5f)),
                )
            }
            ThreadChildRow(
                child = child,
                parentTimestamp = parent.timestamp,
                isFirst = index == 0,
                isLast = index == children.lastIndex,
                onLongPress = onLongPressChild,
            )
        }

        Spacer(Modifier.height(Spacing.xxs))
    }
}
