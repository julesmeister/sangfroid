// app/src/main/java/com/julesmeister/sangfroid/feature/practice/components/IdealAnswerEditorAction.kt

package com.julesmeister.sangfroid.feature.practice.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidAlpha
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidCyan
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun IdealAnswerEditorAction(
    hasIdealAnswer: Boolean,
    isGenerating: Boolean,
    enabled: Boolean,
    onGenerate: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actionLabel =
        when {
            isGenerating ->
                "Preparing"

            hasIdealAnswer ->
                "Refresh"

            else ->
                "Ideal answer"
        }

    val actionDescription =
        when {
            isGenerating ->
                "Preparing an ideal answer"

            hasIdealAnswer ->
                "Refresh the ideal answer guide"

            else ->
                "Generate an ideal answer guide"
        }

    val action =
        if (hasIdealAnswer) {
            onRefresh
        } else {
            onGenerate
        }

    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Row(
        modifier =
            modifier
                .graphicsLayer {
                    alpha =
                        if (
                            enabled ||
                            isGenerating
                        ) {
                            1f
                        } else {
                            SangfroidAlpha.Disabled
                        }
                }
                .semantics {
                    role =
                        Role.Button

                    contentDescription =
                        actionDescription
                }
                .clickable(
                    interactionSource =
                        interactionSource,
                    indication =
                        null,
                    enabled =
                        enabled &&
                                !isGenerating,
                    role =
                        Role.Button,
                    onClickLabel =
                        actionDescription,
                    onClick =
                        action
                )
                .padding(
                    start =
                        SangfroidSpacing.Sm
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        if (isGenerating) {
            CompactRoseOrbitLoader(
                modifier =
                    Modifier.size(
                        18.dp
                    )
            )
        }

        Text(
            text =
                actionLabel,
            modifier =
                Modifier
                    .padding(
                        start =
                            if (isGenerating) {
                                6.dp
                            } else {
                                0.dp
                            }
                    )
                    .alignBy(
                        FirstBaseline
                    ),
            style =
                MaterialTheme
                    .typography
                    .labelSmall,
            fontWeight =
                when {
                    isGenerating ->
                        FontWeight.Normal

                    hasIdealAnswer ->
                        FontWeight.Bold

                    else ->
                        FontWeight.Bold
                },
            color =
                when {
                    isGenerating ->
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant

                    hasIdealAnswer ->
                        MaterialTheme
                            .colorScheme
                            .onSurface

                    else ->
                        MaterialTheme
                            .colorScheme
                            .onSurface
                },
            maxLines =
                1
        )
    }
}

@Composable
private fun CompactRoseOrbitLoader(
    modifier: Modifier = Modifier
) {
    val infiniteTransition =
        rememberInfiniteTransition(
            label =
                "compact-rose-orbit-loader"
        )

    val progress by
    infiniteTransition.animateFloat(
        initialValue =
            0f,
        targetValue =
            1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis =
                            2_100,
                        easing =
                            LinearEasing
                    ),
                repeatMode =
                    RepeatMode.Restart
            ),
        label =
            "compact-orbit-progress"
    )

    val pulse by
    infiniteTransition.animateFloat(
        initialValue =
            0f,
        targetValue =
            1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis =
                            2_800,
                        easing =
                            LinearEasing
                    ),
                repeatMode =
                    RepeatMode.Restart
            ),
        label =
            "compact-orbit-pulse"
    )

    val rotation by
    infiniteTransition.animateFloat(
        initialValue =
            0f,
        targetValue =
            -360f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis =
                            9_000,
                        easing =
                            LinearEasing
                    ),
                repeatMode =
                    RepeatMode.Restart
            ),
        label =
            "compact-orbit-rotation"
    )

    val particleColor =
        SangfroidCyan

    val orbitColor =
        MaterialTheme
            .colorScheme
            .onSurface

    Canvas(
        modifier =
            modifier.graphicsLayer {
                rotationZ =
                    rotation
            }
    ) {
        val orbitRadius =
            size.minDimension *
                    0.34f

        val detailAmplitude =
            size.minDimension *
                    0.095f

        val center =
            Offset(
                x =
                    size.width /
                            2f,
                y =
                    size.height /
                            2f
            )

        val frequency =
            5

        val particleCount =
            28

        val trailSpan =
            0.38f

        val twoPi =
            2f *
                    Math.PI
                        .toFloat()

        val pulseAngle =
            pulse *
                    twoPi +
                    0.55f

        val detailScale =
            0.58f +
                    (
                            (
                                    sin(
                                        pulseAngle
                                    ) +
                                            1f
                                    ) /
                                    2f
                            ) *
                    0.42f

        val path =
            Path()

        for (
        index in
        0..200
        ) {
            val angle =
                (
                        index /
                                200f
                        ) *
                        twoPi

            val radius =
                orbitRadius -
                        detailAmplitude *
                        detailScale *
                        cos(
                            frequency *
                                    angle
                        )

            val x =
                center.x +
                        cos(
                            angle
                        ) *
                        radius

            val y =
                center.y +
                        sin(
                            angle
                        ) *
                        radius

            if (index == 0) {
                path.moveTo(
                    x,
                    y
                )
            } else {
                path.lineTo(
                    x,
                    y
                )
            }
        }

        drawPath(
            path =
                path,
            color =
                orbitColor.copy(
                    alpha =
                        0.42f
                ),
            style =
                Stroke(
                    width =
                        1.25.dp
                            .toPx(),
                    cap =
                        StrokeCap.Round
                )
        )

        for (
        index in
        0 until particleCount
        ) {
            val tailOffset =
                index.toFloat() /
                        (
                                particleCount -
                                        1
                                )

            val particleProgress =
                (
                        (
                                progress -
                                        tailOffset *
                                        trailSpan
                                ) %
                                1f +
                                1f
                        ) %
                        1f

            val angle =
                particleProgress *
                        twoPi

            val radius =
                orbitRadius -
                        detailAmplitude *
                        detailScale *
                        cos(
                            frequency *
                                    angle
                        )

            val x =
                center.x +
                        cos(
                            angle
                        ) *
                        radius

            val y =
                center.y +
                        sin(
                            angle
                        ) *
                        radius

            val fade =
                (
                        1f -
                                tailOffset
                        ).pow(
                        0.52f
                    )

            val alpha =
                0.32f +
                        fade *
                        0.68f

            val particleRadius =
                (
                        0.45f +
                                fade *
                                1.15f
                        ).dp
                    .toPx()

            drawCircle(
                color =
                    particleColor.copy(
                        alpha =
                            alpha
                    ),
                radius =
                    particleRadius,
                center =
                    Offset(
                        x,
                        y
                    )
            )
        }
    }
}