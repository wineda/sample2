package com.example.sample2.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * モノスペース系のテキストスタイル。
 * Reflection 系画面の「テクニカル/レポート風」演出に使う。
 * MaterialTheme.typography には乗せず、必要な箇所で直接参照する。
 */
object MonoTypography {
    /** 極小オーバーライン（DAILY REFLECTION 等のキャップ） */
    val Caption = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 1.sp)
    /** 9sp の最小ラベル（METRIC 名・TODAY バッジ等） */
    val Micro = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, fontSize = 9.sp, lineHeight = 12.sp, letterSpacing = 1.sp)
    /** 大きめの数値表示（METRIC 値、PROGRESS 表記等） */
    val Numeric = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 22.sp)
    /** 通常本文サイズのモノスペース（INPUT 内の説明等） */
    val Body = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 16.sp)
}
