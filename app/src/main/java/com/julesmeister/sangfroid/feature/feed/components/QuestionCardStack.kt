package com.julesmeister.sangfroid.feature.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidDimensions
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.theme.FeedbackError
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidCharcoal
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidCyan
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidLime
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.data.recommendation.QuestionRecommendationScore
import com.julesmeister.sangfroid.feature.feed.model.SwipeAction
import com.julesmeister.sangfroid.feature.feed.presentation.SwipeCardState
import kotlin.math.abs
import kotlin.math.max
import kotlinx.coroutines.launch

@Composable
fun QuestionCardStack(
    questions: List<QuestionEntity>,
    totalQuestions: Int,
    recommendationScores:
    Map<String, QuestionRecommendationScore>,
    swipeState: SwipeCardState,
    enabled: Boolean,
    onSwipeResolved:
        (SwipeAction, QuestionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val frontQuestion =
        questions.firstOrNull()
            ?: return

    val frontRecommendation =
        recommendationScores[
            frontQuestion.id
        ]

    val rearQuestions =
        questions
            .drop(
                1
            )
            .take(
                SangfroidDimensions
                    .SwipeVisibleRearCards
            )

    val scope =
        rememberCoroutineScope()

    val density =
        LocalDensity.current

    val currentOnSwipeResolved by
    rememberUpdatedState(
        onSwipeResolved
    )

    LaunchedEffect(
        frontQuestion.id
    ) {
        swipeState.snapBack()
    }

    BoxWithConstraints(
        modifier =
            modifier
    ) {
        /*
         * The recommendation is no longer overlaid on the
         * question card.
         *
         * The card area begins below:
         *
         * recommendation maximum height: 76dp
         * recommendation/card gap:       8dp
         * rear-card peek reserve:         existing token
         *
         * When there is no recommendation, only the normal
         * rear-card reserve is applied.
         */
        val recommendationReserve =
            if (frontRecommendation != null) {
                FeedRecommendationLayout
                    .MaximumHeight +
                        FeedRecommendationLayout
                            .GapBelow
            } else {
                0.dp
            }

        val cardTopInset =
            recommendationReserve +
                    SangfroidDimensions
                        .SwipeStackTopReserve

        val cardAreaModifier =
            Modifier
                .fillMaxSize()
                .padding(
                    top =
                        cardTopInset
                )

        val availableCardHeight =
            (
                    maxHeight -
                            cardTopInset
                    )
                .coerceAtLeast(
                    1.dp
                )

        val containerWidthPx =
            with(density) {
                maxWidth.toPx()
            }

        val containerHeightPx =
            with(density) {
                availableCardHeight.toPx()
            }

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

        val peekPx =
            with(density) {
                SangfroidDimensions
                    .SwipeStackPeek
                    .toPx()
            }

        val offset =
            swipeState.offset.value

        val horizontalProgress =
            (
                    abs(
                        offset.x
                    ) /
                            horizontalThresholdPx
                    )
                .coerceIn(
                    minimumValue =
                        0f,
                    maximumValue =
                        1f
                )

        val verticalProgress =
            (
                    -offset.y /
                            verticalThresholdPx
                    )
                .coerceIn(
                    minimumValue =
                        0f,
                    maximumValue =
                        1f
                )

        val dragProgress =
            max(
                horizontalProgress,
                verticalProgress
            )

        rearQuestions
            .indices
            .reversed()
            .forEach { index ->
                val question =
                    rearQuestions[
                        index
                    ]

                val depth =
                    (
                            index +
                                    1 -
                                    dragProgress
                            )
                        .coerceAtLeast(
                            0f
                        )

                key(
                    question.id
                ) {
                    InterviewQuestionCard(
                        question =
                            question,
                        position =
                            index + 2,
                        totalQuestions =
                            totalQuestions,
                        depth =
                            depth,
                        modifier =
                            cardAreaModifier
                                .graphicsLayer {
                                    val scale =
                                        1f -
                                                SangfroidDimensions
                                                    .SwipeRearScaleStep *
                                                depth

                                    transformOrigin =
                                        TransformOrigin(
                                            pivotFractionX =
                                                0.5f,
                                            pivotFractionY =
                                                0f
                                        )

                                    scaleX =
                                        scale

                                    scaleY =
                                        scale

                                    translationY =
                                        -peekPx *
                                                depth
                                }
                                .blur(
                                    radius =
                                        SangfroidDimensions
                                            .SwipeRearBlurPerDepth *
                                                depth,
                                    edgeTreatment =
                                        BlurredEdgeTreatment
                                            .Unbounded
                                )
                                .zIndex(
                                    index.toFloat()
                                )
                    )
                }
            }

        key(
            frontQuestion.id
        ) {
            val rotation =
                (
                        offset.x /
                                horizontalThresholdPx
                        )
                    .coerceIn(
                        minimumValue =
                            -1f,
                        maximumValue =
                            1f
                    ) *
                        SangfroidDimensions
                            .SwipeMaximumRotationDegrees

            val stampAction =
                when {
                    offset.y < 0f &&
                            verticalProgress >
                            horizontalProgress -> {
                        SwipeAction.PRACTICE
                    }

                    offset.x > 0f -> {
                        SwipeAction.SAVE
                    }

                    offset.x < 0f -> {
                        SwipeAction.SKIP
                    }

                    else -> {
                        null
                    }
                }

            Box(
                modifier =
                    cardAreaModifier
                        .pointerInput(
                            frontQuestion.id,
                            enabled
                        ) {
                            if (!enabled) {
                                return@pointerInput
                            }

                            detectDragGestures(
                                onDragEnd = {
                                    scope.launch {
                                        val action =
                                            swipeState
                                                .settle(
                                                    horizontalThresholdPx =
                                                        horizontalThresholdPx,
                                                    verticalThresholdPx =
                                                        verticalThresholdPx,
                                                    containerWidthPx =
                                                        containerWidthPx,
                                                    containerHeightPx =
                                                        containerHeightPx
                                                )

                                        if (action != null) {
                                            currentOnSwipeResolved(
                                                action,
                                                frontQuestion
                                            )
                                        }
                                    }
                                },
                                onDragCancel = {
                                    scope.launch {
                                        swipeState.reset()
                                    }
                                },
                                onDrag = {
                                        change,
                                        dragAmount ->

                                    change.consume()

                                    scope.launch {
                                        swipeState.dragBy(
                                            dragAmount
                                        )
                                    }
                                }
                            )
                        }
                        .graphicsLayer {
                            translationX =
                                offset.x

                            translationY =
                                offset.y

                            rotationZ =
                                rotation
                        }
                        .zIndex(
                            rearQuestions.size
                                .toFloat() +
                                    1f
                        )
            ) {
                InterviewQuestionCard(
                    question =
                        frontQuestion,
                    position =
                        1,
                    totalQuestions =
                        totalQuestions,
                    modifier =
                        Modifier.fillMaxSize()
                )

                if (
                    stampAction != null &&
                    dragProgress > 0f
                ) {
                    SwipeStamp(
                        action =
                            stampAction,
                        progress =
                            dragProgress,
                        modifier =
                            Modifier.align(
                                Alignment.Center
                            )
                    )
                }
            }
        }

        frontRecommendation
            ?.let { recommendation ->
                FeedRecommendationBanner(
                    recommendation =
                        recommendation,
                    modifier =
                        Modifier
                            .align(
                                Alignment.TopCenter
                            )
                            .padding(
                                horizontal =
                                    FeedRecommendationLayout
                                        .HorizontalInset
                            )
                            .zIndex(
                                rearQuestions.size
                                    .toFloat() +
                                        2f
                            )
                )
            }
    }
}

@Composable
private fun SwipeStamp(
    action: SwipeAction,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val label =
        when (action) {
            SwipeAction.SKIP -> {
                "SKIP"
            }

            SwipeAction.SAVE -> {
                "SAVE"
            }

            SwipeAction.PRACTICE -> {
                "PRACTICE"
            }
        }

    val containerColor =
        when (action) {
            SwipeAction.SKIP -> {
                FeedbackError
            }

            SwipeAction.SAVE -> {
                SangfroidLime
            }

            SwipeAction.PRACTICE -> {
                SangfroidCyan
            }
        }

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    val scale =
                        SangfroidDimensions
                            .SwipeStampMinScale +
                                (
                                        1f -
                                                SangfroidDimensions
                                                    .SwipeStampMinScale
                                        ) *
                                progress

                    alpha =
                        progress

                    scaleX =
                        scale

                    scaleY =
                        scale
                }
                .clip(
                    MaterialTheme
                        .shapes
                        .medium
                )
                .background(
                    containerColor
                )
                .padding(
                    horizontal =
                        SangfroidSpacing.Xl,
                    vertical =
                        SangfroidSpacing.Sm
                )
    ) {
        Text(
            text =
                label,
            style =
                MaterialTheme
                    .typography
                    .titleLarge,
            fontWeight =
                FontWeight.Bold,
            color =
                SangfroidCharcoal
        )
    }
}