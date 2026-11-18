package com.julesmeister.sangfroid.feature.feedback.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing

@Composable
fun FeedbackOverviewPanel(
    summary: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier =
            modifier.fillMaxWidth(),
        color =
            MaterialTheme
                .colorScheme
                .secondary,
        contentColor =
            MaterialTheme
                .colorScheme
                .onSecondary,
        shape =
            MaterialTheme
                .shapes
                .extraLarge
    ) {
        Column(
            modifier =
                Modifier.padding(
                    SangfroidSpacing.CardContent
                )
        ) {
            Text(
                text = "OVERVIEW",
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(
                        SangfroidSpacing.Md
                    )
            )

            Text(
                text = summary,
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge
            )
        }
    }
}