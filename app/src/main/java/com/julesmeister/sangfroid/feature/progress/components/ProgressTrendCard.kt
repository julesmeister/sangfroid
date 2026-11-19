// app/src/main/java/com/julesmeister/sangfroid/feature/progress/components/ProgressTrendCard.kt

package com.julesmeister.sangfroid.feature.progress.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.theme.FeedbackError
import com.julesmeister.sangfroid.core.designsystem.theme.FeedbackSuccess
import com.julesmeister.sangfroid.feature.progress.model.OverallProgressSummary

private val CardShape =
    RoundedCornerShape(
        28.dp
    )

private val StripShape =
    RoundedCornerShape(
        20.dp
    )

private val CoverageBarHeight =
    6.dp

private enum class TrendDirection {
    UP,
    DOWN,
    STABLE
}

@Composable
fun ProgressTrendCard(
    summary: OverallProgressSummary,
    modifier: Modifier = Modifier
) {
    val attempts =
        summary.totalAttemptCount

    val evaluated =
        summary.completedEvaluationCount

    val revisions =
        summary.savedRevisionCount

    val coverage =
        if (
            attempts > 0
        ) {
            (
                    evaluated.toFloat() /
                            attempts.toFloat()
                    )
                .coerceIn(
                    0f,
                    1f
                )
        } else {
            0f
        }

    val animatedCoverage by
    animateFloatAsState(
        targetValue =
            coverage,
        animationSpec =
            tween(
                durationMillis =
                    700
            ),
        label =
            "evaluation-coverage"
    )

    /*
     * Match CvInsightsCard:
     *
     * outer card:
     * surfaceVariant @ 55%
     *
     * inner metric surface:
     * solid surface
     *
     * main content:
     * onSurface
     *
     * secondary content:
     * onSurfaceVariant
     *
     * progress accent:
     * primary
     */
    Surface(
        modifier =
            modifier.fillMaxWidth(),
        shape =
            CardShape,
        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant
                .copy(
                    alpha =
                        0.55f
                ),
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
            CardHeader(
                change =
                    summary
                        .averageScoreChangePercentagePoints
            )

            HeroAttempts(
                attempts =
                    attempts
            )

            MetricStrip(
                evaluated =
                    evaluated,
                revisions =
                    revisions,
                coveragePercent =
                    (
                            coverage *
                                    100f
                            )
                        .toInt()
            )

            CoverageBar(
                progress =
                    animatedCoverage,
                evaluated =
                    evaluated,
                attempts =
                    attempts
            )
        }
    }
}

@Composable
private fun CardHeader(
    change: Number?
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text =
                "PRACTICE SNAPSHOT",
            modifier =
                Modifier.weight(
                    1f
                ),
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

        if (
            change != null
        ) {
            TrendPill(
                change =
                    change
            )
        }
    }
}

@Composable
private fun TrendPill(
    change: Number
) {
    val value =
        change.toDouble()

    val direction =
        when {
            value > 0.0 ->
                TrendDirection.UP

            value < 0.0 ->
                TrendDirection.DOWN

            else ->
                TrendDirection.STABLE
        }

    val magnitude =
        formatPoints(
            kotlin.math.abs(
                value
            )
        )

    val tone =
        when (
            direction
        ) {
            TrendDirection.UP ->
                FeedbackSuccess

            TrendDirection.DOWN ->
                FeedbackError

            TrendDirection.STABLE ->
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        }

    val label =
        when (
            direction
        ) {
            TrendDirection.UP ->
                "+$magnitude pts"

            TrendDirection.DOWN ->
                "−$magnitude pts"

            TrendDirection.STABLE ->
                "Stable"
        }

    val description =
        when (
            direction
        ) {
            TrendDirection.UP ->
                "Recent question scores are up $magnitude percentage points on average."

            TrendDirection.DOWN ->
                "Recent question scores are down $magnitude percentage points on average."

            TrendDirection.STABLE ->
                "Recent question scores are stable."
        }

    Row(
        modifier =
            Modifier
                .clip(
                    RoundedCornerShape(
                        50
                    )
                )
                .background(
                    tone.copy(
                        alpha =
                            0.14f
                    )
                )
                .padding(
                    horizontal =
                        10.dp,
                    vertical =
                        5.dp
                )
                .semantics {
                    contentDescription =
                        description
                },
        horizontalArrangement =
            Arrangement.spacedBy(
                6.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        TrendGlyph(
            direction =
                direction,
            color =
                tone,
            modifier =
                Modifier.size(
                    10.dp
                )
        )

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
                tone,
            maxLines =
                1
        )
    }
}

