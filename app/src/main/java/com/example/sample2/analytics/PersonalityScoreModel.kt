package com.example.sample2.analytics

import com.example.sample2.data.DailyRecord
import com.example.sample2.data.MessageV2
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import kotlin.math.pow
import kotlin.math.roundToInt

data class DailyEmotionAverages(
    val anxiety: Double,
    val angry: Double,
    val sad: Double,
    val happy: Double,
    val calm: Double
)

data class DailyFlagCounts(
    val exercised: Int,
    val socialized: Int,
    val intent: Int,
    val insight: Int,
    val reflection: Int,
    val quickAction: Int,
    val pendingTask: Int,
    val meetingStress: Int,
    val smartphoneDrift: Int,
    val alcohol: Int,
    val hangover: Int
)

data class DailyStructuredFeatures(
    val sleepHours: Double?,
    val sleepQuality: Int?,
    val steps: Int?,
    val emotions: DailyEmotionAverages,
    val flags: DailyFlagCounts
)

enum class PersonalityState(val label: String) {
    STABLE("安定"),
    RECOVERING("回復中"),
    TENSE("張りつめ"),
    EXHAUSTED("消耗")
}

data class DailyPersonalityScore(
    val date: LocalDate,
    val stability: Double,   // 0..100
    val anxiety: Double,     // 0..10
    val energy: Double,      // 0..100
    val control: Double,     // 0..100
    val state: PersonalityState,
    val summary: String,
    val features: DailyStructuredFeatures
)

data class WeeklyPersonalityScore(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val dayCount: Int,
    val stability: Double,      // 週の raw 平均
    val anxiety: Double,        // 週の raw 平均
    val energy: Double,         // 週の raw 平均
    val control: Double,        // 週の raw 平均
    val trendStability: Double, // 週末時点の trend
    val trendAnxiety: Double,
    val trendEnergy: Double,
    val trendControl: Double,
    val trendState: PersonalityState,
    val summary: String
)

data class DailyFlagAverages(
    val exercised: Double,
    val socialized: Double,
    val intent: Double,
    val insight: Double,
    val reflection: Double,
    val quickAction: Double,
    val pendingTask: Double,
    val meetingStress: Double,
    val smartphoneDrift: Double,
    val alcohol: Double,
    val hangover: Double
)

data class PersonalBaseline(
    val sleepHours: Double,
    val sleepQuality: Double,
    val steps: Double,
    val emotions: DailyEmotionAverages,
    val flags: DailyFlagAverages
)

data class IntradayPersonalityPoint(
    val index: Int,
    val timestamp: Long,
    val stability: Float,
    val anxiety: Float,
    val energy: Float,
    val control: Float
)

private data class ScoreCore(
    val stabilityRaw: Double,
    val anxietyRaw: Double,
    val energyRaw: Double,
    val controlRaw: Double
)

enum class EmotionWeighting {
    UNIFORM,
    RECENT_BIASED
}

object PersonalityScoreModel {

    private const val BASELINE_WINDOW_DAYS = 28
    private const val EMOTION_MAX = 3.0
    private const val EMOTION_DELTA_SCALE = 0.8
    private const val INTRADAY_RECENCY_BASE = 1.45
    private const val LAST_MESSAGE_BLEND = 0.35
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * 日次一覧の標準。
     * 各日そのものを返す。前日の trend は混ぜない。
     */
    fun analyzeAllDays(
        messages: List<MessageV2>,
        dailyRecords: List<DailyRecord> = emptyList(),
        zoneId: ZoneId = ZoneId.of("Asia/Tokyo")
    ): List<DailyPersonalityScore> {
        return analyzeAllDaysRaw(messages, dailyRecords, zoneId)
    }

    /**
     * 1日詳細向けの強調版。
     * 前日は混ぜず、その日の中でも直近感情を少し強める。
     */
    fun analyzeDay(
        date: LocalDate,
        messages: List<MessageV2>,
        dailyRecord: DailyRecord? = null,
        baseline: PersonalBaseline? = null
    ): DailyPersonalityScore {
        return analyzeDayFocused(
            date = date,
            messages = messages,
            dailyRecord = dailyRecord,
            baseline = baseline
        )
    }

