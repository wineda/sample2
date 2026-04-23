package com.example.sample2.analytics

internal class BaselineCalculator {

    fun buildPersonalBaseline(
        history: List<DailyStructuredFeatures>,
        current: DailyStructuredFeatures? = null
    ): PersonalBaseline {
        val recent = history.takeLast(PersonalityAnalysisConstants.BASELINE_WINDOW_DAYS)

        if (recent.isEmpty()) {
            return PersonalBaseline(
                sleepHours = PersonalityMath.blend(current?.sleepHours, 6.8, 0.35),
                sleepQuality = PersonalityMath.blend(current?.sleepQuality?.toDouble(), 1.5, 0.35),
                steps = PersonalityMath.blend(current?.steps?.toDouble(), 5000.0, 0.35),
                emotions = DailyEmotionAverages(
                    anxiety = PersonalityMath.blend(current?.emotions?.anxiety, 1.2, 0.35),
                    angry = PersonalityMath.blend(current?.emotions?.angry, 0.7, 0.35),
                    sad = PersonalityMath.blend(current?.emotions?.sad, 1.0, 0.35),
                    happy = PersonalityMath.blend(current?.emotions?.happy, 1.6, 0.35),
                    calm = PersonalityMath.blend(current?.emotions?.calm, 1.6, 0.35)
                ),
                flags = DailyFlagAverages(
                    exercised = PersonalityMath.blend(current?.flags?.exercised?.toDouble(), 0.4, 0.35),
                    socialized = PersonalityMath.blend(current?.flags?.socialized?.toDouble(), 0.7, 0.35),
                    delegate = PersonalityMath.blend(current?.flags?.delegate?.toDouble(), 0.4, 0.35),
                    challenge = PersonalityMath.blend(current?.flags?.challenge?.toDouble(), 0.4, 0.35),
                    breakdown = PersonalityMath.blend(current?.flags?.breakdown?.toDouble(), 0.5, 0.35),
                    quickAction = PersonalityMath.blend(current?.flags?.quickAction?.toDouble(), 0.6, 0.35),
                    pendingTask = PersonalityMath.blend(current?.flags?.pendingTask?.toDouble(), 0.8, 0.35),
                    meetingStress = PersonalityMath.blend(current?.flags?.meetingStress?.toDouble(), 0.5, 0.35),
                    smartphoneDrift = PersonalityMath.blend(current?.flags?.smartphoneDrift?.toDouble(), 0.6, 0.35),
                    alcohol = PersonalityMath.blend(current?.flags?.alcohol?.toDouble(), 0.1, 0.35),
                    hangover = PersonalityMath.blend(current?.flags?.hangover?.toDouble(), 0.05, 0.35)
                )
            )
        }

        return PersonalBaseline(
            sleepHours = recent.mapNotNull { it.sleepHours }.averageOr(6.8),
            sleepQuality = recent.mapNotNull { it.sleepQuality?.toDouble() }.averageOr(1.5),
            steps = recent.mapNotNull { it.steps?.toDouble() }.averageOr(5000.0),
            emotions = DailyEmotionAverages(
                anxiety = recent.map { it.emotions.anxiety }.averageOr(1.2),
                angry = recent.map { it.emotions.angry }.averageOr(0.7),
                sad = recent.map { it.emotions.sad }.averageOr(1.0),
                happy = recent.map { it.emotions.happy }.averageOr(1.6),
                calm = recent.map { it.emotions.calm }.averageOr(1.6)
            ),
            flags = DailyFlagAverages(
                exercised = recent.map { it.flags.exercised.toDouble() }.averageOr(0.4),
                socialized = recent.map { it.flags.socialized.toDouble() }.averageOr(0.7),
                delegate = recent.map { it.flags.delegate.toDouble() }.averageOr(0.4),
                challenge = recent.map { it.flags.challenge.toDouble() }.averageOr(0.4),
                breakdown = recent.map { it.flags.breakdown.toDouble() }.averageOr(0.5),
                quickAction = recent.map { it.flags.quickAction.toDouble() }.averageOr(0.6),
                pendingTask = recent.map { it.flags.pendingTask.toDouble() }.averageOr(0.8),
                meetingStress = recent.map { it.flags.meetingStress.toDouble() }.averageOr(0.5),
                smartphoneDrift = recent.map { it.flags.smartphoneDrift.toDouble() }.averageOr(0.6),
                alcohol = recent.map { it.flags.alcohol.toDouble() }.averageOr(0.1),
                hangover = recent.map { it.flags.hangover.toDouble() }.averageOr(0.05)
            )
        )
    }
}
