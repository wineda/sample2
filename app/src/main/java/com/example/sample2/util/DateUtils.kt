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
