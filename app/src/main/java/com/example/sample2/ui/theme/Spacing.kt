package com.example.sample2.ui.theme

import androidx.compose.ui.unit.dp

/**
 * アプリ全体のスペーシング（余白・ギャップ）トークン。
 *
 * 採用ポリシー:
 *  - 4 の倍数を基本グリッドとする
 *  - カード内パディングは md (12dp) を標準
 *  - 画面端パディングは lg (16dp) を標準
 *  - 行内ギャップ（アイコンと文字、要素間）は xs (4dp) / sm (8dp)
 */
object Spacing {
    /** 2dp: ヘアライン的な微調整。原則使わない */
    val xxs = 2.dp
    /** 4dp: アイコン直下のギャップ、行内ミクロ調整 */
    val xs = 4.dp
    /** 8dp: 標準ギャップ、Row 内要素間 */
    val sm = 8.dp
    /** 12dp: カード内パディングの標準、Column 内ブロック間 */
    val md = 12.dp
    /** 16dp: 画面端パディングの標準、セクション間 */
    val lg = 16.dp
    /** 20dp: 大きめ余白 */
    val xl = 20.dp
    /** 24dp: セクション大区切り */
    val xxl = 24.dp
    /** 32dp: 画面冒頭・末尾の大きい余白 */
    val xxxl = 32.dp
}
