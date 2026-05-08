package com.example.sample2.ui.journal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChildRelativeTimeTest {
    @Test
    fun `same timestamp returns null`() {
        assertNull(formatRelativeFromParent(BASE, BASE))
    }

    @Test
    fun `less than one minute returns null`() {
        assertNull(formatRelativeFromParent(BASE, BASE + seconds(30)))
    }

    @Test
    fun `five minutes returns minute label`() {
        assertEquals("+5min", formatRelativeFromParent(BASE, BASE + minutes(5)))
    }

    @Test
    fun `sixty minutes returns hour label`() {
        assertEquals("+1h", formatRelativeFromParent(BASE, BASE + minutes(60)))
    }

    @Test
    fun `ninety minutes returns hour and minute label`() {
        assertEquals("+1h 30m", formatRelativeFromParent(BASE, BASE + minutes(90)))
    }

    @Test
    fun `twenty five hours returns day label`() {
        assertEquals("+1d", formatRelativeFromParent(BASE, BASE + hours(25)))
    }

    @Test
    fun `timestamp before parent returns null`() {
        assertNull(formatRelativeFromParent(BASE, BASE - minutes(5)))
    }

    private companion object {
        const val BASE = 1_700_000_000_000L
    }
}

private fun seconds(value: Long): Long = value * 1000L
private fun minutes(value: Long): Long = seconds(value * 60L)
private fun hours(value: Long): Long = minutes(value * 60L)
