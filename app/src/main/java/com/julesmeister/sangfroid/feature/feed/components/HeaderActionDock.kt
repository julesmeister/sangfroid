package com.julesmeister.sangfroid.feature.feed.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidHeaderTokens
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidCyan
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidLime
import kotlinx.coroutines.delay

private object HeaderDockTokens {

    val Padding =
        3.dp

    val DividerWidth =
        1.dp

    val DividerHeight =
        18.dp

    val BorderWidth =
        1.25.dp

    val NormalActionSize =
        42.dp

    val CompactActionSize =
        38.dp

    val NormalGlyphSize =
        19.dp

    val CompactGlyphSize =
        17.dp

    const val ShimmerMillis =
        4_800
}

@Composable
internal fun HeaderActionDock(
    compact: Boolean,
    skippedCount: Int,
    canRestoreSkipped: Boolean,
    onRestoreSkipped: () -> Unit,
    onOpenSaved: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val entrance =
        remember {
            Animatable(
                0f
            )
        }

    LaunchedEffect(Unit) {
        delay(
            SangfroidHeaderTokens
                .ClusterEntranceDelayMillis
        )

        entrance.animateTo(
            targetValue =
                1f,
            animationSpec =
                spring(
                    dampingRatio =
                        Spring
                            .DampingRatioLowBouncy,
                    stiffness =
                        Spring
                            .StiffnessMediumLow
                )
        )
    }

    val shiftPx =
        with(
            LocalDensity.current
        ) {
            SangfroidSpacing
                .Lg
                .toPx()
        }

    val shimmerTransition =
        rememberInfiniteTransition(
            label =
                "header-dock-shimmer"
        )

    val shimmer by
    shimmerTransition.animateFloat(
        initialValue =
            0f,
        targetValue =
            1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis =
                            HeaderDockTokens
                                .ShimmerMillis,
                        easing =
                            LinearEasing
                    )
            ),
        label =
            "header-dock-shimmer-progress"
    )

    /*
     * Resolve MaterialTheme values in composable context.
     * drawBehind and Canvas receive plain colors.
     */
    val containerColor =
        MaterialTheme
            .colorScheme
            .surfaceVariant
            .copy(
                alpha =
                    0.72f
            )

    val dividerColor =
        MaterialTheme
            .colorScheme
            .onSurfaceVariant
            .copy(
                alpha =
                    0.16f
            )

    Row(
        modifier =
            modifier
                .graphicsLayer {
                    alpha =
                        entrance.value
                            .coerceIn(
                                0f,
                                1f
                            )

                    translationX =
                        (
                                1f -
                                        entrance.value
                                ) *
                                shiftPx
                }
                .clip(
                    RoundedCornerShape(
                        percent =
                            50
                    )
                )
                .background(
                    containerColor
                )
                .drawBehind {
                    val stroke =
                        HeaderDockTokens
                            .BorderWidth
                            .toPx()

                    val travel =
                        size.width *
                                2f *
                                shimmer

                    val borderBrush =
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    SangfroidLime,
                                    SangfroidCyan,
                                    SangfroidLime.copy(
                                        alpha =
                                            0.16f
                                    ),
                                    SangfroidLime
                                ),
                            start =
                                Offset(
                                    x =
                                        travel -
                                                size.width,
                                    y =
                                        0f
                                ),
                            end =
                                Offset(
                                    x =
                                        travel,
                                    y =
                                        size.height
                                ),
                            tileMode =
                                TileMode.Mirror
                        )

                    drawRoundRect(
                        brush =
                            borderBrush,
                        topLeft =
                            Offset(
                                x =
                                    stroke / 2f,
                                y =
                                    stroke / 2f
                            ),
                        size =
                            Size(
                                width =
                                    size.width -
                                            stroke,
                                height =
                                    size.height -
                                            stroke
                            ),
                        cornerRadius =
                            CornerRadius(
                                x =
                                    size.height / 2f,
                                y =
                                    size.height / 2f
                            ),
                        style =
                            Stroke(
                                width =
                                    stroke
                            )
                    )
                }
                .padding(
                    HeaderDockTokens.Padding
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible =
                canRestoreSkipped &&
                        skippedCount > 0,
            enter =
                fadeIn() +
                        expandHorizontally(
                            expandFrom =
                                Alignment.Start
                        ) +
                        scaleIn(
                            initialScale =
                                0.88f
                        ),
            exit =
                fadeOut() +
                        shrinkHorizontally(
                            shrinkTowards =
                                Alignment.Start
                        ) +
                        scaleOut(
                            targetScale =
                                0.88f
                        )
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                HeaderRestoreChip(
                    skippedCount =
                        skippedCount,
                    compact =
                        true,
                    onRestore =
                        onRestoreSkipped
                )

                HeaderDockDivider(
                    color =
                        dividerColor
                )
            }
        }

        HeaderDockAction(
            label =
                "Open saved questions",
            glyph =
                HeaderGlyph.SAVED,
            compact =
                compact,
            onClick =
                onOpenSaved
        )

        HeaderDockDivider(
            color =
                dividerColor
        )

        HeaderDockAction(
            label =
                "Open practice progress",
            glyph =
                HeaderGlyph.PROGRESS,
            compact =
                compact,
            onClick =
                onOpenProgress
        )

        HeaderDockDivider(
            color =
                dividerColor
        )

        HeaderDockAction(
            label =
                "Open interview profile",
            glyph =
                HeaderGlyph.PROFILE,
            compact =
                compact,
            onClick =
                onOpenProfile
        )
    }
}

