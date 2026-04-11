package com.example.sample2.analytics

import com.example.sample2.data.DailyRecord
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

internal object PersonalityAnalysisConstants {
    const val BASELINE_WINDOW_DAYS = 28
    const val EMOTION_MAX = 3.0
    const val EMOTION_DELTA_SCALE = 0.8
    const val INTRADAY_RECENCY_BASE = 1.45
    const val LAST_MESSAGE_BLEND = 0.35
    val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
}

internal data class ScoreCore(
    val stabilityRaw: Double,
    val anxietyRaw: Double,
    val energyRaw: Double,
    val controlRaw: Double
)

internal object PersonalityMath {
    fun clamp(value: Double, min: Double, max: Double): Double {
        return when {
            value < min -> min
            value > max -> max
            else -> value
        }
    }

    fun round1(value: Double): Double {
        return (value * 10.0).roundToInt() / 10.0
    }

    fun round2(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }

    fun format0(value: Double): String {
        return value.roundToInt().toString()
    }

    fun normalizeEmotionAbs(value: Double): Double {
        return clamp(value / PersonalityAnalysisConstants.EMOTION_MAX, 0.0, 1.0)
    }

    fun normalizeCount(value: Int, max: Int): Double {
        if (max <= 0) return 0.0
        return clamp(value.toDouble() / max.toDouble(), 0.0, 1.0)
    }

    fun normalizeSleepHours(value: Double?): Double {
        if (value == null) return 0.5
        return clamp((value - 4.0) / 4.0, 0.0, 1.0)
    }

    fun normalizeSleepQuality(value: Int?): Double {
        if (value == null) return 0.5
        return clamp(value.toDouble() / 3.0, 0.0, 1.0)
    }

    fun normalizeSteps(value: Int?): Double {
        if (value == null) return 0.5
        return clamp(value.toDouble() / 8000.0, 0.0, 1.0)
    }

    fun compareHigherIsBetter(current: Double?, baseline: Double, scale: Double): Double {
        if (current == null || scale <= 0.0) return 0.0
        return clamp((current - baseline) / scale, -1.0, 1.0)
    }

    fun compareLowerIsBetter(current: Double?, baseline: Double, scale: Double): Double {
        if (current == null || scale <= 0.0) return 0.0
        return clamp((baseline - current) / scale, -1.0, 1.0)
    }

    fun blend(current: Double?, default: Double, currentWeight: Double): Double {
        return if (current == null) {
            default
        } else {
            current * currentWeight + default * (1.0 - currentWeight)
        }
    }
}

internal fun Iterable<Double>.averageOr(default: Double): Double {
    val list = this.toList()
    return if (list.isEmpty()) default else list.average()
}

internal fun String.toLocalDateOrNull(): LocalDate? {
    return runCatching { LocalDate.parse(this, PersonalityAnalysisConstants.DATE_FORMATTER) }.getOrNull()
}

internal inline fun <K, V> Iterable<V>.associateByNotNull(
    keySelector: (V) -> K?
): Map<K, V> {
    val map = linkedMapOf<K, V>()
    for (item in this) {
        val key = keySelector(item) ?: continue
        map[key] = item
    }
    return map
}

internal fun DailyRecord.toLocalDateOrNullFromDate(): LocalDate? = date.toLocalDateOrNull()
