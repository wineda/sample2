package com.example.sample2.ui

import android.app.AlertDialog
import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Calendar

private const val AppTimePickerTitle = "時刻の設定"

fun showAppTimePickerDialog(
    context: Context,
    initialHour: Int,
    initialMinute: Int,
    onSelected: (hour: Int, minute: Int) -> Unit
) {
    val hourInput = EditText(context).apply {
        inputType = InputType.TYPE_CLASS_NUMBER
        filters = arrayOf(InputFilter.LengthFilter(2))
        setText(initialHour.toString().padStart(2, '0'))
        gravity = Gravity.CENTER
        textSize = 28f
        minEms = 2
    }
    val minuteInput = EditText(context).apply {
        inputType = InputType.TYPE_CLASS_NUMBER
        filters = arrayOf(InputFilter.LengthFilter(2))
        setText(initialMinute.toString().padStart(2, '0'))
        gravity = Gravity.CENTER
        textSize = 28f
        minEms = 2
    }

    val content = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        setPadding(48, 32, 48, 8)
        addView(hourInput)
        addView(TextView(context).apply {
            text = " : "
            textSize = 28f
            gravity = Gravity.CENTER
        })
        addView(minuteInput)
    }

    AlertDialog.Builder(context)
        .setTitle(AppTimePickerTitle)
        .setView(content)
        .setNegativeButton("キャンセル", null)
        .setPositiveButton("OK") { _, _ ->
            val hour = hourInput.text.toString().toIntOrNull()?.coerceIn(0, 23) ?: initialHour
            val minute = minuteInput.text.toString().toIntOrNull()?.coerceIn(0, 59) ?: initialMinute
            onSelected(hour, minute)
        }
        .show()
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