@Composable
private fun HeaderDockDivider(
    color: Color
) {
    Box(
        modifier =
            Modifier
                .width(
                    HeaderDockTokens
                        .DividerWidth
                )
                .height(
                    HeaderDockTokens
                        .DividerHeight
                )
                .background(
                    color
                )
    )
}

@Composable
private fun HeaderDockAction(
    label: String,
    glyph: HeaderGlyph,
    compact: Boolean,
    onClick: () -> Unit
) {
    val actionSize: Dp =
        if (compact) {
            HeaderDockTokens
                .CompactActionSize
        } else {
            HeaderDockTokens
                .NormalActionSize
        }

    val glyphSize: Dp =
        if (compact) {
            HeaderDockTokens
                .CompactGlyphSize
        } else {
            HeaderDockTokens
                .NormalGlyphSize
        }

    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val pressed by
    interactionSource
        .collectIsPressedAsState()

    val pressScale by
    animateFloatAsState(
        targetValue =
            if (pressed) {
                0.91f
            } else {
                1f
            },
        animationSpec =
            spring(
                dampingRatio =
                    Spring
                        .DampingRatioMediumBouncy
            ),
        label =
            "$label-press"
    )

    /*
     * Again, resolve the MaterialTheme color before
     * entering Canvas.
     */
    val glyphColor =
        MaterialTheme
            .colorScheme
            .onSurface

    val pressedColor =
        SangfroidLime.copy(
            alpha =
                0.18f
        )

    Box(
        modifier =
            Modifier
                .size(
                    actionSize
                )
                .clip(
                    CircleShape
                )
                .background(
                    if (pressed) {
                        pressedColor
                    } else {
                        Color.Transparent
                    }
                )
                .clickable(
                    interactionSource =
                        interactionSource,
                    indication =
                        null,
                    role =
                        Role.Button,
                    onClickLabel =
                        label,
                    onClick =
                        onClick
                )
                .semantics {
                    contentDescription =
                        label
                },
        contentAlignment =
            Alignment.Center
    ) {
        Canvas(
            modifier =
                Modifier
                    .size(
                        glyphSize
                    )
                    .graphicsLayer {
                        scaleX =
                            pressScale

                        scaleY =
                            pressScale
                    }
        ) {
            drawHeaderGlyph(
                glyph =
                    glyph,
                color =
                    glyphColor,
                strokeWidth =
                    2.dp.toPx()
            )
        }
    }
}