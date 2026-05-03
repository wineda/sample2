package com.example.sample2.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatTime(ts: Long): String {

    val cal = Calendar.getInstance()
    cal.timeInMillis = ts

    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))
}

fun getTimeSlot(hour: Int): String {
    return when (hour) {
        in 5 until 11 -> "朝"
        in 11 until 16 -> "昼"
        in 16 until 19 -> "夕"
        else -> "夜"
    }
}

fun formatDate(ts: Long): String {
    val sdf = SimpleDateFormat("M月d日(E)", Locale.JAPANESE)
    return sdf.format(Date(ts))
}

fun getWeekday(ts: Long): Int {
    val cal = Calendar.getInstance()
    cal.timeInMillis = ts
    return cal.get(Calendar.DAY_OF_WEEK) - 1
}

fun getDateKey(ts: Long): String {
    val sdf = SimpleDateFormat("yyyyMMdd", Locale.JAPANESE)
    return sdf.format(Date(ts))
}

fun getMondayStartOfWeekMillis(timestamp: Long): Long {
    val cal = Calendar.getInstance(Locale.JAPAN).apply {
        timeInMillis = timestamp
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        val dayOfWeek = get(Calendar.DAY_OF_WEEK)
        val diff = if (dayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - dayOfWeek
        add(Calendar.DAY_OF_MONTH, diff)
    }
    return cal.timeInMillis
}

fun plusWeeks(timestamp: Long, weeks: Int): Long {
    return Calendar.getInstance(Locale.JAPAN).apply {
        timeInMillis = timestamp
        add(Calendar.WEEK_OF_YEAR, weeks)
    }.timeInMillis
}

fun formatWeekRangeLabel(weekStartMillis: Long): String {
    val start = Calendar.getInstance(Locale.JAPAN).apply {
        timeInMillis = weekStartMillis
    }
    val end = Calendar.getInstance(Locale.JAPAN).apply {
        timeInMillis = weekStartMillis
        add(Calendar.DAY_OF_MONTH, 6)
    }
    val formatter = SimpleDateFormat("M月d日(E)", Locale.JAPAN)
    return "${formatter.format(start.time)} - ${formatter.format(end.time)}"
}

fun isCurrentWeek(weekStartMillis: Long): Boolean {
    return weekStartMillis == getMondayStartOfWeekMillis(System.currentTimeMillis())
}
