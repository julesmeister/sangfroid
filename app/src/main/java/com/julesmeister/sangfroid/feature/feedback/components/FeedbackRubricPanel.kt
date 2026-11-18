// app/src/main/java/com/julesmeister/sangfroid/feature/feedback/components/FeedbackRubricPanel.kt

package com.julesmeister.sangfroid.feature.feedback.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.domain.evaluation.EvaluationResult

@Composable
fun FeedbackRubricPanel(
    evaluation: EvaluationResult,
    modifier: Modifier = Modifier
) {
    if (evaluation.rubricScores.isEmpty()) {
        return
    }

    val scheme = MaterialTheme.colorScheme
    val dark = scheme.surface.luminance() < 0.5f

    val background = if (dark) {
        Color(0xFF202733)
    } else {
        Color(0xFFFFFFFF)
    }

    val heading = if (dark) {
        Color(0xFFF2F5FA)
    } else {
        Color(0xFF182638)
    }

    val body = if (dark) {
        Color(0xFFDCE3ED)
    } else {
        Color(0xFF344458)
    }

    val muted = if (dark) {
        Color(0xFFB8C4D4)
    } else {
        Color(0xFF526174)
    }

    val primary = if (dark) {
        Color(0xFFA9C8FF)
    } else {
        Color(0xFF254F91)
    }

    val tertiary = if (dark) {
        Color(0xFF92DED0)
    } else {
        Color(0xFF176354)
    }

    val error = if (dark) {
        Color(0xFFFFB4AB)
    } else {
        Color(0xFF9C3028)
    }

    val divider = heading.copy(
        alpha = if (dark) 0.08f else 0.06f
    )

    /*
     * Keep the corrected colors local to rubric rows.
     * The application's global color scheme is unchanged.
     */
    val panelScheme = scheme.copy(
        surface = background,
        onSurface = heading,
        onSurfaceVariant = body,
        primary = primary,
        onPrimary = rubricForeground(primary),
        secondary = primary,
        onSecondary = rubricForeground(primary),
        tertiary = tertiary,
        onTertiary = rubricForeground(tertiary),
        error = error,
        onError = rubricForeground(error),
        outline = muted,
        outlineVariant = divider.compositeOver(background)
    )

    MaterialTheme(
        colorScheme = panelScheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = background,
            contentColor = heading,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(
                    SangfroidSpacing.CardContent
                ),
                verticalArrangement = Arrangement.spacedBy(
                    SangfroidSpacing.Lg
                )
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "RUBRIC",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = heading
                    )

                    Text(
                        text = "A closer look at your answer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = muted
                    )
                }

                evaluation.rubricScores.forEachIndexed {
                        index,
                        score ->

                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 0.5.dp,
                            color = divider
                        )
                    }

                    FeedbackScoreRow(
                        score = score
                    )
                }
            }
        }
    }
}

private fun rubricForeground(
    background: Color
): Color {
    val luminance = background.luminance()

    val blackContrast =
        (luminance + 0.05f) / 0.05f

    val whiteContrast =
        1.05f / (luminance + 0.05f)

    return if (blackContrast >= whiteContrast) {
        Color.Black
    } else {
        Color.White
    }
}