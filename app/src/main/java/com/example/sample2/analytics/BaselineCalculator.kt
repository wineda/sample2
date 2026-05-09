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
                    pendingTask = PersonalityMath.blend(current?.flags?.pendingTask?.toDouble(), 0.8, 0.35),
                    reluctance = PersonalityMath.blend(current?.flags?.reluctance?.toDouble(), 0.3, 0.35),
                    meetingStress = PersonalityMath.blend(current?.flags?.meetingStress?.toDouble(), 0.5, 0.35),
                    rumination = PersonalityMath.blend(current?.flags?.rumination?.toDouble(), 0.3, 0.35),
                    idleDrift = PersonalityMath.blend(current?.flags?.idleDrift?.toDouble(), 0.6, 0.35),
                    alcohol = PersonalityMath.blend(current?.flags?.alcohol?.toDouble(), 0.1, 0.35),
                    hyperfocus = PersonalityMath.blend(current?.flags?.hyperfocus?.toDouble(), 0.2, 0.35),
                    noDrinkChoice = PersonalityMath.blend(current?.flags?.noDrinkChoice?.toDouble(), 0.2, 0.35),
                    quickAction = PersonalityMath.blend(current?.flags?.quickAction?.toDouble(), 0.6, 0.35),
                    breakdown = PersonalityMath.blend(current?.flags?.breakdown?.toDouble(), 0.5, 0.35),
                    rest = PersonalityMath.blend(current?.flags?.rest?.toDouble(), 0.2, 0.35),
                    exercised = PersonalityMath.blend(current?.flags?.exercised?.toDouble(), 0.4, 0.35),
                    mindfulAction = PersonalityMath.blend(current?.flags?.mindfulAction?.toDouble(), 0.4, 0.35),
                    insight = PersonalityMath.blend(current?.flags?.insight?.toDouble(), 0.3, 0.35),
                    tomorrowBaton = PersonalityMath.blend(current?.flags?.tomorrowBaton?.toDouble(), 0.3, 0.35),
                    consultConnect = PersonalityMath.blend(current?.flags?.consultConnect?.toDouble(), 0.7, 0.35)
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
                pendingTask = recent.map { it.flags.pendingTask.toDouble() }.averageOr(0.8),
                reluctance = recent.map { it.flags.reluctance.toDouble() }.averageOr(0.3),
                meetingStress = recent.map { it.flags.meetingStress.toDouble() }.averageOr(0.5),
                rumination = recent.map { it.flags.rumination.toDouble() }.averageOr(0.3),
                idleDrift = recent.map { it.flags.idleDrift.toDouble() }.averageOr(0.6),
                alcohol = recent.map { it.flags.alcohol.toDouble() }.averageOr(0.1),
                hyperfocus = recent.map { it.flags.hyperfocus.toDouble() }.averageOr(0.2),
                noDrinkChoice = recent.map { it.flags.noDrinkChoice.toDouble() }.averageOr(0.2),
                quickAction = recent.map { it.flags.quickAction.toDouble() }.averageOr(0.6),
                breakdown = recent.map { it.flags.breakdown.toDouble() }.averageOr(0.5),
                rest = recent.map { it.flags.rest.toDouble() }.averageOr(0.2),
                exercised = recent.map { it.flags.exercised.toDouble() }.averageOr(0.4),
                mindfulAction = recent.map { it.flags.mindfulAction.toDouble() }.averageOr(0.4),
                insight = recent.map { it.flags.insight.toDouble() }.averageOr(0.3),
                tomorrowBaton = recent.map { it.flags.tomorrowBaton.toDouble() }.averageOr(0.3),
                consultConnect = recent.map { it.flags.consultConnect.toDouble() }.averageOr(0.7)
            )
        )
    }
}
