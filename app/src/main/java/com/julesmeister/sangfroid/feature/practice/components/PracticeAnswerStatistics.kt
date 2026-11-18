package com.julesmeister.sangfroid.feature.practice.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidCyan
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidLime
import com.julesmeister.sangfroid.domain.model.AnswerInputMethod

private const val SavingRotationDurationMillis =
    1_200

private enum class StatisticType {
    WORDS,
    ATTEMPTS,
    SAVED,
    SAVING,
    INPUT
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeAnswerStatistics(
    wordCount: Int,
    attemptCount: Int,
    isSaved: Boolean,
    inputMethod: AnswerInputMethod
) {
    FlowRow(
        horizontalArrangement =
            Arrangement.spacedBy(
                SangfroidSpacing.Xs
            ),
        verticalArrangement =
            Arrangement.spacedBy(
                SangfroidSpacing.Xs
            )
    ) {
        StatisticPill(
            label = "Words",
            value = wordCount.toString(),
            type = StatisticType.WORDS
        )

        StatisticPill(
            label =
                if (attemptCount == 1) {
                    "Attempt"
                } else {
                    "Attempts"
                },
            value = attemptCount.toString(),
            type = StatisticType.ATTEMPTS
        )

        StatisticPill(
            label =
                if (isSaved) {
                    "Saved"
                } else {
                    "Saving"
                },
            value = null,
            type =
                if (isSaved) {
                    StatisticType.SAVED
                } else {
                    StatisticType.SAVING
                }
        )

        StatisticPill(
            label =
                when (inputMethod) {
                    AnswerInputMethod.TYPED ->
                        "Typed"

                    AnswerInputMethod.SPEECH ->
                        "Voice"

                    AnswerInputMethod.MIXED ->
                        "Voice + edits"
                },
            value = null,
            type = StatisticType.INPUT
        )
    }
}

@Composable
private fun StatisticPill(
    label: String,
    value: String?,
    type: StatisticType
) {
    val transition =
        rememberInfiniteTransition(
            label =
                "statistic-pill"
        )

    val rotation by
    transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis =
                            SavingRotationDurationMillis,
                        easing =
                            LinearEasing
                    ),
                repeatMode =
                    RepeatMode.Restart
            ),
        label =
            "statistic-rotation"
    )

    val containerColor =
        when (type) {
            StatisticType.SAVED ->
                SangfroidLime.copy(
                    alpha = 0.22f
                )

            StatisticType.SAVING ->
                SangfroidCyan.copy(
                    alpha = 0.18f
                )

            else ->
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
        }

    val contentColor =
        when (type) {
            StatisticType.SAVED,
            StatisticType.SAVING ->
                MaterialTheme
                    .colorScheme
                    .onSurface

            else ->
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape =
            RoundedCornerShape(50)
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal =
                        SangfroidSpacing.Sm,
                    vertical =
                        SangfroidSpacing.Xs
                ),
            horizontalArrangement =
                Arrangement.spacedBy(
                    SangfroidSpacing.Xs
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            StatisticGlyph(
                type = type,
                color = contentColor,
                rotation = rotation
            )

            value?.let { currentValue ->
                AnimatedContent(
                    targetState =
                        currentValue,
                    transitionSpec = {
                        (
                                slideInVertically {
                                        height ->
                                    height
                                } +
                                        fadeIn()
                                ) togetherWith (
                                slideOutVertically {
                                        height ->
                                    -height
                                } +
                                        fadeOut()
                                )
                    },
                    label =
                        "statistic-value"
                ) { animatedValue ->
                    Text(
                        text = animatedValue,
                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }
            }

            Text(
                text = label,
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                fontWeight =
                    if (
                        type ==
                        StatisticType.SAVED
                    ) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    }
            )
        }
    }
}

@Composable
private fun StatisticGlyph(
    type: StatisticType,
    color: Color,
    rotation: Float
) {
    Canvas(
        modifier =
            Modifier
                .size(14.dp)
                .graphicsLayer {
                    rotationZ =
                        if (
                            type ==
                            StatisticType.SAVING
                        ) {
                            rotation
                        } else {
                            0f
                        }
                }
    ) {
        val stroke =
            1.7.dp.toPx()

        when (type) {
            StatisticType.WORDS -> {
                repeat(3) { index ->
                    val y =
                        size.height *
                                (
                                        0.22f +
                                                index * 0.28f
                                        )

                    drawLine(
                        color = color,
                        start =
                            Offset(
                                1.dp.toPx(),
                                y
                            ),
                        end =
                            Offset(
                                size.width -
                                        1.dp.toPx(),
                                y
                            ),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                }
            }

            StatisticType.ATTEMPTS -> {
                drawArc(
                    color = color,
                    startAngle = 35f,
                    sweepAngle = 285f,
                    useCenter = false,
                    style =
                        Stroke(
                            width = stroke,
                            cap = StrokeCap.Round
                        )
                )
            }

            StatisticType.SAVED -> {
                drawCheckGlyph(
                    color = color,
                    strokeWidth = stroke
                )
            }

            StatisticType.SAVING -> {
                drawArc(
                    color = color,
                    startAngle = 15f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style =
                        Stroke(
                            width = stroke,
                            cap = StrokeCap.Round
                        )
                )
            }

            StatisticType.INPUT -> {
                drawCircle(
                    color = color,
                    radius =
                        size.minDimension *
                                0.26f,
                    center = center,
                    style =
                        Stroke(width = stroke)
                )

                drawLine(
                    color = color,
                    start =
                        Offset(
                            center.x,
                            size.height * 0.76f
                        ),
                    end =
                        Offset(
                            center.x,
                            size.height
                        ),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}