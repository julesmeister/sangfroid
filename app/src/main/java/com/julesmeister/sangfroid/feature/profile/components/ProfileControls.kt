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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import com.julesmeister.sangfroid.core.designsystem.shape.SangfroidRadius
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidCharcoal
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidLime
import com.julesmeister.sangfroid.domain.model.SkillCode

enum class ProfileButtonVariant {
    Primary,
    Secondary,
    Ghost
}

/**
 * Icons used by the profile controls. Named ProfileIcon so it does not
 * collide with the existing ProfileGlyph in ProfileComponents.kt.
 */
enum class ProfileIcon {
    None,
    Plus,
    Cross,
    Check,
    Back,
    Document
}

private val ButtonHeight = 48.dp
private val ChipHeight = 36.dp
private val ChipIconContainer = 24.dp

@Composable
fun ProfileButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ProfileButtonVariant = ProfileButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    icon: ProfileIcon = ProfileIcon.None,
    fillWidth: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val clickable = enabled && !loading

    val scale by animateFloatAsState(
        targetValue = if (pressed && clickable) 0.97f else 1f,
        label = "profile-button-scale"
    )

    val scheme = MaterialTheme.colorScheme

    val targetContainer = when {
        !enabled -> scheme.surfaceVariant.copy(alpha = 0.6f)
        variant == ProfileButtonVariant.Primary -> scheme.onSurface
        variant == ProfileButtonVariant.Secondary -> scheme.surfaceVariant
        else -> Color.Transparent
    }

    val targetContent = when {
        !enabled -> scheme.onSurfaceVariant.copy(alpha = 0.6f)
        variant == ProfileButtonVariant.Primary -> scheme.surface
        else -> scheme.onSurface
    }

    val container by animateColorAsState(targetContainer, label = "profile-button-container")
    val content by animateColorAsState(targetContent, label = "profile-button-content")

    val shape = RoundedCornerShape(SangfroidRadius.Pill)

    Row(
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = ButtonHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(container)
            .then(
                if (variant == ProfileButtonVariant.Ghost) {
                    Modifier.border(1.dp, content.copy(alpha = 0.3f), shape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = clickable,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = content,
                strokeWidth = 2.dp
            )
        } else if (icon != ProfileIcon.None) {
            ProfileIconView(
                icon = icon,
                color = content,
                iconSize = 14.dp
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ProfileIconView(
    icon: ProfileIcon,
    color: Color,
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(iconSize)) {
        drawProfileIcon(
            icon = icon,
            color = color,
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun SelectedSkillChip(
    label: String,
    fromCv: Boolean,
    onRemove: () -> Unit
) {
    val container = if (fromCv) SangfroidLime else MaterialTheme.colorScheme.onSurface
    val content = if (fromCv) SangfroidCharcoal else MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .height(ChipHeight)
            .clip(RoundedCornerShape(SangfroidRadius.Pill))
            .background(container)
            .clickable(
                role = Role.Button,
                onClickLabel = "Remove $label",
                onClick = onRemove
            )
            .semantics { contentDescription = "$label. Tap to remove." }
            .padding(start = 12.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = content,
            maxLines = 1
        )

        Box(
            modifier = Modifier
                .size(ChipIconContainer)
                .clip(CircleShape)
                .background(content.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            ProfileIconView(
                icon = ProfileIcon.Cross,
                color = content,
                iconSize = 9.dp
            )
        }
    }
}

@Composable
fun AddSkillChip(
    label: String,
    onAdd: () -> Unit
) {
    val shape = RoundedCornerShape(SangfroidRadius.Pill)
    val content = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .height(ChipHeight)
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f), shape)
            .clickable(
                role = Role.Button,
                onClickLabel = "Add $label",
                onClick = onAdd
            )
            .semantics { contentDescription = "Add $label" }
            .padding(start = 6.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(ChipIconContainer)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            ProfileIconView(
                icon = ProfileIcon.Plus,
                color = content,
                iconSize = 10.dp
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = content,
            maxLines = 1
        )
    }
}

@Composable
fun ChoicePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    val container by animateColorAsState(
        if (selected) scheme.onSurface else scheme.surfaceVariant,
        label = "choice-container"
    )

    val content by animateColorAsState(
        if (selected) scheme.surface else scheme.onSurfaceVariant,
        label = "choice-content"
    )

    Row(
        modifier = Modifier
            .height(ChipHeight)
            .clip(RoundedCornerShape(SangfroidRadius.Pill))
            .background(container)
            .clickable(
                role = Role.RadioButton,
                onClickLabel = "Select $label",
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (selected) {
            ProfileIconView(
                icon = ProfileIcon.Check,
                color = content,
                iconSize = 10.dp
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = content,
            maxLines = 1
        )
    }
}

@Composable
fun StaticSkillChip(
    label: String,
    alreadyInProfile: Boolean
) {
    val shape = RoundedCornerShape(SangfroidRadius.Pill)
    val content = if (alreadyInProfile) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        SangfroidCharcoal
    }

    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(shape)
            .background(
                if (alreadyInProfile) {
                    MaterialTheme.colorScheme.surface
                } else {
                    SangfroidLime.copy(alpha = 0.75f)
                }
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        ProfileIconView(
            icon = if (alreadyInProfile) ProfileIcon.Check else ProfileIcon.Plus,
            color = content,
            iconSize = 9.dp
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = content,
            maxLines = 1
        )
    }
}

fun SkillCode.profileLabel(): String {
    return when (this) {
        SkillCode.CI_CD -> "CI/CD"
        SkillCode.REST_API -> "REST API"
        SkillCode.HTTP -> "HTTP"
        SkillCode.SQL -> "SQL"
        SkillCode.NOSQL -> "NoSQL"
        SkillCode.DYNAMODB -> "DynamoDB"
        SkillCode.AWS -> "AWS"
        else -> name
            .lowercase()
            .split("_")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
    }
}

fun DrawScope.drawProfileIcon(
    icon: ProfileIcon,
    color: Color,
    strokeWidth: Float
) {
    val w = size.width
    val h = size.height

    fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
        drawLine(
            color,
            Offset(w * x1, h * y1),
            Offset(w * x2, h * y2),
            strokeWidth,
            StrokeCap.Round
        )
    }

    when (icon) {
        ProfileIcon.None -> Unit

        ProfileIcon.Plus -> {
            line(0.5f, 0.1f, 0.5f, 0.9f)
            line(0.1f, 0.5f, 0.9f, 0.5f)
        }

        ProfileIcon.Cross -> {
            line(0.15f, 0.15f, 0.85f, 0.85f)
            line(0.85f, 0.15f, 0.15f, 0.85f)
        }

        ProfileIcon.Check -> {
            line(0.12f, 0.55f, 0.4f, 0.82f)
            line(0.4f, 0.82f, 0.9f, 0.2f)
        }

        ProfileIcon.Back -> {
            line(0.85f, 0.5f, 0.15f, 0.5f)
            line(0.15f, 0.5f, 0.45f, 0.2f)
            line(0.15f, 0.5f, 0.45f, 0.8f)
        }

        ProfileIcon.Document -> {
            line(0.2f, 0.06f, 0.6f, 0.06f)
            line(0.6f, 0.06f, 0.82f, 0.28f)
            line(0.82f, 0.28f, 0.82f, 0.94f)
            line(0.82f, 0.94f, 0.2f, 0.94f)
            line(0.2f, 0.94f, 0.2f, 0.06f)
            line(0.34f, 0.52f, 0.68f, 0.52f)
            line(0.34f, 0.7f, 0.58f, 0.7f)
        }
    }
}