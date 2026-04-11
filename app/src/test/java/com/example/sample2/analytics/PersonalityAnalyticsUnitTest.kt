package com.example.sample2.analytics

import com.example.sample2.data.ActionFlags
import com.example.sample2.data.DailyRecord
import com.example.sample2.data.EmotionMetrics
import com.example.sample2.data.MessageV2
import com.example.sample2.data.SleepData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class PersonalityAnalyticsUnitTest {

    private val featureExtractor = FeatureExtractor()
    private val baselineCalculator = BaselineCalculator()
    private val scoreCalculator = ScoreCalculator()
    private val trendCalculator = TrendCalculator()
    private val summaryBuilder = SummaryBuilder(scoreCalculator)

    @Test
    fun featureExtractor_returnsZeroFeatures_whenMessagesAreEmpty() {
        val features = featureExtractor.extractStructuredFeatures(messages = emptyList())

        assertEquals(0.0, features.emotions.anxiety, 0.0001)
        assertEquals(0.0, features.emotions.happy, 0.0001)
        assertEquals(0, features.flags.quickAction)
        assertEquals(0, features.flags.pendingTask)
        assertEquals(null, features.sleepHours)
        assertEquals(null, features.sleepQuality)
        assertEquals(null, features.steps)
    }

    @Test
    fun featureExtractor_extractsEmotionsFlagsAndDailyRecord() {
        val features = featureExtractor.extractStructuredFeatures(
            messages = listOf(
                message(
                    id = "m1",
                    anxiety = 2,
                    happy = 1,
                    quickAction = true
                ),
                message(
                    id = "m2",
                    anxiety = 0,
                    calm = 2,
                    pendingTask = true
                )
            ),
            dailyRecord = DailyRecord(
                date = "2026-04-10",
                sleep = SleepData(durationMinutes = 420, quality = 3),
                steps = 6400
            )
        )

        assertEquals(1.0, features.emotions.anxiety, 0.0001)
        assertEquals(0.5, features.emotions.happy, 0.0001)
        assertEquals(1.0, features.emotions.calm, 0.0001)
        assertEquals(1, features.flags.quickAction)
        assertEquals(1, features.flags.pendingTask)
        assertEquals(7.0, features.sleepHours!!, 0.0001)
        assertEquals(3, features.sleepQuality)
        assertEquals(6400, features.steps)
    }

    @Test
    fun baselineCalculator_blendsCurrent_whenHistoryIsEmpty() {
        val current = DailyStructuredFeatures(
            sleepHours = 8.0,
            sleepQuality = 3,
            steps = 8000,
            emotions = DailyEmotionAverages(anxiety = 3.0, angry = 0.0, sad = 1.0, happy = 1.0, calm = 2.0),
            flags = DailyFlagCounts(
                exercised = 1,
                socialized = 2,
                intent = 1,
                insight = 0,
                reflection = 1,
                quickAction = 3,
                pendingTask = 0,
                meetingStress = 0,
                smartphoneDrift = 1,
                alcohol = 0,
                hangover = 0
            )
        )

        val baseline = baselineCalculator.buildPersonalBaseline(history = emptyList(), current = current)

        assertEquals(7.22, baseline.sleepHours, 0.0001)
        assertEquals(2.025, baseline.sleepQuality, 0.0001)
        assertEquals(6050.0, baseline.steps, 0.0001)
        assertEquals(1.83, baseline.emotions.anxiety, 0.0001)
        assertEquals(1.44, baseline.flags.quickAction, 0.0001)
    }

    @Test
    fun baselineCalculator_usesHistoryAverage_whenHistoryExists() {
        val history = listOf(
            featuresForBaseline(anxiety = 1.0, quickAction = 0, pendingTask = 2),
            featuresForBaseline(anxiety = 2.0, quickAction = 2, pendingTask = 1),
            featuresForBaseline(anxiety = 3.0, quickAction = 1, pendingTask = 0)
        )

        val baseline = baselineCalculator.buildPersonalBaseline(history = history)

        assertEquals(7.0, baseline.sleepHours, 0.0001)
        assertEquals(2.0, baseline.sleepQuality, 0.0001)
        assertEquals(5000.0, baseline.steps, 0.0001)
        assertEquals(2.0, baseline.emotions.anxiety, 0.0001)
        assertEquals(1.0, baseline.flags.quickAction, 0.0001)
        assertEquals(1.0, baseline.flags.pendingTask, 0.0001)
    }

    @Test
    fun scoreCalculator_appliesQuickActionPendingTaskOffset() {
        val base = DailyStructuredFeatures(
            sleepHours = 7.0,
            sleepQuality = 2,
            steps = 5000,
            emotions = DailyEmotionAverages(anxiety = 1.0, angry = 0.0, sad = 0.0, happy = 1.0, calm = 1.0),
            flags = DailyFlagCounts(
                exercised = 0,
                socialized = 0,
                intent = 0,
                insight = 0,
                reflection = 0,
                quickAction = 0,
                pendingTask = 3,
                meetingStress = 0,
                smartphoneDrift = 0,
                alcohol = 0,
                hangover = 0
            )
        )
        val withQuickAction = base.copy(flags = base.flags.copy(quickAction = 3))

        val noOffsetScore = scoreCalculator.calculateRawCore(base)
        val offsetScore = scoreCalculator.calculateRawCore(withQuickAction)

        assertTrue(offsetScore.controlRaw > noOffsetScore.controlRaw)
        assertTrue(offsetScore.stabilityRaw > noOffsetScore.stabilityRaw)
        assertTrue(offsetScore.anxietyRaw < noOffsetScore.anxietyRaw)
    }

    @Test
    fun scoreCalculator_snapshot_forEmotionOnlyAndActionOnlyCases() {
        val emotionOnly = DailyStructuredFeatures(
            sleepHours = null,
            sleepQuality = null,
            steps = null,
            emotions = DailyEmotionAverages(anxiety = 2.0, angry = 1.0, sad = 2.0, happy = 0.0, calm = 0.0),
            flags = DailyFlagCounts(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        )
        val actionOnly = DailyStructuredFeatures(
            sleepHours = null,
            sleepQuality = null,
            steps = null,
            emotions = DailyEmotionAverages(0.0, 0.0, 0.0, 0.0, 0.0),
            flags = DailyFlagCounts(
                exercised = 1,
                socialized = 1,
                intent = 1,
                insight = 1,
                reflection = 1,
                quickAction = 2,
                pendingTask = 0,
                meetingStress = 0,
                smartphoneDrift = 0,
                alcohol = 0,
                hangover = 0
            )
        )

        val emotionCore = scoreCalculator.calculateRawCore(emotionOnly)
        val actionCore = scoreCalculator.calculateRawCore(actionOnly)

        assertEquals(26.13, PersonalityMath.round2(emotionCore.stabilityRaw), 0.0001)
        assertEquals(7.23, PersonalityMath.round2(emotionCore.anxietyRaw), 0.0001)
        assertEquals(20.0, PersonalityMath.round2(emotionCore.energyRaw), 0.0001)
        assertEquals(23.08, PersonalityMath.round2(emotionCore.controlRaw), 0.0001)

        assertEquals(72.92, PersonalityMath.round2(actionCore.stabilityRaw), 0.0001)
        assertEquals(0.0, PersonalityMath.round2(actionCore.anxietyRaw), 0.0001)
        assertEquals(70.83, PersonalityMath.round2(actionCore.energyRaw), 0.0001)
        assertEquals(91.42, PersonalityMath.round2(actionCore.controlRaw), 0.0001)
    }

    @Test
    fun trendCalculator_smoothsTowardPreviousTrend() {
        val previousTrend = DailyPersonalityScore(
            date = LocalDate.of(2026, 4, 9),
            stability = 80.0,
            anxiety = 2.0,
            energy = 76.0,
            control = 78.0,
            state = PersonalityState.STABLE,
            summary = "prev",
            features = featuresForBaseline(anxiety = 0.5, quickAction = 1, pendingTask = 0)
        )
        val rawToday = DailyPersonalityScore(
            date = LocalDate.of(2026, 4, 10),
            stability = 40.0,
            anxiety = 8.0,
            energy = 32.0,
            control = 34.0,
            state = PersonalityState.TENSE,
            summary = "raw",
            features = featuresForBaseline(anxiety = 3.0, quickAction = 0, pendingTask = 3)
        )

        val trended = trendCalculator.analyzeDayTrend(
            rawToday = rawToday,
            previousTrend = previousTrend,
            baselineCalculator = baselineCalculator,
            summaryBuilder = summaryBuilder
        )

        assertEquals(58.0, trended.stability, 0.0001)
        assertEquals(5.3, trended.anxiety, 0.0001)
        assertEquals(49.6, trended.energy, 0.0001)
        assertEquals(53.8, trended.control, 0.0001)
        assertEquals(PersonalityState.RECOVERING, trended.state)
    }

    @Test
    fun facadeApi_analyzeDay_andSummarySnapshot() {
        val date = LocalDate.of(2026, 4, 10)
        val result = PersonalityScoreModel.analyzeDay(
            date = date,
            messages = listOf(
                message(id = "m1", anxiety = 1, happy = 1, quickAction = true),
                message(id = "m2", anxiety = 2, pendingTask = true)
            ),
            dailyRecord = DailyRecord(
                date = "2026-04-10",
                sleep = SleepData(durationMinutes = 360, quality = 2),
                steps = 3000
            )
        )

        assertEquals(LocalDate.of(2026, 4, 10), result.date)
        assertEquals(40.43, result.stability, 0.0001)
        assertEquals(4.78, result.anxiety, 0.0001)
        assertEquals(34.03, result.energy, 0.0001)
        assertEquals(42.29, result.control, 0.0001)
        assertEquals(PersonalityState.EXHAUSTED, result.state)
        assertEquals(
            "今日単体では消耗寄り。 安定度40 / 不安4.8 / 活力34 / 制御感42。 追い風: すぐやるが平常より多い。 注意点: 未処理が平常より多い、強い不安。 まず休息優先。睡眠・食事・刺激を減らす方向が合う日。",
            result.summary
        )
    }

    @Test
    fun facadeApi_buildIntradayScoreSeries_createsProgressiveSeries() {
        val points = PersonalityScoreModel.buildIntradayScoreSeries(
            messages = listOf(
                message(id = "m1", timestamp = 1_000L, anxiety = 0, happy = 1, quickAction = true),
                message(id = "m2", timestamp = 2_000L, anxiety = 2, pendingTask = true),
                message(id = "m3", timestamp = 3_000L, anxiety = 1, calm = 1)
            ),
            dailyRecord = DailyRecord(
                date = "2026-04-10",
                sleep = SleepData(durationMinutes = 420, quality = 3),
                steps = 7000
            ),
            zoneId = ZoneId.of("UTC")
        )

        assertEquals(3, points.size)
        assertEquals(0, points.first().index)
        assertEquals(1_000L, points.first().timestamp)
        assertEquals(2, points.last().index)
        assertEquals(3_000L, points.last().timestamp)

        assertEquals(83.75f, points[0].stability, 0.001f)
        assertEquals(0.0f, points[0].anxiety, 0.001f)
        assertEquals(89.92f, points[0].energy, 0.001f)
        assertEquals(98.63f, points[0].control, 0.001f)

        assertEquals(53.47f, points[1].stability, 0.001f)
        assertEquals(4.4f, points[1].anxiety, 0.001f)
        assertEquals(58.08f, points[1].energy, 0.001f)
        assertEquals(58.26f, points[1].control, 0.001f)

        assertEquals(56.31f, points[2].stability, 0.001f)
        assertEquals(4.26f, points[2].anxiety, 0.001f)
        assertEquals(56.84f, points[2].energy, 0.001f)
        assertEquals(58.53f, points[2].control, 0.001f)
    }

    @Test
    fun facadeApi_analyzeWeeks_buildsWeeklyTrend() {
        val zone = ZoneId.of("UTC")
        val messages = listOf(
            message(id = "d1", timestamp = LocalDate.of(2026, 4, 6).atStartOfDay(zone).toInstant().toEpochMilli(), happy = 2, calm = 2, quickAction = true),
            message(id = "d2", timestamp = LocalDate.of(2026, 4, 7).atStartOfDay(zone).toInstant().toEpochMilli(), anxiety = 2, pendingTask = true),
            message(id = "d3", timestamp = LocalDate.of(2026, 4, 8).atStartOfDay(zone).toInstant().toEpochMilli(), anxiety = 1, calm = 1)
        )

        val weeks = PersonalityScoreModel.analyzeWeeks(messages = messages, zoneId = zone)

        assertEquals(1, weeks.size)
        val week = weeks.single()
        assertEquals(LocalDate.of(2026, 4, 6), week.weekStart)
        assertEquals(3, week.dayCount)
        assertEquals(64.17, week.stability, 0.0001)
        assertEquals(2.2, week.anxiety, 0.0001)
        assertEquals(65.71, week.energy, 0.0001)
        assertEquals(74.45, week.control, 0.0001)
        assertEquals(72.54, week.trendStability, 0.0001)
        assertEquals(1.8, week.trendAnxiety, 0.0001)
        assertEquals(72.15, week.trendEnergy, 0.0001)
        assertEquals(80.32, week.trendControl, 0.0001)
        assertEquals(PersonalityState.STABLE, week.trendState)
    }

    private fun message(
        id: String,
        timestamp: Long = 0L,
        anxiety: Int = 0,
        angry: Int = 0,
        sad: Int = 0,
        happy: Int = 0,
        calm: Int = 0,
        quickAction: Boolean = false,
        pendingTask: Boolean = false
    ): MessageV2 = MessageV2(
        id = id,
        timestamp = timestamp,
        text = "msg-$id",
        emotions = EmotionMetrics(
            anxiety = anxiety,
            angry = angry,
            sad = sad,
            happy = happy,
            calm = calm
        ),
        flags = ActionFlags(
            quickAction = quickAction,
            pendingTask = pendingTask
        )
    )

    private fun featuresForBaseline(
        anxiety: Double,
        quickAction: Int,
        pendingTask: Int
    ): DailyStructuredFeatures = DailyStructuredFeatures(
        sleepHours = 7.0,
        sleepQuality = 2,
        steps = 5000,
        emotions = DailyEmotionAverages(
            anxiety = anxiety,
            angry = 0.0,
            sad = 0.0,
            happy = 1.0,
            calm = 1.0
        ),
        flags = DailyFlagCounts(
            exercised = 0,
            socialized = 0,
            intent = 0,
            insight = 0,
            reflection = 0,
            quickAction = quickAction,
            pendingTask = pendingTask,
            meetingStress = 0,
            smartphoneDrift = 0,
            alcohol = 0,
            hangover = 0
        )
    )
}
