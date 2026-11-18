package com.julesmeister.sangfroid.feature.profile.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.shape.SangfroidRadius
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidCharcoal
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidLime

val ProfileButtonHeight: Dp = 48.dp
val ProfileChipHeight: Dp = 36.dp

enum class ProfileButtonStyle {
    Primary,
    Secondary,
    Success
}

enum class ProfileGlyph {
    Plus,
    Cross,
    Check,
    Back,
    Document,
    ChevronDown,
    ChevronUp
}

@Composable
fun rememberProfilePressScale(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true
): Float {
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.96f else 1f,
        label = "profile-press-scale"
    )

    return scale
}

/**
 * The single button used across the profile screen.
 * Every action shares the same height, shape, and typography.
 */
@Composable
fun ProfileButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ProfileButtonStyle = ProfileButtonStyle.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingGlyph: ProfileGlyph? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val clickable = enabled && !loading
    val scale = rememberProfilePressScale(interactionSource, clickable)
    val colors = MaterialTheme.colorScheme

    val targetContainer =
        when {
            !enabled -> colors.surfaceVariant
            style == ProfileButtonStyle.Primary -> colors.onSurface
            style == ProfileButtonStyle.Success -> SangfroidLime
            else -> colors.surfaceVariant
        }

    val targetContent =
        when {
            !enabled -> colors.onSurfaceVariant.copy(alpha = 0.6f)
            style == ProfileButtonStyle.Primary -> colors.surface
            style == ProfileButtonStyle.Success -> SangfroidCharcoal
            else -> colors.onSurface
        }

    val container by animateColorAsState(targetContainer, label = "profile-button-container")
    val content by animateColorAsState(targetContent, label = "profile-button-content")

    Row(
        modifier =
            modifier
                .heightIn(min = ProfileButtonHeight)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(SangfroidRadius.Pill))
                .background(container)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = clickable,
                    role = Role.Button,
                    onClick = onClick
                )
                .padding(
                    horizontal = SangfroidSpacing.Lg,
                    vertical = SangfroidSpacing.Xs
                ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = content,
                strokeWidth = 2.dp
            )

            Spacer(Modifier.width(SangfroidSpacing.Xs))
        } else if (leadingGlyph != null) {
            GlyphIcon(
                glyph = leadingGlyph,
                color = content,
                glyphSize = 14.dp
            )

            Spacer(Modifier.width(SangfroidSpacing.Xs))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ProfileTextLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingGlyph: ProfileGlyph? = null
) {
    val color = MaterialTheme.colorScheme.primary

    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(SangfroidRadius.Pill))
                .clickable(role = Role.Button, onClick = onClick)
                .padding(
                    horizontal = SangfroidSpacing.Xs,
                    vertical = SangfroidSpacing.Xs
                ),
        horizontalArrangement = Arrangement.spacedBy(SangfroidSpacing.Xxs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = color
        )

        if (trailingGlyph != null) {
            GlyphIcon(
                glyph = trailingGlyph,
                color = color,
                glyphSize = 10.dp
            )
        }
    }
}

/**
 * Selected chips: lime with a ✕ bubble (tap removes).
 * Available chips: outlined with a + bubble (tap adds).
 */
@Composable
fun SkillChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fromCv: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberProfilePressScale(interactionSource)
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(SangfroidRadius.Pill)

    val container by animateColorAsState(
        if (selected) SangfroidLime else colors.surface,
        label = "skill-chip-container"
    )

    val content = if (selected) SangfroidCharcoal else colors.onSurface

    val description =
        if (selected) {
            "$label${if (fromCv) ", from your CV" else ""}. Selected. Tap to remove."
        } else {
            "Add $label"
        }

    Row(
        modifier =
            modifier
                .height(ProfileChipHeight)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(shape)
                .background(container)
                .then(
                    if (selected) {
                        Modifier
                    } else {
                        Modifier.border(1.dp, colors.outlineVariant, shape)
                    }
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClickLabel = if (selected) "Remove $label" else "Add $label",
                    onClick = onClick
                )
                .semantics { contentDescription = description }
                .padding(
                    start = if (selected) SangfroidSpacing.Md else SangfroidSpacing.Xs,
                    end = if (selected) SangfroidSpacing.Xs else SangfroidSpacing.Md
                ),
        horizontalArrangement = Arrangement.spacedBy(SangfroidSpacing.Xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!selected) {
            GlyphBubble(
                glyph = ProfileGlyph.Plus,
                background = colors.surfaceVariant,
                tint = colors.onSurface
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = content,
            maxLines = 1
        )

        if (selected && fromCv) {
            TagLabel(
                text = "CV",
                containerColor = SangfroidCharcoal.copy(alpha = 0.12f),
                contentColor = SangfroidCharcoal
            )
        }

        if (selected) {
            GlyphBubble(
                glyph = ProfileGlyph.Cross,
                background = SangfroidCharcoal.copy(alpha = 0.12f),
                tint = SangfroidCharcoal
            )
        }
    }
}

