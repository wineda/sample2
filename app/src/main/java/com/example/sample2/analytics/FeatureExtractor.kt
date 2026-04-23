package com.example.sample2.analytics

import com.example.sample2.data.DailyRecord
import com.example.sample2.data.MessageV2
import kotlin.math.pow

internal class FeatureExtractor {

    fun extractStructuredFeatures(
        messages: List<MessageV2>,
        dailyRecord: DailyRecord? = null,
        emotionWeighting: EmotionWeighting = EmotionWeighting.UNIFORM
    ): DailyStructuredFeatures {
        val emotions = DailyEmotionAverages(
            anxiety = averageEmotionOf(messages, { it.emotions.anxiety }, emotionWeighting),
            angry = averageEmotionOf(messages, { it.emotions.angry }, emotionWeighting),
            sad = averageEmotionOf(messages, { it.emotions.sad }, emotionWeighting),
            happy = averageEmotionOf(messages, { it.emotions.happy }, emotionWeighting),
            calm = averageEmotionOf(messages, { it.emotions.calm }, emotionWeighting)
        )

        val flags = DailyFlagCounts(
            exercised = messages.count { it.flags.exercised },
            socialized = messages.count { it.flags.socialized },
            delegate = messages.count { it.flags.delegate },
            intent = messages.count { it.flags.intent },
            insight = messages.count { it.flags.insight },
            reflection = messages.count { it.flags.reflection },
            quickAction = messages.count { it.flags.quickAction },
            pendingTask = messages.count { it.flags.pendingTask },
            meetingStress = messages.count { it.flags.meetingStress },
            smartphoneDrift = messages.count { it.flags.smartphoneDrift },
            alcohol = messages.count { it.flags.alcohol },
            hangover = messages.count { it.flags.hangover }
        )

        val sleepHours = dailyRecord?.sleep?.durationMinutes
            ?.takeIf { it > 0 }
            ?.let { it / 60.0 }

        val sleepQuality = dailyRecord?.sleep?.quality
            ?.takeIf { it in 0..3 }

        val steps = dailyRecord?.steps
            ?.takeIf { it > 0 }

        return DailyStructuredFeatures(
            sleepHours = sleepHours,
            sleepQuality = sleepQuality,
            steps = steps,
            emotions = emotions,
            flags = flags
        )
    }

    private fun averageEmotionOf(
        messages: List<MessageV2>,
        selector: (MessageV2) -> Int,
        weighting: EmotionWeighting
    ): Double {
        if (messages.isEmpty()) return 0.0

        if (weighting == EmotionWeighting.UNIFORM) {
            return messages.map(selector).average().coerceIn(0.0, PersonalityAnalysisConstants.EMOTION_MAX)
        }

        val lastValue = selector(messages.last()).toDouble()
        val weightedSum = messages.indices.sumOf { index ->
            val distanceFromLast = (messages.lastIndex - index).toDouble()
            val weight = PersonalityAnalysisConstants.INTRADAY_RECENCY_BASE.pow(-distanceFromLast)
            selector(messages[index]).toDouble() * weight
        }
        val totalWeight = messages.indices.sumOf { index ->
            val distanceFromLast = (messages.lastIndex - index).toDouble()
            PersonalityAnalysisConstants.INTRADAY_RECENCY_BASE.pow(-distanceFromLast)
        }
        val weightedAverage = if (totalWeight <= 0.0) 0.0 else weightedSum / totalWeight

        return PersonalityMath.clamp(
            weightedAverage * (1.0 - PersonalityAnalysisConstants.LAST_MESSAGE_BLEND) +
                lastValue * PersonalityAnalysisConstants.LAST_MESSAGE_BLEND,
            0.0,
            PersonalityAnalysisConstants.EMOTION_MAX
        )
    }
}
