package com.example.sample2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sample2.util.getDateKey
import kotlinx.coroutines.launch

@Composable
fun <T> JournalMessageListPane(
    messages: List<T>,
    listState: LazyListState,
    isSingleLineMode: Boolean,
    timestampOf: (T) -> Long,
    modifier: Modifier = Modifier,
    itemContent: @Composable (
        message: T,
        isConnectedToPreviousInDay: Boolean,
        isConnectedToNextInDay: Boolean
    ) -> Unit
) {
    val scope = rememberCoroutineScope()

    val isAtBottom by remember(listState, messages) {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            lastVisibleIndex == messages.lastIndex
        }
    }


    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        VerticalScrollbar(
            listState = listState,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 2.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(messages) { index, message ->
                val previous = messages.getOrNull(index - 1)
                val next = messages.getOrNull(index + 1)
                val currentTimestamp = timestampOf(message)
                val currentDateKey = getDateKey(currentTimestamp)
                val isConnectedToPreviousInDay =
                    previous != null && getDateKey(timestampOf(previous)) == currentDateKey
                val isConnectedToNextInDay =
                    next != null && getDateKey(timestampOf(next)) == currentDateKey

                if (
                    previous == null ||
                    !isConnectedToPreviousInDay
                ) {
                    DateLabel(currentTimestamp)
                }

                itemContent(
                    message,
                    isConnectedToPreviousInDay,
                    isConnectedToNextInDay
                )
            }
        }

        if (!isAtBottom && messages.isNotEmpty()) {
            ScrollToBottomButton(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(messages.lastIndex)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun ScrollToBottomButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50)
            )
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "bottom"
        )
    }
}
