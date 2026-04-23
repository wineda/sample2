package com.example.sample2.analytics

internal class ScoreCalculator {

    fun calculateRawCore(features: DailyStructuredFeatures): ScoreCore {
        val e = features.emotions
        val f = features.flags

        val anxietyNorm = PersonalityMath.normalizeEmotionAbs(e.anxiety)
        val angryNorm = PersonalityMath.normalizeEmotionAbs(e.angry)
        val sadNorm = PersonalityMath.normalizeEmotionAbs(e.sad)
        val happyNorm = PersonalityMath.normalizeEmotionAbs(e.happy)
        val calmNorm = PersonalityMath.normalizeEmotionAbs(e.calm)

        val sleepNorm = PersonalityMath.normalizeSleepHours(features.sleepHours)
        val sleepQualityNorm = PersonalityMath.normalizeSleepQuality(features.sleepQuality)
        val stepsNorm = PersonalityMath.normalizeSteps(features.steps)

        val exercisedNorm = PersonalityMath.normalizeCount(f.exercised, 2)
        val socializedNorm = PersonalityMath.normalizeCount(f.socialized, 2)
        val intentNorm = PersonalityMath.normalizeCount(f.intent, 2)
        val quickActionNorm = PersonalityMath.normalizeCount(f.quickAction, 3)

        val pendingTaskNorm = PersonalityMath.normalizeCount(f.pendingTask, 3)
        val meetingStressNorm = PersonalityMath.normalizeCount(f.meetingStress, 2)
        val smartphoneDriftNorm = PersonalityMath.normalizeCount(f.smartphoneDrift, 3)
        val alcoholNorm = PersonalityMath.normalizeCount(f.alcohol, 2)
        val hangoverNorm = PersonalityMath.normalizeCount(f.hangover, 1)

        val effectivePendingTaskNorm = PersonalityMath.clamp(pendingTaskNorm - 0.85 * quickActionNorm, 0.0, 1.0)
        val quickActionReliefNorm = PersonalityMath.clamp(quickActionNorm - 0.35 * pendingTaskNorm, 0.0, 1.0)

        val stabilityPendingAmplifier = 1.0 + 0.22 * effectivePendingTaskNorm
        val anxietyPendingAmplifier = 1.0 + 0.30 * effectivePendingTaskNorm
        val energyPendingAmplifier = 1.0 + 0.18 * effectivePendingTaskNorm
        val controlPendingAmplifier = 1.0 + 0.45 * effectivePendingTaskNorm

        val stabilityNegativeLoad =
            30.0 * anxietyNorm +
                14.0 * angryNorm +
                13.0 * sadNorm +
                7.0 * meetingStressNorm +
                5.0 * smartphoneDriftNorm
        val anxietyNegativeLoad =
            7.6 * anxietyNorm +
                3.1 * angryNorm +
                2.3 * sadNorm +
                1.0 * meetingStressNorm +
                0.9 * smartphoneDriftNorm
        val energyNegativeLoad =
            10.0 * anxietyNorm +
                5.0 * angryNorm +
                18.0 * sadNorm +
                5.0 * meetingStressNorm +
                5.0 * smartphoneDriftNorm
        val controlNegativeLoad =
            19.0 * anxietyNorm +
                16.0 * angryNorm +
                7.0 * sadNorm +
                12.0 * meetingStressNorm +
                11.0 * smartphoneDriftNorm

        val stabilityRaw =
            56.0 +
                25.0 * calmNorm +
                12.0 * happyNorm +
                6.5 * sleepNorm +
                4.0 * sleepQualityNorm +
                3.0 * stepsNorm +
                2.0 * exercisedNorm +
                6.0 * socializedNorm +
                3.0 * intentNorm +
                5.0 * quickActionReliefNorm -
                stabilityNegativeLoad * stabilityPendingAmplifier -
                7.0 * effectivePendingTaskNorm -
                4.0 * alcoholNorm -
                5.0 * hangoverNorm

        val anxietyRaw =
            1.4 +
                anxietyNegativeLoad * anxietyPendingAmplifier -
                1.8 * calmNorm -
                0.8 * happyNorm -
                0.5 * sleepNorm -
                0.3 * sleepQualityNorm -
                0.3 * stepsNorm -
                0.2 * exercisedNorm -
                0.2 * socializedNorm -
                0.5 * intentNorm -
                0.9 * quickActionReliefNorm +
                1.4 * effectivePendingTaskNorm +
                0.4 * alcoholNorm +
                0.8 * hangoverNorm

        val energyRaw =
            44.0 +
                20.0 * sleepNorm +
                10.0 * sleepQualityNorm +
                14.0 * stepsNorm +
                14.0 * exercisedNorm +
                3.0 * socializedNorm +
                4.0 * intentNorm +
                6.0 * quickActionReliefNorm +
                10.0 * happyNorm +
                1.0 * calmNorm -
                energyNegativeLoad * energyPendingAmplifier -
                6.0 * effectivePendingTaskNorm -
                4.0 * alcoholNorm -
                13.0 * hangoverNorm

        val controlRaw =
            50.0 +
                15.0 * intentNorm +
                5.0 * socializedNorm +
                3.0 * exercisedNorm +
                1.5 * happyNorm +
                2.0 * calmNorm +
                3.0 * sleepNorm +
                15.0 * quickActionReliefNorm -
                controlNegativeLoad * controlPendingAmplifier -
                16.0 * effectivePendingTaskNorm -
                5.0 * alcoholNorm -
                7.0 * hangoverNorm

        return ScoreCore(stabilityRaw, anxietyRaw, energyRaw, controlRaw)
    }

    fun determineStateFromScores(
        stability: Double,
        anxiety: Double,
        energy: Double,
        control: Double
    ): PersonalityState {
        return when {
            energy <= 30.0 || (stability <= 40.0 && control <= 38.0) -> PersonalityState.EXHAUSTED
            anxiety >= 7.0 || stability <= 48.0 || control <= 42.0 -> PersonalityState.TENSE
            stability >= 72.0 && anxiety <= 3.0 && control >= 58.0 && energy >= 52.0 -> PersonalityState.STABLE
            else -> PersonalityState.RECOVERING
        }
    }
}
