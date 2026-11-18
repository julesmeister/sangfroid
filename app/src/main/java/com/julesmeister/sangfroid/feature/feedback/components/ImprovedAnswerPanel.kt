package com.julesmeister.sangfroid.feature.feedback.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.sangfroid.core.designsystem.component.SangfroidPillButton
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing

@Composable
fun ImprovedAnswerPanel(
    answer: String,
    revisionCount: Int,
    canSave: Boolean,
    isSaving: Boolean,
    isSaved: Boolean,
    errorMessage: String?,
    onSave: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier =
            modifier.fillMaxWidth(),
        color =
            MaterialTheme
                .colorScheme
                .tertiaryContainer,
        contentColor =
            MaterialTheme
                .colorScheme
                .onTertiaryContainer,
        shape =
            MaterialTheme
                .shapes
                .extraLarge
    ) {
        Column(
            modifier =
                Modifier.padding(
                    SangfroidSpacing.CardContent
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    SangfroidSpacing.Md
                )
        ) {
            Text(
                text =
                    "EXAMPLE REVISION",
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text = answer,
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge
            )

            SangfroidPillButton(
                text =
                    when {
                        isSaving -> {
                            "Saving..."
                        }

                        isSaved -> {
                            "Version saved"
                        }

                        else -> {
                            "Save version"
                        }
                    },
                onClick = onSave,
                enabled = canSave,
                modifier =
                    Modifier.fillMaxWidth()
            )

            if (revisionCount > 0) {
                Surface(
                    onClick =
                        onOpenHistory,
                    modifier =
                        Modifier.fillMaxWidth(),
                    color =
                        MaterialTheme
                            .colorScheme
                            .surface
                            .copy(
                                alpha = 0.55f
                            ),
                    contentColor =
                        MaterialTheme
                            .colorScheme
                            .onSurface,
                    shape =
                        MaterialTheme
                            .shapes
                            .large
                ) {
                    Row(
                        modifier =
                            Modifier.padding(
                                horizontal =
                                    SangfroidSpacing.Md,
                                vertical =
                                    SangfroidSpacing.Sm
                            ),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Text(
                            text =
                                "Previous versions",
                            style =
                                MaterialTheme
                                    .typography
                                    .titleSmall,
                            fontWeight =
                                FontWeight.SemiBold
                        )

                        Text(
                            text =
                                "$revisionCount  ›",
                            style =
                                MaterialTheme
                                    .typography
                                    .titleSmall,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }

            errorMessage
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )
                ?.let { message ->
                    Text(
                        text = message,
                        style =
                            MaterialTheme
                                .typography
                                .bodySmall,
                        color =
                            MaterialTheme
                                .colorScheme
                                .error
                    )
                }
        }
    }
}