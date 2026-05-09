package com.example.sample2.data

import androidx.compose.ui.graphics.Color

data class MessageV2(
    val id: String,
    val timestamp: Long,
    val text: String,
    val emotions: EmotionMetrics = EmotionMetrics(),
    val flags: ActionFlags = ActionFlags(),
    val parentId: String? = null,
    val entryType: JournalEntryType = JournalEntryType.MEMO,
    val response: EmotionResponse? = null,
)

enum class JournalEntryType {
    MEMO,
    EMOTION_RESPONSE
}

data class EmotionResponse(
    val targetEmotionKey: String,
    val actionKey: String,
    val effectScore: Int = 0,
    val note: String = "",
    val createdAt: Long = 0L
)

data class EmotionMetrics(
    val anxiety: Int = 0,
    val angry: Int = 0,
    val sad: Int = 0,
    val happy: Int = 0,
    val calm: Int = 0
)

enum class ActionGroup(val label: String, val color: Color) {
    LOAD("負荷", Color(0xFF8E24AA)),
    DECLINE("崩れ", Color(0xFFE53935)),
    RECOVER("立て直し", Color(0xFF43A047)),
    FORWARD("前進", Color(0xFFFB8C00))
}

data class ActionFlags(
    val pendingTask: Boolean = false,
    val reluctance: Boolean = false,
    val meetingStress: Boolean = false,
    val rumination: Boolean = false,
    val idleDrift: Boolean = false,
    val alcohol: Boolean = false,
    val hyperfocus: Boolean = false,
    val noDrinkChoice: Boolean = false,
    val quickAction: Boolean = false,
    val breakdown: Boolean = false,
    val rest: Boolean = false,
    val exercised: Boolean = false,
    val mindfulAction: Boolean = false,
    val insight: Boolean = false,
    val tomorrowBaton: Boolean = false,
    val consultConnect: Boolean = false
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


data class DailyReflection(
    val date: String,
    val wins: String = "",
    val difficulties: String = "",
    val insights: String = "",
    val tomorrowFirstAction: String = "",
    val summary: String = "",
    val updatedAt: Long = 0L,
    /** その日にランダム選択した「明日やること」の番号（1-365）。自由入力や未選択の場合は null。 */
    val tomorrowActionId: Int? = null
)
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
    val group: ActionGroup,
    private val matcher: (ActionFlags) -> Boolean
) {
    PENDING_TASK("pending_task", "未処理", ActionGroup.LOAD, { it.pendingTask }),
    RELUCTANCE("reluctance", "めんどくさい", ActionGroup.LOAD, { it.reluctance }),
    MEETING_STRESS("meeting_stress", "会議ストレス", ActionGroup.LOAD, { it.meetingStress }),
    RUMINATION("rumination", "ぐるぐる思考", ActionGroup.LOAD, { it.rumination }),
    IDLE_DRIFT("idle_drift", "ダラダラ", ActionGroup.DECLINE, { it.idleDrift }),
    ALCOHOL("alcohol", "飲酒・飲酒欲", ActionGroup.DECLINE, { it.alcohol }),
    HYPERFOCUS("hyperfocus", "過集中", ActionGroup.DECLINE, { it.hyperfocus }),
    NO_DRINK_CHOICE("no_drink_choice", "飲まない選択", ActionGroup.RECOVER, { it.noDrinkChoice }),
    QUICK_ACTION("quick_action", "すぐやる", ActionGroup.RECOVER, { it.quickAction }),
    BREAKDOWN("breakdown", "分解", ActionGroup.RECOVER, { it.breakdown }),
    REST("rest", "休息", ActionGroup.RECOVER, { it.rest }),
    EXERCISED("exercised", "運動", ActionGroup.RECOVER, { it.exercised }),
    MINDFUL_ACTION("mindful_action", "意識して行動", ActionGroup.FORWARD, { it.mindfulAction }),
    INSIGHT("insight", "気づき", ActionGroup.FORWARD, { it.insight }),
    TOMORROW_BATON("tomorrow_baton", "明日のバトン", ActionGroup.FORWARD, { it.tomorrowBaton }),
    CONSULT_CONNECT("consult_connect", "相談・つながり", ActionGroup.FORWARD, { it.consultConnect });

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
