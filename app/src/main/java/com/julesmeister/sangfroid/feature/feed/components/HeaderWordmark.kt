package com.julesmeister.sangfroid.feature.feed.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidHeaderTokens
import kotlinx.coroutines.delay

@Composable
internal fun HeaderWordmark(
    text: String,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val density =
        LocalDensity.current

    val risePx =
        with(density) {
            SangfroidHeaderTokens
                .TitleLetterRise
                .toPx()
        }

    val style =
        if (compact) {
            MaterialTheme
                .typography
                .headlineSmall
        } else {
            MaterialTheme
                .typography
                .headlineMedium
        }

    Row(
        modifier =
            modifier
                .clearAndSetSemantics {
                    contentDescription =
                        text
                }
    ) {
        text.forEachIndexed {
                index,
                character ->

            key(index) {
                val entrance =
                    remember {
                        Animatable(
                            0f
                        )
                    }

                LaunchedEffect(Unit) {
                    delay(
                        index *
                                SangfroidHeaderTokens
                                    .TitleLetterStaggerMillis
                    )

                    entrance.animateTo(
                        targetValue =
                            1f,
                        animationSpec =
                            spring(
                                dampingRatio =
                                    Spring
                                        .DampingRatioMediumBouncy,
                                stiffness =
                                    Spring
                                        .StiffnessLow
                            )
                    )
                }

                Text(
                    text =
                        character.toString(),
                    style =
                        style,
                    fontWeight =
                        FontWeight.ExtraBold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onBackground,
                    maxLines =
                        1,
                    modifier =
                        Modifier.graphicsLayer {
                            val value =
                                entrance.value

                            alpha =
                                value.coerceIn(
                                    0f,
                                    1f
                                )

                            translationY =
                                (1f - value) *
                                        risePx

                            rotationZ =
                                (1f - value) *
                                        -SangfroidHeaderTokens
                                            .TitleLetterTilt
                        }
                )
            }
        }
    }
}