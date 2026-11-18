package com.julesmeister.sangfroid.feature.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julesmeister.sangfroid.core.designsystem.component.SangfroidPillButton
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidDimensions
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.layout.SangfroidScreen
import com.julesmeister.sangfroid.feature.feed.components.FeedEmptyState
import com.julesmeister.sangfroid.feature.feed.components.FeedHeader
import com.julesmeister.sangfroid.feature.feed.components.QuestionCardStack
import com.julesmeister.sangfroid.feature.feed.components.SwipeActionBar
import com.julesmeister.sangfroid.feature.feed.custom.QuestionImportDialog
import com.julesmeister.sangfroid.feature.feed.model.SwipeAction
import com.julesmeister.sangfroid.feature.feed.model.SwipeHighlight
import com.julesmeister.sangfroid.feature.feed.presentation.rememberSwipeCardState
import kotlinx.coroutines.launch

private const val VisibleStackSize =
    3

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    onPracticeQuestion:
        (String) -> Unit = {},
    onOpenSaved:
        () -> Unit = {},
    onOpenProgress:
        () -> Unit = {},
    onOpenProfile:
        () -> Unit = {},
    viewModel: FeedViewModel =
        hiltViewModel()
) {
    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()

    FeedContent(
        modifier =
            modifier,
        uiState =
            uiState,
        onOpenSaved =
            onOpenSaved,
        onOpenProgress =
            onOpenProgress,
        onOpenProfile =
            onOpenProfile,
        onSwipeResolved = {
                action,
                questionId ->

            viewModel.resolveSwipe(
                action =
                    action,
                questionId =
                    questionId,
                onPracticeRequested =
                    onPracticeQuestion
            )
        },
        onGenerateFresh =
            viewModel::generateFreshQuestions,
        onRestoreSkipped =
            viewModel::restoreSkippedQuestions,
        onRestartFeed =
            viewModel::restartFeed,
        onOpenQuestionImport =
            viewModel::openQuestionImport,
        onQuestionImportContentChanged =
            viewModel::updateQuestionImportContent,
        onExtractImportedQuestions =
            viewModel::extractImportedQuestions,
        onConfirmQuestionImport =
            viewModel::confirmQuestionImport,
        onEditQuestionImport =
            viewModel::editQuestionImportContent,
        onDismissQuestionImport =
            viewModel::closeQuestionImport
    )
}

