package com.julesmeister.sangfroid.feature.progress.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.feature.progress.model.CvEvidenceProgress
import com.julesmeister.sangfroid.feature.progress.model.CvInsights

private const val CollapsedEvidenceCount = 4

@Composable
fun CvInsightsCard(
    insights: CvInsights,
    onPracticeQuestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAll by rememberSaveable { mutableStateOf(false) }

    val shownEvidence =
        if (showAll) {
            insights.evidenceItems
        } else {
            insights.evidenceItems.take(CollapsedEvidenceCount)
        }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(
            modifier =
                Modifier
                    .padding(SangfroidSpacing.CardContent)
                    .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "FROM YOUR CV",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "CV-based questions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "How Qwen used your CV evidence to shape your practice.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatTile(
                    value = insights.cvQuestionCount.toString(),
                    label = "${insights.sharePercentage}% of questions",
                    modifier = Modifier.weight(1f)
                )

                StatTile(
                    value = "${insights.practisedCvQuestionCount}",
                    label = "Practised",
                    modifier = Modifier.weight(1f)
                )

                StatTile(
                    value = insights.cvAveragePercentage?.let { "$it%" } ?: "—",
                    label =
                        insights.scoreDifference?.let { diff ->
                            when {
                                diff > 0 -> "+$diff vs general"
                                diff < 0 -> "−${-diff} vs general"
                                else -> "Same as general"
                            }
                        } ?: "Avg score",
                    modifier = Modifier.weight(1f)
                )
            }

            insights.coverageFraction?.let { fraction ->
                CoverageBar(
                    fraction = fraction,
                    covered = insights.coveredEvidenceCount,
                    total = insights.totalEvidenceCount ?: 0
                )
            }

            if (insights.typeBreakdown.isNotEmpty()) {
                TypeBreakdown(insights)
            }

            if (shownEvidence.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel("By CV item")

                    shownEvidence.forEach { evidence ->
                        EvidenceRow(
                            evidence = evidence,
                            onClick = { onPracticeQuestion(evidence.nextQuestionId) }
                        )
                    }

                    if (insights.evidenceItems.size > CollapsedEvidenceCount) {
                        Text(
                            text =
                                if (showAll) {
                                    "Show less"
                                } else {
                                    "Show all ${insights.evidenceItems.size}"
                                },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable(role = Role.Button) { showAll = !showAll }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            if (insights.untappedEvidence.isNotEmpty()) {
                UntappedEvidence(insights)
            }
        }
    }
}

@Composable
private fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CoverageBar(
    fraction: Float,
    covered: Int,
    total: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SectionLabel("CV coverage")

            Text(
                text = "$covered of $total items asked",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth(fraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TypeBreakdown(
    insights: CvInsights
) {
    val total = insights.typeBreakdown.sumOf { it.count }.coerceAtLeast(1)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel("Question sources")

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50)),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            insights.typeBreakdown.forEach { entry ->
                Box(
                    modifier =
                        Modifier
                            .weight(entry.count.toFloat() / total)
                            .fillMaxHeight()
                            .background(typeColor(entry.type))
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            insights.typeBreakdown.forEach { entry ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(typeColor(entry.type))
                    )

                    Text(
                        text = "${entry.label} · ${entry.count}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EvidenceRow(
    evidence: CvEvidenceProgress,
    onClick: () -> Unit
) {
    val tone = typeColor(evidence.evidenceType)

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tone.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = evidence.typeLabel.take(1),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = tone
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = evidence.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text =
                    "${evidence.typeLabel} · " +
                            "${evidence.questionCount} " +
                            (if (evidence.questionCount == 1) "question" else "questions") +
                            " · ${evidence.practisedCount} practised",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = evidence.averagePercentage?.let { "$it%" } ?: "Practise",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = tone,
            modifier =
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(tone.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UntappedEvidence(
    insights: CvInsights
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel("Not asked yet")

        Text(
            text = "These CV items haven't produced a question yet. New questions will rotate toward them.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            insights.untappedEvidence.forEach { item ->
                val tone = typeColor(item.evidenceType)

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .border(1.dp, tone.copy(alpha = 0.35f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(
    text: String
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun typeColor(type: String): Color {
    val scheme = MaterialTheme.colorScheme

    return when (type) {
        "PROJECT" -> scheme.primary
        "ACHIEVEMENT" -> scheme.secondary
        "WORK_HIGHLIGHT" -> scheme.tertiary
        "CERTIFICATION" -> scheme.error.copy(alpha = 0.8f)
        else -> scheme.onSurfaceVariant
    }
}