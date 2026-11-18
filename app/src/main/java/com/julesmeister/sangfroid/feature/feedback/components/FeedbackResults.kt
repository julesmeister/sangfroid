package com.julesmeister.sangfroid.feature.feedback.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.julesmeister.sangfroid.core.designsystem.component.SangfroidPillButton
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.feature.feedback.FeedbackUiState

@Composable
fun FeedbackResults(
    uiState: FeedbackUiState,
    onSaveRevision: () -> Unit,
    onOpenRevisionHistory: () -> Unit,
    onTryAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val evaluation =
        requireNotNull(
            uiState.evaluation
        )

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                ),
        verticalArrangement =
            Arrangement.spacedBy(
                SangfroidSpacing.Md
            )
    ) {
        FeedbackOverviewPanel(
            summary =
                evaluation.summary
        )

        /*
         * Question progress is reactive.
         *
         * FeedbackViewModel observes QuestionProgress,
         * stores it in FeedbackUiState, and Compose
         * automatically recomposes this section whenever
         * progress changes.
         */
        uiState.questionProgress
            ?.let { progress ->
                QuestionProgressPanel(
                    progress = progress,
                    modifier =
                        Modifier.fillMaxWidth()
                )
            }

        FeedbackSection(
            title =
                "WHAT WORKED",
            items =
                evaluation.strengths
        )

        FeedbackSection(
            title =
                "WHAT'S MISSING",
            items =
                evaluation.missingPoints
        )

        FeedbackSection(
            title =
                "HOW TO IMPROVE",
            items =
                evaluation.suggestions
        )

        FeedbackSection(
            title =
                "STRONGER STRUCTURE",
            items =
                evaluation.improvedOutline
        )

        uiState
            .improvedAnswerExample
            ?.let { example ->
                ImprovedAnswerPanel(
                    answer = example,
                    revisionCount =
                        uiState.revisionCount,
                    canSave =
                        uiState.canSaveRevision,
                    isSaving =
                        uiState.isSavingRevision,
                    isSaved =
                        uiState
                            .isCurrentRevisionSaved ||
                                uiState.revisionSaved,
                    errorMessage =
                        uiState
                            .revisionErrorMessage,
                    onSave =
                        onSaveRevision,
                    onOpenHistory =
                        onOpenRevisionHistory
                )
            }

        FeedbackRubricPanel(
            evaluation =
                evaluation
        )

        FeedbackSection(
            title =
                "LIKELY FOLLOW-UPS",
            items =
                evaluation
                    .followUpQuestions
                    .map { followUp ->
                        buildString {
                            append(
                                followUp.question
                            )

                            followUp.reason
                                ?.trim()
                                ?.takeIf(
                                    String::isNotBlank
                                )
                                ?.let { reason ->
                                    append("\n")
                                    append(reason)
                                }
                        }
                    }
        )

        SangfroidPillButton(
            text = "Try again",
            onClick = onTryAgain,
            modifier =
                Modifier.fillMaxWidth()
        )

        Text(
            text =
                "These scores describe answer quality only. They do not predict hiring or interview outcomes.",
            style =
                MaterialTheme
                    .typography
                    .bodySmall,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Spacer(
            modifier =
                Modifier.height(
                    SangfroidSpacing.Section
                )
        )
    }
}