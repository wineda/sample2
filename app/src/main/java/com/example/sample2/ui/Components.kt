package com.example.sample2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.sample2.ui.theme.AppShapeTokens
import androidx.compose.ui.unit.dp



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

