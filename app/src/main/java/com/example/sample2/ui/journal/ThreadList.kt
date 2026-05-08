package com.example.sample2.ui.journal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sample2.data.MessageV2
import com.example.sample2.ui.DateLabel
import com.example.sample2.ui.ScrollToBottomButton
import com.example.sample2.ui.VerticalScrollbar
import com.example.sample2.ui.theme.Spacing
import com.example.sample2.util.getDateKey
import kotlinx.coroutines.launch

@Composable
fun ThreadList(
    parents: List<MessageV2>,
    childrenByParentId: Map<String, List<MessageV2>>,
    listState: LazyListState,
    isSingleLineMode: Boolean,
    isExpanded: (MessageV2) -> Boolean,
    onToggleExpand: (MessageV2) -> Unit,
    onLongPressParent: (MessageV2) -> Unit,
    onLongPressChild: (MessageV2) -> Unit,
    onDoubleClickParent: (MessageV2) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    val isAtBottom by remember(listState, parents) {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            lastVisibleIndex == parents.lastIndex
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        VerticalScrollbar(
            listState = listState,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 2.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.sm),
        ) {
            itemsIndexed(parents, key = { _, parent -> parent.id }) { index, parent ->
                val previous = parents.getOrNull(index - 1)
                val isFirstOfDay = previous == null ||
                        getDateKey(parent.timestamp) != getDateKey(previous.timestamp)

                if (isFirstOfDay) {
                    DateLabel(parent.timestamp)
                }

                ThreadCard(
                    parent = parent,
                    children = childrenByParentId[parent.id].orEmpty(),
                    expanded = isExpanded(parent),
                    isSingleLineMode = isSingleLineMode,
                    onToggleExpand = { onToggleExpand(parent) },
                    onLongPressParent = { onLongPressParent(parent) },
                    onLongPressChild = onLongPressChild,
                    onDoubleClickParent = onDoubleClickParent,
                    modifier = Modifier.padding(bottom = Spacing.sm),
                )
            }
        }

        if (!isAtBottom && parents.isNotEmpty()) {
            ScrollToBottomButton(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(parents.lastIndex)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = Spacing.md)
            )
        }
    }
}
