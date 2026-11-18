package com.julesmeister.sangfroid.feature.saved.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.component.SangfroidConfirmationDialog
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.theme.QuestionCategoryColors
import com.julesmeister.sangfroid.core.designsystem.theme.questionCategoryColors
import com.julesmeister.sangfroid.domain.progress.QuestionMasteryLevel
import com.julesmeister.sangfroid.feature.saved.model.SavedQuestionItem

private val CardShape = RoundedCornerShape(26.dp)
private val ActionTrackHeight = 40.dp
private val ActionTrackPadding = 3.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SavedQuestionCard(
    item: SavedQuestionItem,
    isRemoving: Boolean,
    onOpenHistory: (String) -> Unit,
    onPractice: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRemoveConfirmation by
    remember(item.id) {
        mutableStateOf(false)
    }

    val colors =
        questionCategoryColors(item.question.category)

    if (showRemoveConfirmation) {
        SangfroidConfirmationDialog(
            title = "Remove from Saved?",
            message =
                "This question will leave your Saved library. " +
                        "Its practice attempts, evaluations, and saved " +
                        "revisions will remain available in Progress.",
            confirmText = "Remove",
            onConfirm = {
                showRemoveConfirmation = false
                onRemove(item.id)
            },
            onDismiss = {
                showRemoveConfirmation = false
            },
            isDestructive = false
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.container,
        contentColor = colors.content,
        shape = CardShape
    ) {
        Column(
            modifier = Modifier.padding(SangfroidSpacing.CardContent),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryPill(
                    label = item.categoryLabel,
                    colors = colors
                )

                MasteryMeter(
                    masteryLevel = item.masteryLevel,
                    colors = colors
                )
            }

            Text(
                text = item.question.question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = colors.content,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InfoChip(
                    text = item.progressLabel,
                    colors = colors
                )

                item.revisionLabel
                    ?.takeIf(String::isNotBlank)
                    ?.let { revision ->
                        InfoChip(
                            text = revision,
                            colors = colors
                        )
                    }
            }

            item.question.whyAsked
                .trim()
                .takeIf(String::isNotBlank)
                ?.let { whyAsked ->
                    WhyAskedPanel(
                        text = whyAsked,
                        colors = colors
                    )
                }

            SavedActions(
                colors = colors,
                isRemoving = isRemoving,
                onPractice = { onPractice(item.id) },
                onOpenHistory = { onOpenHistory(item.id) },
                onRemove = { showRemoveConfirmation = true }
            )
        }
    }
}

@Composable
private fun CategoryPill(
    label: String,
    colors: QuestionCategoryColors
) {
    Row(
        modifier =
            Modifier
                .clip(RoundedCornerShape(50))
                .background(colors.accent.copy(alpha = 0.14f))
                .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier =
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(colors.accent)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = colors.accent,
            maxLines = 1
        )
    }
}

@Composable
private fun MasteryMeter(
    masteryLevel: QuestionMasteryLevel,
    colors: QuestionCategoryColors
) {
    val step =
        when (masteryLevel) {
            QuestionMasteryLevel.NOT_STARTED -> 0
            QuestionMasteryLevel.NEEDS_WORK -> 1
            QuestionMasteryLevel.DEVELOPING -> 2
            QuestionMasteryLevel.STRONG -> 3
            QuestionMasteryLevel.MASTERED -> 4
        }

    val tone =
        when (masteryLevel) {
            QuestionMasteryLevel.NEEDS_WORK ->
                MaterialTheme.colorScheme.error

            QuestionMasteryLevel.NOT_STARTED ->
                colors.secondaryContent

            else ->
                colors.accent
        }

    Row(
        modifier =
            Modifier
                .clip(RoundedCornerShape(50))
                .background(colors.content.copy(alpha = 0.06f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier =
                        Modifier
                            .width(10.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (index < step) {
                                    tone
                                } else {
                                    colors.content.copy(alpha = 0.12f)
                                }
                            )
                )
            }
        }

        Text(
            text = masteryLevel.displayName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = tone,
            maxLines = 1
        )
    }
}

@Composable
private fun InfoChip(
    text: String,
    colors: QuestionCategoryColors
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = colors.secondaryContent,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier =
            Modifier
                .clip(RoundedCornerShape(50))
                .background(colors.content.copy(alpha = 0.06f))
                .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
private fun WhyAskedPanel(
    text: String,
    colors: QuestionCategoryColors
) {
    val panelColor =
        lerp(
            colors.container,
            MaterialTheme.colorScheme.surface,
            0.55f
        )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(RoundedCornerShape(16.dp))
                .background(panelColor)
    ) {
        Box(
            modifier =
                Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(colors.accent.copy(alpha = 0.6f))
        )

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "WHY THIS IS ASKED",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = colors.secondaryContent.copy(alpha = 0.8f)
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = colors.content,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SavedActions(
    colors: QuestionCategoryColors,
    isRemoving: Boolean,
    onPractice: () -> Unit,
    onOpenHistory: () -> Unit,
    onRemove: () -> Unit
) {
    val raisedColor =
        lerp(
            colors.container,
            MaterialTheme.colorScheme.surface,
            0.6f
        )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(ActionTrackHeight)
                .clip(RoundedCornerShape(50))
                .background(colors.content.copy(alpha = 0.06f))
                .padding(ActionTrackPadding),
        horizontalArrangement = Arrangement.spacedBy(ActionTrackPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionPill(
            label = "Practise",
            enabled = !isRemoving,
            containerColor = colors.content,
            contentColor = colors.container,
            border = null,
            onClick = onPractice,
            modifier = Modifier.weight(1.3f)
        )

        ActionPill(
            label = "History",
            enabled = !isRemoving,
            containerColor = raisedColor,
            contentColor = colors.content,
            border = BorderStroke(1.dp, colors.content.copy(alpha = 0.08f)),
            onClick = onOpenHistory,
            modifier = Modifier.weight(0.9f)
        )

        /*
         * Neutral, category-tinted remove action. The confirmation
         * dialog provides the safety, so no alarm-red is needed here.
         */
        Surface(
            onClick = onRemove,
            enabled = !isRemoving,
            modifier =
                Modifier
                    .width(ActionTrackHeight)
                    .fillMaxHeight()
                    .semantics {
                        contentDescription = "Remove from Saved"
                    },
            shape = CircleShape,
            color = colors.content.copy(alpha = 0.07f),
            contentColor = colors.secondaryContent
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isRemoving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = colors.accent,
                        strokeWidth = 1.5.dp
                    )
                } else {
                    RemoveGlyph(
                        color = colors.secondaryContent.copy(alpha = 0.85f)
                    )
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
private fun RemoveGlyph(
    color: Color
) {
    Canvas(modifier = Modifier.size(14.dp)) {
        drawLine(
            color = color,
            start = Offset(size.width * 0.2f, size.height / 2f),
            end = Offset(size.width * 0.8f, size.height / 2f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