@Composable
private fun HeroAttempts(
    attempts: Int
) {
    Row(
        verticalAlignment =
            Alignment.Bottom,
        horizontalArrangement =
            Arrangement.spacedBy(
                SangfroidSpacing.Xs
            )
    ) {
        Text(
            text =
                attempts.toString(),
            style =
                MaterialTheme
                    .typography
                    .displaySmall,
            fontWeight =
                FontWeight.ExtraBold,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurface,
            maxLines =
                1
        )

        Text(
            text =
                if (
                    attempts == 1
                ) {
                    "attempt so far"
                } else {
                    "attempts so far"
                },
            modifier =
                Modifier.padding(
                    bottom =
                        6.dp
                ),
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
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
}

@Composable
private fun MetricStrip(
    evaluated: Int,
    revisions: Int,
    coveragePercent: Int
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    StripShape
                )
                /*
                 * CvInsightsCard's StatTile uses the solid
                 * theme surface rather than a translucent
                 * surface on top of surfaceVariant.
                 */
                .background(
                    MaterialTheme
                        .colorScheme
                        .surface
                )
                .padding(
                    vertical =
                        SangfroidSpacing.Sm
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Metric(
            value =
                evaluated.toString(),
            label =
                "Evaluated",
            modifier =
                Modifier.weight(
                    1f
                )
        )

        StripDivider()

        Metric(
            value =
                revisions.toString(),
            label =
                "Revisions",
            modifier =
                Modifier.weight(
                    1f
                )
        )

        StripDivider()

        Metric(
            value =
                "$coveragePercent%",
            label =
                "Coverage",
            modifier =
                Modifier.weight(
                    1f
                )
        )
    }
}

@Composable
private fun Metric(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier,
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(
                SangfroidSpacing.Xxs
            )
    ) {
        Text(
            text =
                value,
            style =
                MaterialTheme
                    .typography
                    .titleLarge,
            fontWeight =
                FontWeight.ExtraBold,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurface,
            maxLines =
                1
        )

        Text(
            text =
                label,
            style =
                MaterialTheme
                    .typography
                    .labelSmall,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
            maxLines =
                1
        )
    }
}

@Composable
private fun StripDivider() {
    Box(
        modifier =
            Modifier
                .width(
                    1.dp
                )
                .height(
                    28.dp
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .outlineVariant
                        .copy(
                            alpha =
                                0.45f
                        )
                )
    )
}

@Composable
private fun CoverageBar(
    progress: Float,
    evaluated: Int,
    attempts: Int
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                6.dp
            )
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        CoverageBarHeight
                    )
                    .clip(
                        CircleShape
                    )
                    /*
                     * Matches the CV Insights coverage track:
                     * a clean solid surface sitting on the
                     * translucent surfaceVariant card.
                     */
                    .background(
                        MaterialTheme
                            .colorScheme
                            .surface
                    )
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(
                            progress
                        )
                        .clip(
                            CircleShape
                        )
                        /*
                         * Match CV Insights exactly here too.
                         * No dedicated cyan treatment on this card.
                         */
                        .background(
                            MaterialTheme
                                .colorScheme
                                .primary
                        )
            )
        }

        Text(
            text =
                when {
                    attempts == 0 ->
                        "Submit an answer to start tracking feedback coverage."

                    evaluated >= attempts ->
                        "Every attempt has feedback."

                    else ->
                        "$evaluated of $attempts attempts have feedback."
                },
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
}

@Composable
private fun TrendGlyph(
    direction: TrendDirection,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier =
            modifier
    ) {
        val stroke =
            1.8.dp.toPx()

        val w =
            size.width

        val h =
            size.height

        when (
            direction
        ) {
            TrendDirection.UP -> {
                drawLine(
                    color,
                    Offset(
                        w * 0.5f,
                        h * 0.9f
                    ),
                    Offset(
                        w * 0.5f,
                        h * 0.1f
                    ),
                    stroke,
                    StrokeCap.Round
                )

                drawLine(
                    color,
                    Offset(
                        w * 0.15f,
                        h * 0.45f
                    ),
                    Offset(
                        w * 0.5f,
                        h * 0.1f
                    ),
                    stroke,
                    StrokeCap.Round
                )

                drawLine(
                    color,
                    Offset(
                        w * 0.85f,
                        h * 0.45f
                    ),
                    Offset(
                        w * 0.5f,
                        h * 0.1f
                    ),
                    stroke,
                    StrokeCap.Round
                )
            }

            TrendDirection.DOWN -> {
                drawLine(
                    color,
                    Offset(
                        w * 0.5f,
                        h * 0.1f
                    ),
                    Offset(
                        w * 0.5f,
                        h * 0.9f
                    ),
                    stroke,
                    StrokeCap.Round
                )

                drawLine(
                    color,
                    Offset(
                        w * 0.15f,
                        h * 0.55f
                    ),
                    Offset(
                        w * 0.5f,
                        h * 0.9f
                    ),
                    stroke,
                    StrokeCap.Round
                )

                drawLine(
                    color,
                    Offset(
                        w * 0.85f,
                        h * 0.55f
                    ),
                    Offset(
                        w * 0.5f,
                        h * 0.9f
                    ),
                    stroke,
                    StrokeCap.Round
                )
            }

            TrendDirection.STABLE -> {
                drawLine(
                    color,
                    Offset(
                        w * 0.1f,
                        h * 0.5f
                    ),
                    Offset(
                        w * 0.9f,
                        h * 0.5f
                    ),
                    stroke,
                    StrokeCap.Round
                )
            }
        }
    }
}

private fun formatPoints(
    value: Double
): String {
    return if (
        value %
        1.0 ==
        0.0
    ) {
        value
            .toLong()
            .toString()
    } else {
        String.format(
            java.util.Locale.ROOT,
            "%.1f",
            value
        )
    }
}