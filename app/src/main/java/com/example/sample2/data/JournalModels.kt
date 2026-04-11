package com.example.sample2.data

data class MessageV2(
    val id: String,
    val timestamp: Long,
    val text: String,
    val emotions: EmotionMetrics = EmotionMetrics(),
    val flags: ActionFlags = ActionFlags(),
)

data class EmotionMetrics(
    val anxiety: Int = 0,
    val angry: Int = 0,
    val sad: Int = 0,
    val happy: Int = 0,
    val calm: Int = 0
)

data class ActionFlags(
    val exercised: Boolean = false,
    val socialized: Boolean = false,
    val intent: Boolean = false,
    val insight: Boolean = false,
    val reflection: Boolean = false,
    val quickAction: Boolean = false,

    // 負荷フラグ
    val pendingTask: Boolean = false,
    val meetingStress: Boolean = false,
    val smartphoneDrift: Boolean = false,
    val alcohol: Boolean = false,
    val hangover: Boolean = false
)

/**
 * 1日単位で保存する生活データ
 * date は yyyy-MM-dd を想定
 */
data class DailyRecord(
    val date: String,
    val sleep: SleepData = SleepData(),
    val steps: Int = 0
)

/**
 * 睡眠データ
 * durationMinutes を主軸にしておくと扱いやすい
 * start/end は分かる時だけ入れる
 */
data class SleepData(
    val durationMinutes: Int = 0,
    val quality: Int = 0,          // 0〜5 など
    val startTimestamp: Long? = null,
    val endTimestamp: Long? = null
) {
    val sleptWell: Boolean
        get() = quality >= 3
}

data class EmotionItem(
    val type: EmotionType,
    val score: Int
) {
    val key: String get() = type.key
    val label: String get() = type.label
    val enabled: Boolean get() = score > 0
}

data class ActionItem(
    val type: ActionType,
    val enabled: Boolean
) {
    val key: String get() = type.key
    val label: String get() = type.label
}

enum class EmotionType(
    val key: String,
    val label: String,
    private val scorer: (EmotionMetrics) -> Int
) {
    ANXIETY(
        key = "anxiety",
        label = "不安",
        scorer = { it.anxiety }
    ),
    ANGRY(
        key = "angry",
        label = "怒り",
        scorer = { it.angry }
    ),
    SAD(
        key = "sad",
        label = "悲しみ",
        scorer = { it.sad }
    ),
    HAPPY(
        key = "happy",
        label = "喜び",
        scorer = { it.happy }
    ),
    CALM(
        key = "calm",
        label = "安心",
        scorer = { it.calm }
    );

    fun scoreOf(emotions: EmotionMetrics): Int = scorer(emotions)

    fun matches(emotions: EmotionMetrics): Boolean = scoreOf(emotions) > 0
}

enum class ActionType(
    val key: String,
    val label: String,
    private val matcher: (ActionFlags) -> Boolean
) {
    EXERCISED(
        key = "exercised",
        label = "運動",
        matcher = { it.exercised }
    ),
    SOCIALIZED(
        key = "socialized",
        label = "会話",
        matcher = { it.socialized }
    ),
    INTENT(
        key = "intent",
        label = "ゴール",
        matcher = { it.intent }
    ),
    INSIGHT(
        key = "insight",
        label = "気づき",
        matcher = { it.insight }
    ),
    REFLECTION(
        key = "reflection",
        label = "内省",
        matcher = { it.reflection }
    ),
    QUICK_ACTION(
        key = "quick_action",
        label = "すぐやる",
        matcher = { it.quickAction }
    ),

    // 負荷フラグ
    PENDING_TASK(
        key = "pending_task",
        label = "未処理",
        matcher = { it.pendingTask }
    ),
    MEETING_STRESS(
        key = "meeting_stress",
        label = "会議負荷",
        matcher = { it.meetingStress }
    ),
    SMARTPHONE_DRIFT(
        key = "smartphone_drift",
        label = "スマホ逸脱",
        matcher = { it.smartphoneDrift }
    ),
    ALCOHOL(
        key = "alcohol",
        label = "飲酒",
        matcher = { it.alcohol }
    ),
    HANGOVER(
        key = "hangover",
        label = "体調不良",
        matcher = { it.hangover }
    );

    fun matches(flags: ActionFlags): Boolean = matcher(flags)
}

fun EmotionMetrics.toItemList(): List<EmotionItem> {
    return EmotionType.entries.map { type ->
        EmotionItem(type = type, score = type.scoreOf(this))
    }
}

fun ActionFlags.toItemList(): List<ActionItem> {
    return ActionType.entries.map { type ->
        ActionItem(type = type, enabled = type.matches(this))
    }
}

fun EmotionMetrics.maxEmotionOrNull(): EmotionType? {
    return EmotionType.entries
        .maxByOrNull { it.scoreOf(this) }
        ?.takeIf { it.scoreOf(this) > 0 }
}

fun ActionFlags.firstEnabledActionOrNull(): ActionType? {
    return ActionType.entries.firstOrNull { it.matches(this) }
}
