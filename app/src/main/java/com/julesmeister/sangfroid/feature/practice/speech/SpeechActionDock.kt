package com.julesmeister.sangfroid.feature.practice.speech

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidAlpha
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidDimensions
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.shape.SangfroidRadius
import com.julesmeister.sangfroid.core.designsystem.theme.FeedbackError
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidCharcoal
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidCyan
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidLime
import com.julesmeister.sangfroid.core.designsystem.theme.swipeDockColors

private enum class SpeechDockGlyph {
    CROSS,
    MICROPHONE,
    STOP,
    SUBMIT
}

@Composable
fun SpeechActionDock(
    state: SpeechUiState,
    submitEnabled: Boolean,
    isSubmitting: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = swipeDockColors()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    SangfroidRadius.Pill
                )
            )
            .background(
                colors.container
            )
            .padding(
                SangfroidSpacing.Xs
            ),
        horizontalArrangement =
            Arrangement.spacedBy(
                SangfroidSpacing.Xs
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        SpeechCircularAction(
            glyph =
                SpeechDockGlyph.CROSS,
            label =
                if (state.isRecording) {
                    "Cancel recording"
                } else {
                    "Clear voice input"
                },
            accent = FeedbackError,
            enabled =
                state.canCancel ||
                        state.finalTranscript != null,
            onClick =
                onCancelRecording
        )

        when {
            state.isPreparing -> {
                SpeechStatusAction(
                    label =
                        "Preparing offline model...",
                    modifier =
                        Modifier.weight(1f)
                )
            }

            state.isProcessing -> {
                SpeechStatusAction(
                    label =
                        "Transcribing...",
                    modifier =
                        Modifier.weight(1f)
                )
            }

            state.isRecording -> {
                RecordingAction(
                    elapsedLabel =
                        state.elapsedLabel,
                    enabled =
                        state.canStop,
                    onClick =
                        onStopRecording,
                    modifier =
                        Modifier.weight(1f)
                )
            }

            else -> {
                StartSpeakingAction(
                    enabled =
                        state.canStart,
                    onClick =
                        onStartRecording,
                    modifier =
                        Modifier.weight(1f)
                )
            }
        }

        SpeechCircularAction(
            glyph =
                SpeechDockGlyph.SUBMIT,
            label =
                if (isSubmitting) {
                    "Submitting answer"
                } else {
                    "Submit answer"
                },
            accent =
                SangfroidLime,
            enabled =
                submitEnabled &&
                        !state.isRecording &&
                        !state.isProcessing &&
                        !isSubmitting,
            onClick =
                onSubmit
        )
    }
}

@Composable
private fun SpeechCircularAction(
    glyph: SpeechDockGlyph,
    label: String,
    accent: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors =
        swipeDockColors()

    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val pressed by interactionSource
        .collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue =
            if (pressed) {
                SangfroidDimensions
                    .SpeechDockPressedScale
            } else {
                1f
            },
        label =
            "speechCircularActionPress"
    )

    Canvas(
        modifier = Modifier
            .size(
                SangfroidDimensions
                    .SpeechDockButtonSize
            )
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale

                alpha =
                    if (enabled) {
                        1f
                    } else {
                        SangfroidAlpha
                            .DockDisabled
                    }
            }
            .clickable(
                interactionSource =
                    interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick
            )
            .semantics {
                contentDescription =
                    label
            }
    ) {
        drawCircle(
            color =
                if (enabled) {
                    accent
                } else {
                    colors.buttonContainer
                }
        )

        drawSpeechDockGlyph(
            glyph = glyph,
            color =
                if (enabled) {
                    SangfroidCharcoal
                } else {
                    colors.buttonContent
                }
        )
    }
}

@Composable
private fun StartSpeakingAction(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val pressed by interactionSource
        .collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue =
            if (pressed) {
                SangfroidDimensions
                    .SpeechDockPressedScale
            } else {
                1f
            },
        label =
            "startSpeakingPress"
    )

    Row(
        modifier = modifier
            .height(
                SangfroidDimensions
                    .SpeechDockButtonSize
            )
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale

                alpha =
                    if (enabled) {
                        1f
                    } else {
                        SangfroidAlpha
                            .DockDisabled
                    }
            }
            .clip(
                RoundedCornerShape(
                    SangfroidRadius.Pill
                )
            )
            .background(
                SangfroidCyan
            )
            .clickable(
                interactionSource =
                    interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClickLabel =
                    "Start speaking",
                onClick = onClick
            )
            .padding(
                horizontal =
                    SangfroidSpacing.Lg
            ),
        horizontalArrangement =
            Arrangement.spacedBy(
                SangfroidSpacing.Xs,
                alignment =
                    Alignment.CenterHorizontally
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Canvas(
            modifier = Modifier.size(
                SangfroidDimensions
                    .SpeechDockIconSize
            )
        ) {
            drawSpeechDockGlyph(
                glyph =
                    SpeechDockGlyph
                        .MICROPHONE,
                color =
                    SangfroidCharcoal
            )
        }

        Text(
            text = "Start speaking",
            style =
                MaterialTheme
                    .typography
                    .labelLarge,
            fontWeight =
                FontWeight.Bold,
            color =
                SangfroidCharcoal,
            maxLines = 1
        )
    }
}

