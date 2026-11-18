// app/src/main/java/com/julesmeister/sangfroid/feature/feedback/components/QuestionProgressPanel.kt

package com.julesmeister.sangfroid.feature.feedback.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.domain.progress.QuestionMasteryLevel
import com.julesmeister.sangfroid.domain.progress.QuestionProgress
import com.julesmeister.sangfroid.domain.progress.RubricProgress

private data class ProgressPanelPalette(
    val background: Color,
    val card: Color,
    val heading: Color,
    val body: Color,
    val muted: Color,
    val track: Color,
    val blue: Color,
    val blueContainer: Color,
    val teal: Color,
    val tealContainer: Color,
    val amber: Color,
    val amberContainer: Color,
    val violet: Color,
    val violetContainer: Color
)

/*
 * This section intentionally remains light in both app themes.
 * Every foreground is explicitly paired with its light surface.
 */
private val LightProgressPalette = ProgressPanelPalette(
    background = Color(0xFFFAFBFD),
    card = Color(0xFFF1F4F8),
    heading = Color(0xFF19283B),
    body = Color(0xFF35465A),
    muted = Color(0xFF536276),
    track = Color(0xFFDDE5EF),
    blue = Color(0xFF28528F),
    blueContainer = Color(0xFFEDF3FD),
    teal = Color(0xFF1B6254),
    tealContainer = Color(0xFFEDF8F3),
    amber = Color(0xFF805211),
    amberContainer = Color(0xFFFFF6E8),
    violet = Color(0xFF654394),
    violetContainer = Color(0xFFF5EFFD)
)

@Composable
fun QuestionProgressPanel(
    progress: QuestionProgress,
    modifier: Modifier = Modifier
) {
    val palette = LightProgressPalette

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        color = palette.background,
        contentColor = palette.heading,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                SangfroidSpacing.CardContent
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            ProgressHeader(
                progress = progress,
                palette = palette
            )

            if (progress.hasProgressData) {
                ProgressScoreCard(
                    progress = progress,
                    palette = palette
                )

                ProgressStats(
                    progress = progress,
                    palette = palette
                )

                if (
                    progress.strongestCriteria.isNotEmpty() ||
                    progress.weakestCriteria.isNotEmpty()
                ) {
                    CriteriaSummary(
                        strongest =
                            progress.strongestCriteria.firstOrNull(),
                        weakest =
                            progress.weakestCriteria.firstOrNull(),
                        palette = palette
                    )
                }
            } else {
                EmptyProgressState(
                    attemptCount = progress.attemptCount,
                    palette = palette
                )
            }
        }
    }
}

@Composable
private fun ProgressHeader(
    progress: QuestionProgress,
    palette: ProgressPanelPalette
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "QUESTION PROGRESS",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = palette.blue
        )

        Text(
            text = "Build a stronger answer",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = palette.heading
        )

        MasteryBadge(
            level = progress.masteryLevel,
            palette = palette
        )

        Text(
            text = progress.progressSummary,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.body
        )
    }
}

@Composable
private fun MasteryBadge(
    level: QuestionMasteryLevel,
    palette: ProgressPanelPalette
) {
    val accent = masteryAccent(level, palette)
    val background = masteryContainer(level, palette)

    Surface(
        color = background,
        contentColor = accent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 8.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accent)
            )

            Text(
                text = level.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

@Composable
private fun ProgressScoreCard(
    progress: QuestionProgress,
    palette: ProgressPanelPalette
) {
    val normalizedScore =
        progress.latestNormalizedScore
            ?.takeIf { it.isFinite() }
            ?.coerceIn(0f, 1f)
            ?: 0f

    val accent = masteryAccent(
        progress.masteryLevel,
        palette
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = palette.card,
        contentColor = palette.heading,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "LATEST SCORE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = palette.muted
                    )

                    Text(
                        text = "Rubric performance",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.body
                    )
                }

                Text(
                    text = progress.latestPercentage
                        ?.coerceIn(0, 100)
                        ?.let { "$it%" }
                        ?: "—",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent
                )
            }

            LinearProgressIndicator(
                progress = { normalizedScore },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
                color = accent,
                trackColor = palette.track
            )
        }
    }
}