@Composable
private fun FeedContent(
    uiState: FeedUiState,
    onOpenSaved: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenProfile: () -> Unit,
    onSwipeResolved:
        (SwipeAction, String) -> Unit,
    onGenerateFresh: () -> Unit,
    onRestoreSkipped: () -> Unit,
    onRestartFeed: () -> Unit,
    onOpenQuestionImport: () -> Unit,
    onQuestionImportContentChanged:
        (String) -> Unit,
    onExtractImportedQuestions: () -> Unit,
    onConfirmQuestionImport: () -> Unit,
    onEditQuestionImport: () -> Unit,
    onDismissQuestionImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val swipeState =
        rememberSwipeCardState()

    val scope =
        rememberCoroutineScope()

    val density =
        LocalDensity.current

    val containerSize =
        LocalWindowInfo
            .current
            .containerSize

    val horizontalThresholdPx =
        with(density) {
            SangfroidDimensions
                .SwipeHorizontalThreshold
                .toPx()
        }

    val verticalThresholdPx =
        with(density) {
            SangfroidDimensions
                .SwipeVerticalThreshold
                .toPx()
        }

    val highlight:
                () -> SwipeHighlight =
        remember(
            swipeState,
            horizontalThresholdPx,
            verticalThresholdPx
        ) {
            {
                SwipeHighlight.from(
                    offset =
                        swipeState.offset.value,
                    horizontalThresholdPx =
                        horizontalThresholdPx,
                    verticalThresholdPx =
                        verticalThresholdPx
                )
            }
        }

    val resolveAction:
                (SwipeAction, String) -> Unit = {
            action,
            questionId ->

        onSwipeResolved(
            action,
            questionId
        )

        if (
            action ==
            SwipeAction.PRACTICE
        ) {
            scope.launch {
                swipeState.reset()
            }
        }
    }

    val restoreSkipped:
                () -> Unit = {
        scope.launch {
            swipeState.snapBack()
        }

        onRestoreSkipped()
    }

    val restartFeed:
                () -> Unit = {
        scope.launch {
            swipeState.snapBack()
        }

        onRestartFeed()
    }

    val interactionEnabled =
        !uiState.isProcessingAction &&
                !uiState.isGeneratingFreshQuestions &&
                !uiState.isImportingQuestions

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {
        SangfroidScreen(
            modifier =
                modifier
        ) {
            FeedHeader(
                skippedCount =
                    uiState.skippedCount,
                canRestoreSkipped =
                    uiState.canRestoreSkipped,
                onRestoreSkipped =
                    restoreSkipped,
                onOpenSaved =
                    onOpenSaved,
                onOpenProgress =
                    onOpenProgress,
                onOpenProfile =
                    onOpenProfile
            )

            Spacer(
                modifier =
                    Modifier.height(
                        SangfroidSpacing.Xs
                    )
            )

            SangfroidPillButton(
                text =
                    "Paste questions",
                onClick =
                    onOpenQuestionImport,
                enabled =
                    interactionEnabled,
                emphasized =
                    false,
                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                modifier =
                    Modifier.height(
                        SangfroidSpacing.Sm
                    )
            )

            when {
                uiState.isLoading -> {
                    FeedMessageArea {
                        CircularProgressIndicator(
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurface
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    SangfroidSpacing.Md
                                )
                        )

                        Text(
                            text =
                                "Preparing your questions",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurface
                        )
                    }
                }

                uiState.questions.isEmpty() -> {
                    FeedMessageArea {
                        FeedEmptyState(
                            skippedCount =
                                uiState.skippedCount,
                            enabled =
                                interactionEnabled,
                            generationState =
                                uiState.freshGeneration,
                            onGenerateFresh =
                                onGenerateFresh,
                            onRestoreSkipped =
                                restoreSkipped,
                            onRestartFeed =
                                restartFeed
                        )
                    }
                }

                uiState.errorMessage != null -> {
                    val errorMessage =
                        uiState.errorMessage

                    FeedMessageArea {
                        Text(
                            text =
                                "Something went wrong",
                            style =
                                MaterialTheme
                                    .typography
                                    .titleLarge,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    SangfroidSpacing.Xs
                                )
                        )

                        Text(
                            text =
                                errorMessage,
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurface
                        )
                    }
                }

                else -> {
                    QuestionCardStack(
                        questions =
                            uiState.questions.take(
                                VisibleStackSize
                            ),
                        totalQuestions =
                            uiState.questions.size,
                        recommendationScores =
                            uiState
                                .recommendationScores,
                        swipeState =
                            swipeState,
                        enabled =
                            interactionEnabled,
                        onSwipeResolved = {
                                action,
                                question ->

                            resolveAction(
                                action,
                                question.id
                            )
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(
                                    1f
                                )
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                SangfroidSpacing.Sm
                            )
                    )

                    SwipeActionBar(
                        enabled =
                            interactionEnabled,
                        highlight =
                            highlight,
                        onAction = { action ->
                            val currentQuestion =
                                uiState
                                    .currentQuestion
                                    ?: return@SwipeActionBar

                            scope.launch {
                                swipeState.dismiss(
                                    action =
                                        action,
                                    containerWidthPx =
                                        containerSize
                                            .width
                                            .toFloat(),
                                    containerHeightPx =
                                        containerSize
                                            .height
                                            .toFloat()
                                )

                                resolveAction(
                                    action,
                                    currentQuestion.id
                                )
                            }
                        }
                    )
                }
            }
        }

        QuestionImportDialog(
            state =
                uiState.questionImport,
            onContentChanged =
                onQuestionImportContentChanged,
            onExtractQuestions =
                onExtractImportedQuestions,
            onImportQuestions =
                onConfirmQuestionImport,
            onEditContent =
                onEditQuestionImport,
            onDismiss =
                onDismissQuestionImport
        )
    }
}

@Composable
private fun FeedMessageArea(
    content: @Composable () -> Unit
) {
    Box(
        modifier =
            Modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}