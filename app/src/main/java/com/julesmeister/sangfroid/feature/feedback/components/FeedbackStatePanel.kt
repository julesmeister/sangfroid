package com.julesmeister.sangfroid.feature.feedback.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing

@Composable
fun FeedbackStatePanel(
    title: String,
    message: String,
    showProgress: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier,
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        if (showProgress) {
            RoseOrbitLoader()

            Spacer(
                modifier =
                    Modifier.height(
                        SangfroidSpacing.Lg
                    )
            )
        }

        Surface(
            modifier =
                Modifier.fillMaxWidth(),
            color =
                MaterialTheme
                    .colorScheme
                    .surface,
            contentColor =
                MaterialTheme
                    .colorScheme
                    .onSurface,
            shape =
                MaterialTheme
                    .shapes
                    .extraLarge
        ) {
            Column(
                modifier =
                    Modifier.padding(
                        SangfroidSpacing
                            .CardContent
                    ),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Text(
                    text =
                        title,
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            SangfroidSpacing.Xs
                        )
                )

                Text(
                    text =
                        message,
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}