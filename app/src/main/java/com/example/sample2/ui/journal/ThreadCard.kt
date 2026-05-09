package com.example.sample2.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sample2.data.MessageV2
import com.example.sample2.ui.components.AppCard
import com.example.sample2.ui.components.AppCardVariant
import com.example.sample2.ui.theme.appColors

@Composable
fun ThreadCard(
    parent: MessageV2,
    children: List<MessageV2>,
    expanded: Boolean,
    isSingleLineMode: Boolean,
    onToggleExpand: () -> Unit,
    onLongPressParent: () -> Unit,
    onLongPressChild: (MessageV2) -> Unit,
    onDoubleClickParent: (MessageV2) -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = remember(parent, children) { deriveThreadStatus(parent, children) }
    // 縦線色は「きっかけ」の色に統一。未設定なら薄いグレー（divider）にして主張を抑える。
    val accentColor = parent.trigger?.color ?: MaterialTheme.appColors.dividerSoft

    AppCard(
        modifier = modifier,
        variant = AppCardVariant.Outlined,
        contentPadding = PaddingValues(0.dp),
        verticalSpacing = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            BoxColorBar(
                color = accentColor,
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
            )
            Column(modifier = Modifier.weight(1f)) {
                ThreadHead(
                    parent = parent,
                    expanded = expanded,
                    isSingleLineMode = isSingleLineMode,
                    onToggleExpand = onToggleExpand,
                    onLongPress = onLongPressParent,
                    onDoubleClick = { onDoubleClickParent(parent) },
                )

                if (!expanded) {
                    ThreadSummaryRow(
                        status = status,
                        actionCount = children.size,
                    )
                } else {
                    ThreadBody(
                        parent = parent,
                        children = children,
                        onLongPressChild = onLongPressChild,
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxColorBar(
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.background(color)
    )
}
