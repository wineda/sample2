package com.example.sample2.analytics

internal class SummaryBuilder(
    private val scoreCalculator: ScoreCalculator
) {

    fun buildDailySummary(
        state: PersonalityState,
        stability: Double,
        anxiety: Double,
        energy: Double,
        control: Double,
        features: DailyStructuredFeatures,
        baseline: PersonalBaseline,
        isTrend: Boolean
    ): String {
        val plus = mutableListOf<String>()
        val minus = mutableListOf<String>()

        val e = features.emotions
        val f = features.flags

        val sleepDelta = PersonalityMath.compareHigherIsBetter(features.sleepHours, baseline.sleepHours, 1.5)
        val stepsDelta = PersonalityMath.compareHigherIsBetter(features.steps?.toDouble(), baseline.steps, 3000.0)
        val challengeDelta = PersonalityMath.compareHigherIsBetter(f.challenge.toDouble(), baseline.flags.challenge, 1.0)
        val breakdownDelta = PersonalityMath.compareHigherIsBetter(f.breakdown.toDouble(), baseline.flags.breakdown, 1.0)
        val socializedDelta = PersonalityMath.compareHigherIsBetter(f.socialized.toDouble(), baseline.flags.socialized, 1.0)
        val quickActionDelta = PersonalityMath.compareHigherIsBetter(f.quickAction.toDouble(), baseline.flags.quickAction, 1.0)
        val anxietyDelta = PersonalityMath.compareLowerIsBetter(
            e.anxiety,
            baseline.emotions.anxiety,
            PersonalityAnalysisConstants.EMOTION_DELTA_SCALE
        )
        val sadDelta = PersonalityMath.compareLowerIsBetter(
            e.sad,
            baseline.emotions.sad,
            PersonalityAnalysisConstants.EMOTION_DELTA_SCALE
        )
        val pendingTaskDelta = PersonalityMath.compareLowerIsBetter(f.pendingTask.toDouble(), baseline.flags.pendingTask, 1.2)
        val meetingStressDelta = PersonalityMath.compareLowerIsBetter(f.meetingStress.toDouble(), baseline.flags.meetingStress, 1.0)
        val smartphoneDelta = PersonalityMath.compareLowerIsBetter(f.smartphoneDrift.toDouble(), baseline.flags.smartphoneDrift, 1.0)
        val calmDelta = PersonalityMath.compareHigherIsBetter(
            e.calm,
            baseline.emotions.calm,
            PersonalityAnalysisConstants.EMOTION_DELTA_SCALE
        )
        val happyDelta = PersonalityMath.compareHigherIsBetter(
            e.happy,
            baseline.emotions.happy,
            PersonalityAnalysisConstants.EMOTION_DELTA_SCALE
        )

        if (sleepDelta >= 0.35) plus += "睡眠が平常より良い"
        if (stepsDelta >= 0.35) plus += "歩数が平常より多い"
        if (socializedDelta >= 0.35) plus += "会話が平常より多い"
        if (challengeDelta >= 0.35) plus += "チャレンジが平常より多い"
        if (breakdownDelta >= 0.35) plus += "細分化が平常より多い"
        if (quickActionDelta >= 0.35) plus += "すぐやるが平常より多い"
        if (happyDelta >= 0.35) plus += "喜びが平常より高い"
        if (calmDelta >= 0.35) plus += "安心感が平常より高い"
        if (anxietyDelta >= 0.35) plus += "不安が平常より低い"

        if (anxietyDelta <= -0.35) minus += "不安が平常より高い"
        if (sadDelta <= -0.35) minus += "悲しみが平常より高い"
        if (pendingTaskDelta <= -0.35) minus += "未処理が平常より多い"
        if (meetingStressDelta <= -0.35) minus += "会議負荷が平常より高い"
        if (smartphoneDelta <= -0.35) minus += "スマホ逸脱が平常より多い"
        if ((features.sleepHours ?: 99.0) in 0.1..<5.5) minus += "睡眠不足"
        if (e.anxiety >= 2.0) minus += "強い不安"
        if (e.angry >= 1.8) minus += "怒りが強い"

        val headPrefix = if (isTrend) "流れで見ると" else "今日単体では"

        val head = when (state) {
            PersonalityState.STABLE -> "${headPrefix}安定。"
            PersonalityState.RECOVERING -> "${headPrefix}回復中。"
            PersonalityState.TENSE -> "${headPrefix}張りつめ。"
            PersonalityState.EXHAUSTED -> "${headPrefix}消耗寄り。"
        }

        val plusText = if (plus.isNotEmpty()) "追い風: ${plus.joinToString("、")}。" else ""
        val minusText = if (minus.isNotEmpty()) "注意点: ${minus.joinToString("、")}。" else ""

        val tail = when (state) {
            PersonalityState.STABLE -> {
                when {
                    control < 50.0 -> "未処理を1つ減らすか、すぐやるを1つ増やすとさらに整いやすい。"
                    energy < 45.0 -> "安定寄りだが消耗はあるので、無理に詰めすぎない。"
                    else -> "この調子で小さい行動を維持すると崩れにくい。"
                }
            }
            PersonalityState.RECOVERING -> {
                when {
                    anxiety >= 6.0 -> "安心感を増やす行動が効きやすい。"
                    energy < 45.0 -> "まず睡眠と軽い運動が効きやすい。"
                    else -> "やることを絞って、すぐやるに変えるほど制御感が上がりやすい。"
                }
            }
            PersonalityState.TENSE -> {
                when {
                    control < 40.0 -> "抱え込みを減らし、未処理を1つ片づけるか、すぐやるを1つ増やすと戻しやすい。"
                    anxiety >= 7.0 -> "刺激を減らし、休憩や会話を先に入れたい。"
                    else -> "押し切るより、負荷源を減らす方が効く。"
                }
            }
            PersonalityState.EXHAUSTED -> "まず休息優先。睡眠・食事・刺激を減らす方向が合う日。"
        }

        return listOf(
            head,
            "安定度${PersonalityMath.format0(stability)} / 不安${PersonalityMath.round1(anxiety)} / 活力${PersonalityMath.format0(energy)} / 制御感${PersonalityMath.format0(control)}。",
            plusText,
            minusText,
            tail
        ).filter { it.isNotBlank() }.joinToString(" ")
    }

    fun buildWeeklySummary(
        rawWeekDays: List<DailyPersonalityScore>,
        trendLast: DailyPersonalityScore?
    ): String {
        val stability = rawWeekDays.map { it.stability }.average()
        val anxiety = rawWeekDays.map { it.anxiety }.average()
        val energy = rawWeekDays.map { it.energy }.average()
        val control = rawWeekDays.map { it.control }.average()

        val dominantState = trendLast?.state ?: scoreCalculator.determineStateFromScores(
            stability = stability,
            anxiety = anxiety,
            energy = energy,
            control = control
        )

        val strongDays = rawWeekDays.count { it.state == PersonalityState.STABLE }
        val tenseDays = rawWeekDays.count {
            it.state == PersonalityState.TENSE || it.state == PersonalityState.EXHAUSTED
        }

        val flowText = when {
            dominantState == PersonalityState.STABLE -> "週の流れは安定寄り。"
            dominantState == PersonalityState.RECOVERING -> "週の流れは回復寄り。"
            dominantState == PersonalityState.TENSE -> "週の流れは張りつめ寄り。"
            else -> "週の流れは消耗寄り。"
        }

        val dayText = when {
            strongDays >= 4 -> "安定した日が比較的多い。"
            tenseDays >= 3 -> "負荷が高い日がやや多い。"
            else -> "日ごとの波はあるが中間帯。"
        }

        val tail = when {
            anxiety >= 6.5 -> "来週は安心感を増やす行動と、未処理の削減を優先したい。"
            energy <= 40.0 -> "来週は回復優先で、睡眠と活動量の底上げが効きやすい。"
            control <= 45.0 -> "来週はやることを絞るほど整いやすい。"
            else -> "来週も小さな良い行動の継続が効きやすい。"
        }

        return listOf(
            flowText,
            "平均: 安定度${PersonalityMath.format0(stability)} / 不安${PersonalityMath.round1(anxiety)} / 活力${PersonalityMath.format0(energy)} / 制御感${PersonalityMath.format0(control)}。",
            dayText,
            tail
        ).joinToString(" ")
    }
}