@Composable
fun ChoicePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = rememberProfilePressScale(interactionSource)
    val colors = MaterialTheme.colorScheme

    val container by animateColorAsState(
        if (selected) colors.onSurface else colors.surfaceVariant,
        label = "choice-container"
    )

    val content by animateColorAsState(
        if (selected) colors.surface else colors.onSurfaceVariant,
        label = "choice-content"
    )

    Box(
        modifier =
            modifier
                .height(ProfileChipHeight)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(SangfroidRadius.Pill))
                .background(container)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.RadioButton,
                    onClickLabel = "Select $label",
                    onClick = onClick
                )
                .padding(horizontal = SangfroidSpacing.Md),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = content
        )
    }
}

@Composable
fun ProfileCard(
    title: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier.padding(SangfroidSpacing.Lg),
            verticalArrangement = Arrangement.spacedBy(SangfroidSpacing.Md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SangfroidSpacing.Sm)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(SangfroidSpacing.Xxs)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (!caption.isNullOrBlank()) {
                        Text(
                            text = caption,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                trailing?.invoke()
            }

            content()
        }
    }
}

@Composable
fun InfoBanner(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(SangfroidLime.copy(alpha = 0.18f))
                .padding(SangfroidSpacing.Md),
        horizontalArrangement = Arrangement.spacedBy(SangfroidSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlyphBubble(
            glyph = ProfileGlyph.Check,
            background = SangfroidLime,
            tint = SangfroidCharcoal
        )

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun TagLabel(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(SangfroidRadius.Pill))
                .background(containerColor)
                .padding(horizontal = SangfroidSpacing.Sm, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            maxLines = 1
        )
    }
}

@Composable
fun CountBadge(count: Int) {
    TagLabel(
        text = count.toString(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun GlyphBubble(
    glyph: ProfileGlyph,
    background: Color,
    tint: Color,
    modifier: Modifier = Modifier,
    bubbleSize: Dp = 22.dp
) {
    Box(
        modifier =
            modifier
                .size(bubbleSize)
                .clip(CircleShape)
                .background(background),
        contentAlignment = Alignment.Center
    ) {
        GlyphIcon(
            glyph = glyph,
            color = tint,
            glyphSize = bubbleSize * 0.42f
        )
    }
}

@Composable
fun GlyphIcon(
    glyph: ProfileGlyph,
    color: Color,
    modifier: Modifier = Modifier,
    glyphSize: Dp = 14.dp
) {
    Canvas(modifier = modifier.size(glyphSize)) {
        val stroke = (this.size.minDimension * 0.14f).coerceAtLeast(1.5.dp.toPx())

        drawGlyph(glyph, color, stroke)
    }
}

private fun DrawScope.drawGlyph(
    glyph: ProfileGlyph,
    color: Color,
    stroke: Float
) {
    val w = size.width
    val h = size.height

    fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
        drawLine(
            color = color,
            start = Offset(w * x1, h * y1),
            end = Offset(w * x2, h * y2),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }

    when (glyph) {
        ProfileGlyph.Plus -> {
            line(0.5f, 0.1f, 0.5f, 0.9f)
            line(0.1f, 0.5f, 0.9f, 0.5f)
        }

        ProfileGlyph.Cross -> {
            line(0.15f, 0.15f, 0.85f, 0.85f)
            line(0.85f, 0.15f, 0.15f, 0.85f)
        }

        ProfileGlyph.Check -> {
            line(0.12f, 0.55f, 0.4f, 0.8f)
            line(0.4f, 0.8f, 0.9f, 0.22f)
        }

        ProfileGlyph.Back -> {
            line(0.85f, 0.5f, 0.15f, 0.5f)
            line(0.15f, 0.5f, 0.45f, 0.2f)
            line(0.15f, 0.5f, 0.45f, 0.8f)
        }

        ProfileGlyph.ChevronDown -> {
            line(0.15f, 0.35f, 0.5f, 0.7f)
            line(0.5f, 0.7f, 0.85f, 0.35f)
        }

        ProfileGlyph.ChevronUp -> {
            line(0.15f, 0.65f, 0.5f, 0.3f)
            line(0.5f, 0.3f, 0.85f, 0.65f)
        }

        ProfileGlyph.Document -> {
            line(0.2f, 0.05f, 0.6f, 0.05f)
            line(0.6f, 0.05f, 0.85f, 0.3f)
            line(0.85f, 0.3f, 0.85f, 0.95f)
            line(0.85f, 0.95f, 0.2f, 0.95f)
            line(0.2f, 0.95f, 0.2f, 0.05f)
            line(0.35f, 0.5f, 0.7f, 0.5f)
            line(0.35f, 0.7f, 0.6f, 0.7f)
        }
    }
}