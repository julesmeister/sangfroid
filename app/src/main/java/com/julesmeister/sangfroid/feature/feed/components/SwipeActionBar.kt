package com.julesmeister.sangfroid.feature.feed.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidAlpha
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidDimensions
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.shape.SangfroidRadius
import com.julesmeister.sangfroid.core.designsystem.theme.SwipeDockColors
import com.julesmeister.sangfroid.core.designsystem.theme.swipeDockColors
import com.julesmeister.sangfroid.feature.feed.model.SwipeAction
import com.julesmeister.sangfroid.feature.feed.model.SwipeHighlight

private enum class DockGlyph {
    CROSS,
    ARROW_UP,
    BOOKMARK
}

@Composable
fun SwipeActionBar(
    enabled: Boolean,
    onAction: (SwipeAction) -> Unit,
    modifier: Modifier = Modifier,
    highlight: () -> SwipeHighlight = { SwipeHighlight.None }
) {
    val colors = swipeDockColors()
    val pillShape = RoundedCornerShape(SangfroidRadius.Pill)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(pillShape)
            .background(colors.container)
            .padding(SangfroidSpacing.Xs),
        horizontalArrangement = Arrangement.spacedBy(SangfroidSpacing.Xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DockCircleButton(
            action = SwipeAction.SKIP,
            glyph = DockGlyph.CROSS,
            label = "Skip question",
            accent = colors.skipAccent,
            colors = colors,
            enabled = enabled,
            highlight = highlight,
            onClick = { onAction(SwipeAction.SKIP) }
        )

        DockPrimaryButton(
            label = "Practice",
            colors = colors,
            enabled = enabled,
            highlight = highlight,
            onClick = { onAction(SwipeAction.PRACTICE) },
            modifier = Modifier.weight(1f)
        )

        DockCircleButton(
            action = SwipeAction.SAVE,
            glyph = DockGlyph.BOOKMARK,
            label = "Save question",
            accent = colors.saveAccent,
            colors = colors,
            enabled = enabled,
            highlight = highlight,
            onClick = { onAction(SwipeAction.SAVE) }
        )
    }
}

@Composable
private fun DockCircleButton(
    action: SwipeAction,
    glyph: DockGlyph,
    label: String,
    accent: Color,
    colors: SwipeDockColors,
    enabled: Boolean,
    highlight: () -> SwipeHighlight,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (pressed) SangfroidDimensions.SwipeDockPressedScale else 1f,
        label = "dockCirclePress"
    )

    Canvas(
        modifier = Modifier
            .size(SangfroidDimensions.SwipeDockButtonSize)
            .graphicsLayer {
                val progress = highlight().progressFor(action)
                val scale = pressScale *
                        (1f + SangfroidDimensions.SwipeDockHighlightScale * progress)

                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else SangfroidAlpha.DockDisabled
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick
            )
            .semantics { contentDescription = label }
    ) {
        val progress = highlight().progressFor(action)

        drawCircle(
            color = lerp(colors.buttonContainer, accent, progress)
        )

        drawGlyph(
            glyph = glyph,
            color = lerp(colors.buttonContent, colors.highlightContent, progress)
        )
    }
}

@Composable
private fun DockPrimaryButton(
    label: String,
    colors: SwipeDockColors,
    enabled: Boolean,
    highlight: () -> SwipeHighlight,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (pressed) SangfroidDimensions.SwipeDockPressedScale else 1f,
        label = "dockPrimaryPress"
    )

    Row(
        modifier = modifier
            .height(SangfroidDimensions.SwipeDockButtonSize)
            .graphicsLayer {
                val progress = highlight().progressFor(SwipeAction.PRACTICE)
                val scale = pressScale *
                        (1f + SangfroidDimensions.SwipeDockHighlightScale * progress * 0.5f)

                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else SangfroidAlpha.DockDisabled
            }
            .clip(RoundedCornerShape(SangfroidRadius.Pill))
            .background(colors.primaryContainer)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick
            )
            .padding(horizontal = SangfroidSpacing.Lg),
        horizontalArrangement = Arrangement.spacedBy(
            SangfroidSpacing.Xs,
            alignment = Alignment.CenterHorizontally
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            modifier = Modifier.size(SangfroidDimensions.SwipeDockIconSize)
        ) {
            drawGlyph(
                glyph = DockGlyph.ARROW_UP,
                color = colors.primaryContent
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = colors.primaryContent,
            maxLines = 1
        )
    }
}

private fun DrawScope.drawGlyph(
    glyph: DockGlyph,
    color: Color
) {
    val iconSize = SangfroidDimensions.SwipeDockIconSize.toPx()
    val strokeWidth = SangfroidDimensions.SwipeDockIconStroke.toPx()

    val left = (size.width - iconSize) / 2f
    val top = (size.height - iconSize) / 2f

    fun point(x: Float, y: Float): Offset {
        return Offset(
            x = left + iconSize * x,
            y = top + iconSize * y
        )
    }

    val stroke = Stroke(
        width = strokeWidth,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )

    when (glyph) {
        DockGlyph.CROSS -> {
            drawLine(
                color = color,
                start = point(0.22f, 0.22f),
                end = point(0.78f, 0.78f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            drawLine(
                color = color,
                start = point(0.78f, 0.22f),
                end = point(0.22f, 0.78f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        DockGlyph.ARROW_UP -> {
            val path = Path().apply {
                val bottom = point(0.5f, 0.86f)
                val tip = point(0.5f, 0.14f)
                val leftWing = point(0.2f, 0.44f)
                val rightWing = point(0.8f, 0.44f)

                moveTo(bottom.x, bottom.y)
                lineTo(tip.x, tip.y)
                moveTo(leftWing.x, leftWing.y)
                lineTo(tip.x, tip.y)
                lineTo(rightWing.x, rightWing.y)
            }

            drawPath(
                path = path,
                color = color,
                style = stroke
            )
        }

        DockGlyph.BOOKMARK -> {
            val path = Path().apply {
                val topLeft = point(0.26f, 0.12f)
                val topRight = point(0.74f, 0.12f)
                val bottomRight = point(0.74f, 0.88f)
                val notch = point(0.5f, 0.68f)
                val bottomLeft = point(0.26f, 0.88f)

                moveTo(topLeft.x, topLeft.y)
                lineTo(topRight.x, topRight.y)
                lineTo(bottomRight.x, bottomRight.y)
                lineTo(notch.x, notch.y)
                lineTo(bottomLeft.x, bottomLeft.y)
                close()
            }

            drawPath(
                path = path,
                color = color,
                style = stroke
            )
        }
    }
}