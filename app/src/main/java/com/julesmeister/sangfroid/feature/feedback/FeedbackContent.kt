package com.julesmeister.sangfroid.feature.feedback

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.layout.SangfroidScreen
import com.julesmeister.sangfroid.feature.feedback.components.FeedbackErrorState
import com.julesmeister.sangfroid.feature.feedback.components.FeedbackHeader
import com.julesmeister.sangfroid.feature.feedback.components.FeedbackResults
import com.julesmeister.sangfroid.feature.feedback.components.FeedbackStatePanel
import com.julesmeister.sangfroid.feature.feedback.components.QueuedEvaluationState
import com.julesmeister.sangfroid.feature.feedback.components.RoseOrbitLoader
import kotlinx.coroutines.delay

private const val MinimizeAfterMillis = 3_000L

private enum class FeedbackMode {
    LOADING,
    EVALUATING,
    EVALUATING_MINIMIZED,
    RESULTS,
    ERROR,
    QUEUED,
    EMPTY
}

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
    var isMinimized by
    remember {
        mutableStateOf(false)
    }

    var elapsedSeconds by
    remember {
        mutableIntStateOf(0)
    }

    /*
     * Start counting only while Qwen is actively evaluating.
     * After 3 seconds, the full loading panel collapses into the
     * compact floating card. Any state change resets the timer.
     */
    LaunchedEffect(uiState.isEvaluating) {
        isMinimized = false
        elapsedSeconds = 0

        if (!uiState.isEvaluating) {
            return@LaunchedEffect
        }

        val startedAt =
            System.currentTimeMillis()

        while (true) {
            delay(1_000L)

            val elapsed =
                System.currentTimeMillis() - startedAt

            elapsedSeconds =
                (elapsed / 1_000L).toInt()

            if (elapsed >= MinimizeAfterMillis) {
                isMinimized = true
            }
        }
    }

    val mode =
        when {
            uiState.isLoading ->
                FeedbackMode.LOADING

            uiState.evaluation != null ->
                FeedbackMode.RESULTS

            uiState.isEvaluating && isMinimized ->
                FeedbackMode.EVALUATING_MINIMIZED

            uiState.isEvaluating ->
                FeedbackMode.EVALUATING

            uiState.errorMessage != null ->
                FeedbackMode.ERROR

            uiState.isQueued ->
                FeedbackMode.QUEUED

            else ->
                FeedbackMode.EMPTY
        }

    SangfroidScreen(
        modifier = modifier
    ) {
        FeedbackHeader(
            onNavigateBack = onNavigateBack
        )

        Spacer(
            modifier =
                Modifier.height(
                    SangfroidSpacing.Section
                )
        )

        AnimatedContent(
            targetState = mode,
            transitionSpec = {
                feedbackTransition(
                    from = initialState,
                    to = targetState
                )
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            label = "feedback-mode"
        ) { currentMode ->
            when (currentMode) {
                FeedbackMode.LOADING -> {
                    FeedbackStatePanel(
                        title = "Loading attempt",
                        message =
                            "Preparing your saved interview answer.",
                        showProgress = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                FeedbackMode.RESULTS -> {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        FeedbackResults(
                            uiState = uiState,
                            onSaveRevision = onSaveRevision,
                            onOpenRevisionHistory =
                                onOpenRevisionHistory,
                            onTryAgain = onTryAgain
                        )
                    }
                }

                FeedbackMode.EVALUATING -> {
                    FeedbackStatePanel(
                        title = "Reviewing your answer",
                        message =
                            "Qwen is checking structure, evidence, clarity, and missing details.",
                        showProgress = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                FeedbackMode.EVALUATING_MINIMIZED -> {
                    MinimizedEvaluation(
                        questionText =
                            uiState.question?.question,
                        elapsedSeconds = elapsedSeconds,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                FeedbackMode.ERROR -> {
                    FeedbackErrorState(
                        message =
                            uiState.errorMessage.orEmpty(),
                        canRetry = uiState.canRetry,
                        onRetry = onRetryEvaluation
                    )
                }

                FeedbackMode.QUEUED -> {
                    QueuedEvaluationState(
                        canRetry = uiState.canRetry,
                        onRetry = onRetryEvaluation
                    )
                }

                FeedbackMode.EMPTY -> {
                    FeedbackStatePanel(
                        title = "Feedback unavailable",
                        message =
                            "No evaluation result is available yet.",
                        showProgress = false,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/*
 * Transition rules:
 *
 * full loading -> minimized:
 *     the panel drops away and the compact card rises from below.
 *
 * minimized -> results:
 *     results slide up from the bottom, as if the small card
 *     expanded into the full feedback screen.
 *
 * everything else:
 *     simple crossfade.
 */
private fun feedbackTransition(
    from: FeedbackMode,
    to: FeedbackMode
): ContentTransform {
    return when {
        from == FeedbackMode.EVALUATING &&
                to == FeedbackMode.EVALUATING_MINIMIZED -> {
            (
                    fadeIn(
                        animationSpec =
                            tween(
                                durationMillis =
                                    420
                            )
                    ) +
                            slideInVertically(
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            420,
                                        easing =
                                            FastOutSlowInEasing
                                    )
                            ) { height ->
                                height / 4
                            }
                    ) togetherWith (
                    fadeOut(
                        animationSpec =
                            tween(
                                durationMillis =
                                    210
                            )
                    ) +
                            slideOutVertically(
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            420,
                                        easing =
                                            FastOutSlowInEasing
                                    )
                            ) { height ->
                                height / 6
                            }
                    )
        }

        from == FeedbackMode.EVALUATING_MINIMIZED &&
                to == FeedbackMode.RESULTS -> {
            (
                    fadeIn(
                        animationSpec =
                            tween(
                                durationMillis =
                                    520
                            )
                    ) +
                            slideInVertically(
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            520,
                                        easing =
                                            FastOutSlowInEasing
                                    )
                            ) { height ->
                                height / 2
                            } +
                            scaleIn(
                                initialScale =
                                    0.96f,
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            520
                                    )
                            )
                    ) togetherWith
                    fadeOut(
                        animationSpec =
                            tween(
                                durationMillis =
                                    210
                            )
                    )
        }

        else -> {
            fadeIn(
                animationSpec =
                    tween(
                        durationMillis =
                            260
                    )
            ) togetherWith
                    fadeOut(
                        animationSpec =
                            tween(
                                durationMillis =
                                    180
                            )
                    )
        }
    }
}

@Composable
private fun MinimizedEvaluation(
    questionText: String?,
    elapsedSeconds: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        /*
         * Calm background while the compact card waits at the bottom.
         */
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 140.dp),
            verticalArrangement =
                Arrangement.spacedBy(SangfroidSpacing.Sm)
        ) {
            Text(
                text = "YOUR QUESTION",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text =
                    questionText
                        ?.takeIf(String::isNotBlank)
                        ?: "Your submitted answer",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text =
                    "Your full feedback will open here automatically as soon as it's ready.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(
            visible = true,
            enter =
                fadeIn() +
                        slideInVertically { height -> height },
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
        ) {
            MinimizedEvaluationCard(
                elapsedSeconds = elapsedSeconds
            )
        }
    }
}

@Composable
private fun MinimizedEvaluationCard(
    elapsedSeconds: Int
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = SangfroidSpacing.Md),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 3.dp,
        shadowElevation = 14.dp
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = SangfroidSpacing.Md,
                    vertical = 14.dp
                ),
            horizontalArrangement =
                Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoseOrbitLoader(
                modifier = Modifier.size(48.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Reviewing your answer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text =
                        "Checking structure, evidence and clarity",
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Text(
                text = "${elapsedSeconds}s",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}