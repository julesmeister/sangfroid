package com.julesmeister.sangfroid.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.feature.profile.components.ProfileButton
import com.julesmeister.sangfroid.feature.profile.components.ProfileButtonVariant
import com.julesmeister.sangfroid.feature.profile.components.ProfileIcon
import com.julesmeister.sangfroid.feature.profile.components.ProfileIconView
import com.julesmeister.sangfroid.feature.profile.components.StaticSkillChip

@Composable
internal fun CvSection(
    model: CvSectionModel,
    padding: Dp,
    onChooseCv: () -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    SectionCard(
        title =
            "Quick start from your CV",
        caption =
            model.caption,
        padding =
            padding
    ) {
        model.panel?.let { panel ->
            CvPanel(
                panel =
                    panel,
                onApply =
                    onApply,
                onDismiss =
                    onDismiss
            )
        }

        ProfileButton(
            label =
                model.chooseButton.label,
            onClick =
                onChooseCv,
            variant =
                if (
                    model.chooseButton.primary
                ) {
                    ProfileButtonVariant.Primary
                } else {
                    ProfileButtonVariant.Secondary
                },
            enabled =
                model.chooseButton.enabled,
            loading =
                model.chooseButton.loading,
            icon =
                ProfileIcon.Document
        )
    }
}

@Composable
private fun CvPanel(
    panel: CvPanelModel,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    when (panel) {
        is CvPanelModel.Working -> {
            WorkingPanel(
                model =
                    panel
            )
        }

        is CvPanelModel.Message -> {
            MessagePanel(
                model =
                    panel,
                onDismiss =
                    onDismiss
            )
        }

        is CvPanelModel.Result -> {
            ResultPanel(
                model =
                    panel,
                onApply =
                    onApply
            )
        }
    }
}

