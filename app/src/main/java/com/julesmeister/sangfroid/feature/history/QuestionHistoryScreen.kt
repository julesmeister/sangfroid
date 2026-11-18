package com.julesmeister.sangfroid.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julesmeister.sangfroid.core.designsystem.component.SangfroidPillButton
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.layout.SangfroidScreen
import com.julesmeister.sangfroid.domain.history.SavedRevision
import com.julesmeister.sangfroid.feature.history.components.AttemptTimeline
import com.julesmeister.sangfroid.feature.history.components.QuestionHistoryHeader
import com.julesmeister.sangfroid.feature.history.components.QuestionHistoryOverview
import com.julesmeister.sangfroid.feature.history.components.RevisionHistorySection

@Composable
fun QuestionHistoryScreen(
    onNavigateBack: () -> Unit,
    onPracticeAgain: (String) -> Unit,
    onOpenFeedback: (String) -> Unit,
    onUseRevision:
        (String, SavedRevision) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuestionHistoryViewModel =
        hiltViewModel()
) {
    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()

    QuestionHistoryContent(
        uiState =
            uiState,
        onNavigateBack =
            onNavigateBack,
        onPracticeAgain =
            onPracticeAgain,
        onOpenFeedback =
            onOpenFeedback,
        onUseRevision = {
                revision ->

            val questionId =
                uiState.questionId
                    ?: return@QuestionHistoryContent

            onUseRevision(
                questionId,
                revision
            )
        },
        onDeleteAttempt =
            viewModel::deleteAttempt,
        onDeleteRevision =
            viewModel::deleteRevision,
        modifier =
            modifier
    )
}

@Composable
private fun QuestionHistoryContent(
    uiState: QuestionHistoryUiState,
    onNavigateBack: () -> Unit,
    onPracticeAgain: (String) -> Unit,
    onOpenFeedback: (String) -> Unit,
    onUseRevision: (SavedRevision) -> Unit,
    onDeleteAttempt: (String) -> Unit,
    onDeleteRevision: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SangfroidScreen(
        modifier =
            modifier,
        applyHorizontalPadding =
            false,
        applyVerticalPadding =
            false
    ) {
        QuestionHistoryHeader(
            questionText =
                uiState.questionText,
            onNavigateBack =
                onNavigateBack,
            modifier =
                Modifier.padding(
                    horizontal =
                        SangfroidSpacing
                            .ScreenHorizontal,
                    vertical =
                        SangfroidSpacing
                            .ScreenVertical
                )
        )

        when {
            uiState.isLoading -> {
                HistoryMessage(
                    title =
                        "Loading question history",
                    text =
                        "Reviewing your previous attempts and saved revisions.",
                    showProgress =
                        true,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )
            }

            uiState.question == null -> {
                HistoryMessage(
                    title =
                        "Question unavailable",
                    text =
                        uiState.errorMessage
                            ?: "This interview question could not be found.",
                    showProgress =
                        false,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )
            }

            else -> {
                LazyColumn(
                    modifier =
                        Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(
                            start =
                                SangfroidSpacing
                                    .ScreenHorizontal,
                            end =
                                SangfroidSpacing
                                    .ScreenHorizontal,
                            bottom =
                                SangfroidSpacing
                                    .Section
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            SangfroidSpacing.Lg
                        )
                ) {
                    uiState.errorMessage
                        ?.let { errorMessage ->
                            item(
                                key =
                                    "error-message"
                            ) {
                                ErrorMessageCard(
                                    message =
                                        errorMessage
                                )
                            }
                        }

                    item(
                        key =
                            "history-overview"
                    ) {
                        QuestionHistoryOverview(
                            uiState =
                                uiState
                        )
                    }

                    item(
                        key =
                            "practice-action"
                    ) {
                        SangfroidPillButton(
                            text =
                                "Practise this question again",
                            onClick = {
                                uiState
                                    .questionId
                                    ?.let(
                                        onPracticeAgain
                                    )
                            },
                            enabled =
                                uiState
                                    .canPracticeAgain,
                            modifier =
                                Modifier.fillParentMaxWidth()
                        )
                    }

                    if (uiState.hasAttempts) {
                        item(
                            key =
                                "attempt-heading"
                        ) {
                            HistorySectionHeading(
                                title =
                                    "Attempt timeline",
                                supportingText =
                                    "Your newest attempt appears first."
                            )
                        }

                        item(
                            key =
                                "attempt-timeline"
                        ) {
                            AttemptTimeline(
                                items =
                                    uiState.attempts,
                                deletingAttemptIds =
                                    uiState
                                        .deletingAttemptIds,
                                onOpenFeedback =
                                    onOpenFeedback,
                                onDeleteAttempt =
                                    onDeleteAttempt
                            )
                        }
                    } else {
                        item(
                            key =
                                "no-attempts"
                        ) {
                            EmptyHistoryCard(
                                title =
                                    "No attempts yet",
                                text =
                                    "Practise this question to begin its history."
                            )
                        }
                    }

                    if (uiState.hasRevisions) {
                        item(
                            key =
                                "revision-history"
                        ) {
                            RevisionHistorySection(
                                items =
                                    uiState.revisions,
                                deletingRevisionIds =
                                    uiState
                                        .deletingRevisionIds,
                                onUseRevision = {
                                        revisionItem ->

                                    onUseRevision(
                                        revisionItem.revision
                                    )
                                },
                                onDeleteRevision =
                                    onDeleteRevision
                            )
                        }
                    }

                    item(
                        key =
                            "bottom-space"
                    ) {
                        Spacer(
                            modifier =
                                Modifier.height(
                                    SangfroidSpacing.Section
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySectionHeading(
    title: String,
    supportingText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier,
        verticalArrangement =
            Arrangement.spacedBy(
                SangfroidSpacing.Xxs
            )
    ) {
        Text(
            text =
                title,
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
                supportingText,
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

@Composable
private fun ErrorMessageCard(
    message: String,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        modifier =
            modifier,
        color =
            MaterialTheme
                .colorScheme
                .errorContainer,
        contentColor =
            MaterialTheme
                .colorScheme
                .onErrorContainer,
        shape =
            MaterialTheme
                .shapes
                .large
    ) {
        Text(
            text =
                message,
            modifier =
                Modifier.padding(
                    SangfroidSpacing.Md
                ),
            style =
                MaterialTheme
                    .typography
                    .bodyMedium
        )
    }
}

@Composable
private fun EmptyHistoryCard(
    title: String,
    text: String,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        modifier =
            modifier,
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
                    SangfroidSpacing.CardContent
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    SangfroidSpacing.Xs
                )
        ) {
            Text(
                text =
                    title,
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    text,
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )
        }
    }
}

@Composable
private fun HistoryMessage(
    title: String,
    text: String,
    showProgress: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(
                    horizontal =
                        SangfroidSpacing
                            .ScreenHorizontal
                ),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        if (showProgress) {
            CircularProgressIndicator()

            Spacer(
                modifier =
                    Modifier.height(
                        SangfroidSpacing.Md
                    )
            )
        }

        Text(
            text =
                title,
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

        Spacer(
            modifier =
                Modifier.height(
                    SangfroidSpacing.Xs
                )
        )

        Text(
            text =
                text,
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
}