    /**
     * 1日詳細 / 日次一覧向け raw モデル
     */
    fun analyzeAllDaysRaw(
        messages: List<MessageV2>,
        dailyRecords: List<DailyRecord> = emptyList(),
        zoneId: ZoneId = ZoneId.of("Asia/Tokyo")
    ): List<DailyPersonalityScore> {
        val messageMap = messages.groupBy {
            Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate()
        }

        val dailyRecordMap = dailyRecords.associateByNotNull { record ->
            record.date.toLocalDateOrNull()
        }

        val allDates = (messageMap.keys + dailyRecordMap.keys).toSortedSet()
        val historyFeatures = mutableListOf<DailyStructuredFeatures>()
        val results = mutableListOf<DailyPersonalityScore>()

        for (date in allDates) {
            val baseline = buildPersonalBaseline(historyFeatures.takeLast(BASELINE_WINDOW_DAYS))
            val score = analyzeDayRaw(
                date = date,
                messages = messageMap[date].orEmpty(),
                dailyRecord = dailyRecordMap[date],
                baseline = baseline
            )
            results += score
            historyFeatures += score.features
        }

        return results
    }

    /**
     * 単日だけ見せる用途向け。
     * 前日の影響は混ぜず、直近感情にやや寄せる。
     */
    fun analyzeDayFocused(
        date: LocalDate,
        messages: List<MessageV2>,
        dailyRecord: DailyRecord? = null,
        baseline: PersonalBaseline? = null
    ): DailyPersonalityScore {
        val features = extractStructuredFeatures(
            messages = messages,
            dailyRecord = dailyRecord,
            emotionWeighting = EmotionWeighting.RECENT_BIASED
        )
        val personalBaseline = baseline ?: buildPersonalBaseline(emptyList(), current = features)
        val core = calculateRawCore(features)

        val stability = round2(clamp(core.stabilityRaw, 0.0, 100.0))
        val anxiety = round2(clamp(core.anxietyRaw, 0.0, 10.0))
        val energy = round2(clamp(core.energyRaw, 0.0, 100.0))
        val control = round2(clamp(core.controlRaw, 0.0, 100.0))
        val state = determineStateFromScores(stability, anxiety, energy, control)

        return DailyPersonalityScore(
            date = date,
            stability = stability,
            anxiety = anxiety,
            energy = energy,
            control = control,
            state = state,
            summary = buildDailySummary(
                state = state,
                stability = stability,
                anxiety = anxiety,
                energy = energy,
                control = control,
                features = features,
                baseline = personalBaseline,
                isTrend = false
            ),
            features = features
        )
    }

    /**
     * 1日単体モデル:
     * 前日の状態を使わない。
     */
    fun analyzeDayRaw(
        date: LocalDate,
        messages: List<MessageV2>,
        dailyRecord: DailyRecord? = null,
        baseline: PersonalBaseline? = null
    ): DailyPersonalityScore {
        val features = extractStructuredFeatures(
            messages = messages,
            dailyRecord = dailyRecord,
            emotionWeighting = EmotionWeighting.UNIFORM
        )
        val personalBaseline = baseline ?: buildPersonalBaseline(emptyList(), current = features)
        val core = calculateRawCore(features)

        val stability = round2(clamp(core.stabilityRaw, 0.0, 100.0))
        val anxiety = round2(clamp(core.anxietyRaw, 0.0, 10.0))
        val energy = round2(clamp(core.energyRaw, 0.0, 100.0))
        val control = round2(clamp(core.controlRaw, 0.0, 100.0))
        val state = determineStateFromScores(stability, anxiety, energy, control)

        return DailyPersonalityScore(
            date = date,
            stability = stability,
            anxiety = anxiety,
            energy = energy,
            control = control,
            state = state,
            summary = buildDailySummary(
                state = state,
                stability = stability,
                anxiety = anxiety,
                energy = energy,
                control = control,
                features = features,
                baseline = personalBaseline,
                isTrend = false
            ),
            features = features
        )
    }

