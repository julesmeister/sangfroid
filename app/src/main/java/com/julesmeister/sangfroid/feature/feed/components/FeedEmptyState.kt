package com.julesmeister.sangfroid.feature.feed.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.component.SangfroidPillButton
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidDimensions
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.feature.feed.FreshQuestionGenerationState

@Composable
fun FeedEmptyState(
    skippedCount: Int,
    enabled: Boolean,
    generationState:
    FreshQuestionGenerationState,
    onGenerateFresh: () -> Unit,
    onRestoreSkipped: () -> Unit,
    onRestartFeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGenerating =
        generationState is
                FreshQuestionGenerationState
                .Preparing ||
                generationState is
                        FreshQuestionGenerationState
                        .Generating ||
                generationState is
                        FreshQuestionGenerationState
                        .Reviewing

    Column(
        modifier =
            modifier.widthIn(
                max =
                    SangfroidDimensions
                        .EmptyStateMaxWidth
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        EmptyStateGlyph(
            isGenerating = isGenerating
        )

        Spacer(
            modifier =
                Modifier.height(
                    SangfroidSpacing.Md
                )
        )

        Text(
            text =
                if (isGenerating) {
                    "Building a fresh set"
                } else {
                    "You've covered this set"
                },
            style =
                MaterialTheme
                    .typography
                    .headlineSmall,
            fontWeight =
                FontWeight.ExtraBold,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurface,
            textAlign =
                TextAlign.Center
        )

        Spacer(
            modifier =
                Modifier.height(
                    SangfroidSpacing.Xs
                )
        )

        Text(
            text =
                if (isGenerating) {
                    "Sangfroid is finding underused CV evidence and checking new questions against everything already in your library."
                } else {
                    "Generate questions from CV evidence that has not been explored yet, or bring your previous cards back."
                },
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
            textAlign =
                TextAlign.Center
        )

        Spacer(
            modifier =
                Modifier.height(
                    SangfroidSpacing.Lg
                )
        )

        AnimatedContent(
            targetState =
                generationState,
            transitionSpec = {
                fadeIn() togetherWith
                        fadeOut() using
                        SizeTransform(
                            clip = false
                        )
            },
            label =
                "generation-status"
        ) { state ->
            when (state) {
                FreshQuestionGenerationState.Idle -> {
                    GenerationPromptCard()
                }

                is FreshQuestionGenerationState.Preparing -> {
                    GenerationStatusCard(
                        title =
                            "Finding new CV angles",
                        message =
                            if (
                                state.cvEvidenceCount >
                                0
                            ) {
                                "Reviewing ${state.cvEvidenceCount} CV evidence items and prioritizing topics used least often."
                            } else {
                                "Reviewing your profile, skills, and previous questions."
                            },
                        showProgress = true,
                        tone =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }

                is FreshQuestionGenerationState.Generating -> {
                    GenerationStatusCard(
                        title =
                            "Generating ${state.requestedCount} candidates",
                        message =
                            "Qwen is creating a new batch grounded in your CV.",
                        showProgress = true,
                        tone =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }

                is FreshQuestionGenerationState.Reviewing -> {
                    GenerationStatusCard(
                        title =
                            "Checking ${state.candidateCount} questions",
                        message =
                            "Removing exact duplicates and questions that are too similar to previous ones.",
                        showProgress = true,
                        tone =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }

                is FreshQuestionGenerationState.Complete -> {
                    GenerationStatusCard(
                        title =
                            "${state.addedCount} fresh " +
                                    if (
                                        state.addedCount ==
                                        1
                                    ) {
                                        "question added"
                                    } else {
                                        "questions added"
                                    },
                        message =
                            if (
                                state.duplicateCount >
                                0
                            ) {
                                "${state.duplicateCount} duplicate or very similar " +
                                        if (
                                            state.duplicateCount ==
                                            1
                                        ) {
                                            "question was removed."
                                        } else {
                                            "questions were removed."
                                        }
                            } else {
                                "The new set passed the similarity review."
                            },
                        showProgress = false,
                        tone =
                            MaterialTheme
                                .colorScheme
                                .tertiary
                    )
                }

                is FreshQuestionGenerationState.Failed -> {
                    GenerationStatusCard(
                        title =
                            "Fresh questions weren't added",
                        message =
                            state.message,
                        showProgress = false,
                        tone =
                            MaterialTheme
                                .colorScheme
                                .error
                    )
                }
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    SangfroidSpacing.Lg
                )
        )

        Column(
            modifier =
                Modifier.fillMaxWidth(),
            verticalArrangement =
                Arrangement.spacedBy(
                    SangfroidSpacing.Sm
                )
        ) {
            SangfroidPillButton(
                text =
                    if (isGenerating) {
                        generationButtonLabel(
                            generationState
                        )
                    } else {
                        "Generate fresh questions"
                    },
                onClick =
                    onGenerateFresh,
                enabled =
                    enabled &&
                            !isGenerating,
                emphasized =
                    true,
                modifier =
                    Modifier.fillMaxWidth()
            )

            if (skippedCount > 0) {
                SangfroidPillButton(
                    text =
                        "Bring back skipped ($skippedCount)",
                    onClick =
                        onRestoreSkipped,
                    enabled =
                        enabled &&
                                !isGenerating,
                    emphasized =
                        false,
                    modifier =
                        Modifier.fillMaxWidth()
                )
            }

            SangfroidPillButton(
                text =
                    "Start over",
                onClick =
                    onRestartFeed,
                enabled =
                    enabled &&
                            !isGenerating,
                emphasized =
                    false,
                modifier =
                    Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GenerationPromptCard() {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(20.dp),
        color =
            MaterialTheme
                .colorScheme
                .primaryContainer
                .copy(alpha = 0.55f),
        contentColor =
            MaterialTheme
                .colorScheme
                .onPrimaryContainer
    ) {
        Row(
            modifier =
                Modifier.padding(14.dp),
            horizontalArrangement =
                Arrangement.spacedBy(12.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Box(
                modifier =
                    Modifier
                        .size(38.dp)
                        .background(
                            MaterialTheme
                                .colorScheme
                                .primary
                                .copy(alpha = 0.14f),
                            CircleShape
                        ),
                contentAlignment =
                    Alignment.Center
            ) {
                SparkGlyph(
                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            }

            Column(
                modifier =
                    Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text =
                        "Explore untouched evidence",
                    style =
                        MaterialTheme
                            .typography
                            .titleSmall,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "Unused projects, achievements, work highlights, and certifications are prioritized first.",
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onPrimaryContainer
                            .copy(alpha = 0.78f)
                )
            }
        }
    }
}

@Composable
private fun GenerationStatusCard(
    title: String,
    message: String,
    showProgress: Boolean,
    tone: Color
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(20.dp),
        color =
            tone.copy(alpha = 0.09f),
        contentColor =
            MaterialTheme
                .colorScheme
                .onSurface
    ) {
        Row(
            modifier =
                Modifier.padding(14.dp),
            horizontalArrangement =
                Arrangement.spacedBy(12.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.size(24.dp),
                    color =
                        tone,
                    strokeWidth =
                        2.5.dp
                )
            } else {
                Box(
                    modifier =
                        Modifier
                            .size(10.dp)
                            .background(
                                tone,
                                CircleShape
                            )
                )
            }

            Column(
                modifier =
                    Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text =
                        title,
                    style =
                        MaterialTheme
                            .typography
                            .titleSmall,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        tone
                )

                Text(
                    text =
                        message,
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}

private fun generationButtonLabel(
    state: FreshQuestionGenerationState
): String {
    return when (state) {
        is FreshQuestionGenerationState.Preparing ->
            "Reviewing CV…"

        is FreshQuestionGenerationState.Generating ->
            "Generating questions…"

        is FreshQuestionGenerationState.Reviewing ->
            "Checking duplicates…"

        else ->
            "Generate fresh questions"
    }
}

@Composable
private fun EmptyStateGlyph(
    isGenerating: Boolean
) {
    Surface(
        modifier =
            Modifier.size(72.dp),
        shape =
            CircleShape,
        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant
                .copy(alpha = 0.7f)
    ) {
        Box(
            contentAlignment =
                Alignment.Center
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.size(34.dp),
                    color =
                        MaterialTheme
                            .colorScheme
                            .primary,
                    strokeWidth =
                        2.5.dp
                )
            } else {
                SparkGlyph(
                    color =
                        MaterialTheme
                            .colorScheme
                            .primary,
                    modifier =
                        Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
private fun SparkGlyph(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(20.dp)) {
        val stroke =
            2.dp.toPx()

        val center =
            Offset(
                size.width * 0.5f,
                size.height * 0.5f
            )

        drawLine(
            color = color,
            start =
                Offset(
                    center.x,
                    size.height * 0.12f
                ),
            end =
                Offset(
                    center.x,
                    size.height * 0.88f
                ),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start =
                Offset(
                    size.width * 0.12f,
                    center.y
                ),
            end =
                Offset(
                    size.width * 0.88f,
                    center.y
                ),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color.copy(alpha = 0.7f),
            start =
                Offset(
                    size.width * 0.25f,
                    size.height * 0.25f
                ),
            end =
                Offset(
                    size.width * 0.75f,
                    size.height * 0.75f
                ),
            strokeWidth = stroke * 0.75f,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color.copy(alpha = 0.7f),
            start =
                Offset(
                    size.width * 0.75f,
                    size.height * 0.25f
                ),
            end =
                Offset(
                    size.width * 0.25f,
                    size.height * 0.75f
                ),
            strokeWidth = stroke * 0.75f,
            cap = StrokeCap.Round
        )
    }
}