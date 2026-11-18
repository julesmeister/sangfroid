package com.julesmeister.sangfroid.feature.feed.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidHeaderTokens
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidCyanDeep
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidLime
import kotlinx.coroutines.delay

private val TaglinePhrases =
    listOf(
        "Your interview practice deck",
        "Breathe. Then answer.",
        "Calm is a trained skill.",
        "One card at a time."
    )

@Composable
internal fun HeaderTagline(
    modifier: Modifier = Modifier
) {
    Row(
        modifier =
            modifier,
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                SangfroidSpacing.Xs
            )
    ) {
        BreathingOrb()

        RotatingTagline()
    }
}

@Composable
private fun BreathingOrb() {
    val transition =
        rememberInfiniteTransition(
            label =
                "header-breathing-orb"
        )

    val breath by
    transition.animateFloat(
        initialValue =
            0f,
        targetValue =
            1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis =
                            SangfroidHeaderTokens
                                .BreathingDurationMillis,
                        easing =
                            FastOutSlowInEasing
                    ),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label =
            "header-breath"
    )

    val orbColor =
        if (isSystemInDarkTheme()) {
            SangfroidLime
        } else {
            SangfroidCyanDeep
        }

    Canvas(
        modifier =
            Modifier.size(
                SangfroidHeaderTokens
                    .BreathingHaloSize
            )
    ) {
        val coreRadius =
            SangfroidHeaderTokens
                .BreathingOrbSize
                .toPx() / 2f

        val haloRadius =
            size.minDimension / 2f

        drawCircle(
            color =
                orbColor.copy(
                    alpha =
                        0.06f +
                                0.18f *
                                (1f - breath)
                ),
            radius =
                coreRadius +
                        (haloRadius - coreRadius) *
                        breath
        )

        drawCircle(
            color =
                orbColor,
            radius =
                coreRadius *
                        (0.85f + 0.15f * breath)
        )
    }
}

@Composable
private fun RotatingTagline() {
    var phraseIndex by
    remember {
        mutableIntStateOf(
            0
        )
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(
                SangfroidHeaderTokens
                    .SubtitleRotationMillis
            )

            phraseIndex =
                (phraseIndex + 1) %
                        TaglinePhrases.size
        }
    }

    AnimatedContent(
        targetState =
            phraseIndex,
        transitionSpec = {
            (
                    slideInVertically(
                        animationSpec =
                            tween(
                                SangfroidHeaderTokens
                                    .SubtitleEnterMillis
                            )
                    ) { height ->
                        height / 2
                    } +
                            fadeIn(
                                animationSpec =
                                    tween(
                                        SangfroidHeaderTokens
                                            .SubtitleEnterMillis
                                    )
                            )
                    ) togetherWith (
                    slideOutVertically(
                        animationSpec =
                            tween(
                                SangfroidHeaderTokens
                                    .SubtitleExitMillis
                            )
                    ) { height ->
                        -height / 2
                    } +
                            fadeOut(
                                animationSpec =
                                    tween(
                                        SangfroidHeaderTokens
                                            .SubtitleExitMillis
                                    )
                            )
                    )
        },
        label =
            "header-tagline"
    ) { index ->
        Text(
            text =
                TaglinePhrases[index],
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
            maxLines =
                1,
            overflow =
                TextOverflow.Ellipsis
        )
    }
}