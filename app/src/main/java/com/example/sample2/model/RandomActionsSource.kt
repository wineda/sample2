package com.example.sample2.model

import android.content.Context
import org.json.JSONObject

/**
 * 365項目「明日やること」候補を assets/random_actions.json から読み込むデータソース。
 *
 * 一度読み込んだら静的に保持する（メモリキャッシュ）。
 */
object RandomActionsSource {

    /** id=1〜365、text=候補テキスト */
    data class RandomAction(val id: Int, val text: String)

    @Volatile
    private var cached: List<RandomAction>? = null

    fun load(context: Context): List<RandomAction> {
        cached?.let { return it }
        synchronized(this) {
            cached?.let { return it }
            val text = context.assets.open("random_actions.json").use { input ->
                input.bufferedReader(Charsets.UTF_8).readText()
            }
            val root = JSONObject(text)
            val arr = root.getJSONArray("actions")
            val list = (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                RandomAction(
                    id = obj.getInt("id"),
                    text = obj.getString("text")
                )
            }
            cached = list
            return list
        }
    }

    fun findById(context: Context, id: Int): RandomAction? =
        load(context).firstOrNull { it.id == id }

    /**
     * 使用済みID集合を除いた候補プールから抽選。
     * 全て使用済み（365個埋まった）の場合は自動リセットして全候補から抽選する。
     */
    fun pickRandom(
        context: Context,
        usedIds: Set<Int>,
        random: kotlin.random.Random = kotlin.random.Random.Default
    ): RandomAction {
        val all = load(context)
        val pool = all.filter { it.id !in usedIds }
        val candidate = if (pool.isEmpty()) all else pool
        return candidate[random.nextInt(candidate.size)]
    }
}
