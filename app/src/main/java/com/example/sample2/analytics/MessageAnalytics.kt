package com.example.sample2.analytics

import com.example.sample2.data.ActionType
import com.example.sample2.data.EmotionType
import com.example.sample2.data.MessageV2
import com.example.sample2.util.getTimeSlot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun buildEmotionHeatmap(
    messages: List<MessageV2>,
    emotion: EmotionType,
    fromDate: Long? = null,
    toDate: Long? = null
): Map<Pair<String, String>, Double> {
    val cal = Calendar.getInstance()
    val dayFormat = SimpleDateFormat("E", Locale.JAPAN)

    return messages
        .asSequence()
        .filter { message -> isInDateRange(message.timestamp, fromDate, toDate) }
        .filter { message -> emotion.matches(message.emotions) }
        .groupingBy { message ->
            toDaySlot(message.timestamp, cal, dayFormat)
        }
        .eachCount()
        .mapValues { it.value.toDouble() }
}

fun buildActionHeatmap(
    messages: List<MessageV2>,
    action: ActionType,
    fromDate: Long? = null,
    toDate: Long? = null
): Map<Pair<String, String>, Double> {
    val cal = Calendar.getInstance()
    val dayFormat = SimpleDateFormat("E", Locale.JAPAN)

    return messages
        .asSequence()
        .filter { message -> isInDateRange(message.timestamp, fromDate, toDate) }
        .filter { message -> action.matches(message.flags) }
        .groupingBy { message ->
            toDaySlot(message.timestamp, cal, dayFormat)
        }
        .eachCount()
        .mapValues { it.value.toDouble() }
}

fun getMessagesForEmotionSlot(
    messages: List<MessageV2>,
    day: String,
    slot: String,
    emotion: EmotionType,
    fromDate: Long? = null,
    toDate: Long? = null
): List<MessageV2> {
    val cal = Calendar.getInstance()
    val dayFormat = SimpleDateFormat("E", Locale.JAPAN)

    return messages.filter { message ->
        if (!isInDateRange(message.timestamp, fromDate, toDate)) return@filter false
        if (!emotion.matches(message.emotions)) return@filter false

        val (msgDay, msgSlot) = toDaySlot(message.timestamp, cal, dayFormat)
        msgDay == day && msgSlot == slot
    }
}

fun getMessagesForActionSlot(
    messages: List<MessageV2>,
    day: String,
    slot: String,
    action: ActionType,
    fromDate: Long? = null,
    toDate: Long? = null
): List<MessageV2> {
    val cal = Calendar.getInstance()
    val dayFormat = SimpleDateFormat("E", Locale.JAPAN)

    return messages.filter { message ->
        if (!isInDateRange(message.timestamp, fromDate, toDate)) return@filter false
        if (!action.matches(message.flags)) return@filter false

        val (msgDay, msgSlot) = toDaySlot(message.timestamp, cal, dayFormat)
        msgDay == day && msgSlot == slot
    }
}

private fun isInDateRange(
    timestamp: Long,
    fromDate: Long?,
    toDate: Long?
): Boolean {
    return (fromDate == null || timestamp >= fromDate) &&
            (toDate == null || timestamp <= toDate)
}

private fun toDaySlot(
    timestamp: Long,
    cal: Calendar,
    dayFormat: SimpleDateFormat
): Pair<String, String> {
    cal.timeInMillis = timestamp
    val day = dayFormat.format(cal.time)
    val slot = getTimeSlot(cal.get(Calendar.HOUR_OF_DAY))
    return day to slot
}