package com.julesmeister.sangfroid.feature.profile.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.feature.profile.components.CountBadge

private val ItemShape = RoundedCornerShape(18.dp)
private val TileShape = RoundedCornerShape(12.dp)
private const val CollapsedSignalCount = 3

/*
 * ===========================================================
 * SECTIONS
 * ===========================================================
 */

@Composable
internal fun CvProfileSections(
    model: CvAnalysisModel,
    padding: Dp
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (model.hasExperience) {
            CvExperienceSection(
                model = model,
                padding = padding
            )
        }

        if (model.hasCredentials) {
            CvCredentialsSection(
                certifications = model.certifications,
                padding = padding
            )
        }

        if (model.hasAchievements) {
            CvAchievementsSection(
                achievements = model.achievements,
                padding = padding
            )
        }

        if (model.hasSignals) {
            CvSignalsSection(
                signals = model.experienceSignals,
                padding = padding
            )
        }
    }
}

private enum class ExperienceTab {
    PROJECTS,
    WORK
}

@Composable
private fun CvExperienceSection(
    model: CvAnalysisModel,
    padding: Dp
) {
    val hasProjects = model.projects.isNotEmpty()
    val hasWork = model.workHighlights.isNotEmpty()

    var selectedTab by
    rememberSaveable {
        mutableStateOf(
            if (hasProjects) ExperienceTab.PROJECTS else ExperienceTab.WORK
        )
    }

    val activeTab =
        when {
            !hasProjects -> ExperienceTab.WORK
            !hasWork -> ExperienceTab.PROJECTS
            else -> selectedTab
        }

    SectionCard(
        title = "Experience",
        caption = "Projects and work evidence that shape your interview questions.",
        padding = padding,
        trailing = {
            CountBadge(
                count = model.projects.size + model.workHighlights.size
            )
        }
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (hasProjects && hasWork) {
                SegmentedSwitch(
                    options =
                        listOf(
                            "Projects" to model.projects.size,
                            "Work" to model.workHighlights.size
                        ),
                    selectedIndex = activeTab.ordinal,
                    onSelect = { index ->
                        selectedTab = ExperienceTab.entries[index]
                    }
                )
            }

            when (activeTab) {
                ExperienceTab.PROJECTS ->
                    model.projects.forEach { project ->
                        ProjectItem(project)
                    }

                ExperienceTab.WORK ->
                    model.workHighlights.forEach { highlight ->
                        WorkItem(highlight)
                    }
            }
        }
    }
}

@Composable
private fun CvCredentialsSection(
    certifications: List<CvCertificationModel>,
    padding: Dp
) {
    SectionCard(
        title = "Credentials",
        caption = "Certifications identified from your CV.",
        padding = padding,
        trailing = {
            CountBadge(count = certifications.size)
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            certifications.forEach { certification ->
                CertificationItem(certification)
            }
        }
    }
}

@Composable
private fun CvAchievementsSection(
    achievements: List<CvAchievementModel>,
    padding: Dp
) {
    SectionCard(
        title = "Achievements",
        caption = "Results you can use as proof in your answers.",
        padding = padding,
        trailing = {
            CountBadge(count = achievements.size)
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            achievements.forEach { achievement ->
                AchievementItem(achievement)
            }
        }
    }
}

@Composable
private fun CvSignalsSection(
    signals: List<String>,
    padding: Dp
) {
    val visibleSignals = signals.filter(String::isNotBlank)

    var showAll by
    rememberSaveable {
        mutableStateOf(false)
    }

    val shown =
        if (showAll) {
            visibleSignals
        } else {
            visibleSignals.take(CollapsedSignalCount)
        }

    SectionCard(
        title = "Experience signals",
        caption = "Extra context Qwen noticed in your CV.",
        padding = padding,
        trailing = {
            CountBadge(count = visibleSignals.size)
        }
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            shown.forEachIndexed { index, signal ->
                SignalRow(
                    number = index + 1,
                    text = signal
                )
            }

            if (visibleSignals.size > CollapsedSignalCount) {
                TextToggle(
                    text =
                        if (showAll) {
                            "Show less"
                        } else {
                            "Show all ${visibleSignals.size}"
                        },
                    onClick = {
                        showAll = !showAll
                    }
                )
            }
        }
    }
}

