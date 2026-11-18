package com.julesmeister.sangfroid.feature.history.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.component.SangfroidConfirmationDialog
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.feature.history.model.RevisionHistoryItem

private val CardShape = RoundedCornerShape(22.dp)
private val ActionTrackHeight = 38.dp
private val ActionTrackPadding = 3.dp
private const val CollapsedPreviewLines = 4

@Composable
fun RevisionHistorySection(
    items: List<RevisionHistoryItem>,
    deletingRevisionIds: Set<String>,
    onUseRevision: (RevisionHistoryItem) -> Unit,
    onDeleteRevision: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SangfroidSpacing.Xxs)
            ) {
                Text(
                    text = "Saved revisions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Browse older answer versions or use one as your next practice draft.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = items.size.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        items.forEach { item ->
            RevisionHistoryCard(
                item = item,
                isDeleting = item.id in deletingRevisionIds,
                onUseRevision = onUseRevision,
                onDeleteRevision = onDeleteRevision
            )
        }
    }
}

@Composable
private fun RevisionHistoryCard(
    item: RevisionHistoryItem,
    isDeleting: Boolean,
    onUseRevision: (RevisionHistoryItem) -> Unit,
    onDeleteRevision: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember(item.id) { mutableStateOf(false) }
    var showDeleteConfirmation by remember(item.id) { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        SangfroidConfirmationDialog(
            title = "Delete ${item.title}?",
            message =
                "This saved answer revision will be permanently " +
                        "removed. Older attempts and other saved " +
                        "versions will remain.",
            confirmText = "Delete revision",
            onConfirm = {
                showDeleteConfirmation = false
                onDeleteRevision(item.id)
            },
            onDismiss = {
                showDeleteConfirmation = false
            }
        )
    }

    val accent = MaterialTheme.colorScheme.primary

    val chevronRotation by
    animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(220),
        label = "revision-chevron"
    )

    Surface(
        onClick = { if (!isDeleting) isExpanded = !isExpanded },
        enabled = !isDeleting,
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border =
            BorderStroke(
                1.dp,
                if (isExpanded) accent.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    DocumentGlyph(color = accent)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.sourceLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            maxLines = 1,
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(accent.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                        )

                        Text(
                            text = item.createdLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Chevron(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.graphicsLayer { rotationZ = chevronRotation }
                )
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (isExpanded) "FULL REVISION" else "PREVIEW",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent
                )

                SelectionContainer {
                    Text(
                        text =
                            if (isExpanded) {
                                item.content.ifBlank { item.preview }
                            } else {
                                item.preview
                            },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (isExpanded) Int.MAX_VALUE else CollapsedPreviewLines,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            ActionTrack(
                isExpanded = isExpanded,
                isDeleting = isDeleting,
                onUse = { onUseRevision(item) },
                onToggle = { isExpanded = !isExpanded },
                onDelete = { showDeleteConfirmation = true }
            )
        }
    }
}

@Composable
private fun ActionTrack(
    isExpanded: Boolean,
    isDeleting: Boolean,
    onUse: () -> Unit,
    onToggle: () -> Unit,
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
        ActionPill(
            label = "Use as draft",
            enabled = !isDeleting,
            containerColor = onSurface,
            contentColor = surface,
            border = null,
            onClick = onUse,
            modifier = Modifier.weight(1.3f)
        )

        ActionPill(
            label = if (isExpanded) "Collapse" else "Expand",
            enabled = !isDeleting,
            containerColor = surface,
            contentColor = onSurface,
            border = BorderStroke(1.dp, onSurface.copy(alpha = 0.08f)),
            onClick = onToggle,
            modifier = Modifier.weight(1f)
        )

        Surface(
            onClick = onDelete,
            enabled = !isDeleting,
            modifier =
                Modifier
                    .width(ActionTrackHeight)
                    .fillMaxHeight()
                    .semantics { contentDescription = "Delete revision" },
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
private fun DocumentGlyph(color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val stroke = 1.6.dp.toPx()
        val w = size.width
        val h = size.height

        val outline =
            Path().apply {
                moveTo(w * 0.22f, h * 0.08f)
                lineTo(w * 0.62f, h * 0.08f)
                lineTo(w * 0.8f, h * 0.26f)
                lineTo(w * 0.8f, h * 0.92f)
                lineTo(w * 0.22f, h * 0.92f)
                close()
            }

        drawPath(
            path = outline,
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        listOf(0.46f, 0.62f, 0.78f).forEach { y ->
            drawLine(
                color = color,
                start = Offset(w * 0.34f, h * y),
                end = Offset(w * 0.68f, h * y),
                strokeWidth = stroke * 0.85f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun Chevron(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(18.dp)) {
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