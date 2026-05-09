package com.example.sample2.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.sample2.ui.theme.AppShapeTokens
import com.example.sample2.ui.theme.AppTextStyles
import com.example.sample2.ui.theme.MenuIconPalette
import com.example.sample2.ui.theme.SemanticColors
import com.example.sample2.ui.theme.appColors
import com.example.sample2.ui.theme.Spacing
import java.util.Locale

@Composable
fun JournalDrawerContent(
    messageCount: Int,
    dailyRecordCount: Int,
    dailyReflectionCount: Int,
    dataSizeBytes: Long,
    onClose: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(AppShapeTokens.Tech)
                    .background(MaterialTheme.appColors.dividerSoft)
            )

            Spacer(modifier = Modifier.size(10.dp))

            SmartDrawerHeader(onClose = onClose)

            Text(
                text = "データ概要",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.66.sp,
                color = MaterialTheme.appColors.inkTertiary,
                modifier = Modifier.padding(horizontal = Spacing.xl, vertical = 12.dp)
            )

            DataOverviewCard(
                messageCount = messageCount,
                dailyRecordCount = dailyRecordCount,
                dailyReflectionCount = dailyReflectionCount,
                dataSizeBytes = dataSizeBytes,
                modifier = Modifier.padding(horizontal = Spacing.xl)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md, bottom = 8.dp)
                    .height(8.dp)
                    .background(MaterialTheme.appColors.surfaceQuiet)
            )

            Text(
                text = "書き出し",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.66.sp,
                color = MaterialTheme.appColors.inkTertiary,
                modifier = Modifier.padding(horizontal = Spacing.xl, vertical = 12.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SmartDrawerMenuItem(
                    title = "クリップボードにコピー",
                    iconBackground = MenuIconPalette.Copy.background,
                    iconTint = MenuIconPalette.Copy.tint,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = onCopy
                )

                SmartDrawerMenuItem(
                    title = "他のアプリに共有",
                    iconBackground = MenuIconPalette.Share.background,
                    iconTint = MenuIconPalette.Share.tint,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = onShare
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md, bottom = 8.dp)
                    .height(8.dp)
                    .background(MaterialTheme.appColors.surfaceQuiet)
            )

            Text(
                text = "バックアップ",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.66.sp,
                color = MaterialTheme.appColors.inkTertiary,
                modifier = Modifier.padding(horizontal = Spacing.xl, vertical = 12.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SmartDrawerMenuItem(
                    title = "バックアップを作成",
                    subtitle = "JSON形式で端末に保存",
                    iconBackground = MenuIconPalette.Backup.background,
                    iconTint = MenuIconPalette.Backup.tint,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = onBackup
                )

            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.md, bottom = 8.dp)
                    .height(8.dp)
                    .background(MaterialTheme.appColors.surfaceQuiet)
            )

            Text(
                text = "復元",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.66.sp,
                color = MaterialTheme.appColors.inkTertiary,
                modifier = Modifier.padding(horizontal = Spacing.xl, vertical = 12.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SmartDrawerMenuItem(
                    title = "バックアップから復元",
                    subtitle = "現在のデータは上書きされます",
                    iconBackground = SemanticColors.NegativeSoft,
                    iconTint = SemanticColors.NegativeMain,
                    titleColor = SemanticColors.NegativeMain,
                    trailing = TrailingKind.DangerBadge,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = onRestore
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(
                color = MaterialTheme.appColors.dividerSoft
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.appColors.surfaceQuiet)
                    .padding(horizontal = Spacing.xl, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Journal",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.inkSecondary
                )
                Text(
                    text = "v1.2.3",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.appColors.inkTertiary
                )
            }
        }
    }
}


@Composable
private fun DataOverviewCard(
    messageCount: Int,
    dailyRecordCount: Int,
    dailyReflectionCount: Int,
    dataSizeBytes: Long,
    modifier: Modifier = Modifier
) {
    val formattedDataSize = formatDataSize(dataSizeBytes)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.appColors.dividerSoft)
    ) {
        Column(modifier = Modifier.padding(horizontal = Spacing.md, vertical = 4.dp)) {
            DataOverviewRow(label = "記録", value = messageCount.toString(), unit = "件")
            DataOverviewDivider()
            DataOverviewRow(label = "日次記録", value = dailyRecordCount.toString(), unit = "日")
            DataOverviewDivider()
            DataOverviewRow(label = "振り返り", value = dailyReflectionCount.toString(), unit = "日")
            DataOverviewDivider()
            DataOverviewRow(
                label = "データサイズ",
                value = formattedDataSize.first,
                unit = formattedDataSize.second
            )
        }
    }
}

@Composable
private fun DataOverviewRow(label: String, value: String, unit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.appColors.inkSecondary
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.appColors.inkPrimary
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.appColors.inkTertiary,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

@Composable
private fun DataOverviewDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = MaterialTheme.appColors.dividerSoft
    )
}

/**
 * バイト数を「2.4 / MB」のような (値, 単位) ペアにフォーマット。
 * KB / MB / GB を 1024 ベースで判定する。
 */
private fun formatDataSize(bytes: Long): Pair<String, String> {
    if (bytes < 1024) return bytes.toString() to "B"
    val kb = bytes / 1024.0
    if (kb < 1024.0) return String.format(Locale.JAPAN, "%.1f", kb) to "KB"
    val mb = kb / 1024.0
    if (mb < 1024.0) return String.format(Locale.JAPAN, "%.1f", mb) to "MB"
    val gb = mb / 1024.0
    return String.format(Locale.JAPAN, "%.1f", gb) to "GB"
}

private enum class TrailingKind {
    Chevron,
    DangerBadge
}

@Composable
private fun SmartDrawerHeader(
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Spacing.xl, end = 16.dp, top = 2.dp, bottom = Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "設定",
                style = AppTextStyles.ScreenTitleLarge,
                color = MaterialTheme.appColors.inkStrongAlt
            )
            Text(
                text = "データ管理",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.appColors.inkTertiary,
                modifier = Modifier.padding(bottom = 1.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(MaterialTheme.shapes.large)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "close drawer",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    HorizontalDivider(color = MaterialTheme.appColors.dividerSoft)
}

@Composable
private fun SmartDrawerMenuItem(
    title: String,
    subtitle: String? = null,
    iconBackground: Color,
    iconTint: Color,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    trailing: TrailingKind = TrailingKind.Chevron,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val shape = MaterialTheme.shapes.medium

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = iconBackground
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides iconTint
                    ) {
                        icon()
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = titleColor
                )
                if (!subtitle.isNullOrEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            when (trailing) {
                TrailingKind.Chevron -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.appColors.dividerStrong
                    )
                }
                TrailingKind.DangerBadge -> {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = SemanticColors.WarningSoft
                    ) {
                        Text(
                            text = "注意",
                            style = MaterialTheme.typography.labelMedium,
                            color = SemanticColors.WarningMain,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}
