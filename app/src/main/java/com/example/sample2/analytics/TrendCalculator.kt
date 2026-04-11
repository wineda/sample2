package com.example.sample2.analytics

internal class TrendCalculator {

    fun analyzeDayTrend(
        rawToday: DailyPersonalityScore,
        previousTrend: DailyPersonalityScore? = null,
        baselineCalculator: BaselineCalculator,
        summaryBuilder: SummaryBuilder
    ): DailyPersonalityScore {
        if (previousTrend == null) {
            return rawToday.copy(
                summary = summaryBuilder.buildDailySummary(
                    state = rawToday.state,
                    stability = rawToday.stability,
                    anxiety = rawToday.anxiety,
                    energy = rawToday.energy,
                    control = rawToday.control,
                    features = rawToday.features,
                    baseline = baselineCalculator.buildPersonalBaseline(emptyList(), current = rawToday.features),
                    isTrend = true
                )
            )
        }

        val shock = calculateShock(rawToday)
        val inertia = when {
            shock >= 0.80 -> 0.45
            shock >= 0.55 -> 0.60
            shock >= 0.35 -> 0.72
            else -> 0.82
        }
        val currentWeight = 1.0 - inertia

        val stability = PersonalityMath.round2(
            PersonalityMath.clamp(previousTrend.stability * inertia + rawToday.stability * currentWeight, 0.0, 100.0)
        )
        val anxiety = PersonalityMath.round2(
            PersonalityMath.clamp(previousTrend.anxiety * inertia + rawToday.anxiety * currentWeight, 0.0, 10.0)
        )
        val energy = PersonalityMath.round2(
            PersonalityMath.clamp(previousTrend.energy * inertia + rawToday.energy * currentWeight, 0.0, 100.0)
        )
        val control = PersonalityMath.round2(
            PersonalityMath.clamp(previousTrend.control * inertia + rawToday.control * currentWeight, 0.0, 100.0)
        )
        val state = ScoreCalculator().determineStateFromScores(stability, anxiety, energy, control)

        return DailyPersonalityScore(
            date = rawToday.date,
            stability = stability,
            anxiety = anxiety,
            energy = energy,
            control = control,
            state = state,
            summary = summaryBuilder.buildDailySummary(
                state = state,
                stability = stability,
                anxiety = anxiety,
                energy = energy,
                control = control,
                features = rawToday.features,
                baseline = baselineCalculator.buildPersonalBaseline(emptyList(), current = rawToday.features),
                isTrend = true
            ),
            features = rawToday.features
        )
    }

    private fun calculateShock(rawToday: DailyPersonalityScore): Double {
        val e = rawToday.features.emotions
        val f = rawToday.features.flags
        val pendingNorm = PersonalityMath.normalizeCount(f.pendingTask, 3)
        val quickActionNorm = PersonalityMath.normalizeCount(f.quickAction, 3)
        val effectivePendingNorm = PersonalityMath.clamp(pendingNorm - 0.85 * quickActionNorm, 0.0, 1.0)

        val shock =
            0.40 * PersonalityMath.normalizeEmotionAbs(e.anxiety) +
                0.14 * PersonalityMath.normalizeEmotionAbs(e.sad) +
                0.18 * PersonalityMath.normalizeEmotionAbs(e.angry) +
                0.16 * effectivePendingNorm +
                0.08 * PersonalityMath.normalizeEmotionAbs(e.anxiety) * effectivePendingNorm +
                0.06 * PersonalityMath.normalizeEmotionAbs(e.angry) * effectivePendingNorm -
                0.10 * quickActionNorm +
                0.10 * PersonalityMath.normalizeCount(f.meetingStress, 2) +
                0.08 * PersonalityMath.normalizeCount(f.smartphoneDrift, 2) +
                0.10 * when {
                    (rawToday.features.sleepHours ?: 7.0) < 4.5 -> 1.0
                    (rawToday.features.sleepHours ?: 7.0) < 5.5 -> 0.6
                    else -> 0.0
                }

        return PersonalityMath.clamp(shock, 0.0, 1.0)
    }
}
