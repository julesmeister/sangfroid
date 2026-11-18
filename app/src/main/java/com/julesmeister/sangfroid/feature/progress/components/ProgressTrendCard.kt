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
fun ProgressTrendCard(
    summary: OverallProgressSummary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier =
            modifier.fillMaxWidth(),
        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant,
        contentColor =
            MaterialTheme
                .colorScheme
                .onSurfaceVariant,
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
                    "Practice snapshot",
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.Bold
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Metric(
                    value =
                        summary
                            .totalAttemptCount
                            .toString(),
                    label =
                        "Attempts",
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                Metric(
                    value =
                        summary
                            .completedEvaluationCount
                            .toString(),
                    label =
                        "Evaluated",
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                Metric(
                    value =
                        summary
                            .savedRevisionCount
                            .toString(),
                    label =
                        "Revisions",
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )
            }

            summary
                .averageScoreChangePercentagePoints
                ?.let { change ->
                    val text =
                        when {
                            change > 0 -> {
                                "Recent question scores are up " +
                                        "$change percentage points on average."
                            }

                            change < 0 -> {
                                "Recent question scores are down " +
                                        "${-change} percentage points on average."
                            }

                            else -> {
                                "Recent question scores are stable."
                            }
                        }

                    Text(
                        text =
                            text,
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,
                        color =
                            when {
                                change > 0 -> {
                                    MaterialTheme
                                        .colorScheme
                                        .tertiary
                                }

                                change < 0 -> {
                                    MaterialTheme
                                        .colorScheme
                                        .error
                                }

                                else -> {
                                    MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                                }
                            }
                    )
                }
        }
    }
}

@Composable
private fun Metric(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier,
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(
                SangfroidSpacing.Xxs
            )
    ) {
        Text(
            text =
                value,
            style =
                MaterialTheme
                    .typography
                    .headlineSmall,
            fontWeight =
                FontWeight.ExtraBold
        )

        Text(
            text =
                label,
            style =
                MaterialTheme
                    .typography
                    .labelMedium
        )
    }
}