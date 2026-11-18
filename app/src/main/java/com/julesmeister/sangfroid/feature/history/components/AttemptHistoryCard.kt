package com.julesmeister.sangfroid.feature.history.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.component.SangfroidConfirmationDialog
import com.julesmeister.sangfroid.feature.history.model.AttemptHistoryItem

private val CardShape = RoundedCornerShape(22.dp)
private val ActionTrackHeight = 38.dp
private val ActionTrackPadding = 3.dp
private const val CollapsedAnswerLines = 3

@Composable
fun AttemptHistoryCard(
    item: AttemptHistoryItem,
    isDeleting: Boolean,
    onOpenFeedback: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember(item.id) { mutableStateOf(false) }
    var showDeleteConfirmation by remember(item.id) { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        SangfroidConfirmationDialog(
            title = "Delete ${item.title}?",
            message =
                "This permanently removes this practice attempt, " +
                        "its evaluation, and revisions associated with " +
                        "the attempt. This action cannot be undone.",
            confirmText = "Delete attempt",
            onConfirm = {
                showDeleteConfirmation = false
                onDelete(item.id)
            },
            onDismiss = {
                showDeleteConfirmation = false
            }
        )
    }

    val accent = MaterialTheme.colorScheme.primary

    Surface(
        onClick = {
            if (!isDeleting) isExpanded = !isExpanded
        },
        enabled = !isDeleting,
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border =
            BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
    ) {
        Column(
            modifier =
                Modifier
                    .padding(14.dp)
                    .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = item.supportingLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                StatusPill(
                    text = item.evaluationLabel,
                    hasEvaluation = item.hasEvaluation,
                    accent = accent
                )
            }

            AnswerPanel(
                answer = item.answerPreview,
                expanded = isExpanded,
                accent = accent
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item.summary
                        ?.takeIf(String::isNotBlank)
                        ?.let { summary ->
                            FeedbackSummaryPanel(
                                summary = summary,
                                accent = accent
                            )
                        }

                    if (item.revisionCount > 0) {
                        Text(
                            text =
                                if (item.revisionCount == 1) {
                                    "1 saved revision"
                                } else {
                                    "${item.revisionCount} saved revisions"
                                },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(accent.copy(alpha = 0.1f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            ActionTrack(
                hasEvaluation = item.hasEvaluation,
                isExpanded = isExpanded,
                isDeleting = isDeleting,
                onOpenFeedback = { onOpenFeedback(item.id) },
                onToggleExpanded = { isExpanded = !isExpanded },
                onDelete = { showDeleteConfirmation = true }
            )
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    hasEvaluation: Boolean,
    accent: Color
) {
    val tone =
        if (hasEvaluation) accent
        else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier =
            Modifier
                .clip(RoundedCornerShape(50))
                .background(tone.copy(alpha = if (hasEvaluation) 0.12f else 0.08f))
                .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(tone)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = tone,
            maxLines = 1
        )
    }
}

@Composable
private fun AnswerPanel(
    answer: String,
    expanded: Boolean,
    accent: Color
) {
    val isBlank = answer.isBlank()

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuoteGlyph(color = accent)

            Text(
                text = "YOUR ANSWER",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = accent
            )
        }

        Text(
            text = if (isBlank) "No answer was recorded." else answer,
            style = MaterialTheme.typography.bodyMedium,
            color =
                if (isBlank) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
            maxLines = if (expanded) Int.MAX_VALUE else CollapsedAnswerLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FeedbackSummaryPanel(
    summary: String,
    accent: Color
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.07f))
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
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "FEEDBACK SUMMARY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ActionTrack(
    hasEvaluation: Boolean,
    isExpanded: Boolean,
    isDeleting: Boolean,
    onOpenFeedback: () -> Unit,
    onToggleExpanded: () -> Unit,
    onDelete: () -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(ActionTrackHeight)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .padding(ActionTrackPadding),
        horizontalArrangement = Arrangement.spacedBy(ActionTrackPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasEvaluation) {
            ActionPill(
                label = "View feedback",
                enabled = !isDeleting,
                containerColor = onSurface,
                contentColor = surface,
                border = null,
                onClick = onOpenFeedback,
                modifier = Modifier.weight(1.3f)
            )
        }

        ActionPill(
            label =
                when {
                    isExpanded -> "Collapse"
                    hasEvaluation -> "Expand"
                    else -> "Show answer"
                },
            enabled = !isDeleting,
            containerColor = if (hasEvaluation) surface else onSurface,
            contentColor = if (hasEvaluation) onSurface else surface,
            border =
                if (hasEvaluation) {
                    BorderStroke(1.dp, onSurface.copy(alpha = 0.08f))
                } else {
                    null
                },
            onClick = onToggleExpanded,
            modifier = Modifier.weight(1f)
        )

        Surface(
            onClick = onDelete,
            enabled = !isDeleting,
            modifier =
                Modifier
                    .width(ActionTrackHeight)
                    .fillMaxHeight()
                    .semantics { contentDescription = "Delete attempt" },
            shape = CircleShape,
            color = onSurface.copy(alpha = 0.06f),
            contentColor = muted
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = muted,
                        strokeWidth = 1.5.dp
                    )
                } else {
                    TrashGlyph(color = muted.copy(alpha = 0.85f))
                }
            }
        }
    }
}

@Composable
private fun ActionPill(
    label: String,
    enabled: Boolean,
    containerColor: Color,
    contentColor: Color,
    border: BorderStroke?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(50),
        color = if (enabled) containerColor else containerColor.copy(alpha = 0.4f),
        contentColor = contentColor,
        border = border
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun QuoteGlyph(color: Color) {
    Canvas(modifier = Modifier.size(width = 12.dp, height = 10.dp)) {
        val radius = size.height * 0.28f
        val stroke = 1.6.dp.toPx()

        listOf(size.width * 0.24f, size.width * 0.76f).forEach { centerX ->
            drawCircle(
                color = color,
                radius = radius,
                center = Offset(centerX, radius)
            )

            drawLine(
                color = color,
                start = Offset(centerX + radius * 0.7f, radius),
                end = Offset(centerX - radius * 0.4f, size.height),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun TrashGlyph(color: Color) {
    Canvas(modifier = Modifier.size(15.dp)) {
        val stroke = 1.5.dp.toPx()
        val w = size.width
        val h = size.height

        drawLine(
            color = color,
            start = Offset(w * 0.16f, h * 0.25f),
            end = Offset(w * 0.84f, h * 0.25f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(w * 0.4f, h * 0.11f),
            end = Offset(w * 0.6f, h * 0.11f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        val body =
            Path().apply {
                moveTo(w * 0.25f, h * 0.34f)
                lineTo(w * 0.31f, h * 0.87f)
                lineTo(w * 0.69f, h * 0.87f)
                lineTo(w * 0.75f, h * 0.34f)
            }

        drawPath(
            path = body,
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}