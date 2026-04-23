package com.example.sample2.analytics

import com.example.sample2.data.DailyRecord
import com.example.sample2.data.MessageV2
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

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
    val delegate: Int,
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
    val stability: Double,
    val anxiety: Double,
    val energy: Double,
    val control: Double,
    val state: PersonalityState,
    val summary: String,
    val features: DailyStructuredFeatures
)

data class WeeklyPersonalityScore(
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val dayCount: Int,
    val stability: Double,
    val anxiety: Double,
    val energy: Double,
    val control: Double,
    val trendStability: Double,
    val trendAnxiety: Double,
    val trendEnergy: Double,
    val trendControl: Double,
    val trendState: PersonalityState,
    val summary: String
)

data class DailyFlagAverages(
    val exercised: Double,
    val socialized: Double,
    val delegate: Double,
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

enum class EmotionWeighting {
    UNIFORM,
    RECENT_BIASED
}

object PersonalityScoreModel {

    private val featureExtractor = FeatureExtractor()
    private val baselineCalculator = BaselineCalculator()
    private val scoreCalculator = ScoreCalculator()
    private val summaryBuilder = SummaryBuilder(scoreCalculator)
    private val trendCalculator = TrendCalculator()

    fun analyzeAllDays(
        messages: List<MessageV2>,
        dailyRecords: List<DailyRecord> = emptyList(),
        zoneId: ZoneId = ZoneId.of("Asia/Tokyo")
    ): List<DailyPersonalityScore> = analyzeAllDaysRaw(messages, dailyRecords, zoneId)

    fun analyzeDay(
        date: LocalDate,
        messages: List<MessageV2>,
        dailyRecord: DailyRecord? = null,
        baseline: PersonalBaseline? = null
    ): DailyPersonalityScore = analyzeDayFocused(date, messages, dailyRecord, baseline)

    fun analyzeAllDaysRaw(
        messages: List<MessageV2>,
        dailyRecords: List<DailyRecord> = emptyList(),
        zoneId: ZoneId = ZoneId.of("Asia/Tokyo")
    ): List<DailyPersonalityScore> {
        val messageMap = messages.groupBy {
            Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate()
        }

        val dailyRecordMap = dailyRecords.associateByNotNull { record ->
            record.toLocalDateOrNullFromDate()
        }

        val allDates = (messageMap.keys + dailyRecordMap.keys).toSortedSet()
        val historyFeatures = mutableListOf<DailyStructuredFeatures>()
        val results = mutableListOf<DailyPersonalityScore>()

        for (date in allDates) {
            val baseline = baselineCalculator.buildPersonalBaseline(
                historyFeatures.takeLast(PersonalityAnalysisConstants.BASELINE_WINDOW_DAYS)
            )
            val score = analyzeDayRaw(date, messageMap[date].orEmpty(), dailyRecordMap[date], baseline)
            results += score
            historyFeatures += score.features
        }

        return results
    }

    fun analyzeDayFocused(
        date: LocalDate,
        messages: List<MessageV2>,
        dailyRecord: DailyRecord? = null,
        baseline: PersonalBaseline? = null
    ): DailyPersonalityScore {
        val features = featureExtractor.extractStructuredFeatures(
            messages = messages,
            dailyRecord = dailyRecord,
            emotionWeighting = EmotionWeighting.RECENT_BIASED
        )
        val personalBaseline = baseline ?: baselineCalculator.buildPersonalBaseline(emptyList(), current = features)
        val core = scoreCalculator.calculateRawCore(features)

        val stability = PersonalityMath.round2(PersonalityMath.clamp(core.stabilityRaw, 0.0, 100.0))
        val anxiety = PersonalityMath.round2(PersonalityMath.clamp(core.anxietyRaw, 0.0, 10.0))
        val energy = PersonalityMath.round2(PersonalityMath.clamp(core.energyRaw, 0.0, 100.0))
        val control = PersonalityMath.round2(PersonalityMath.clamp(core.controlRaw, 0.0, 100.0))
        val state = scoreCalculator.determineStateFromScores(stability, anxiety, energy, control)

        return DailyPersonalityScore(
            date = date,
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
                features = features,
                baseline = personalBaseline,
                isTrend = false
            ),
            features = features
        )
    }

    fun analyzeDayRaw(
        date: LocalDate,
        messages: List<MessageV2>,
        dailyRecord: DailyRecord? = null,
        baseline: PersonalBaseline? = null
    ): DailyPersonalityScore {
        val features = featureExtractor.extractStructuredFeatures(
            messages = messages,
            dailyRecord = dailyRecord,
            emotionWeighting = EmotionWeighting.UNIFORM
        )
        val personalBaseline = baseline ?: baselineCalculator.buildPersonalBaseline(emptyList(), current = features)
        val core = scoreCalculator.calculateRawCore(features)

        val stability = PersonalityMath.round2(PersonalityMath.clamp(core.stabilityRaw, 0.0, 100.0))
        val anxiety = PersonalityMath.round2(PersonalityMath.clamp(core.anxietyRaw, 0.0, 10.0))
        val energy = PersonalityMath.round2(PersonalityMath.clamp(core.energyRaw, 0.0, 100.0))
        val control = PersonalityMath.round2(PersonalityMath.clamp(core.controlRaw, 0.0, 100.0))
        val state = scoreCalculator.determineStateFromScores(stability, anxiety, energy, control)

        return DailyPersonalityScore(
            date = date,
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
                features = features,
                baseline = personalBaseline,
                isTrend = false
            ),
            features = features
        )
    }

    fun analyzeAllDaysTrend(
        messages: List<MessageV2>,
        dailyRecords: List<DailyRecord> = emptyList(),
        zoneId: ZoneId = ZoneId.of("Asia/Tokyo")
    ): List<DailyPersonalityScore> {
        val rawDays = analyzeAllDaysRaw(messages, dailyRecords, zoneId)
        val trendDays = mutableListOf<DailyPersonalityScore>()

        rawDays.forEach { rawToday ->
            val trendToday = analyzeDayTrend(rawToday = rawToday, previousTrend = trendDays.lastOrNull())
            trendDays += trendToday
        }

        return trendDays
    }

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
            .groupBy { day -> day.date.with(TemporalAdjusters.previousOrSame(weekStartsOn)) }
            .toSortedMap()
            .map { (weekStart, rawWeekDays) ->
                val weekEnd = weekStart.plusDays(6)
                val trendWeekDays = rawWeekDays.mapNotNull { trendByDate[it.date] }
                val trendLast = trendWeekDays.lastOrNull()

                val stability = PersonalityMath.round2(rawWeekDays.map { it.stability }.average())
                val anxiety = PersonalityMath.round2(rawWeekDays.map { it.anxiety }.average())
                val energy = PersonalityMath.round2(rawWeekDays.map { it.energy }.average())
                val control = PersonalityMath.round2(rawWeekDays.map { it.control }.average())

                WeeklyPersonalityScore(
                    weekStart = weekStart,
                    weekEnd = weekEnd,
                    dayCount = rawWeekDays.size,
                    stability = PersonalityMath.clamp(stability, 0.0, 100.0),
                    anxiety = PersonalityMath.clamp(anxiety, 0.0, 10.0),
                    energy = PersonalityMath.clamp(energy, 0.0, 100.0),
                    control = PersonalityMath.clamp(control, 0.0, 100.0),
                    trendStability = trendLast?.stability ?: stability,
                    trendAnxiety = trendLast?.anxiety ?: anxiety,
                    trendEnergy = trendLast?.energy ?: energy,
                    trendControl = trendLast?.control ?: control,
                    trendState = trendLast?.state ?: scoreCalculator.determineStateFromScores(
                        stability = stability,
                        anxiety = anxiety,
                        energy = energy,
                        control = control
                    ),
                    summary = summaryBuilder.buildWeeklySummary(rawWeekDays, trendLast)
                )
            }
    }

    fun analyzeDayTrend(
        rawToday: DailyPersonalityScore,
        previousTrend: DailyPersonalityScore? = null
    ): DailyPersonalityScore {
        return trendCalculator.analyzeDayTrend(rawToday, previousTrend, baselineCalculator, summaryBuilder)
    }

    fun extractStructuredFeatures(
        messages: List<MessageV2>,
        dailyRecord: DailyRecord? = null,
        emotionWeighting: EmotionWeighting = EmotionWeighting.UNIFORM
    ): DailyStructuredFeatures {
        return featureExtractor.extractStructuredFeatures(messages, dailyRecord, emotionWeighting)
    }

    fun buildIntradayScoreSeries(
        messages: List<MessageV2>,
        dailyRecord: DailyRecord? = null,
        zoneId: ZoneId = ZoneId.of("Asia/Tokyo"),
        baseline: PersonalBaseline? = null
    ): List<IntradayPersonalityPoint> {
        if (messages.isEmpty()) return emptyList()

        val sorted = messages.sortedBy { it.timestamp }
        Instant.ofEpochMilli(sorted.first().timestamp).atZone(zoneId).toLocalDate()
        val points = mutableListOf<IntradayPersonalityPoint>()

        sorted.indices.forEach { index ->
            val partialMessages = sorted.subList(0, index + 1)
            val features = featureExtractor.extractStructuredFeatures(
                messages = partialMessages,
                dailyRecord = dailyRecord,
                emotionWeighting = EmotionWeighting.RECENT_BIASED
            )
            val core = scoreCalculator.calculateRawCore(features)

            points += IntradayPersonalityPoint(
                index = index,
                timestamp = sorted[index].timestamp,
                stability = PersonalityMath.clamp(core.stabilityRaw, 0.0, 100.0).toFloat(),
                anxiety = PersonalityMath.clamp(core.anxietyRaw, 0.0, 10.0).toFloat(),
                energy = PersonalityMath.clamp(core.energyRaw, 0.0, 100.0).toFloat(),
                control = PersonalityMath.clamp(core.controlRaw, 0.0, 100.0).toFloat()
            )
        }

        return points
    }
}

