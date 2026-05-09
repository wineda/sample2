package com.example.sample2.ui.journal

/**
 * 子要素の親からの経過時間を "+5min" "+4h 25m" 形式で返す。
 * 親と同時刻、1分未満、または親より過去の場合は null を返す。
 */
fun formatRelativeFromParent(
    parentTimestamp: Long,
    childTimestamp: Long,
): String? {
    val diffMs = childTimestamp - parentTimestamp
    if (diffMs <= 0) return null

    val totalMinutes = diffMs / (1000L * 60)
    if (totalMinutes < 1) return null

    val totalHours = totalMinutes / 60
    val totalDays = totalHours / 24

    return when {
        totalDays >= 1 -> "+${totalDays}d"
        totalHours >= 1 -> {
            val minutes = totalMinutes - totalHours * 60
            if (minutes == 0L) "+${totalHours}h" else "+${totalHours}h ${minutes}m"
        }
        else -> "+${totalMinutes}min"
    }
}
