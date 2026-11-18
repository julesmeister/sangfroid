package com.julesmeister.sangfroid.feature.feed.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.domain.repository.QuestionImportRepository

@Composable
fun QuestionImportPreview(
    preview: QuestionImportRepository.ImportPreview,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(
                12.dp
            )
    ) {
        ImportSummary(
            preview =
                preview
        )

        if (
            preview.candidates.isEmpty()
        ) {
            Surface(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    RoundedCornerShape(
                        18.dp
                    ),
                color =
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
                        .copy(
                            alpha = 0.55f
                        )
            ) {
                Text(
                    text =
                        "No new questions are available to add. The extracted questions were duplicates, very similar to existing questions, or invalid.",
                    modifier =
                        Modifier.padding(
                            16.dp
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }

            return
        }

        Text(
            text =
                "Questions ready to add",
            style =
                MaterialTheme
                    .typography
                    .titleSmall,
            fontWeight =
                FontWeight.Bold,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurface
        )

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(
                        1f,
                        fill = false
                    ),
            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {
            items(
                items =
                    preview.candidates,
                key = { candidate ->
                    candidate.question.id
                }
            ) { candidate ->
                ImportQuestionRow(
                    candidate =
                        candidate
                )
            }
        }
    }
}

@Composable
private fun ImportSummary(
    preview: QuestionImportRepository.ImportPreview
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                20.dp
            ),
        color =
            MaterialTheme
                .colorScheme
                .primaryContainer
                .copy(
                    alpha = 0.55f
                ),
        contentColor =
            MaterialTheme
                .colorScheme
                .onPrimaryContainer
    ) {
        Column(
            modifier =
                Modifier.padding(
                    16.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {
            Text(
                text =
                    "${preview.detectedCount} possible " +
                            if (
                                preview.detectedCount ==
                                1
                            ) {
                                "question found"
                            } else {
                                "questions found"
                            },
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                SummaryMetric(
                    value =
                        preview.importableCount,
                    label =
                        "New",
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                SummaryMetric(
                    value =
                        preview.duplicateCount,
                    label =
                        "Similar",
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                SummaryMetric(
                    value =
                        preview
                            .invalidCandidateCount,
                    label =
                        "Invalid",
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )
            }
        }
    }
}

@Composable
private fun SummaryMetric(
    value: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier =
            modifier,
        shape =
            RoundedCornerShape(
                14.dp
            ),
        color =
            MaterialTheme
                .colorScheme
                .surface
                .copy(
                    alpha = 0.75f
                )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 8.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text =
                    value.toString(),
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text =
                    label,
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ImportQuestionRow(
    candidate: QuestionImportRepository.ImportCandidate
) {
    val question =
        candidate.question

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(
                18.dp
            ),
        color =
            MaterialTheme
                .colorScheme
                .surface,
        tonalElevation =
            1.dp
    ) {
        Column(
            modifier =
                Modifier.padding(
                    14.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        10.dp
                    ),
                verticalAlignment =
                    Alignment.Top
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(
                                24.dp
                            )
                            .background(
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .primary
                                        .copy(
                                            alpha =
                                                0.13f
                                        ),
                                shape =
                                    CircleShape
                            ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            "✓",
                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,
                        fontWeight =
                            FontWeight.Bold,
                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }

                Text(
                    text =
                        question.question,
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface,
                    maxLines =
                        4,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(
                color =
                    MaterialTheme
                        .colorScheme
                        .outlineVariant
                        .copy(
                            alpha = 0.55f
                        )
            )

            Text(
                text =
                    "${question.category.name.toDisplayLabel()} · " +
                            "${question.difficulty.name.toDisplayLabel()} · " +
                            question
                                .questionAngle
                                .name
                                .toDisplayLabel(),
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

private fun String.toDisplayLabel():
        String {
    return lowercase()
        .split(
            "_"
        )
        .joinToString(
            " "
        ) { word ->
            word.replaceFirstChar { character ->
                character.uppercase()
            }
        }
}