@Composable
private fun WorkingPanel(
    model: CvPanelModel.Working
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        18.dp
                    )
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
                        .copy(
                            alpha =
                                0.55f
                        )
                )
                .padding(
                    14.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                12.dp
            )
    ) {
        CircularProgressIndicator(
            modifier =
                Modifier.size(
                    22.dp
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
                    2.dp
                )
        ) {
            Text(
                text =
                    model.title,
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    model.detail,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultPanel(
    model: CvPanelModel.Result,
    onApply: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        18.dp
                    )
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
                        .copy(
                            alpha =
                                0.45f
                        )
                )
                .padding(
                    14.dp
                ),
        verticalArrangement =
            Arrangement.spacedBy(
                12.dp
            )
    ) {
        ResultHeader(
            model =
                model
        )

        if (
            model.summary
                .isNotBlank()
        ) {
            Text(
                text =
                    model.summary,
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                maxLines =
                    4,
                overflow =
                    TextOverflow.Ellipsis
            )
        }

        AnalysisStatusPanel(
            analysis =
                model.analysis
        )

        if (
            model.suggestedSkills
                .isNotEmpty()
        ) {
            FieldLabel(
                "Skills ready for your profile"
            )

            FlowRow(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        6.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        6.dp
                    )
            ) {
                model.suggestedSkills
                    .forEach { skill ->
                        StaticSkillChip(
                            label =
                                skill.label,
                            alreadyInProfile =
                                skill.alreadyAdded
                        )
                    }
            }
        } else {
            Text(
                text =
                    "No additional profile skills were found. " +
                            "Projects and other CV evidence will still " +
                            "appear in the dedicated sections below.",
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

        model.appliedNote
            ?.let { note ->
                AppliedNote(
                    text =
                        note
                )
            }

        ProfileButton(
            label =
                model.applyLabel,
            onClick =
                onApply,
            enabled =
                model.applyEnabled,
            icon =
                if (
                    model.applyDone
                ) {
                    ProfileIcon.Check
                } else {
                    ProfileIcon.Plus
                }
        )
    }
}

@Composable
private fun ResultHeader(
    model: CvPanelModel.Result
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.Top,
        horizontalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {
        Column(
            modifier =
                Modifier.weight(
                    1f
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    3.dp
                )
        ) {
            Text(
                text =
                    model.role,
                style =
                    MaterialTheme
                        .typography
                        .titleSmall,
                fontWeight =
                    FontWeight.Bold,
                maxLines =
                    2,
                overflow =
                    TextOverflow.Ellipsis
            )

            model.fileName
                ?.takeIf(
                    String::isNotBlank
                )
                ?.let { fileName ->
                    Text(
                        text =
                            fileName,
                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,
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

            Text(
                text =
                    model.analysis.modelLabel,
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary,
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }

        model.level
            ?.takeIf(
                String::isNotBlank
            )
            ?.let { level ->
                Text(
                    text =
                        level,
                    modifier =
                        Modifier
                            .clip(
                                RoundedCornerShape(
                                    100.dp
                                )
                            )
                            .background(
                                MaterialTheme
                                    .colorScheme
                                    .onSurface
                            )
                            .padding(
                                horizontal =
                                    10.dp,
                                vertical =
                                    5.dp
                            ),
                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .surface,
                    maxLines =
                        1,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnalysisStatusPanel(
    analysis: CvAnalysisModel
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        14.dp
                    )
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .surface
                )
                .padding(
                    12.dp
                ),
        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    10.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            ProfileIconView(
                icon =
                    ProfileIcon.Check,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary,
                iconSize =
                    14.dp
            )

            Column(
                modifier =
                    Modifier.weight(
                        1f
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        2.dp
                    )
            ) {
                Text(
                    text =
                        "CV analysis ready",
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "The complete analysis is displayed in " +
                                "separate profile sections below.",
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

        FlowRow(
            horizontalArrangement =
                Arrangement.spacedBy(
                    6.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    6.dp
                )
        ) {
            AnalysisCountChip(
                label =
                    "Skills",
                count =
                    analysis.strengths.size
            )

            AnalysisCountChip(
                label =
                    "Projects",
                count =
                    analysis.projects.size
            )

            AnalysisCountChip(
                label =
                    "Credentials",
                count =
                    analysis.certifications.size
            )

            AnalysisCountChip(
                label =
                    "Achievements",
                count =
                    analysis.achievements.size
            )

            AnalysisCountChip(
                label =
                    "Work",
                count =
                    analysis.workHighlights.size
            )

            AnalysisCountChip(
                label =
                    "Signals",
                count =
                    analysis.experienceSignals.size
            )
        }
    }
}

@Composable
private fun AnalysisCountChip(
    label: String,
    count: Int
) {
    if (
        count <=
        0
    ) {
        return
    }

    Text(
        text =
            "$label $count",
        modifier =
            Modifier
                .clip(
                    RoundedCornerShape(
                        100.dp
                    )
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .secondaryContainer
                )
                .padding(
                    horizontal =
                        9.dp,
                    vertical =
                        5.dp
                ),
        style =
            MaterialTheme
                .typography
                .labelSmall,
        fontWeight =
            FontWeight.SemiBold,
        color =
            MaterialTheme
                .colorScheme
                .onSecondaryContainer
    )
}

@Composable
private fun AppliedNote(
    text: String
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        12.dp
                    )
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .surface
                )
                .padding(
                    horizontal =
                        12.dp,
                    vertical =
                        10.dp
                ),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {
        ProfileIconView(
            icon =
                ProfileIcon.Check,
            color =
                MaterialTheme
                    .colorScheme
                    .primary,
            iconSize =
                12.dp
        )

        Text(
            text =
                text,
            modifier =
                Modifier.weight(
                    1f
                ),
            style =
                MaterialTheme
                    .typography
                    .bodySmall,
            fontWeight =
                FontWeight.Medium
        )
    }
}

@Composable
private fun MessagePanel(
    model: CvPanelModel.Message,
    onDismiss: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        18.dp
                    )
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .errorContainer
                )
                .padding(
                    14.dp
                ),
        verticalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {
        Text(
            text =
                model.title,
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            fontWeight =
                FontWeight.Bold,
            color =
                MaterialTheme
                    .colorScheme
                    .onErrorContainer
        )

        Text(
            text =
                model.message,
            style =
                MaterialTheme
                    .typography
                    .bodySmall,
            color =
                MaterialTheme
                    .colorScheme
                    .onErrorContainer
        )

        ProfileButton(
            label =
                "Dismiss",
            onClick =
                onDismiss,
            variant =
                ProfileButtonVariant.Secondary,
            fillWidth =
                false
        )
    }
}