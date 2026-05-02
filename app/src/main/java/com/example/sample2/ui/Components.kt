package com.example.sample2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.example.sample2.ui.theme.AppShapeTokens
import com.example.sample2.ui.theme.SemanticColors
import com.example.sample2.ui.theme.appColors
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



@Composable
fun VerticalScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {

    val layoutInfo = listState.layoutInfo
    val totalItems = layoutInfo.totalItemsCount
    val visibleItems = layoutInfo.visibleItemsInfo

    if (totalItems == 0 || visibleItems.isEmpty()) return

    val firstVisible = visibleItems.first().index
    val visibleCount = visibleItems.size

    val proportion = visibleCount.toFloat() / totalItems
    val offset = firstVisible.toFloat() / totalItems

    @Suppress("UnusedBoxWithConstraintsScope")
    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(4.dp)
    ) {
        val height = maxHeight

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height * proportion)
                .offset(y = height * offset)
                .background(MaterialTheme.appColors.inkTertiary, AppShapeTokens.Tech)
        )
    }
}

@Composable
fun JournalTopHeader(
    title: String,
    subtitle: String?,
    navigationIcon: ImageVector,
    navigationContentDescription: String,
    onNavigationClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompactHeaderIconButton(
            selected = false,
            onClick = onNavigationClick,
            icon = navigationIcon,
            contentDescription = navigationContentDescription
        )
        Spacer(modifier = Modifier.size(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.inkPrimary
            )
            subtitle?.let {
                Text(
                    text = it,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.appColors.inkTertiary
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
}

@Composable
fun CompactHeaderIconButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    showNotificationDot: Boolean = false,
    modifier: Modifier = Modifier
) {
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        modifier = modifier.size(40.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        contentColor = contentColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(22.dp)
            )
            if (showNotificationDot) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 9.dp, end = 8.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(SemanticColors.NegativeMain)
                )
            }
        }
    }
}
