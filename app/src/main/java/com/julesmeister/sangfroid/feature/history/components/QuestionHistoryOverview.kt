package com.julesmeister.sangfroid.feature.history.components

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
import androidx.compose.ui.text.style.TextOverflow
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.feature.history.QuestionHistoryUiState

@Composable
fun QuestionHistoryOverview(
    uiState: QuestionHistoryUiState,
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
                    SangfroidSpacing.CardContent
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    SangfroidSpacing.Md
                )
        ) {
            Text(
                text =
                    uiState.questionText,
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.ExtraBold,
                maxLines =
                    4,
                overflow =
                    TextOverflow.Ellipsis
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        SangfroidSpacing.Sm
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                HistoryMetric(
                    value =
                        uiState
                            .latestPercentage
                            ?.let { percentage ->
                                "$percentage%"
                            }
                            ?: "—",
                    label =
                        "Latest",
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                HistoryMetric(
                    value =
                        uiState
                            .attemptCount
                            .toString(),
                    label =
                        if (
                            uiState.attemptCount ==
                            1
                        ) {
                            "Attempt"
                        } else {
                            "Attempts"
                        },
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                HistoryMetric(
                    value =
                        uiState
                            .revisionCount
                            .toString(),
                    label =
                        if (
                            uiState.revisionCount ==
                            1
                        ) {
                            "Revision"
                        } else {
                            "Revisions"
                        },
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )
            }

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            SangfroidSpacing.Xxs
                        )
                ) {
                    Text(
                        text =
                            "Mastery",
                        style =
                            MaterialTheme
                                .typography
                                .labelMedium
                    )

                    Text(
                        text =
                            uiState.masteryLabel,
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.Bold
                    )
                }

                uiState
                    .scoreChangePercentagePoints
                    ?.let { change ->
                        ScoreChangeBadge(
                            change =
                                change
                        )
                    }
            }

            Text(
                text =
                    "${uiState.evaluatedAttemptCount} of " +
                            "${uiState.attemptCount} attempts evaluated",
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSecondaryContainer
                        .copy(
                            alpha =
                                0.78f
                        )
            )
        }
    }
}

@Composable
private fun HistoryMetric(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier =
            modifier,
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha =
                        0.58f
                ),
        contentColor =
            MaterialTheme
                .colorScheme
                .onSurface,
        shape =
            MaterialTheme
                .shapes
                .large
    ) {
        Column(
            modifier =
                Modifier.padding(
                    SangfroidSpacing.Sm
                ),
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
                    FontWeight.ExtraBold,
                maxLines =
                    1
            )

            Text(
                text =
                    label,
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ScoreChangeBadge(
    change: Int,
    modifier: Modifier = Modifier
) {
    val containerColor =
        when {
            change > 0 -> {
                MaterialTheme
                    .colorScheme
                    .tertiaryContainer
            }

            change < 0 -> {
                MaterialTheme
                    .colorScheme
                    .errorContainer
            }

            else -> {
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
            }
        }

    val contentColor =
        when {
            change > 0 -> {
                MaterialTheme
                    .colorScheme
                    .onTertiaryContainer
            }

            change < 0 -> {
                MaterialTheme
                    .colorScheme
                    .onErrorContainer
            }

            else -> {
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
            }
        }

    val label =
        when {
            change > 0 -> {
                "+$change points"
            }

            change < 0 -> {
                "$change points"
            }

            else -> {
                "No change"
            }
        }

    Surface(
        modifier =
            modifier,
        color =
            containerColor,
        contentColor =
            contentColor,
        shape =
            MaterialTheme
                .shapes
                .large
    ) {
        Text(
            text =
                label,
            modifier =
                Modifier.padding(
                    horizontal =
                        SangfroidSpacing.Md,
                    vertical =
                        SangfroidSpacing.Sm
                ),
            style =
                MaterialTheme
                    .typography
                    .labelLarge,
            fontWeight =
                FontWeight.Bold
        )
    }
}