package com.example.sample2.ui.journal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sample2.data.MessageV2
import com.example.sample2.data.firstEnabledActionOrNull
import com.example.sample2.data.maxEmotionOrNull
import com.example.sample2.ui.StatusIconBox
import com.example.sample2.ui.categoryColorFor
import com.example.sample2.ui.theme.Spacing
import com.example.sample2.ui.theme.appColors
import com.example.sample2.util.formatTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThreadHead(
    parent: MessageV2,
    expanded: Boolean,
    isSingleLineMode: Boolean,
    onToggleExpand: () -> Unit,
    onLongPress: () -> Unit,
    onDoubleClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val categoryColor = categoryColorFor(parent)
    val categoryLabel = parent.flags.firstEnabledActionOrNull()?.label
        ?: parent.emotions.maxEmotionOrNull()?.label
        ?: "メモ"
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 0f else -90f,
        label = "thread-toggle"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = onToggleExpand,
                onDoubleClick = onDoubleClick,
                onLongClick = onLongPress
            )
            .padding(horizontal = Spacing.md, vertical = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        StatusIconBox(
            message = parent,
            modifier = Modifier.size(28.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(
                    text = categoryLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = categoryColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "·",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.appColors.inkTertiary,
                )
                Text(
                    text = formatTime(parent.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.appColors.inkSecondary,
                )
            }

            Text(
                text = parent.text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.16).sp,
                ),
                color = MaterialTheme.appColors.inkPrimary,
                maxLines = if (isSingleLineMode) 1 else 3,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(Spacing.xs))
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = if (expanded) "折りたたむ" else "展開する",
            modifier = Modifier
                .padding(top = Spacing.xs)
                .size(18.dp)
                .graphicsLayer { rotationZ = rotation },
            tint = MaterialTheme.appColors.inkTertiary,
        )
    }
}