    /**
     * 週 / 月トレンド向け:
     * raw の日次スコアを材料にして、前日の流れを混ぜる。
     */
    fun analyzeAllDaysTrend(
        messages: List<MessageV2>,
        dailyRecords: List<DailyRecord> = emptyList(),
        zoneId: ZoneId = ZoneId.of("Asia/Tokyo")
    ): List<DailyPersonalityScore> {
        val rawDays = analyzeAllDaysRaw(messages, dailyRecords, zoneId)
        val trendDays = mutableListOf<DailyPersonalityScore>()

        rawDays.forEach { rawToday ->
            val trendToday = analyzeDayTrend(
                rawToday = rawToday,
                previousTrend = trendDays.lastOrNull()
            )
            trendDays += trendToday
        }

        return trendDays
    }

    /**
     * 週スコア:
     * 各日の raw をまず出し、その1週間を集約する。
     * trend は週末時点の「流れ」を補助的に持たせる。
     */
    fun analyzeWeeks(
        messages: List<MessageV2>,
        dailyRecords: List<DailyRecord> = emptyList(),
        zoneId: ZoneId = ZoneId.of("Asia/Tokyo"),
        weekStartsOn: DayOfWeek = DayOfWeek.MONDAY
    ): List<WeeklyPersonalityScore> {
        val rawDays = analyzeAllDaysRaw(messages, dailyRecords, zoneId)
        val trendDays = analyzeAllDaysTrend(messages, dailyRecords, zoneId)

        if (rawDays.isEmpty()) return emptyList()

        val trendByDate = trendDays.associateBy { it.date }

        return rawDays
            .groupBy { day ->
                day.date.with(TemporalAdjusters.previousOrSame(weekStartsOn))
            }
            .toSortedMap()
            .map { (weekStart, rawWeekDays) ->
                val weekEnd = weekStart.plusDays(6)
                val trendWeekDays = rawWeekDays.mapNotNull { trendByDate[it.date] }
                val trendLast = trendWeekDays.lastOrNull()

                val stability = round2(rawWeekDays.map { it.stability }.average())
                val anxiety = round2(rawWeekDays.map { it.anxiety }.average())
                val energy = round2(rawWeekDays.map { it.energy }.average())
                val control = round2(rawWeekDays.map { it.control }.average())

                WeeklyPersonalityScore(
                    weekStart = weekStart,
                    weekEnd = weekEnd,
                    dayCount = rawWeekDays.size,
                    stability = clamp(stability, 0.0, 100.0),
                    anxiety = clamp(anxiety, 0.0, 10.0),
                    energy = clamp(energy, 0.0, 100.0),
                    control = clamp(control, 0.0, 100.0),
                    trendStability = trendLast?.stability ?: stability,
                    trendAnxiety = trendLast?.anxiety ?: anxiety,
                    trendEnergy = trendLast?.energy ?: energy,
                    trendControl = trendLast?.control ?: control,
                    trendState = trendLast?.state ?: determineStateFromScores(
                        stability = stability,
                        anxiety = anxiety,
                        energy = energy,
                        control = control
                    ),
                    summary = buildWeeklySummary(
                        rawWeekDays = rawWeekDays,
                        trendLast = trendLast
                    )
                )
            }
    }

