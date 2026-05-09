package com.example.sample2.ui.journal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sample2.data.JournalEntryType
import com.example.sample2.data.MessageV2
import com.example.sample2.ui.StatusIconBox
import com.example.sample2.ui.theme.MonoTypography
import com.example.sample2.ui.theme.Spacing
import com.example.sample2.ui.theme.appColors
import com.example.sample2.util.formatTime

/**
 * スレッド展開時に1つの子要素を表示する行。
 *
 * モックの .child に相当する3カラムグリッド:
 *   [時刻 48dp] [マーカー 18dp] [コンテンツ 1fr]
 *
 * @param parentTimestamp 親メッセージのタイムスタンプ（経過時間メタ表示に使用）
 * @param isFirst リストの先頭要素か（マーカーの上半分縦線を省略）
 * @param isLast リストの末尾要素か（マーカーの下半分縦線を省略）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThreadChildRow(
    child: MessageV2,
    parentTimestamp: Long,
    isFirst: Boolean,
    isLast: Boolean,
    onLongPress: (MessageV2) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (child.entryType != JournalEntryType.EMOTION_RESPONSE) return

    val relativeText = formatRelativeFromParent(
        parentTimestamp = parentTimestamp,
        childTimestamp = child.timestamp,
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .combinedClickable(
                onClick = {},
                onLongClick = { onLongPress(child) },
            )
            .padding(horizontal = Spacing.sm, vertical = 7.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = formatTime(child.timestamp),
            style = MonoTypography.Caption,
            color = MaterialTheme.appColors.inkTertiary,
            textAlign = TextAlign.End,
            modifier = Modifier
                .width(48.dp)
                .padding(top = 2.dp),
        )

        Spacer(Modifier.width(Spacing.sm))

        ThreadChildMarker(
            child = child,
            isFirst = isFirst,
            isLast = isLast,
        )

        Spacer(Modifier.width(Spacing.sm))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 1.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            Text(
                text = child.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.appColors.inkPrimary,
            )
            if (relativeText != null) {
                Text(
                    text = relativeText,
                    style = MonoTypography.Micro,
                    color = MaterialTheme.appColors.inkTertiary,
                )
            }
        }
    }
}

@Composable
private fun ThreadChildMarker(
    child: MessageV2,
    isFirst: Boolean,
    isLast: Boolean,
) {
    Box(
        modifier = Modifier
            .width(18.dp)
            .fillMaxHeight()
            .heightIn(min = 28.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        if (!isFirst) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(8.dp)
                    .align(Alignment.TopCenter)
                    .background(MaterialTheme.appColors.dividerSoft),
            )
        }
        if (!isLast) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .padding(top = 16.dp)
                    .align(Alignment.TopCenter)
                    .background(MaterialTheme.appColors.dividerSoft),
            )
        }
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(14.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            StatusIconBox(
                message = child,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}
