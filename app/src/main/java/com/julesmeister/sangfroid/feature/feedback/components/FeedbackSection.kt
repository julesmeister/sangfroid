package com.julesmeister.sangfroid.feature.feedback.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidDimensions
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing

@Composable
fun FeedbackSection(
    title: String,
    items: List<String>,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        return
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color =
            MaterialTheme
                .colorScheme
                .surface,
        contentColor =
            MaterialTheme
                .colorScheme
                .onSurface,
        shape =
            MaterialTheme
                .shapes
                .extraLarge
    ) {
        Column(
            modifier = Modifier.padding(
                SangfroidSpacing.CardContent
            )
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                fontWeight =
                    FontWeight.ExtraBold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface
            )

            Spacer(
                modifier = Modifier.height(
                    SangfroidSpacing.Md
                )
            )

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        SangfroidSpacing.Sm
                    )
            ) {
                items.forEach { item ->
                    FeedbackBullet(
                        text = item
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedbackBullet(
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(
                SangfroidSpacing.Sm
            ),
        verticalAlignment =
            Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(
                SangfroidDimensions.CategoryDot
            ),
            color =
                MaterialTheme
                    .colorScheme
                    .tertiary,
            shape =
                CircleShape
        ) {}

        Text(
            text = text,
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurface,
            modifier =
                Modifier.weight(1f)
        )
    }
}