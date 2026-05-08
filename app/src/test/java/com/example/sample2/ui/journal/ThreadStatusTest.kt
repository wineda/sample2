package com.example.sample2.ui.journal

import com.example.sample2.data.EmotionMetrics
import com.example.sample2.data.JournalEntryType
import com.example.sample2.data.MessageV2
import org.junit.Assert.assertEquals
import org.junit.Test

class ThreadStatusTest {
    @Test
    fun `negative parent without children derives unresolved`() {
        val parent = message(
            id = "parent",
            emotions = EmotionMetrics(angry = 1)
        )

        assertEquals(ThreadStatus.Unresolved, deriveThreadStatus(parent, emptyList()))
    }

    @Test
    fun `negative parent with children derives in progress`() {
        val parent = message(
            id = "parent",
            emotions = EmotionMetrics(anxiety = 1)
        )
        val child = message(
            id = "child",
            parentId = parent.id,
            entryType = JournalEntryType.EMOTION_RESPONSE,
        )

        assertEquals(ThreadStatus.InProgress, deriveThreadStatus(parent, listOf(child)))
    }

    @Test
    fun `non negative parent derives record only`() {
        val parent = message(
            id = "parent",
            emotions = EmotionMetrics(happy = 1)
        )
        val child = message(
            id = "child",
            parentId = parent.id,
            entryType = JournalEntryType.EMOTION_RESPONSE,
        )

        assertEquals(ThreadStatus.RecordOnly, deriveThreadStatus(parent, listOf(child)))
    }

    private fun message(
        id: String,
        emotions: EmotionMetrics = EmotionMetrics(),
        parentId: String? = null,
        entryType: JournalEntryType = JournalEntryType.MEMO,
    ): MessageV2 = MessageV2(
        id = id,
        timestamp = 0L,
        text = id,
        emotions = emotions,
        parentId = parentId,
        entryType = entryType,
    )
}
