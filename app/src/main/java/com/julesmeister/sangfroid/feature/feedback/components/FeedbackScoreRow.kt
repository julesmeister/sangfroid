package com.julesmeister.sangfroid.feature.feedback.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.domain.evaluation.EvaluationRubricScore

@Composable
fun FeedbackScoreRow(
    score: EvaluationRubricScore,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                text = score.criterion,
                style =
                    MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color =
                    MaterialTheme.colorScheme.onSurface
            )

            Text(
                text =
                    "${score.score}/${score.maximumScore}",
                style =
                    MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color =
                    MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(
            modifier = Modifier.height(
                SangfroidSpacing.Xs
            )
        )

        LinearProgressIndicator(
            progress = {
                score.normalizedProgress
            },
            modifier = Modifier.fillMaxWidth(),
            color =
                MaterialTheme.colorScheme.secondary,
            trackColor =
                MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(
            modifier = Modifier.height(
                SangfroidSpacing.Xs
            )
        )

        Text(
            text = score.explanation,
            style =
                MaterialTheme.typography.bodySmall,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}