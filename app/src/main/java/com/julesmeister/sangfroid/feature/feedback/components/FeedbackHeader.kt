package com.julesmeister.sangfroid.feature.feedback.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing

@Composable
fun FeedbackHeader(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier =
            modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Surface(
            onClick = onNavigateBack,
            shape = CircleShape,
            color =
                MaterialTheme
                    .colorScheme
                    .surfaceVariant,
            contentColor =
                MaterialTheme
                    .colorScheme
                    .onSurface
        ) {
            Box(
                modifier =
                    Modifier.padding(
                        SangfroidSpacing.Sm
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text = "←",
                    style =
                        MaterialTheme
                            .typography
                            .titleLarge,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }

        Column(
            horizontalAlignment =
                Alignment.End
        ) {
            Text(
                text =
                    "Answer feedback",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground
            )

            Text(
                text =
                    "AI interview review",
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}