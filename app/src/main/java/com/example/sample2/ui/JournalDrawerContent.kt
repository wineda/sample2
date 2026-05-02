package com.example.sample2.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.sample2.ui.theme.MenuIconPalette
import com.example.sample2.ui.theme.appColors

@Composable
fun JournalDrawerContent(
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
                text = "データ操作",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.66.sp,
                color = MaterialTheme.appColors.inkTertiary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SmartDrawerMenuItem(
                    title = "コピー",
                    subtitle = "内容をクリップボードへ",
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
                    title = "共有",
                    subtitle = "他のアプリへ共有",
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
                    .padding(top = 10.dp, bottom = 8.dp)
                    .height(8.dp)
                    .background(MaterialTheme.appColors.surfaceMuted)
            )

            Text(
                text = "バックアップ",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.66.sp,
                color = MaterialTheme.appColors.inkTertiary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                SmartDrawerMenuItem(
                    title = "バックアップを作成",
                    subtitle = "データを保存して退避",
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

                SmartDrawerMenuItem(
                    title = "リストア",
                    subtitle = "バックアップから復元",
                    iconBackground = MenuIconPalette.Restore.background,
                    iconTint = MenuIconPalette.Restore.tint,
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
                    .background(MaterialTheme.appColors.surfaceMuted)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
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
private fun SmartDrawerHeader(
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 16.dp, top = 2.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "メニュー",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.appColors.inkPrimary
        )
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
    subtitle: String,
    iconBackground: Color,
    iconTint: Color,
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
                .padding(horizontal = 20.dp, vertical = 14.dp),
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
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.appColors.dividerMid
            )
        }
    }
}
