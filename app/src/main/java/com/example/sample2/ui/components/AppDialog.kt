package com.example.sample2.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.sample2.ui.theme.Spacing

/**
 * アプリ全体のダイアログ共通コンポーネント。
 * 画面ごとにバラついていた AlertDialog の見た目を集約する。
 *
 * バリアント:
 *  - Confirm: 通常の確認ダイアログ。OK/キャンセルの 2 ボタン、両方 TextButton
 *  - Destructive: 破壊的アクション（削除等）。確定ボタンが error 色の TextButton
 *  - Info: 情報表示。閉じるボタンのみ
 *
 * 設計方針:
 *  - containerColor は MaterialTheme.colorScheme.surface 固定（background は使わない）
 *  - 確定ボタンは原則 TextButton（Material3 のダイアログ流儀に揃える）
 *  - 破壊的アクションの強調は「色」のみで、ボタン形状は変えない
 */

/**
 * 確認ダイアログ（OK/キャンセル）。
 *
 * @param title ダイアログのタイトル
 * @param message 本文。複数行可
 * @param confirmLabel 確定ボタンの文言（デフォルト "OK"）
 * @param dismissLabel キャンセルボタンの文言（デフォルト "キャンセル"）
 */
@Composable
fun AppConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = "OK",
    dismissLabel: String = "キャンセル"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissLabel)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

/**
 * 破壊的アクションの確認ダイアログ（削除等）。
 * 確定ボタンの文字色のみ error 色になる。
 */
@Composable
fun AppDestructiveDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = "削除",
    dismissLabel: String = "キャンセル"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissLabel)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

/**
 * 情報表示ダイアログ。閉じるボタンのみ。本文はカスタマイズ可能。
 *
 * @param content スクロール可能なコンテンツスロット
 */
@Composable
fun AppInfoDialog(
    title: String,
    onDismiss: () -> Unit,
    dismissLabel: String = "閉じる",
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                content()
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissLabel)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

/**
 * フォーム入力ダイアログ。確定/キャンセルの 2 ボタン + フォームコンテンツスロット。
 * 確定ボタンも TextButton で統一する（filled Button は使わない）。
 */
@Composable
fun AppFormDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String = "保存",
    dismissLabel: String = "キャンセル",
    confirmEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                content()
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = confirmEnabled) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissLabel)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
