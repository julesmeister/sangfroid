// app/src/main/java/com/julesmeister/sangfroid/feature/practice/components/IdealAnswerEditorAction.kt

package com.julesmeister.sangfroid.feature.practice.components
import androidx.compose.animation.togetherWith
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidAlpha
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
                ),
        horizontalArrangement =
            Arrangement.spacedBy(
                7.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        if (isGenerating) {
            CompactRoseOrbitLoader(
                modifier =
                    Modifier.size(
                        20.dp
                    )
            )
        }

        AnimatedContent(
            targetState =
                actionLabel,
            transitionSpec = {
                (
                        fadeIn(
                            animationSpec =
                                tween(
                                    durationMillis =
                                        180
                                )
                        ) togetherWith
                                fadeOut(
                                    animationSpec =
                                        tween(
                                            durationMillis =
                                                120
                                        )
                                )
                        )
                    .using(
                        SizeTransform(
                            clip =
                                false
                        )
                    )
            },
            label =
                "ideal-answer-editor-action"
        ) { label ->
            Text(
                text =
                    label,
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                fontWeight =
                    FontWeight.Bold,
                color =
                    if (isGenerating) {
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                    } else {
                        MaterialTheme
                            .colorScheme
                            .primary
                    },
                maxLines =
                    1
            )
        }
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
                            1_850,
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
                            2_400,
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
                            8_000,
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
        MaterialTheme
            .colorScheme
            .secondary

    val orbitColor =
        MaterialTheme
            .colorScheme
            .onSurfaceVariant

    Canvas(
        modifier =
            modifier.graphicsLayer {
                rotationZ =
                    rotation
            }
    ) {
        val orbitRadius =
            size.minDimension *
                    0.28f

        val detailAmplitude =
            size.minDimension *
                    0.085f

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
            22

        val trailSpan =
            0.34f

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
        0..160
        ) {
            val angle =
                (
                        index /
                                160f
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
                        0.20f
                ),
            style =
                Stroke(
                    width =
                        1.15.dp
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
                        0.56f
                    )

            val alpha =
                0.10f +
                        fade *
                        0.90f

            val particleRadius =
                (
                        0.35f +
                                fade *
                                1.05f
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