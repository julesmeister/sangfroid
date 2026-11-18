package com.julesmeister.sangfroid.feature.profile.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.shape.SangfroidRadius
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidLime
import com.julesmeister.sangfroid.feature.profile.components.AddSkillChip
import com.julesmeister.sangfroid.feature.profile.components.ChoicePill
import com.julesmeister.sangfroid.feature.profile.components.ProfileButton
import com.julesmeister.sangfroid.feature.profile.components.ProfileButtonVariant
import com.julesmeister.sangfroid.feature.profile.components.ProfileIcon
import com.julesmeister.sangfroid.feature.profile.components.ProfileIconView
import com.julesmeister.sangfroid.feature.profile.components.SelectedSkillChip

@Composable
internal fun SectionCard(
    title: String,
    caption: String?,
    padding: Dp,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    caption?.let {
                        Text(
                            text = it,
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
internal fun TargetSection(
    model: TargetSectionModel,
    padding: Dp,
    onRoleChange: (String) -> Unit,
    onLevelSelect: (String) -> Unit
) {
    SectionCard(
        title = "Target",
        caption = "The role and depth your questions are written for.",
        padding = padding
    ) {
        FieldLabel("Role")

        ProfileTextField(
            value = model.role,
            onValueChange = onRoleChange,
            placeholder = "e.g. Customer Success Manager"
        )

        FieldLabel("Experience level")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            model.levelOptions.forEach { option ->
                ChoicePill(
                    label = option,
                    selected = option.equals(model.selectedLevel, ignoreCase = true),
                    onClick = { onLevelSelect(option) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SkillsSection(
    model: SkillsSectionModel,
    padding: Dp,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    var pickerOpen by rememberSaveable { mutableStateOf(model.selected.isEmpty()) }
    var query by rememberSaveable { mutableStateOf("") }

    SectionCard(
        title = "Your skills",
        caption = null,
        padding = padding,
        trailing = { SkillCountBadge(model.selected.size) }
    ) {
        RecommendationNote()

        if (model.selected.isEmpty()) {
            Text(
                text = "No skills yet. Add a few so we can recommend the right questions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                model.selected.forEach { chip ->
                    SelectedSkillChip(
                        label = chip.label,
                        fromCv = chip.fromCv,
                        onRemove = { onRemove(chip.key) }
                    )
                }
            }

            SkillLegend(showCvLegend = model.hasCvSkills)
        }

        ProfileButton(
            label = if (pickerOpen) "Done adding" else "Add skills",
            onClick = {
                pickerOpen = !pickerOpen

                if (!pickerOpen) {
                    query = ""
                }
            },
            variant = ProfileButtonVariant.Secondary,
            icon = if (pickerOpen) ProfileIcon.Check else ProfileIcon.Plus
        )

        AnimatedVisibility(
            visible = pickerOpen,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            SkillPicker(
                groups = model.availableGroups,
                query = query,
                onQueryChange = { query = it },
                onAdd = onAdd
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillPicker(
    groups: List<SkillGroupModel>,
    query: String,
    onQueryChange: (String) -> Unit,
    onAdd: (String) -> Unit
) {
    val trimmed = query.trim()

    val visibleGroups = groups
        .map { group ->
            group.copy(
                skills = group.skills.filter {
                    trimmed.isBlank() || it.label.contains(trimmed, ignoreCase = true)
                }
            )
        }
        .filter { it.skills.isNotEmpty() }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ProfileTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Search skills"
        )

        if (visibleGroups.isEmpty()) {
            Text(
                text = if (trimmed.isBlank()) {
                    "You've added every available skill."
                } else {
                    "No skills match \"$trimmed\"."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        visibleGroups.forEach { group ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FieldLabel(group.title)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    group.skills.forEach { chip ->
                        AddSkillChip(
                            label = chip.label,
                            onAdd = { onAdd(chip.key) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationNote() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProfileIconView(
            icon = ProfileIcon.Check,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            iconSize = 12.dp
        )

        Text(
            text = "These skills decide which interview questions Sangfroid recommends.",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun SkillLegend(showCvLegend: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showCvLegend) {
            LegendDot(label = "From CV", color = SangfroidLime)
        }

        LegendDot(label = "Added by you", color = MaterialTheme.colorScheme.onSurface)

        Text(
            text = "Tap × to remove",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun LegendDot(
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun SkillCountBadge(count: Int) {
    Text(
        text = count.toString(),
        modifier = Modifier
            .clip(RoundedCornerShape(SangfroidRadius.Pill))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
internal fun FieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = if (focused) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
        },
        label = "profile-field-border"
    )

    val shape = RoundedCornerShape(14.dp)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        interactionSource = interactionSource,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(if (focused) 2.dp else 1.dp, borderColor, shape)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                innerTextField()
            }
        }
    )
}