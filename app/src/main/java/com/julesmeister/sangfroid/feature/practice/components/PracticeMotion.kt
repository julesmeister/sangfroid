package com.julesmeister.sangfroid.feature.practice.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun StaggeredPracticeReveal(
    delayMillis: Long,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val progress =
        remember {
            Animatable(0f)
        }

    val density =
        LocalDensity.current

    val offsetPx =
        with(density) {
            22.dp.toPx()
        }

    LaunchedEffect(
        delayMillis
    ) {
        delay(delayMillis)

        progress.animateTo(
            targetValue = 1f,
            animationSpec =
                spring(
                    dampingRatio =
                        Spring.DampingRatioMediumBouncy,
                    stiffness =
                        Spring.StiffnessLow
                )
        )
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .graphicsLayer {
                    val value =
                        progress.value

                    alpha =
                        value.coerceIn(
                            0f,
                            1f
                        )

                    translationY =
                        (1f - value) *
                                offsetPx
                }
    ) {
        content()
    }
}