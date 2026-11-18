package com.julesmeister.sangfroid.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidAlpha
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidDimensions
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.shape.SangfroidRadius

@Composable
fun SangfroidPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = true,
    enabled: Boolean = true
) {
    val containerColor = if (emphasized) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (emphasized) {
        MaterialTheme.colorScheme.onSecondary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .heightIn(min = SangfroidDimensions.ButtonHeight)
            .graphicsLayer {
                alpha = if (enabled) 1f else SangfroidAlpha.Disabled
            },
        shape = RoundedCornerShape(SangfroidRadius.Pill),
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            modifier = Modifier.padding(
                horizontal = SangfroidSpacing.ButtonHorizontal,
                vertical = SangfroidSpacing.ButtonVertical
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}