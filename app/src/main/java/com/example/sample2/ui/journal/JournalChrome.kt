package com.example.sample2.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun JournalBottomModeBar(
    currentMode: JournalScreenMode,
    onOpenJournal: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenHeatmap: () -> Unit,
    onOpenDailyRecord: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CompactActionChip(
            text = "記録",
            icon = Icons.Default.ViewAgenda,
            selected = currentMode == JournalScreenMode.Journal,
            onClick = onOpenJournal
        )

        CompactActionChip(
            text = "分析",
            icon = Icons.Default.ShowChart,
            selected = currentMode == JournalScreenMode.Analytics,
            onClick = onOpenAnalytics
        )

        CompactActionChip(
            text = "ヒートマップ",
            icon = Icons.Default.GridView,
            selected = currentMode == JournalScreenMode.Heatmap,
            onClick = onOpenHeatmap
        )

        CompactActionChip(
            text = "日次",
            icon = Icons.Default.Today,
            selected = currentMode == JournalScreenMode.DailyRecord,
            onClick = onOpenDailyRecord
        )
    }
}

@Composable
fun JournalCompactMetaRow(
    dateLabel: String,
    hasActiveFilter: Boolean,
    isSingleLineMode: Boolean,
    onMenuClick: () -> Unit,
    onFilterClick: () -> Unit,
    onToggleSingleLine: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompactHeaderIconButton(
            selected = false,
            onClick = onMenuClick,
            icon = Icons.Default.Menu,
            contentDescription = "メニュー"
        )

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = dateLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompactHeaderIconButton(
                selected = hasActiveFilter,
                onClick = onFilterClick,
                icon = Icons.Default.FilterList,
                contentDescription = "フィルタ"
            )

            CompactHeaderIconButton(
                selected = isSingleLineMode,
                onClick = onToggleSingleLine,
                icon = if (isSingleLineMode) {
                    Icons.Default.ViewAgenda
                } else {
                    Icons.Default.ViewStream
                },
                contentDescription = if (isSingleLineMode) {
                    "通常表示に切り替え"
                } else {
                    "1行表示に切り替え"
                }
            )
        }
    }
}

@Composable
private fun CompactHeaderIconButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.size(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(16.dp)
            )

            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 4.dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun CompactActionChip(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .height(30.dp)
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )

            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
