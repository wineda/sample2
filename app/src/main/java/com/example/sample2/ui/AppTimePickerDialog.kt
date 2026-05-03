package com.example.sample2.ui

import android.app.TimePickerDialog
import android.content.Context
import java.util.Calendar

private const val AppTimePickerTitle = "時刻の設定"

fun showAppTimePickerDialog(
    context: Context,
    initialHour: Int,
    initialMinute: Int,
    onSelected: (hour: Int, minute: Int) -> Unit
) {
    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onSelected(hourOfDay, minute)
        },
        initialHour,
        initialMinute,
        true
    ).apply {
        setTitle(AppTimePickerTitle)
    }.show()
}

fun showAppTimePickerDialog(
    context: Context,
    initialTimestamp: Long,
    onSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = initialTimestamp
    }

    showAppTimePickerDialog(
        context = context,
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE)
    ) { hour, minute ->
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        onSelected(calendar.timeInMillis)
    }
}
