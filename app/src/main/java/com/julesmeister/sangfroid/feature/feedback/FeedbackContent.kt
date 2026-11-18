package com.julesmeister.sangfroid.feature.feedback

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.layout.SangfroidScreen
import com.julesmeister.sangfroid.feature.feedback.components.FeedbackErrorState
import com.julesmeister.sangfroid.feature.feedback.components.FeedbackHeader
import com.julesmeister.sangfroid.feature.feedback.components.FeedbackResults
import com.julesmeister.sangfroid.feature.feedback.components.FeedbackStatePanel
import com.julesmeister.sangfroid.feature.feedback.components.QueuedEvaluationState

@Composable
internal fun FeedbackContent(
    uiState: FeedbackUiState,
    onNavigateBack: () -> Unit,
    onTryAgain: () -> Unit,
    onRetryEvaluation: () -> Unit,
    onSaveRevision: () -> Unit,
    onOpenRevisionHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    SangfroidScreen(
        modifier = modifier
    ) {
        FeedbackHeader(
            onNavigateBack =
                onNavigateBack
        )

        Spacer(
            modifier =
                Modifier.height(
                    SangfroidSpacing.Section
                )
        )

        when {
            uiState.isLoading -> {
                FeedbackStatePanel(
                    title = "Loading attempt",
                    message =
                        "Preparing your saved interview answer.",
                    showProgress = true,
                    modifier =
                        Modifier.fillMaxSize()
                )
            }

            uiState.evaluation != null -> {
                FeedbackResults(
                    uiState = uiState,
                    onSaveRevision =
                        onSaveRevision,
                    onOpenRevisionHistory =
                        onOpenRevisionHistory,
                    onTryAgain =
                        onTryAgain
                )
            }

            uiState.isEvaluating -> {
                FeedbackStatePanel(
                    title =
                        "Reviewing your answer",
                    message =
                        "Qwen is checking structure, evidence, clarity, and missing details.",
                    showProgress = true,
                    modifier =
                        Modifier.fillMaxSize()
                )
            }

            uiState.errorMessage != null -> {
                FeedbackErrorState(
                    message =
                        uiState.errorMessage,
                    canRetry =
                        uiState.canRetry,
                    onRetry =
                        onRetryEvaluation
                )
            }

            uiState.isQueued -> {
                QueuedEvaluationState(
                    canRetry =
                        uiState.canRetry,
                    onRetry =
                        onRetryEvaluation
                )
            }

            else -> {
                FeedbackStatePanel(
                    title =
                        "Feedback unavailable",
                    message =
                        "No evaluation result is available yet.",
                    showProgress = false,
                    modifier =
                        Modifier.fillMaxSize()
                )
            }
        }
    }
}