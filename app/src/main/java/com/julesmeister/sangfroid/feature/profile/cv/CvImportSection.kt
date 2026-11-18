package com.julesmeister.sangfroid.feature.profile.cv

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.shape.SangfroidRadius

@Composable
fun CvImportSection(
    status: CvImportStatus,
    onChoosePdf: () -> Unit,
    onReplacePdf: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier =
            modifier.fillMaxWidth(),
        shape =
            MaterialTheme
                .shapes
                .extraLarge,
        color =
            MaterialTheme
                .colorScheme
                .surface,
        contentColor =
            MaterialTheme
                .colorScheme
                .onSurface
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
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        SangfroidSpacing.Xxs
                    )
            ) {
                Text(
                    text =
                        "Import your CV",
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "Sangfroid reads the PDF locally and suggests skills for personalized questions.",
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

            when (status) {
                CvImportStatus.Idle -> {
                    CvImportAction(
                        label =
                            "Choose CV PDF",
                        emphasized =
                            true,
                        onClick =
                            onChoosePdf
                    )

                    Text(
                        text =
                            "PDF only · Maximum 10 MB · Text-based CVs work best",
                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

                is CvImportStatus.Reading -> {
                    CvBusyRow(
                        title =
                            "Reading ${status.document.displayName}",
                        message =
                            "Extracting text from the PDF…"
                    )
                }

                is CvImportStatus.ExtractingSkills -> {
                    CvBusyRow(
                        title =
                            "Finding relevant skills",
                        message =
                            "Matching the CV against Sangfroid's interview skill catalogue…"
                    )
                }

                is CvImportStatus.Review -> {
                    Text(
                        text =
                            "${status.extraction.skills.size} possible skills found",
                        style =
                            MaterialTheme
                                .typography
                                .bodyMedium,
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }

                is CvImportStatus.Applying -> {
                    CvBusyRow(
                        title =
                            "Applying CV skills",
                        message =
                            "Updating your interview profile…"
                    )
                }

                is CvImportStatus.Imported -> {
                    StatusPanel(
                        title =
                            status.fileName,
                        message =
                            "${status.importedSkillCount} CV skills are shaping your recommendations.",
                        isError =
                            false
                    )

                    CvImportAction(
                        label =
                            "Replace CV",
                        emphasized =
                            false,
                        onClick =
                            onReplacePdf
                    )
                }

                is CvImportStatus.Error -> {
                    StatusPanel(
                        title =
                            status.fileName
                                ?: "CV import failed",
                        message =
                            status.message,
                        isError =
                            true
                    )

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                SangfroidSpacing.Sm
                            )
                    ) {
                        CvImportAction(
                            label =
                                "Try again",
                            emphasized =
                                true,
                            onClick =
                                onRetry,
                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )

                        CvImportAction(
                            label =
                                "Choose another",
                            emphasized =
                                false,
                            onClick =
                                onChoosePdf,
                            modifier =
                                Modifier.weight(
                                    1f
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CvBusyRow(
    title: String,
    message: String
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(
                SangfroidSpacing.Md
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier =
                Modifier.padding(
                    SangfroidSpacing.Xs
                ),
            strokeWidth =
                2.dp
        )

        Column(
            modifier =
                Modifier.weight(
                    1f
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    SangfroidSpacing.Xxs
                )
        ) {
            Text(
                text =
                    title,
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    message,
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
    }
}

@Composable
private fun StatusPanel(
    title: String,
    message: String,
    isError: Boolean
) {
    val containerColor =
        if (isError) {
            MaterialTheme
                .colorScheme
                .errorContainer
        } else {
            MaterialTheme
                .colorScheme
                .surfaceVariant
        }

    val contentColor =
        if (isError) {
            MaterialTheme
                .colorScheme
                .onErrorContainer
        } else {
            MaterialTheme
                .colorScheme
                .onSurfaceVariant
        }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    color =
                        containerColor,
                    shape =
                        MaterialTheme
                            .shapes
                            .large
                )
                .padding(
                    SangfroidSpacing.Md
                ),
        verticalArrangement =
            Arrangement.spacedBy(
                SangfroidSpacing.Xxs
            )
    ) {
        Text(
            text =
                title,
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            fontWeight =
                FontWeight.Bold,
            color =
                contentColor
        )

        Text(
            text =
                message,
            style =
                MaterialTheme
                    .typography
                    .bodySmall,
            color =
                contentColor
        )
    }
}

@Composable
private fun CvImportAction(
    label: String,
    emphasized: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val containerColor =
        if (emphasized) {
            MaterialTheme
                .colorScheme
                .onSurface
        } else {
            MaterialTheme
                .colorScheme
                .surfaceVariant
        }

    val contentColor =
        if (emphasized) {
            MaterialTheme
                .colorScheme
                .surface
        } else {
            MaterialTheme
                .colorScheme
                .onSurfaceVariant
        }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color =
                        containerColor,
                    shape =
                        RoundedCornerShape(
                            SangfroidRadius.Pill
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
                        label,
                    onClick =
                        onClick
                )
                .padding(
                    horizontal =
                        SangfroidSpacing.Md,
                    vertical =
                        SangfroidSpacing.Sm
                ),
        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text =
                label,
            style =
                MaterialTheme
                    .typography
                    .labelLarge,
            fontWeight =
                FontWeight.Bold,
            color =
                contentColor
        )
    }
}