@Composable
private fun RecordingAction(
    elapsedLabel: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val pressed by interactionSource
        .collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue =
            if (pressed) {
                SangfroidDimensions
                    .SpeechDockPressedScale
            } else {
                1f
            },
        label =
            "recordingPress"
    )

    val pulseTransition =
        rememberInfiniteTransition(
            label =
                "speechRecordingPulse"
        )

    val pulse by pulseTransition
        .animateFloat(
            initialValue =
                SangfroidDimensions
                    .SpeechRecordingPulseMinimum,
            targetValue =
                SangfroidDimensions
                    .SpeechRecordingPulseMaximum,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis =
                                850
                        ),
                    repeatMode =
                        RepeatMode.Reverse
                ),
            label =
                "speechRecordingDotPulse"
        )

    Row(
        modifier = modifier
            .height(
                SangfroidDimensions
                    .SpeechDockButtonSize
            )
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale

                alpha =
                    if (enabled) {
                        1f
                    } else {
                        SangfroidAlpha
                            .DockDisabled
                    }
            }
            .clip(
                RoundedCornerShape(
                    SangfroidRadius.Pill
                )
            )
            .background(
                FeedbackError
            )
            .clickable(
                interactionSource =
                    interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClickLabel =
                    "Stop recording",
                onClick = onClick
            )
            .padding(
                horizontal =
                    SangfroidSpacing.Lg
            ),
        horizontalArrangement =
            Arrangement.spacedBy(
                SangfroidSpacing.Xs,
                alignment =
                    Alignment.CenterHorizontally
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Canvas(
            modifier =
                Modifier.size(
                    SangfroidDimensions
                        .SpeechDockIconSize
                )
        ) {
            drawSpeechDockGlyph(
                glyph =
                    SpeechDockGlyph.STOP,
                color =
                    SangfroidCharcoal
            )
        }

        Text(
            text = "Stop",
            style =
                MaterialTheme
                    .typography
                    .labelLarge,
            fontWeight =
                FontWeight.Bold,
            color =
                SangfroidCharcoal,
            maxLines = 1
        )

        Box(
            modifier = Modifier
                .size(
                    SangfroidDimensions
                        .SpeechRecordingDot
                )
                .graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                }
                .clip(
                    CircleShape
                )
                .background(
                    SangfroidCharcoal
                )
        )

        Text(
            text = elapsedLabel,
            style =
                MaterialTheme
                    .typography
                    .labelLarge,
            fontWeight =
                FontWeight.Bold,
            color =
                SangfroidCharcoal,
            maxLines = 1
        )
    }
}

@Composable
private fun SpeechStatusAction(
    label: String,
    modifier: Modifier = Modifier
) {
    val colors =
        swipeDockColors()

    Row(
        modifier = modifier
            .height(
                SangfroidDimensions
                    .SpeechDockButtonSize
            )
            .clip(
                RoundedCornerShape(
                    SangfroidRadius.Pill
                )
            )
            .background(
                colors.buttonContainer
            )
            .padding(
                horizontal =
                    SangfroidSpacing.Lg
            ),
        horizontalArrangement =
            Arrangement.spacedBy(
                SangfroidSpacing.Xs,
                alignment =
                    Alignment.CenterHorizontally
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        ProcessingIndicator(
            color =
                SangfroidCyan
        )

        Text(
            text = label,
            style =
                MaterialTheme
                    .typography
                    .labelLarge,
            fontWeight =
                FontWeight.Bold,
            color =
                colors.buttonContent,
            maxLines = 1
        )
    }
}

@Composable
private fun ProcessingIndicator(
    color: Color
) {
    val transition =
        rememberInfiniteTransition(
            label =
                "speechProcessing"
        )

    val rotation by transition
        .animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(
                            durationMillis =
                                900
                        ),
                    repeatMode =
                        RepeatMode.Restart
                ),
            label =
                "speechProcessingRotation"
        )

    Canvas(
        modifier = Modifier
            .size(
                SangfroidDimensions
                    .SpeechDockIconSize
            )
            .graphicsLayer {
                rotationZ = rotation
            }
    ) {
        drawArc(
            color = color,
            startAngle = 20f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(
                width =
                    SangfroidDimensions
                        .SpeechDockIconStroke
                        .toPx(),
                cap =
                    StrokeCap.Round
            )
        )
    }
}

