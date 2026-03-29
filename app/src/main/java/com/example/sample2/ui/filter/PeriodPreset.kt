package com.example.sample2.ui.filter

import java.util.Calendar


enum class PeriodPreset(
    val label: String,
    val weeksAgo: Int?
) {
    THIS_WEEK("今週", 0),
    LAST_WEEK("先週", 1),
    TWO_WEEKS_AGO("2週前", 2),
    THREE_WEEKS_AGO("3週前", 3),
    ALL("全期間", null);

    fun resolveRange(): Pair<Long?, Long?> {
        if (weeksAgo == null) return null to null

        val cal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            add(Calendar.WEEK_OF_YEAR, -weeksAgo)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val from = cal.timeInMillis

        val to = if (weeksAgo == 0) {
            System.currentTimeMillis()
        } else {
            cal.add(Calendar.DAY_OF_WEEK, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
            cal.timeInMillis
        }

        return from to to
    }

    private fun set(array: Int, index: Int) {}
}
