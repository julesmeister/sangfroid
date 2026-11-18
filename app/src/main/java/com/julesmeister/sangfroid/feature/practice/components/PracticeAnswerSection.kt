package com.julesmeister.sangfroid.feature.practice.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.theme.FeedbackError

@Composable
fun PracticeAnswerSectionHeader(
    wordCount: Int,
    isRecording: Boolean
) {
    Row(
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                SangfroidSpacing.Md
            )
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(
                    SangfroidSpacing.Xxs
                )
        ) {
            Text(
                text = "YOUR ANSWER",
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                fontWeight =
                    FontWeight.ExtraBold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground
            )

            Text(
                text =
                    if (isRecording) {
                        "Speak naturally. You can edit the transcript afterward."
                    } else {
                        "Use a clear situation, action, and result."
                    },
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }

        PracticeWordCountBadge(
            wordCount = wordCount,
            isRecording = isRecording
        )
    }
}

@Composable
private fun PracticeWordCountBadge(
    wordCount: Int,
    isRecording: Boolean
) {
    val containerColor by
    animateColorAsState(
        targetValue =
            if (isRecording) {
                FeedbackError.copy(
                    alpha = 0.16f
                )
            } else {
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
            },
        animationSpec =
            tween(260),
        label =
            "word-count-container"
    )

    val contentColor by
    animateColorAsState(
        targetValue =
            if (isRecording) {
                MaterialTheme
                    .colorScheme
                    .error
            } else {
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
            },
        animationSpec =
            tween(260),
        label =
            "word-count-content"
    )

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape =
            RoundedCornerShape(50)
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal =
                        SangfroidSpacing.Sm,
                    vertical =
                        SangfroidSpacing.Xs
                ),
            horizontalArrangement =
                Arrangement.spacedBy(
                    SangfroidSpacing.Xs
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            if (isRecording) {
                androidx.compose.foundation.layout.Box(
                    modifier =
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                FeedbackError
                            )
                )
            }

            AnimatedContent(
                targetState = wordCount,
                transitionSpec = {
                    (
                            slideInVertically {
                                    height ->
                                height
                            } +
                                    fadeIn()
                            ) togetherWith (
                            slideOutVertically {
                                    height ->
                                -height
                            } +
                                    fadeOut()
                            )
                },
                label =
                    "word-count-value"
            ) { count ->
                Text(
                    text =
                        "$count ${if (count == 1) "word" else "words"}",
                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
    }
}