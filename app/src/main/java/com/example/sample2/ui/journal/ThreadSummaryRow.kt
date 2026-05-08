package com.example.sample2.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.sample2.ui.theme.AppShapeTokens
import com.example.sample2.ui.theme.MonoTypography
import com.example.sample2.ui.theme.Spacing
import com.example.sample2.ui.theme.appColors

@Composable
fun ThreadSummaryRow(
    status: ThreadStatus,
    actionCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 52.dp, end = Spacing.md, bottom = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusPill(status = status)
        Text(
            text = "行動 ${actionCount} 件",
            style = MonoTypography.Caption,
            color = MaterialTheme.appColors.inkTertiary,
        )
    }
}

@Composable
private fun StatusPill(status: ThreadStatus) {
    Row(
        modifier = Modifier
            .clip(AppShapeTokens.Pill)
            .background(status.backColor())
            .padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(status.frontColor())
        )
        Text(
            text = status.label,
            style = MonoTypography.Caption,
            color = status.frontColor(),
        )
    }
}
