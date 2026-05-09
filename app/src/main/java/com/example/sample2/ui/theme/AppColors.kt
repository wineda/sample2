package com.example.sample2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColorScheme(
    // テキスト/アイコンの色階層
    val inkPrimary: Color,
    val inkSecondary: Color,
    val inkTertiary: Color,
    val inkDisabled: Color,
    val inkStrongAlt: Color,
    val inkOnInk: Color,

    /**
     * 静かな淡色背景。カード内のセクション・うっすら塗りに使う。
     * surface よりわずかに暗く、視覚的に「落ち着いた領域」を表す。
     */
    val surfaceQuiet: Color,

    /**
     * 非アクティブ状態の小要素背景。
     * 未入力バッジ・無効ボタン・未選択チップなど「効いていない」状態を表す。
     */
    val surfaceInactive: Color,

    /**
     * 標準の境界線色。カードの枠線・区切り線に使う最頻出のトークン。
     */
    val dividerSoft: Color,

    /**
     * 強調が必要な境界線色。
     * フォーカス前のフォーム枠線・アクセント区切りなど、dividerSoft より一段強い表現。
     */
    val dividerStrong: Color,

    // 維持: 用途が明確で誤用がないトークン
    val borderStrong: Color,
    val scrimDim: Color,

    // メモ編集画面（フラットレイアウト）用トークン
    val backgroundFlat: Color,
    val dividerColor: Color,
    val labelGray: Color,
    val iconActiveBackground: Color,
    val buttonSurface: Color,
    val buttonPrimary: Color,
)

val LightAppColors = AppColorScheme(
    inkPrimary = Color(0xFF1A1F26),
    inkSecondary = Color(0xFF5A6371),
    inkTertiary = Color(0xFF8E97A3),
    inkDisabled = Color(0xFFC5CBD3),
    inkStrongAlt = Color(0xFF14181E),
    inkOnInk = Color(0xFFFFFFFF),

    surfaceQuiet = Color(0xFFFAFAFB),     // 旧 surfaceMuted (+ Elevated)
    surfaceInactive = Color(0xFFF1F3F7),  // 旧 surfaceCool (+ Subtle, SubtleAlt, Light)

    dividerSoft = Color(0xFFE5E7EB),      // 旧 dividerSoft (+ Cool, Subtle)
    dividerStrong = Color(0xFFD1D5DB),    // 旧 dividerMid (+ Neutral, surfaceSubtleDeep)

    borderStrong = Color(0xFFB8C0CC),
    scrimDim = Color(0x52000000),

    backgroundFlat = Color(0xFFF7F6F3),
    dividerColor = Color(0x14000000),
    labelGray = Color(0xFFAAAAAA),
    iconActiveBackground = Color(0x0F000000),
    buttonSurface = Color(0x0F000000),
    buttonPrimary = Color(0xFF1A1A1A),
)

val DarkAppColors = AppColorScheme(
    inkPrimary = Color(0xFFECECF1),
    inkSecondary = Color(0xFFB5BBC4),
    inkTertiary = Color(0xFF8A929C),
    inkDisabled = Color(0xFF5A5F66),
    inkStrongAlt = Color(0xFFF1F1F5),
    inkOnInk = Color(0xFF14181E),

    surfaceQuiet = Color(0xFF1F2128),     // 旧 surfaceMuted
    surfaceInactive = Color(0xFF2A2D35),  // 旧 surfaceCool/Subtle 系を統合した値

    dividerSoft = Color(0xFF383B43),      // 旧 dividerSoft 系
    dividerStrong = Color(0xFF4A4E57),    // 旧 dividerMid 系

    borderStrong = Color(0xFF5A6068),
    scrimDim = Color(0x7A000000),

    backgroundFlat = Color(0xFFF7F6F3),
    dividerColor = Color(0x14000000),
    labelGray = Color(0xFFAAAAAA),
    iconActiveBackground = Color(0x0F000000),
    buttonSurface = Color(0x0F000000),
    buttonPrimary = Color(0xFF1A1A1A),
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

val MaterialTheme.appColors: AppColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current