/*
 * ===========================================================
 * EVIDENCE ITEMS
 * ===========================================================
 */

@Composable
private fun ProjectItem(
    model: CvProjectModel
) {
    val accent = MaterialTheme.colorScheme.primary

    EvidenceItem(
        key = "project-${model.id}",
        accent = accent,
        eyebrow = model.candidateRole ?: "Project",
        title = model.name,
        summary = model.context ?: model.objective,
        highlight = model.metrics.firstOrNull(),
        hasDetails = true
    ) {
        model.objective?.let { DetailLine("Objective", it) }
        DetailBullets("What you did", model.actions, accent)
        DetailBullets("Outcomes", model.outcomes, accent)
        DetailChips("Methods", model.methods)
        DetailChips("Tools", model.tools)
        DetailChips("Metrics", model.metrics.drop(1))
        EvidenceQuote(model.evidence, accent)
        SkillTags(model.relatedSkills, accent)
    }
}

@Composable
private fun WorkItem(
    model: CvWorkHighlightModel
) {
    val accent = MaterialTheme.colorScheme.tertiary

    EvidenceItem(
        key = "work-${model.id}",
        accent = accent,
        eyebrow = model.candidateRole ?: "Work highlight",
        title = model.title,
        summary = model.organizationContext ?: model.responsibility,
        highlight = model.metric,
        hasDetails = true
    ) {
        model.responsibility?.let { DetailLine("Responsibility", it) }
        model.action?.let { DetailLine("Action", it) }
        model.outcome?.let { DetailLine("Outcome", it) }
        EvidenceQuote(model.evidence, accent)
        SkillTags(model.relatedSkills, accent)
    }
}

@Composable
private fun CertificationItem(
    model: CvCertificationModel
) {
    val accent = MaterialTheme.colorScheme.secondary

    EvidenceItem(
        key = "cert-${model.id}",
        accent = accent,
        eyebrow = "Certification",
        title = model.name,
        summary = model.subtitle,
        highlight = model.status,
        highlightIsMetric = false,
        hasDetails =
            model.evidence.isNotBlank() ||
                    model.relatedSkills.isNotEmpty()
    ) {
        EvidenceQuote(model.evidence, accent)
        SkillTags(model.relatedSkills, accent)
    }
}

@Composable
private fun AchievementItem(
    model: CvAchievementModel
) {
    val accent = MaterialTheme.colorScheme.secondary

    EvidenceItem(
        key = "achievement-${model.id}",
        accent = accent,
        eyebrow = "Achievement",
        title = model.title,
        summary = model.result ?: model.context,
        highlight = model.metric,
        hasDetails = true
    ) {
        model.context?.let { DetailLine("Context", it) }
        model.action?.let { DetailLine("Action", it) }
        model.result?.let { DetailLine("Result", it) }
        EvidenceQuote(model.evidence, accent)
        SkillTags(model.relatedSkills, accent)
    }
}

@Composable
private fun EvidenceItem(
    key: String,
    accent: Color,
    eyebrow: String,
    title: String,
    summary: String?,
    highlight: String?,
    hasDetails: Boolean,
    highlightIsMetric: Boolean = true,
    details: @Composable () -> Unit
) {
    var expanded by
    rememberSaveable(key) {
        mutableStateOf(false)
    }

    val chevronRotation by
    animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(220),
        label = "evidence-chevron"
    )

    val borderColor by
    animateColorAsState(
        targetValue =
            if (expanded) {
                accent.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            },
        animationSpec = tween(220),
        label = "evidence-border"
    )

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(ItemShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, borderColor, ItemShape)
                .then(
                    if (hasDetails) {
                        Modifier.clickable(
                            role = Role.Button,
                            onClickLabel =
                                if (expanded) "Collapse $title" else "Expand $title"
                        ) {
                            expanded = !expanded
                        }
                    } else {
                        Modifier
                    }
                )
                .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            MonogramTile(
                text = title,
                accent = accent
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                summary
                    ?.takeIf(String::isNotBlank)
                    ?.let { text ->
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (expanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                highlight
                    ?.takeIf(String::isNotBlank)
                    ?.let { text ->
                        HighlightChip(
                            text = text,
                            accent = accent,
                            isMetric = highlightIsMetric,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
            }

            if (hasDetails) {
                Chevron(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier =
                        Modifier
                            .padding(top = 2.dp)
                            .graphicsLayer {
                                rotationZ = chevronRotation
                            }
                )
            }
        }

        AnimatedVisibility(
            visible = expanded && hasDetails,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )
                )

                details()
            }
        }
    }
}

