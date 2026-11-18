package com.julesmeister.sangfroid.feature.practice.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.feature.practice.PracticeUiState
import com.julesmeister.sangfroid.feature.practice.speech.SpeechActionDock
import com.julesmeister.sangfroid.feature.practice.speech.SpeechUiState
import kotlinx.coroutines.delay

@Composable
fun PracticeBottomDock(
    uiState: PracticeUiState,
    speechUiState: SpeechUiState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    onSubmit: () -> Unit,
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
            22.dp.toPx()
        }

    LaunchedEffect(Unit) {
        delay(320L)

        entrance.animateTo(
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

    AnimatedVisibility(
        visible =
            !uiState.isViewingHistoricalAttempt,
        enter =
            fadeIn() +
                    expandVertically(),
        exit =
            fadeOut() +
                    shrinkVertically()
    ) {
        SpeechActionDock(
            state = speechUiState,
            submitEnabled =
                uiState.canSubmit,
            isSubmitting =
                uiState.isSubmitting,
            onStartRecording =
                onStartRecording,
            onStopRecording =
                onStopRecording,
            onCancelRecording =
                onCancelRecording,
            onSubmit = onSubmit,
            modifier =
                modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha =
                            entrance.value
                                .coerceIn(
                                    0f,
                                    1f
                                )

                        translationY =
                            (1f - entrance.value) *
                                    offsetPx
                    }
        )
    }
}