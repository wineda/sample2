package com.example.sample2.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.sample2.ui.theme.Spacing
import com.example.sample2.ui.theme.appColors

enum class AppCardVariant { Default, Outlined, Elevated }

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    variant: AppCardVariant = AppCardVariant.Outlined,
    contentPadding: PaddingValues = PaddingValues(Spacing.md),
    verticalSpacing: Dp = Spacing.sm,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val border = if (variant == AppCardVariant.Outlined) {
        BorderStroke(1.dp, MaterialTheme.appColors.dividerCool)
    } else null

    val elevation = when (variant) {
        AppCardVariant.Elevated -> CardDefaults.cardElevation(defaultElevation = 1.dp)
        else -> CardDefaults.cardElevation(defaultElevation = 0.dp)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = border,
        elevation = elevation
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            content = content
        )
    }
}
