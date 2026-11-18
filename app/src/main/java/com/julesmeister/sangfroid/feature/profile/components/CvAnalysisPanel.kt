// app/src/main/java/com/julesmeister/sangfroid/feature/profile/components/CvAnalysisPanel.kt

package com.julesmeister.sangfroid.feature.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.domain.cv.CvProfile
import com.julesmeister.sangfroid.feature.profile.CvAchievementUiModel
import com.julesmeister.sangfroid.feature.profile.CvAnalysisUiModel
import com.julesmeister.sangfroid.feature.profile.CvCertificationUiModel
import com.julesmeister.sangfroid.feature.profile.CvProjectUiModel
import com.julesmeister.sangfroid.feature.profile.CvSkillUiModel
import com.julesmeister.sangfroid.feature.profile.CvWorkHighlightUiModel

@Composable
fun CvAnalysisPanel(
    profile: CvProfile,
    modifier: Modifier = Modifier
) {
    val model =
        remember(
            profile
        ) {
            CvAnalysisUiModel.from(
                profile
            )
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(
                16.dp
            )
    ) {
        AnalysisHeader(
            model =
                model
        )

        SummaryCard(
            model =
                model
        )

        if (
            model.suggestedRoles.isNotEmpty() ||
            model.experienceLevel != null
        ) {
            CandidateDirectionCard(
                roles =
                    model.suggestedRoles,
                experienceLevel =
                    model.experienceLevel
            )
        }

        if (model.skills.isNotEmpty()) {
            AnalysisSection(
                eyebrow =
                    "PROFILE SIGNALS",
                title =
                    "Skills",
                description =
                    "Capabilities Qwen found directly in the CV."
            ) {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {
                    model.skills.forEach { skill ->
                        SkillCard(
                            skill =
                                skill
                        )
                    }
                }
            }
        }

        if (model.certifications.isNotEmpty()) {
            AnalysisSection(
                eyebrow =
                    "CREDENTIALS",
                title =
                    "Certifications",
                description =
                    "Credentials are preserved separately from practical work evidence."
            ) {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {
                    model.certifications.forEach { certification ->
                        CertificationCard(
                            certification =
                                certification
                        )
                    }
                }
            }
        }

        if (model.projects.isNotEmpty()) {
            AnalysisSection(
                eyebrow =
                    "EVIDENCE",
                title =
                    "Projects",
                description =
                    "These can anchor project-specific interview questions."
            ) {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(
                            12.dp
                        )
                ) {
                    model.projects.forEach { project ->
                        ProjectCard(
                            project =
                                project
                        )
                    }
                }
            }
        }

        if (model.achievements.isNotEmpty()) {
            AnalysisSection(
                eyebrow =
                    "RESULTS",
                title =
                    "Achievements",
                description =
                    "Outcomes and accomplishments that can support evidence-based answers."
            ) {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {
                    model.achievements.forEach { achievement ->
                        AchievementCard(
                            achievement =
                                achievement
                        )
                    }
                }
            }
        }

        if (model.workHighlights.isNotEmpty()) {
            AnalysisSection(
                eyebrow =
                    "EXPERIENCE",
                title =
                    "Work highlights",
                description =
                    "Responsibilities and contributions extracted from professional experience."
            ) {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {
                    model.workHighlights.forEach { highlight ->
                        WorkHighlightCard(
                            highlight =
                                highlight
                        )
                    }
                }
            }
        }

        if (model.experienceSignals.isNotEmpty()) {
            AnalysisSection(
                eyebrow =
                    "INTERVIEW CONTEXT",
                title =
                    "Experience signals",
                description =
                    "Other CV evidence Sangfroid can use when personalizing practice."
            ) {
                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),
                    color =
                        MaterialTheme
                            .colorScheme
                            .surfaceContainerLow,
                    shape =
                        RoundedCornerShape(
                            20.dp
                        )
                ) {
                    Column(
                        modifier =
                            Modifier.padding(
                                16.dp
                            ),
                        verticalArrangement =
                            Arrangement.spacedBy(
                                10.dp
                            )
                    ) {
                        model.experienceSignals.forEach { signal ->
                            EvidenceLine(
                                text =
                                    signal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisHeader(
    model: CvAnalysisUiModel
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically
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
                    "CV analysis",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Text(
                text =
                    model.modelLabel,
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }

        Surface(
            color =
                MaterialTheme
                    .colorScheme
                    .primaryContainer,
            contentColor =
                MaterialTheme
                    .colorScheme
                    .onPrimaryContainer,
            shape =
                CircleShape
        ) {
            Text(
                text =
                    "${model.evidenceCount} evidence",
                modifier =
                    Modifier.padding(
                        horizontal =
                            12.dp,
                        vertical =
                            7.dp
                    ),
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

@Composable
private fun SummaryCard(
    model: CvAnalysisUiModel
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        color =
            MaterialTheme
                .colorScheme
                .primaryContainer
                .copy(
                    alpha =
                        0.58f
                ),
        contentColor =
            MaterialTheme
                .colorScheme
                .onPrimaryContainer,
        shape =
            RoundedCornerShape(
                24.dp
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    18.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {
            Text(
                text =
                    "PROFILE SUMMARY",
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                fontWeight =
                    FontWeight.ExtraBold,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            Text(
                text =
                    model.summary,
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,
                fontWeight =
                    FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CandidateDirectionCard(
    roles: List<String>,
    experienceLevel: String?
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        color =
            MaterialTheme
                .colorScheme
                .surfaceContainerLow,
        shape =
            RoundedCornerShape(
                22.dp
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    16.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {
            experienceLevel?.let { level ->
                MetadataRow(
                    label =
                        "Suggested level",
                    value =
                        level
                )
            }

            if (roles.isNotEmpty()) {
                Text(
                    text =
                        "Suggested roles",
                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {
                    roles.forEach { role ->
                        Tag(
                            text =
                                role
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisSection(
    eyebrow: String,
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier =
            Modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {
        Text(
            text =
                eyebrow,
            style =
                MaterialTheme
                    .typography
                    .labelSmall,
            fontWeight =
                FontWeight.ExtraBold,
            color =
                MaterialTheme
                    .colorScheme
                    .primary
        )

        Text(
            text =
                title,
            style =
                MaterialTheme
                    .typography
                    .titleMedium,
            fontWeight =
                FontWeight.ExtraBold
        )

        Text(
            text =
                description,
            style =
                MaterialTheme
                    .typography
                    .bodySmall,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        content()
    }
}

@Composable
private fun SkillCard(
    skill: CvSkillUiModel
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        color =
            MaterialTheme
                .colorScheme
                .surfaceContainerLow,
        shape =
            RoundedCornerShape(
                18.dp
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    14.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {
                    Text(
                        text =
                            skill.name,
                        style =
                            MaterialTheme
                                .typography
                                .titleSmall,
                        fontWeight =
                            FontWeight.Bold
                    )

                    Text(
                        text =
                            skill.typeLabel,
                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,
                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }

                Text(
                    text =
                        "${skill.confidencePercent}%",
                    style =
                        MaterialTheme
                            .typography
                            .labelMedium,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }

            LinearProgressIndicator(
                progress = {
                    skill.confidence
                        .toFloat()
                        .coerceIn(
                            0f,
                            1f
                        )
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            4.dp
                        )
                        .clip(
                            CircleShape
                        )
            )

            if (skill.evidence.isNotBlank()) {
                Text(
                    text =
                        skill.evidence,
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
}

@Composable
private fun CertificationCard(
    certification: CvCertificationUiModel
) {
    EvidenceCard(
        title =
            certification.title,
        eyebrow =
            certification.status
    ) {
        certification.subtitle?.let { subtitle ->
            Text(
                text =
                    subtitle,
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

        EvidenceText(
            text =
                certification.evidence
        )

        TagList(
            values =
                certification.relatedSkills
        )
    }
}

@Composable
private fun ProjectCard(
    project: CvProjectUiModel
) {
    EvidenceCard(
        title =
            project.title,
        eyebrow =
            "PROJECT"
    ) {
        project.candidateRole?.let { role ->
            MetadataRow(
                label =
                    "Role",
                value =
                    role
            )
        }

        project.context?.let { context ->
            EvidenceText(
                text =
                    context
            )
        }

        project.objective?.let { objective ->
            DetailBlock(
                label =
                    "Objective",
                values =
                    listOf(
                        objective
                    )
            )
        }

        DetailBlock(
            label =
                "Actions",
            values =
                project.actions
        )

        DetailBlock(
            label =
                "Methods",
            values =
                project.methods
        )

        DetailBlock(
            label =
                "Tools",
            values =
                project.tools
        )

        DetailBlock(
            label =
                "Outcomes",
            values =
                project.outcomes
        )

        DetailBlock(
            label =
                "Metrics",
            values =
                project.metrics
        )

        TagList(
            values =
                project.relatedSkills
        )
    }
}

@Composable
private fun AchievementCard(
    achievement: CvAchievementUiModel
) {
    EvidenceCard(
        title =
            achievement.title,
        eyebrow =
            "ACHIEVEMENT"
    ) {
        achievement.context?.let { value ->
            EvidenceText(
                text =
                    value
            )
        }

        achievement.action?.let { value ->
            MetadataRow(
                label =
                    "Action",
                value =
                    value
            )
        }

        achievement.result?.let { value ->
            MetadataRow(
                label =
                    "Result",
                value =
                    value
            )
        }

        achievement.metric?.let { value ->
            MetadataRow(
                label =
                    "Metric",
                value =
                    value
            )
        }

        TagList(
            values =
                achievement.relatedSkills
        )
    }
}

@Composable
private fun WorkHighlightCard(
    highlight: CvWorkHighlightUiModel
) {
    EvidenceCard(
        title =
            highlight.title,
        eyebrow =
            "WORK HIGHLIGHT"
    ) {
        highlight.candidateRole?.let { value ->
            MetadataRow(
                label =
                    "Role",
                value =
                    value
            )
        }

        highlight.organizationContext?.let { value ->
            EvidenceText(
                text =
                    value
            )
        }

        highlight.responsibility?.let { value ->
            MetadataRow(
                label =
                    "Responsibility",
                value =
                    value
            )
        }

        highlight.action?.let { value ->
            MetadataRow(
                label =
                    "Action",
                value =
                    value
            )
        }

        highlight.outcome?.let { value ->
            MetadataRow(
                label =
                    "Outcome",
                value =
                    value
            )
        }

        highlight.metric?.let { value ->
            MetadataRow(
                label =
                    "Metric",
                value =
                    value
            )
        }

        TagList(
            values =
                highlight.relatedSkills
        )
    }
}

@Composable
private fun EvidenceCard(
    title: String,
    eyebrow: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        color =
            MaterialTheme
                .colorScheme
                .surfaceContainerLow,
        shape =
            RoundedCornerShape(
                20.dp
            )
    ) {
        Column(
            modifier =
                Modifier.padding(
                    16.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {
            Text(
                text =
                    eyebrow,
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                fontWeight =
                    FontWeight.ExtraBold,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            Text(
                text =
                    title,
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.Bold
            )

            HorizontalDivider(
                color =
                    MaterialTheme
                        .colorScheme
                        .outlineVariant
                        .copy(
                            alpha =
                                0.55f
                        )
            )

            content()
        }
    }
}

@Composable
private fun DetailBlock(
    label: String,
    values: List<String>
) {
    if (values.isEmpty()) {
        return
    }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                5.dp
            )
    ) {
        Text(
            text =
                label,
            style =
                MaterialTheme
                    .typography
                    .labelSmall,
            fontWeight =
                FontWeight.Bold,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        values.forEach { value ->
            EvidenceLine(
                text =
                    value
            )
        }
    }
}

@Composable
private fun EvidenceLine(
    text: String
) {
    Row(
        verticalAlignment =
            Alignment.Top
    ) {
        Box(
            modifier =
                Modifier
                    .padding(
                        top =
                            7.dp
                    )
                    .size(
                        5.dp
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        MaterialTheme
                            .colorScheme
                            .primary
                    )
        )

        Spacer(
            modifier =
                Modifier.width(
                    9.dp
                )
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
                    .bodyMedium
        )
    }
}

@Composable
private fun EvidenceText(
    text: String
) {
    if (text.isBlank()) {
        return
    }

    Text(
        text =
            text,
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

@Composable
private fun MetadataRow(
    label: String,
    value: String
) {
    if (value.isBlank()) {
        return
    }

    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(
                10.dp
            ),
        verticalAlignment =
            Alignment.Top
    ) {
        Text(
            text =
                label,
            modifier =
                Modifier.width(
                    92.dp
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
                    .onSurfaceVariant
        )

        Text(
            text =
                value,
            modifier =
                Modifier.weight(
                    1f
                ),
            style =
                MaterialTheme
                    .typography
                    .bodyMedium
        )
    }
}

@Composable
private fun TagList(
    values: List<String>
) {
    if (values.isEmpty()) {
        return
    }

    FlowRow(
        horizontalArrangement =
            Arrangement.spacedBy(
                7.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(
                7.dp
            )
    ) {
        values.forEach { value ->
            Tag(
                text =
                    value
            )
        }
    }
}

@Composable
private fun Tag(
    text: String
) {
    Surface(
        color =
            MaterialTheme
                .colorScheme
                .secondaryContainer,
        contentColor =
            MaterialTheme
                .colorScheme
                .onSecondaryContainer,
        shape =
            CircleShape
    ) {
        Text(
            text =
                text,
            modifier =
                Modifier.padding(
                    horizontal =
                        10.dp,
                    vertical =
                        6.dp
                ),
            style =
                MaterialTheme
                    .typography
                    .labelSmall,
            fontWeight =
                FontWeight.SemiBold
        )
    }
}