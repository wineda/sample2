package com.example.sample2.ui

import android.app.DatePickerDialog
import android.content.Context
import java.util.Calendar

/**
 * 日付編集用のダイアログ。
 *
 * @param initialTimestamp 編集対象の現在のタイムスタンプ（時分は据え置く）
 * @param onSelected 新しいタイムスタンプ（年月日のみ更新、時分は initialTimestamp と同じ）
 */
fun showAppDatePickerDialog(
    context: Context,
    initialTimestamp: Long,
    onSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = initialTimestamp
    }

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val updated = Calendar.getInstance().apply {
                timeInMillis = initialTimestamp
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onSelected(updated.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
