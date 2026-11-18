package com.julesmeister.sangfroid.feature.feed.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private object RestoreTokens {

    val HorizontalPadding =
        8.dp

    val VerticalPadding =
        7.dp

    val IconSize =
        16.dp

    val Gap =
        5.dp

    val Radius =
        20.dp

    const val SpinMillis =
        420
}

@Composable
internal fun HeaderRestoreChip(
    skippedCount: Int,
    compact: Boolean,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayedCount by
    remember {
        mutableIntStateOf(
            skippedCount
        )
    }

    LaunchedEffect(
        skippedCount
    ) {
        if (skippedCount > 0) {
            displayedCount =
                skippedCount
        }
    }

    val spin =
        remember {
            Animatable(
                0f
            )
        }

    val scope =
        rememberCoroutineScope()

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
                0.92f
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
            "restore-dock-press"
    )

    val contentColor =
        MaterialTheme
            .colorScheme
            .onSurface

    val secondaryContentColor =
        MaterialTheme
            .colorScheme
            .onSurfaceVariant

    val label =
        if (displayedCount == 1) {
            "Bring back 1 skipped question"
        } else {
            "Bring back $displayedCount skipped questions"
        }

    Row(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX =
                        scale

                    scaleY =
                        scale

                    alpha =
                        if (pressed) {
                            0.72f
                        } else {
                            1f
                        }
                }
                .clip(
                    RoundedCornerShape(
                        RestoreTokens.Radius
                    )
                )
                .clickable(
                    interactionSource =
                        interactionSource,
                    indication =
                        null,
                    role =
                        Role.Button,
                    onClickLabel =
                        label
                ) {
                    scope.launch {
                        spin.snapTo(
                            0f
                        )

                        spin.animateTo(
                            targetValue =
                                360f,
                            animationSpec =
                                tween(
                                    durationMillis =
                                        RestoreTokens
                                            .SpinMillis,
                                    easing =
                                        FastOutSlowInEasing
                                )
                        )
                    }

                    onRestore()
                }
                .clearAndSetSemantics {
                    contentDescription =
                        label

                    role =
                        Role.Button
                }
                .padding(
                    horizontal =
                        RestoreTokens
                            .HorizontalPadding,
                    vertical =
                        RestoreTokens
                            .VerticalPadding
                ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                RestoreTokens.Gap
            )
    ) {
        Canvas(
            modifier =
                Modifier
                    .size(
                        RestoreTokens
                            .IconSize
                    )
                    .graphicsLayer {
                        rotationZ =
                            -spin.value
                    }
        ) {
            drawHeaderRefreshGlyph(
                color =
                    contentColor,
                strokeWidth =
                    2.dp.toPx()
            )
        }

        AnimatedContent(
            targetState =
                displayedCount,
            label =
                "restore-dock-count"
        ) { count ->
            Text(
                text =
                    count.toString(),
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                fontWeight =
                    FontWeight.ExtraBold,
                color =
                    contentColor
            )
        }

        if (!compact) {
            Text(
                text =
                    "skipped",
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                fontWeight =
                    FontWeight.Medium,
                color =
                    secondaryContentColor,
                maxLines =
                    1
            )
        }
    }
}