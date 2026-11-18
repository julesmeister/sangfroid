package com.julesmeister.sangfroid.feature.feedback.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.julesmeister.sangfroid.core.designsystem.component.SangfroidPillButton
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing

@Composable
fun QueuedEvaluationState(
    canRetry: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier.fillMaxSize(),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        FeedbackStatePanel(
            title =
                "Feedback queued",
            message =
                "Your answer is safely stored on this device. Reconnect and try the evaluation again.",
            showProgress = false
        )

        Spacer(
            modifier =
                Modifier.height(
                    SangfroidSpacing.Xl
                )
        )

        SangfroidPillButton(
            text =
                "Try feedback now",
            onClick =
                onRetry,
            enabled =
                canRetry
        )
    }
}

@Composable
fun FeedbackErrorState(
    message: String,
    canRetry: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier.fillMaxSize(),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        FeedbackStatePanel(
            title =
                "Feedback couldn't be generated",
            message =
                message,
            showProgress = false
        )

        Spacer(
            modifier =
                Modifier.height(
                    SangfroidSpacing.Xl
                )
        )

        SangfroidPillButton(
            text =
                "Try evaluation again",
            onClick =
                onRetry,
            enabled =
                canRetry
        )
    }
}