object PersonalityAnalyzer {
    fun analyzeAllDays(
        messages: List<MessageV2>,
        dailyRecords: List<DailyRecord> = emptyList(),
        zoneId: ZoneId = ZoneId.of("Asia/Tokyo")
    ): List<DailyPersonalityScore> = PersonalityScoreModel.analyzeAllDays(messages, dailyRecords, zoneId)

    fun analyzeDay(
        date: LocalDate,
        messages: List<MessageV2>,
        dailyRecord: DailyRecord? = null,
        baseline: PersonalBaseline? = null
    ): DailyPersonalityScore = PersonalityScoreModel.analyzeDay(date, messages, dailyRecord, baseline)

    fun analyzeWeeks(
        messages: List<MessageV2>,
        dailyRecords: List<DailyRecord> = emptyList(),
        zoneId: ZoneId = ZoneId.of("Asia/Tokyo"),
        weekStartsOn: DayOfWeek = DayOfWeek.MONDAY
    ): List<WeeklyPersonalityScore> = PersonalityScoreModel.analyzeWeeks(messages, dailyRecords, zoneId, weekStartsOn)

    fun buildIntradayScoreSeries(
        messages: List<MessageV2>,
        dailyRecord: DailyRecord? = null,
        zoneId: ZoneId = ZoneId.of("Asia/Tokyo"),
        baseline: PersonalBaseline? = null
    ): List<IntradayPersonalityPoint> =
        PersonalityScoreModel.buildIntradayScoreSeries(messages, dailyRecord, zoneId, baseline)
}