@Composable
private fun ProgressStats(
    progress: QuestionProgress,
    palette: ProgressPanelPalette
) {
    val change = progress.scoreChangePercentagePoints

    val changeColor = when {
        change == null -> palette.heading
        change > 0 -> palette.teal
        change < 0 -> palette.amber
        else -> palette.heading
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProgressStat(
                label = "Attempts",
                value = progress.attemptCount.toString(),
                palette = palette,
                valueColor = palette.heading,
                modifier = Modifier.weight(1f)
            )

            ProgressStat(
                label = "Evaluated",
                value = progress.completedEvaluationCount.toString(),
                palette = palette,
                valueColor = palette.heading,
                modifier = Modifier.weight(1f)
            )

            ProgressStat(
                label = "Change",
                value = formatScoreChange(change),
                palette = palette,
                valueColor = changeColor,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "Change is measured in percentage points.",
            style = MaterialTheme.typography.bodySmall,
            color = palette.muted
        )
    }
}

@Composable
private fun ProgressStat(
    label: String,
    value: String,
    palette: ProgressPanelPalette,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = palette.card,
        contentColor = palette.heading,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 14.dp
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = palette.muted
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun CriteriaSummary(
    strongest: RubricProgress?,
    weakest: RubricProgress?,
    palette: ProgressPanelPalette
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "WHERE TO FOCUS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = palette.heading
        )

        if (strongest != null) {
            CriterionCard(
                label = "Strongest",
                progress = strongest,
                background = palette.tealContainer,
                accent = palette.teal,
                palette = palette
            )
        }

        if (weakest != null) {
            CriterionCard(
                label = "Focus next",
                progress = weakest,
                background = palette.blueContainer,
                accent = palette.blue,
                palette = palette
            )
        }
    }
}

@Composable
private fun CriterionCard(
    label: String,
    progress: RubricProgress,
    background: Color,
    accent: Color,
    palette: ProgressPanelPalette
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = background,
        contentColor = palette.heading,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent
                )

                Text(
                    text = progress.criterion,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.heading
                )
            }

            Text(
                text = "${progress.latestPercentage}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

@Composable
private fun EmptyProgressState(
    attemptCount: Int,
    palette: ProgressPanelPalette
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = palette.card,
        contentColor = palette.body,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (attemptCount > 0) {
                    "Your progress starts with feedback"
                } else {
                    "Start your first attempt"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = palette.heading
            )

            Text(
                text = if (attemptCount > 0) {
                    "Complete an evaluation to start measuring " +
                            "your progress on this question."
                } else {
                    "Practice this question to begin building " +
                            "your progress history."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = palette.body
            )

            Text(
                text = "Mastery reflects rubric performance " +
                        "across repeated attempts.",
                style = MaterialTheme.typography.bodySmall,
                color = palette.muted
            )
        }
    }
}

private fun formatScoreChange(
    change: Int?
): String = when {
    change == null -> "—"
    change > 0 -> "+$change"
    else -> change.toString()
}

private fun masteryAccent(
    level: QuestionMasteryLevel,
    palette: ProgressPanelPalette
): Color = when (level) {
    QuestionMasteryLevel.NOT_STARTED -> palette.body
    QuestionMasteryLevel.NEEDS_WORK -> palette.amber
    QuestionMasteryLevel.DEVELOPING -> palette.blue
    QuestionMasteryLevel.STRONG -> palette.teal
    QuestionMasteryLevel.MASTERED -> palette.violet
}

private fun masteryContainer(
    level: QuestionMasteryLevel,
    palette: ProgressPanelPalette
): Color = when (level) {
    QuestionMasteryLevel.NOT_STARTED -> palette.card
    QuestionMasteryLevel.NEEDS_WORK -> palette.amberContainer
    QuestionMasteryLevel.DEVELOPING -> palette.blueContainer
    QuestionMasteryLevel.STRONG -> palette.tealContainer
    QuestionMasteryLevel.MASTERED -> palette.violetContainer
}