private fun DrawScope.drawSpeechDockGlyph(
    glyph: SpeechDockGlyph,
    color: Color
) {
    val iconSize =
        SangfroidDimensions
            .SpeechDockIconSize
            .toPx()

    val strokeWidth =
        SangfroidDimensions
            .SpeechDockIconStroke
            .toPx()

    val left =
        (size.width - iconSize) /
                2f

    val top =
        (size.height - iconSize) /
                2f

    fun point(
        x: Float,
        y: Float
    ): Offset {
        return Offset(
            x =
                left +
                        iconSize * x,
            y =
                top +
                        iconSize * y
        )
    }

    val stroke =
        Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

    when (glyph) {
        SpeechDockGlyph.CROSS -> {
            drawLine(
                color = color,
                start =
                    point(
                        0.22f,
                        0.22f
                    ),
                end =
                    point(
                        0.78f,
                        0.78f
                    ),
                strokeWidth =
                    strokeWidth,
                cap =
                    StrokeCap.Round
            )

            drawLine(
                color = color,
                start =
                    point(
                        0.78f,
                        0.22f
                    ),
                end =
                    point(
                        0.22f,
                        0.78f
                    ),
                strokeWidth =
                    strokeWidth,
                cap =
                    StrokeCap.Round
            )
        }

        SpeechDockGlyph.STOP -> {
            val stopSize =
                iconSize * 0.46f

            drawRoundRect(
                color = color,
                topLeft = Offset(
                    x =
                        center.x -
                                stopSize / 2f,
                    y =
                        center.y -
                                stopSize / 2f
                ),
                size = Size(
                    stopSize,
                    stopSize
                ),
                cornerRadius =
                    CornerRadius(
                        stopSize *
                                0.18f
                    )
            )
        }

        SpeechDockGlyph.MICROPHONE -> {
            val microphonePath =
                Path().apply {
                    val topPoint =
                        point(
                            0.50f,
                            0.12f
                        )

                    val rightTop =
                        point(
                            0.67f,
                            0.26f
                        )

                    val rightBottom =
                        point(
                            0.67f,
                            0.55f
                        )

                    val bottomPoint =
                        point(
                            0.50f,
                            0.69f
                        )

                    val leftBottom =
                        point(
                            0.33f,
                            0.55f
                        )

                    val leftTop =
                        point(
                            0.33f,
                            0.26f
                        )

                    moveTo(
                        topPoint.x,
                        topPoint.y
                    )

                    cubicTo(
                        rightTop.x,
                        topPoint.y,
                        rightTop.x,
                        rightTop.y,
                        rightTop.x,
                        rightTop.y
                    )

                    lineTo(
                        rightBottom.x,
                        rightBottom.y
                    )

                    cubicTo(
                        rightBottom.x,
                        bottomPoint.y,
                        bottomPoint.x,
                        bottomPoint.y,
                        bottomPoint.x,
                        bottomPoint.y
                    )

                    cubicTo(
                        leftBottom.x,
                        bottomPoint.y,
                        leftBottom.x,
                        leftBottom.y,
                        leftBottom.x,
                        leftBottom.y
                    )

                    lineTo(
                        leftTop.x,
                        leftTop.y
                    )

                    cubicTo(
                        leftTop.x,
                        topPoint.y,
                        topPoint.x,
                        topPoint.y,
                        topPoint.x,
                        topPoint.y
                    )
                }

            drawPath(
                path =
                    microphonePath,
                color = color,
                style = stroke
            )

            val microphoneCup =
                Path().apply {
                    val leftCup =
                        point(
                            0.19f,
                            0.50f
                        )

                    val bottomCup =
                        point(
                            0.50f,
                            0.82f
                        )

                    val rightCup =
                        point(
                            0.81f,
                            0.50f
                        )

                    moveTo(
                        leftCup.x,
                        leftCup.y
                    )

                    cubicTo(
                        leftCup.x,
                        bottomCup.y,
                        bottomCup.x,
                        bottomCup.y,
                        bottomCup.x,
                        bottomCup.y
                    )

                    cubicTo(
                        rightCup.x,
                        bottomCup.y,
                        rightCup.x,
                        rightCup.y,
                        rightCup.x,
                        rightCup.y
                    )
                }

            drawPath(
                path =
                    microphoneCup,
                color = color,
                style = stroke
            )

            drawLine(
                color = color,
                start =
                    point(
                        0.50f,
                        0.82f
                    ),
                end =
                    point(
                        0.50f,
                        0.94f
                    ),
                strokeWidth =
                    strokeWidth,
                cap =
                    StrokeCap.Round
            )

            drawLine(
                color = color,
                start =
                    point(
                        0.34f,
                        0.94f
                    ),
                end =
                    point(
                        0.66f,
                        0.94f
                    ),
                strokeWidth =
                    strokeWidth,
                cap =
                    StrokeCap.Round
            )
        }

        SpeechDockGlyph.SUBMIT -> {
            val submitPath =
                Path().apply {
                    val bottom =
                        point(
                            0.50f,
                            0.84f
                        )

                    val tip =
                        point(
                            0.50f,
                            0.16f
                        )

                    val leftWing =
                        point(
                            0.22f,
                            0.44f
                        )

                    val rightWing =
                        point(
                            0.78f,
                            0.44f
                        )

                    moveTo(
                        bottom.x,
                        bottom.y
                    )

                    lineTo(
                        tip.x,
                        tip.y
                    )

                    moveTo(
                        leftWing.x,
                        leftWing.y
                    )

                    lineTo(
                        tip.x,
                        tip.y
                    )

                    lineTo(
                        rightWing.x,
                        rightWing.y
                    )
                }

            drawPath(
                path =
                    submitPath,
                color = color,
                style = stroke
            )
        }
    }
}