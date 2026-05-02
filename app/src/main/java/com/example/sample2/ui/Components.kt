package com.example.sample2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
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

enum class JournalHeaderTitleStyle { Default, Medium }

@Composable
fun JournalTopHeader(
    title: String,
    subtitle: String? = null,
    showLiveDot: Boolean = false,
    titleStyle: JournalHeaderTitleStyle = JournalHeaderTitleStyle.Default,
    navigationIcon: ImageVector,
    navigationContentDescription: String,
    onNavigationClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    trailing: (@Composable () -> Unit)? = null,
    bottomSlot: (@Composable ColumnScope.() -> Unit)? = null,
    strongBottomBorder: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            CompactHeaderIconButton(selected = false, onClick = onNavigationClick, icon = navigationIcon, contentDescription = navigationContentDescription)
            Spacer(modifier = Modifier.size(8.dp))
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
                if (showLiveDot) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SemanticColors.PositiveMain))
                    Spacer(modifier = Modifier.width(6.dp))
                }
                val titleSize = if (titleStyle == JournalHeaderTitleStyle.Medium) 18.sp else 22.sp
                Text(text = title, fontSize = titleSize, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp, color = MaterialTheme.appColors.inkStrongAlt)
                subtitle?.let {
                    Text(text = it, modifier = Modifier.padding(start = 6.dp), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.appColors.inkTertiary)
                }
            }
            if (trailing != null) trailing() else Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically, content = actions)
        }

        bottomSlot?.let {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.appColors.dividerCool)
            Spacer(modifier = Modifier.height(10.dp))
            Column(content = it)
        }
    }
    HorizontalDivider(color = if (strongBottomBorder) MaterialTheme.appColors.inkStrongAlt else MaterialTheme.appColors.dividerCool)
}

@Composable
fun HeaderProgressStack(current: Int, total: Int, label: String, large: Boolean = false) {
    Column(horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = "$current", fontFamily = FontFamily.Monospace, fontSize = if (large) 24.sp else 18.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.appColors.inkPrimary)
            Text(text = " / $total", fontFamily = FontFamily.Monospace, fontSize = if (large) 14.sp else 12.sp, color = MaterialTheme.appColors.inkTertiary)
        }
        Text(text = label.uppercase(), fontFamily = FontFamily.Monospace, fontSize = 9.sp, letterSpacing = 1.5.sp, color = MaterialTheme.appColors.inkTertiary)
    }
}

@Composable
fun HeaderStatCell(value: String, label: String, delta: String? = null, deltaPositive: Boolean = true) {
    Column {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(text = value, fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp, color = MaterialTheme.appColors.inkPrimary)
            delta?.let {
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = it, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = if (deltaPositive) SemanticColors.PositiveMain else SemanticColors.NegativeMain)
            }
        }
        Text(text = label.uppercase(), fontFamily = FontFamily.Monospace, fontSize = 9.sp, letterSpacing = 1.5.sp, color = MaterialTheme.appColors.inkTertiary)
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
