package com.julesmeister.sangfroid.feature.history.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.feature.history.model.AttemptHistoryItem

private val MarkerWidth = 24.dp
private val MarkerCenterY = 26.dp
private val RowGap = 12.dp

@Composable
fun AttemptTimeline(
    items: List<AttemptHistoryItem>,
    deletingAttemptIds: Set<String>,
    onOpenFeedback: (String) -> Unit,
    onDeleteAttempt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        items.forEachIndexed { index, item ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TimelineMarker(
                    isFirst = index == 0,
                    isLast = index == items.lastIndex,
                    isEvaluated = item.hasEvaluation,
                    isLatest = index == 0,
                    modifier =
                        Modifier
                            .width(MarkerWidth)
                            .fillMaxHeight()
                )

                AttemptHistoryCard(
                    item = item,
                    isDeleting = item.id in deletingAttemptIds,
                    onOpenFeedback = onOpenFeedback,
                    onDelete = onDeleteAttempt,
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(
                                bottom =
                                    if (index == items.lastIndex) 0.dp else RowGap
                            )
                )
            }
        }
    }
}

@Composable
private fun TimelineMarker(
    isFirst: Boolean,
    isLast: Boolean,
    isEvaluated: Boolean,
    isLatest: Boolean,
    modifier: Modifier = Modifier
) {
    val lineColor =
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)

    val accent =
        MaterialTheme.colorScheme.primary

    val surface =
        MaterialTheme.colorScheme.background

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = MarkerCenterY.toPx()
        val radius = 5.dp.toPx()
        val stroke = 2.dp.toPx()

        if (!isFirst) {
            drawLine(
                color = lineColor,
                start = Offset(centerX, 0f),
                end = Offset(centerX, centerY - radius - 3.dp.toPx()),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }

        if (!isLast) {
            drawLine(
                color = lineColor,
                start = Offset(centerX, centerY + radius + 3.dp.toPx()),
                end = Offset(centerX, size.height),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }

        if (isLatest) {
            drawCircle(
                color = accent.copy(alpha = 0.16f),
                radius = radius + 5.dp.toPx(),
                center = Offset(centerX, centerY)
            )
        }

        if (isEvaluated) {
            drawCircle(
                color = accent,
                radius = radius,
                center = Offset(centerX, centerY)
            )
        } else {
            drawCircle(
                color = surface,
                radius = radius,
                center = Offset(centerX, centerY)
            )

            drawCircle(
                color = accent.copy(alpha = 0.7f),
                radius = radius - 1.dp.toPx(),
                center = Offset(centerX, centerY),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}