    /**
     * 週 / 月の流れモデル:
     * rawToday を前日の trend と平滑化して作る。
     */
    fun analyzeDayTrend(
        rawToday: DailyPersonalityScore,
        previousTrend: DailyPersonalityScore? = null
    ): DailyPersonalityScore {
        if (previousTrend == null) {
            return rawToday.copy(
                summary = buildDailySummary(
                    state = rawToday.state,
                    stability = rawToday.stability,
                    anxiety = rawToday.anxiety,
                    energy = rawToday.energy,
                    control = rawToday.control,
                    features = rawToday.features,
                    baseline = buildPersonalBaseline(emptyList(), current = rawToday.features),
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

        val stability = round2(clamp(previousTrend.stability * inertia + rawToday.stability * currentWeight, 0.0, 100.0))
        val anxiety = round2(clamp(previousTrend.anxiety * inertia + rawToday.anxiety * currentWeight, 0.0, 10.0))
        val energy = round2(clamp(previousTrend.energy * inertia + rawToday.energy * currentWeight, 0.0, 100.0))
        val control = round2(clamp(previousTrend.control * inertia + rawToday.control * currentWeight, 0.0, 100.0))
        val state = determineStateFromScores(stability, anxiety, energy, control)

        return DailyPersonalityScore(
            date = rawToday.date,
            stability = stability,
            anxiety = anxiety,
            energy = energy,
            control = control,
            state = state,
            summary = buildDailySummary(
                state = state,
                stability = stability,
                anxiety = anxiety,
                energy = energy,
                control = control,
                features = rawToday.features,
                baseline = buildPersonalBaseline(emptyList(), current = rawToday.features),
                isTrend = true
            ),
            features = rawToday.features
        )
    }

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

    /**
     * 1日内グラフ:
     * その日だけで積み上げて見る。昨日の影響は入れない。
     * さらに、直近の感情をやや重く扱う。
     */
    fun buildIntradayScoreSeries(
        messages: List<MessageV2>,
        dailyRecord: DailyRecord? = null,
        zoneId: ZoneId = ZoneId.of("Asia/Tokyo"),
        baseline: PersonalBaseline? = null
    ): List<IntradayPersonalityPoint> {
        if (messages.isEmpty()) return emptyList()

        val sorted = messages.sortedBy { it.timestamp }
        val date = Instant.ofEpochMilli(sorted.first().timestamp).atZone(zoneId).toLocalDate()
        val points = mutableListOf<IntradayPersonalityPoint>()

        sorted.indices.forEach { index ->
            val partialMessages = sorted.subList(0, index + 1)
            val features = extractStructuredFeatures(
                messages = partialMessages,
                dailyRecord = dailyRecord,
                emotionWeighting = EmotionWeighting.RECENT_BIASED
            )
            val core = calculateRawCore(features)

            points += IntradayPersonalityPoint(
                index = index,
                timestamp = sorted[index].timestamp,
                stability = clamp(core.stabilityRaw, 0.0, 100.0).toFloat(),
                anxiety = clamp(core.anxietyRaw, 0.0, 10.0).toFloat(),
                energy = clamp(core.energyRaw, 0.0, 100.0).toFloat(),
                control = clamp(core.controlRaw, 0.0, 100.0).toFloat()
            )
        }

        return points
    }

    private fun calculateRawCore(
        features: DailyStructuredFeatures
    ): ScoreCore {
        val e = features.emotions
        val f = features.flags

        val anxietyNorm = normalizeEmotionAbs(e.anxiety)
        val angryNorm = normalizeEmotionAbs(e.angry)
        val sadNorm = normalizeEmotionAbs(e.sad)
        val happyNorm = normalizeEmotionAbs(e.happy)
        val calmNorm = normalizeEmotionAbs(e.calm)

        val sleepNorm = normalizeSleepHours(features.sleepHours)
        val sleepQualityNorm = normalizeSleepQuality(features.sleepQuality)
        val stepsNorm = normalizeSteps(features.steps)

        val exercisedNorm = normalizeCount(f.exercised, 2)
        val socializedNorm = normalizeCount(f.socialized, 2)
        val intentNorm = normalizeCount(f.intent, 2)
        val insightNorm = normalizeCount(f.insight, 2)
        val reflectionNorm = normalizeCount(f.reflection, 2)
        val quickActionNorm = normalizeCount(f.quickAction, 3)

        val pendingTaskNorm = normalizeCount(f.pendingTask, 3)
        val meetingStressNorm = normalizeCount(f.meetingStress, 2)
        val smartphoneDriftNorm = normalizeCount(f.smartphoneDrift, 3)
        val alcoholNorm = normalizeCount(f.alcohol, 2)
        val hangoverNorm = normalizeCount(f.hangover, 1)

        val effectivePendingTaskNorm = clamp(pendingTaskNorm - 0.85 * quickActionNorm, 0.0, 1.0)
        val quickActionReliefNorm = clamp(quickActionNorm - 0.35 * pendingTaskNorm, 0.0, 1.0)

        // 指標ごとに「何に反応するか」を分ける。
        // 安定度: 安心感・喜び・睡眠・対人で上がりやすい。
        // 活力: 睡眠・歩数・運動で上下しやすい。
        // 制御感: 意図・気づき・内省と、未処理/すぐやる/会議/スマホ逸脱で上下しやすい。
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
                    2.0 * insightNorm +
                    3.0 * reflectionNorm +
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
                    0.5 * insightNorm -
                    0.3 * reflectionNorm -
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
                    3.0 * insightNorm +
                    2.0 * reflectionNorm +
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
                    14.0 * insightNorm +
                    12.0 * reflectionNorm +
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

        return ScoreCore(
            stabilityRaw = stabilityRaw,
            anxietyRaw = anxietyRaw,
            energyRaw = energyRaw,
            controlRaw = controlRaw
        )
    }

    private fun calculateShock(rawToday: DailyPersonalityScore): Double {
        val e = rawToday.features.emotions
        val f = rawToday.features.flags
        val pendingNorm = normalizeCount(f.pendingTask, 3)
        val quickActionNorm = normalizeCount(f.quickAction, 3)
        val effectivePendingNorm = clamp(pendingNorm - 0.85 * quickActionNorm, 0.0, 1.0)

        val shock =
            0.40 * normalizeEmotionAbs(e.anxiety) +
                    0.14 * normalizeEmotionAbs(e.sad) +
                    0.18 * normalizeEmotionAbs(e.angry) +
                    0.16 * effectivePendingNorm +
                    0.08 * normalizeEmotionAbs(e.anxiety) * effectivePendingNorm +
                    0.06 * normalizeEmotionAbs(e.angry) * effectivePendingNorm -
                    0.10 * quickActionNorm +
                    0.10 * normalizeCount(f.meetingStress, 2) +
                    0.08 * normalizeCount(f.smartphoneDrift, 2) +
                    0.10 * when {
                (rawToday.features.sleepHours ?: 7.0) < 4.5 -> 1.0
                (rawToday.features.sleepHours ?: 7.0) < 5.5 -> 0.6
                else -> 0.0
            }

        return clamp(shock, 0.0, 1.0)
    }

    private fun determineStateFromScores(
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

    private fun buildDailySummary(
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

        val sleepDelta = compareHigherIsBetter(features.sleepHours, baseline.sleepHours, 1.5)
        val stepsDelta = compareHigherIsBetter(features.steps?.toDouble(), baseline.steps, 3000.0)
        val intentDelta = compareHigherIsBetter(f.intent.toDouble(), baseline.flags.intent, 1.0)
        val insightDelta = compareHigherIsBetter(f.insight.toDouble(), baseline.flags.insight, 1.0)
        val reflectionDelta = compareHigherIsBetter(f.reflection.toDouble(), baseline.flags.reflection, 1.0)
        val socializedDelta = compareHigherIsBetter(f.socialized.toDouble(), baseline.flags.socialized, 1.0)
        val quickActionDelta = compareHigherIsBetter(f.quickAction.toDouble(), baseline.flags.quickAction, 1.0)
        val anxietyDelta = compareLowerIsBetter(e.anxiety, baseline.emotions.anxiety, EMOTION_DELTA_SCALE)
        val sadDelta = compareLowerIsBetter(e.sad, baseline.emotions.sad, EMOTION_DELTA_SCALE)
        val pendingTaskDelta = compareLowerIsBetter(f.pendingTask.toDouble(), baseline.flags.pendingTask, 1.2)
        val meetingStressDelta = compareLowerIsBetter(f.meetingStress.toDouble(), baseline.flags.meetingStress, 1.0)
        val smartphoneDelta = compareLowerIsBetter(f.smartphoneDrift.toDouble(), baseline.flags.smartphoneDrift, 1.0)
        val calmDelta = compareHigherIsBetter(e.calm, baseline.emotions.calm, EMOTION_DELTA_SCALE)
        val happyDelta = compareHigherIsBetter(e.happy, baseline.emotions.happy, EMOTION_DELTA_SCALE)

        if (sleepDelta >= 0.35) plus += "睡眠が平常より良い"
        if (stepsDelta >= 0.35) plus += "歩数が平常より多い"
        if (socializedDelta >= 0.35) plus += "会話が平常より多い"
        if (intentDelta >= 0.35) plus += "挑戦が平常より多い"
        if (insightDelta >= 0.35) plus += "気づきが平常より多い"
        if (reflectionDelta >= 0.35) plus += "内省が平常より多い"
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

        val plusText = if (plus.isNotEmpty()) {
            "追い風: ${plus.joinToString("、")}。"
        } else {
            ""
        }

        val minusText = if (minus.isNotEmpty()) {
            "注意点: ${minus.joinToString("、")}。"
        } else {
            ""
        }

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
            PersonalityState.EXHAUSTED -> {
                "まず休息優先。睡眠・食事・刺激を減らす方向が合う日。"
            }
        }

        return listOf(
            head,
            "安定度${format0(stability)} / 不安${round1(anxiety)} / 活力${format0(energy)} / 制御感${format0(control)}。",
            plusText,
            minusText,
            tail
        ).filter { it.isNotBlank() }.joinToString(" ")
    }

    private fun buildWeeklySummary(
        rawWeekDays: List<DailyPersonalityScore>,
        trendLast: DailyPersonalityScore?
    ): String {
        val stability = rawWeekDays.map { it.stability }.average()
        val anxiety = rawWeekDays.map { it.anxiety }.average()
        val energy = rawWeekDays.map { it.energy }.average()
        val control = rawWeekDays.map { it.control }.average()

        val dominantState = trendLast?.state ?: determineStateFromScores(
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
            "平均: 安定度${format0(stability)} / 不安${round1(anxiety)} / 活力${format0(energy)} / 制御感${format0(control)}。",
            dayText,
            tail
        ).joinToString(" ")
    }

    private fun buildPersonalBaseline(
        history: List<DailyStructuredFeatures>,
        current: DailyStructuredFeatures? = null
    ): PersonalBaseline {
        val recent = history.takeLast(BASELINE_WINDOW_DAYS)

        if (recent.isEmpty()) {
            return PersonalBaseline(
                sleepHours = blend(current?.sleepHours, 6.8, 0.35),
                sleepQuality = blend(current?.sleepQuality?.toDouble(), 1.5, 0.35),
                steps = blend(current?.steps?.toDouble(), 5000.0, 0.35),
                emotions = DailyEmotionAverages(
                    anxiety = blend(current?.emotions?.anxiety, 1.2, 0.35),
                    angry = blend(current?.emotions?.angry, 0.7, 0.35),
                    sad = blend(current?.emotions?.sad, 1.0, 0.35),
                    happy = blend(current?.emotions?.happy, 1.6, 0.35),
                    calm = blend(current?.emotions?.calm, 1.6, 0.35)
                ),
                flags = DailyFlagAverages(
                    exercised = blend(current?.flags?.exercised?.toDouble(), 0.4, 0.35),
                    socialized = blend(current?.flags?.socialized?.toDouble(), 0.7, 0.35),
                    intent = blend(current?.flags?.intent?.toDouble(), 0.4, 0.35),
                    insight = blend(current?.flags?.insight?.toDouble(), 0.4, 0.35),
                    reflection = blend(current?.flags?.reflection?.toDouble(), 0.5, 0.35),
                    quickAction = blend(current?.flags?.quickAction?.toDouble(), 0.6, 0.35),
                    pendingTask = blend(current?.flags?.pendingTask?.toDouble(), 0.8, 0.35),
                    meetingStress = blend(current?.flags?.meetingStress?.toDouble(), 0.5, 0.35),
                    smartphoneDrift = blend(current?.flags?.smartphoneDrift?.toDouble(), 0.6, 0.35),
                    alcohol = blend(current?.flags?.alcohol?.toDouble(), 0.1, 0.35),
                    hangover = blend(current?.flags?.hangover?.toDouble(), 0.05, 0.35)
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
                intent = recent.map { it.flags.intent.toDouble() }.averageOr(0.4),
                insight = recent.map { it.flags.insight.toDouble() }.averageOr(0.4),
                reflection = recent.map { it.flags.reflection.toDouble() }.averageOr(0.5),
                quickAction = recent.map { it.flags.quickAction.toDouble() }.averageOr(0.6),
                pendingTask = recent.map { it.flags.pendingTask.toDouble() }.averageOr(0.8),
                meetingStress = recent.map { it.flags.meetingStress.toDouble() }.averageOr(0.5),
                smartphoneDrift = recent.map { it.flags.smartphoneDrift.toDouble() }.averageOr(0.6),
                alcohol = recent.map { it.flags.alcohol.toDouble() }.averageOr(0.1),
                hangover = recent.map { it.flags.hangover.toDouble() }.averageOr(0.05)
            )
        )
    }

    private fun averageEmotionOf(
        messages: List<MessageV2>,
        selector: (MessageV2) -> Int,
        weighting: EmotionWeighting
    ): Double {
        if (messages.isEmpty()) return 0.0

        if (weighting == EmotionWeighting.UNIFORM) {
            return messages.map(selector).average().coerceIn(0.0, EMOTION_MAX)
        }

        val lastValue = selector(messages.last()).toDouble()
        val weightedSum = messages.indices.sumOf { index ->
            val distanceFromLast = (messages.lastIndex - index).toDouble()
            val weight = INTRADAY_RECENCY_BASE.pow(-distanceFromLast)
            selector(messages[index]).toDouble() * weight
        }
        val totalWeight = messages.indices.sumOf { index ->
            val distanceFromLast = (messages.lastIndex - index).toDouble()
            INTRADAY_RECENCY_BASE.pow(-distanceFromLast)
        }
        val weightedAverage = if (totalWeight <= 0.0) 0.0 else weightedSum / totalWeight

        return clamp(
            weightedAverage * (1.0 - LAST_MESSAGE_BLEND) + lastValue * LAST_MESSAGE_BLEND,
            0.0,
            EMOTION_MAX
        )
    }

    private fun compareHigherIsBetter(
        current: Double?,
        baseline: Double,
        scale: Double
    ): Double {
        if (current == null || scale <= 0.0) return 0.0
        return clamp((current - baseline) / scale, -1.0, 1.0)
    }

    private fun compareLowerIsBetter(
        current: Double?,
        baseline: Double,
        scale: Double
    ): Double {
        if (current == null || scale <= 0.0) return 0.0
        return clamp((baseline - current) / scale, -1.0, 1.0)
    }

    private fun normalizeEmotionAbs(value: Double): Double {
        return clamp(value / EMOTION_MAX, 0.0, 1.0)
    }

    private fun normalizeCount(value: Int, max: Int): Double {
        if (max <= 0) return 0.0
        return clamp(value.toDouble() / max.toDouble(), 0.0, 1.0)
    }

    private fun normalizeSleepHours(value: Double?): Double {
        if (value == null) return 0.5
        return clamp((value - 4.0) / 4.0, 0.0, 1.0)
    }

    private fun normalizeSleepQuality(value: Int?): Double {
        if (value == null) return 0.5
        return clamp(value.toDouble() / 3.0, 0.0, 1.0)
    }

    private fun normalizeSteps(value: Int?): Double {
        if (value == null) return 0.5
        return clamp(value.toDouble() / 8000.0, 0.0, 1.0)
    }

    private fun blend(current: Double?, default: Double, currentWeight: Double): Double {
        return if (current == null) {
            default
        } else {
            current * currentWeight + default * (1.0 - currentWeight)
        }
    }

    private fun Iterable<Double>.averageOr(default: Double): Double {
        val list = this.toList()
        return if (list.isEmpty()) default else list.average()
    }

    private fun clamp(value: Double, min: Double, max: Double): Double {
        return when {
            value < min -> min
            value > max -> max
            else -> value
        }
    }

    private fun round1(value: Double): Double {
        return (value * 10.0).roundToInt() / 10.0
    }

    private fun round2(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }

    private fun format0(value: Double): String {
        return value.roundToInt().toString()
    }

    private fun String.toLocalDateOrNull(): LocalDate? {
        return runCatching { LocalDate.parse(this, dateFormatter) }.getOrNull()
    }

    private inline fun <K, V> Iterable<V>.associateByNotNull(
        keySelector: (V) -> K?
    ): Map<K, V> {
        val map = linkedMapOf<K, V>()
        for (item in this) {
            val key = keySelector(item) ?: continue
            map[key] = item
        }
        return map
    }
}
