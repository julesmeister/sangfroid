package com.julesmeister.sangfroid.feature.practice.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.component.SangfroidPillButton
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidCharcoal
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidLime
import com.julesmeister.sangfroid.feature.feedback.components.RoseOrbitLoader

@Composable
fun PracticeLoadingState() {
    Box(
        modifier =
            Modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally,
            verticalArrangement =
                Arrangement.spacedBy(
                    SangfroidSpacing.Lg
                )
        ) {
            RoseOrbitLoader()

            Text(
                text =
                    "Preparing your practice space",
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground
            )

            Text(
                text =
                    "Loading your question and saved draft.",
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

@Composable
fun PracticeErrorPanel(
    title: String,
    message: String
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        color =
            MaterialTheme
                .colorScheme
                .errorContainer,
        contentColor =
            MaterialTheme
                .colorScheme
                .onErrorContainer,
        shape =
            MaterialTheme
                .shapes
                .extraLarge
    ) {
        Row(
            modifier =
                Modifier.padding(
                    SangfroidSpacing.Lg
                ),
            horizontalArrangement =
                Arrangement.spacedBy(
                    SangfroidSpacing.Md
                ),
            verticalAlignment =
                Alignment.Top
        ) {
            Surface(
                modifier =
                    Modifier.size(36.dp),
                color =
                    MaterialTheme
                        .colorScheme
                        .error,
                contentColor =
                    MaterialTheme
                        .colorScheme
                        .onError,
                shape = CircleShape
            ) {
                Box(
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text = "!",
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium,
                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }
            }

            Column(
                modifier =
                    Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(
                        SangfroidSpacing.Xxs
                    )
            ) {
                Text(
                    text = title,
                    style =
                        MaterialTheme
                            .typography
                            .titleSmall,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text = message,
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium
                )
            }
        }
    }
}

@Composable
fun PracticeSubmittedPanel() {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        color =
            SangfroidLime.copy(
                alpha = 0.20f
            ),
        contentColor =
            MaterialTheme
                .colorScheme
                .onSurface,
        shape =
            MaterialTheme
                .shapes
                .extraLarge
    ) {
        Row(
            modifier =
                Modifier.padding(
                    SangfroidSpacing.Lg
                ),
            horizontalArrangement =
                Arrangement.spacedBy(
                    SangfroidSpacing.Md
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Surface(
                modifier =
                    Modifier.size(42.dp),
                color = SangfroidLime,
                contentColor =
                    SangfroidCharcoal,
                shape = CircleShape
            ) {
                Canvas(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(11.dp)
                ) {
                    drawCheckGlyph(
                        color =
                            SangfroidCharcoal,
                        strokeWidth =
                            2.5.dp.toPx()
                    )
                }
            }

            Column(
                modifier =
                    Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(
                        SangfroidSpacing.Xxs
                    )
            ) {
                Text(
                    text =
                        "Answer submitted",
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Text(
                    text =
                        "Your attempt is safely stored and ready for Qwen feedback.",
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

@Composable
fun PracticeMissingQuestion(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    SangfroidSpacing
                        .CardContent
                ),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        Surface(
            modifier =
                Modifier.size(72.dp),
            color =
                MaterialTheme
                    .colorScheme
                    .surfaceVariant,
            contentColor =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
            shape = CircleShape
        ) {
            Box(
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text = "?",
                    style =
                        MaterialTheme
                            .typography
                            .headlineMedium,
                    fontWeight =
                        FontWeight.ExtraBold
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(
                    SangfroidSpacing.Lg
                )
        )

        Text(
            text =
                "Question unavailable",
            style =
                MaterialTheme
                    .typography
                    .headlineSmall,
            fontWeight =
                FontWeight.ExtraBold,
            color =
                MaterialTheme
                    .colorScheme
                    .onBackground
        )

        Spacer(
            modifier =
                Modifier.height(
                    SangfroidSpacing.Xs
                )
        )

        Text(
            text =
                "The selected question could not be loaded.",
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Spacer(
            modifier =
                Modifier.height(
                    SangfroidSpacing.Xl
                )
        )

        SangfroidPillButton(
            text =
                "Return to questions",
            onClick =
                onNavigateBack
        )
    }
}