/*
 * ===========================================================
 * BUILDING BLOCKS
 * ===========================================================
 */

@Composable
private fun SegmentedSwitch(
    options: List<Pair<String, Int>>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEachIndexed { index, (label, count) ->
            val selected = index == selectedIndex

            val background by
            animateColorAsState(
                targetValue =
                    if (selected) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        Color.Transparent
                    },
                animationSpec = tween(180),
                label = "segment-background"
            )

            val content by
            animateColorAsState(
                targetValue =
                    if (selected) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                animationSpec = tween(180),
                label = "segment-content"
            )

            Row(
                modifier =
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(background)
                        .clickable(role = Role.Tab) {
                            onSelect(index)
                        }
                        .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                    color = content
                )

                Text(
                    text = "  $count",
                    style = MaterialTheme.typography.labelMedium,
                    color = content.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun MonogramTile(
    text: String,
    accent: Color
) {
    val letter =
        text.trim()
            .firstOrNull { it.isLetterOrDigit() }
            ?.uppercaseChar()
            ?.toString()
            ?: "•"

    Box(
        modifier =
            Modifier
                .size(38.dp)
                .clip(TileShape)
                .background(accent.copy(alpha = 0.13f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = accent
        )
    }
}

@Composable
private fun HighlightChip(
    text: String,
    accent: Color,
    isMetric: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(50))
                .background(accent.copy(alpha = 0.12f))
                .padding(horizontal = 9.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isMetric) {
            Text(
                text = "↑",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = accent
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(accent)
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DetailLine(
    label: String,
    value: String
) {
    if (value.isBlank()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        DetailLabel(label)

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DetailBullets(
    label: String,
    values: List<String>,
    accent: Color
) {
    val visible = values.filter(String::isNotBlank)
    if (visible.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        DetailLabel(label)

        visible.forEach { value ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier =
                        Modifier
                            .padding(top = 6.dp)
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(accent)
                )

                Text(
                    text = value,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailChips(
    label: String,
    values: List<String>
) {
    val visible = values.filter(String::isNotBlank).distinct()
    if (visible.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        DetailLabel(label)

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            visible.forEach { value ->
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun EvidenceQuote(
    text: String,
    accent: Color
) {
    if (text.isBlank()) return

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Box(
            modifier =
                Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(accent.copy(alpha = 0.6f))
        )

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            DetailLabel("From your CV")

            Text(
                text = "“$text”",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillTags(
    values: List<String>,
    accent: Color
) {
    val visible = values.filter(String::isNotBlank).distinct()
    if (visible.isEmpty()) return

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        visible.forEach { value ->
            Text(
                text = "#$value",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = accent,
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(50))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun SignalRow(
    number: Int,
    text: String
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier =
                Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TextToggle(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            Modifier
                .clip(RoundedCornerShape(50))
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}

@Composable
private fun DetailLabel(
    text: String
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
    )
}

@Composable
private fun Chevron(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(18.dp)
    ) {
        val stroke = 1.8.dp.toPx()

        drawLine(
            color = color,
            start = Offset(size.width * 0.25f, size.height * 0.4f),
            end = Offset(size.width * 0.5f, size.height * 0.64f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(size.width * 0.5f, size.height * 0.64f),
            end = Offset(size.width * 0.75f, size.height * 0.4f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}