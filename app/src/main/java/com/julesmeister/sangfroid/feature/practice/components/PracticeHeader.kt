package com.julesmeister.sangfroid.feature.practice.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidDimensions
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidCharcoal
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidCyan
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidLime
import kotlinx.coroutines.delay

private const val HeaderEntranceDelayMillis = 60L
private const val HeaderEntranceDurationMillis = 460
private const val SavingRotationDurationMillis = 1_200
private const val PressedScale = 0.92f

@Composable
fun PracticeHeader(
    onNavigateBack: () -> Unit,
    isDraftSaved: Boolean,
    wordCount: Int,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val entrance =
        remember {
            Animatable(0f)
        }

    val density =
        LocalDensity.current

    val offsetPx =
        with(density) {
            18.dp.toPx()
        }

    LaunchedEffect(Unit) {
        delay(HeaderEntranceDelayMillis)

        entrance.animateTo(
            targetValue = 1f,
            animationSpec =
                tween(
                    durationMillis =
                        HeaderEntranceDurationMillis
                )
        )
    }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha =
                        entrance.value
                            .coerceIn(0f, 1f)

                    translationY =
                        (1f - entrance.value) *
                                -offsetPx
                },
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        PracticeBackButton(
            onClick = onNavigateBack
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment =
                Alignment.End,
            verticalArrangement =
                Arrangement.spacedBy(
                    SangfroidSpacing.Xxs
                )
        ) {
            Text(
                text = "Practice",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground,
                fontWeight =
                    FontWeight.ExtraBold
            )

            AnimatedVisibility(
                visible = !compact,
                enter =
                    fadeIn() +
                            expandVertically(),
                exit =
                    fadeOut() +
                            shrinkVertically()
            ) {
                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            SangfroidSpacing.Xs
                        ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    DraftStatusIndicator(
                        saved = isDraftSaved
                    )

                    AnimatedContent(
                        targetState =
                            isDraftSaved,
                        transitionSpec = {
                            (
                                    slideInVertically {
                                            height ->
                                        height / 2
                                    } +
                                            fadeIn()
                                    ) togetherWith (
                                    slideOutVertically {
                                            height ->
                                        -height / 2
                                    } +
                                            fadeOut()
                                    )
                        },
                        label =
                            "practice-draft-status"
                    ) { saved ->
                        Text(
                            text =
                                when {
                                    !saved ->
                                        "Saving draft…"

                                    wordCount > 0 ->
                                        "Draft saved locally"

                                    else ->
                                        "Ready when you are"
                                },
                            style =
                                MaterialTheme
                                    .typography
                                    .labelMedium,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PracticeBackButton(
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val pressed by
    interactionSource
        .collectIsPressedAsState()

    val scale by
    animateFloatAsState(
        targetValue =
            if (pressed) {
                PressedScale
            } else {
                1f
            },
        animationSpec =
            spring(
                dampingRatio =
                    Spring.DampingRatioMediumBouncy
            ),
        label =
            "practice-back-button"
    )

    val iconColor =
        MaterialTheme
            .colorScheme
            .onSurface

    Box(
        modifier =
            Modifier
                .size(
                    SangfroidDimensions
                        .PracticeHeaderButtonSize
                )
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
                )
                .clickable(
                    interactionSource =
                        interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClickLabel =
                        "Return to questions",
                    onClick = onClick
                )
                .semantics {
                    contentDescription =
                        "Return to questions"
                },
        contentAlignment =
            Alignment.Center
    ) {
        Canvas(
            modifier =
                Modifier.size(18.dp)
        ) {
            val stroke =
                2.dp.toPx()

            drawLine(
                color = iconColor,
                start =
                    Offset(
                        size.width * 0.83f,
                        center.y
                    ),
                end =
                    Offset(
                        size.width * 0.18f,
                        center.y
                    ),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )

            drawLine(
                color = iconColor,
                start =
                    Offset(
                        size.width * 0.18f,
                        center.y
                    ),
                end =
                    Offset(
                        size.width * 0.44f,
                        size.height * 0.22f
                    ),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )

            drawLine(
                color = iconColor,
                start =
                    Offset(
                        size.width * 0.18f,
                        center.y
                    ),
                end =
                    Offset(
                        size.width * 0.44f,
                        size.height * 0.78f
                    ),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun DraftStatusIndicator(
    saved: Boolean
) {
    val transition =
        rememberInfiniteTransition(
            label =
                "practice-draft-indicator"
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
            "practice-draft-rotation"
    )

    Canvas(
        modifier =
            Modifier
                .size(14.dp)
                .graphicsLayer {
                    rotationZ =
                        if (saved) {
                            0f
                        } else {
                            rotation
                        }
                }
    ) {
        if (saved) {
            drawCircle(
                color = SangfroidLime
            )

            drawCheckGlyph(
                color = SangfroidCharcoal,
                strokeWidth =
                    1.5.dp.toPx()
            )
        } else {
            drawArc(
                color = SangfroidCyan,
                startAngle = 15f,
                sweepAngle = 270f,
                useCenter = false,
                style =
                    Stroke(
                        width =
                            1.8.dp.toPx(),
                        cap =
                            StrokeCap.Round
                    )
            )
        }
    }
}