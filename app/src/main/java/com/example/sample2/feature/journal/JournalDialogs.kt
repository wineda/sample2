package com.example.sample2.feature.journal

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.sample2.data.MessageV2

@Composable
fun RestoreConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("リストア確認") },
        text = { Text("現在のデータは上書きされます。\nよろしいですか？") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } }
    )
}

@Composable
fun DeleteConfirmDialog(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    target: MessageV2?
) {
    if (target == null) return
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        title = { Text("削除") },
        text = { Text("このメッセージを削除しますか？") },
        confirmButton = { Button(onClick = onDelete) { Text("削除") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("キャンセル") } }
    )
}
