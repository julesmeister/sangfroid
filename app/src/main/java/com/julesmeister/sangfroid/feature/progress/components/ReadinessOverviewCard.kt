package com.julesmeister.sangfroid.feature.progress.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.feature.progress.model.OverallProgressSummary

@Composable
fun ReadinessOverviewCard(
    summary: OverallProgressSummary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier =
            modifier.fillMaxWidth(),
        color =
            MaterialTheme
                .colorScheme
                .secondaryContainer,
        contentColor =
            MaterialTheme
                .colorScheme
                .onSecondaryContainer,
        shape =
            MaterialTheme
                .shapes
                .extraLarge
    ) {
        Column(
            modifier =
                Modifier.padding(
                    SangfroidSpacing
                        .CardContent
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    SangfroidSpacing.Md
                )
        ) {
            Text(
                text =
                    "INTERVIEW READINESS",
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                fontWeight =
                    FontWeight.Bold
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.Bottom,
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                Text(
                    text =
                        summary
                            .overallPercentage
                            ?.let { percentage ->
                                "$percentage%"
                            }
                            ?: "—",
                    style =
                        MaterialTheme
                            .typography
                            .displayMedium,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Text(
                    text =
                        summary.readinessLabel,
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Text(
                text =
                    "${summary.practicedQuestionCount} of " +
                            "${summary.totalQuestionCount} questions practised",
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )

            Text(
                text =
                    "Scores describe answer quality and practice progress. " +
                            "They do not predict hiring outcomes.",
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSecondaryContainer
                        .copy(
                            alpha = 0.78f
                        )
            )
        }
    }
}