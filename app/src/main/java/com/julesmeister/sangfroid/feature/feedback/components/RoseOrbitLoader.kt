package com.julesmeister.sangfroid.feature.feedback.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidCyan
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun RoseOrbitLoader(
    modifier: Modifier = Modifier
) {
    val infiniteTransition =
        rememberInfiniteTransition(
            label = "rose-orbit-loader"
        )

    val progress by
    infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 5_200,
                        easing = LinearEasing
                    ),
                repeatMode =
                    RepeatMode.Restart
            ),
        label = "orbit-progress"
    )

    val pulse by
    infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 4_600,
                        easing = LinearEasing
                    ),
                repeatMode =
                    RepeatMode.Restart
            ),
        label = "orbit-pulse"
    )

    val rotation by
    infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 28_000,
                        easing = LinearEasing
                    ),
                repeatMode =
                    RepeatMode.Restart
            ),
        label = "orbit-rotation"
    )

    /*
     * Do not use Color.White here.
     *
     * secondary:
     *     visible brand accent in both themes.
     *
     * onSurfaceVariant:
     *     neutral but visible underlying orbit.
     */
    val particleColor =
        SangfroidCyan

    val orbitColor =
        MaterialTheme
            .colorScheme
            .onSurface

    Canvas(
        modifier = modifier
            .size(200.dp)
            .graphicsLayer {
                rotationZ =
                    rotation
            }
    ) {
        val orbitRadius =
            7.0f

        val detailAmplitude =
            2.7f

        val frequency =
            7

        val curveScale =
            3.9f

        val particleCount =
            72

        val trailSpan =
            0.42f

        val twoPi =
            2f *
                    Math.PI
                        .toFloat()

        val pulseAngle =
            pulse *
                    twoPi +
                    0.55f

        val detailScale =
            0.52f +
                    (
                            (
                                    sin(
                                        pulseAngle
                                    ) +
                                            1f
                                    ) /
                                    2f
                            ) *
                    0.48f

        val canvasScale =
            size.width /
                    100f

        val path =
            Path()

        for (
        index in
        0..480
        ) {
            val angle =
                (
                        index /
                                480f
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
                (
                        50f +
                                cos(angle) *
                                radius *
                                curveScale
                        ) *
                        canvasScale

            val y =
                (
                        50f +
                                sin(angle) *
                                radius *
                                curveScale
                        ) *
                        canvasScale

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
                        0.34f
                ),
            style =
                Stroke(
                    width =
                        1.15.dp.toPx(),
                    cap =
                        StrokeCap.Round
                )
        )

        for (
        index in
        0 until
                particleCount
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
                (
                        50f +
                                cos(angle) *
                                radius *
                                curveScale
                        ) *
                        canvasScale

            val y =
                (
                        50f +
                                sin(angle) *
                                radius *
                                curveScale
                        ) *
                        canvasScale

            val fade =
                (
                        1f -
                                tailOffset
                        ).pow(
                        0.56f
                    )

            val alpha =
                0.10f +
                        fade *
                        0.90f

            val particleRadius =
                (
                        0.9f +
                                fade *
                                2.7f
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