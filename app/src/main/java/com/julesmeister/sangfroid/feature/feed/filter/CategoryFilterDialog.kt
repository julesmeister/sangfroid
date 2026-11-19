package com.julesmeister.sangfroid.feature.feed.filter

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.julesmeister.sangfroid.core.designsystem.component.SangfroidPillButton
import com.julesmeister.sangfroid.domain.model.QuestionCategory

@Composable
fun CategoryFilterDialog(
    visible: Boolean,
    availableCategories: Set<QuestionCategory>,
    selectedCategories: Set<QuestionCategory>,
    onApply: (Set<QuestionCategory>) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) {
        return
    }

    var pendingSelection by
    remember(
        visible,
        availableCategories,
        selectedCategories
    ) {
        mutableStateOf(
            selectedCategories
                .intersect(
                    availableCategories
                )
        )
    }

    val sortedCategories =
        remember(
            availableCategories
        ) {
            availableCategories
                .sortedBy { category ->
                    category
                        .name
                        .toDisplayLabel()
                }
        }

    Dialog(
        onDismissRequest =
            onDismiss,
        properties =
            DialogProperties(
                usePlatformDefaultWidth =
                    false
            )
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth(
                        0.92f
                    )
                    .heightIn(
                        max =
                            620.dp
                    ),
            shape =
                RoundedCornerShape(
                    28.dp
                ),
            color =
                MaterialTheme
                    .colorScheme
                    .surface,
            contentColor =
                MaterialTheme
                    .colorScheme
                    .onSurface,
            tonalElevation =
                2.dp,
            shadowElevation =
                12.dp
        ) {
            Column(
                modifier =
                    Modifier.padding(
                        horizontal = 22.dp,
                        vertical = 20.dp
                    )
            ) {
                CategoryFilterHeader(
                    onDismiss =
                        onDismiss
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            18.dp
                        )
                )

                Text(
                    text =
                        "Choose which question categories appear in your feed. " +
                                "Hidden questions stay safely in your library.",
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )

                if (
                    sortedCategories.isEmpty()
                ) {
                    EmptyCategoryPanel()
                } else {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(
                                    1f,
                                    fill = false
                                )
                                .clip(
                                    RoundedCornerShape(
                                        20.dp
                                    )
                                )
                                .background(
                                    MaterialTheme
                                        .colorScheme
                                        .surfaceVariant
                                        .copy(
                                            alpha = 0.42f
                                        )
                                )
                                .padding(
                                    vertical = 6.dp
                                )
                    ) {
                        items(
                            items =
                                sortedCategories,
                            key = { category ->
                                category.name
                            }
                        ) { category ->
                            CategoryFilterRow(
                                category =
                                    category,
                                selected =
                                    category in
                                            pendingSelection,
                                onClick = {
                                    pendingSelection =
                                        if (
                                            category in
                                            pendingSelection
                                        ) {
                                            pendingSelection -
                                                    category
                                        } else {
                                            pendingSelection +
                                                    category
                                        }
                                }
                            )
                        }
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            18.dp
                        )
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Text(
                        text =
                            if (
                                pendingSelection.isEmpty()
                            ) {
                                "All categories"
                            } else {
                                "${pendingSelection.size} selected"
                            },
                        modifier =
                            Modifier.weight(
                                1f
                            ),
                        style =
                            MaterialTheme
                                .typography
                                .labelMedium,
                        fontWeight =
                            FontWeight.SemiBold,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )

                    if (
                        pendingSelection.isNotEmpty()
                    ) {
                        Text(
                            text =
                                "Clear",
                            modifier =
                                Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            50
                                        )
                                    )
                                    .clickable(
                                        role =
                                            Role.Button
                                    ) {
                                        pendingSelection =
                                            emptySet()
                                    }
                                    .padding(
                                        horizontal = 12.dp,
                                        vertical = 10.dp
                                    ),
                            style =
                                MaterialTheme
                                    .typography
                                    .labelLarge,
                            fontWeight =
                                FontWeight.Bold,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }

                    SangfroidPillButton(
                        text =
                            "Apply",
                        onClick = {
                            onApply(
                                pendingSelection
                            )
                        },
                        enabled =
                            true,
                        emphasized =
                            true
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterHeader(
    onDismiss: () -> Unit
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.Top
    ) {
        Column(
            modifier =
                Modifier.weight(
                    1f
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    2.dp
                )
        ) {
            Text(
                text =
                    "FILTER FEED",
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                fontWeight =
                    FontWeight.ExtraBold,
                color =
                    MaterialTheme
                        .colorScheme
                        .primary
            )

            Text(
                text =
                    "Question categories",
                style =
                    MaterialTheme
                        .typography
                        .headlineSmall,
                fontWeight =
                    FontWeight.ExtraBold
            )
        }

        Surface(
            onClick =
                onDismiss,
            modifier =
                Modifier
                    .size(
                        36.dp
                    )
                    .semantics {
                        role =
                            Role.Button

                        contentDescription =
                            "Close category filter"
                    },
            shape =
                CircleShape,
            color =
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
                    .copy(
                        alpha = 0.62f
                    ),
            contentColor =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        ) {
            Box(
                contentAlignment =
                    Alignment.Center
            ) {
                CloseGlyph(
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                    modifier =
                        Modifier.size(
                            12.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    category: QuestionCategory,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    role =
                        Role.Checkbox,
                    onClick =
                        onClick
                )
                .padding(
                    horizontal = 14.dp,
                    vertical = 12.dp
                ),
        horizontalArrangement =
            Arrangement.spacedBy(
                12.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        CategorySelectionIndicator(
            selected =
                selected
        )

        Column(
            modifier =
                Modifier.weight(
                    1f
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    2.dp
                )
        ) {
            Text(
                text =
                    category
                        .name
                        .toDisplayLabel(),
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface,
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                text =
                    category.supportingLabel(),
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CategorySelectionIndicator(
    selected: Boolean
) {
    val containerColor =
        if (selected) {
            MaterialTheme
                .colorScheme
                .secondary
        } else {
            Color.Transparent
        }

    val borderColor =
        if (selected) {
            MaterialTheme
                .colorScheme
                .secondary
        } else {
            MaterialTheme
                .colorScheme
                .outline
                .copy(
                    alpha = 0.7f
                )
        }

    Surface(
        modifier =
            Modifier.size(
                24.dp
            ),
        shape =
            CircleShape,
        color =
            containerColor,
        border =
            androidx.compose.foundation.BorderStroke(
                width =
                    1.5.dp,
                color =
                    borderColor
            )
    ) {
        if (selected) {
            Box(
                contentAlignment =
                    Alignment.Center
            ) {
                CheckGlyph(
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSecondary,
                    modifier =
                        Modifier.size(
                            12.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun EmptyCategoryPanel() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        20.dp
                    )
                )
                .background(
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
                        .copy(
                            alpha = 0.42f
                        )
                )
                .padding(
                    18.dp
                ),
        verticalArrangement =
            Arrangement.spacedBy(
                4.dp
            )
    ) {
        Text(
            text =
                "No categories yet",
            style =
                MaterialTheme
                    .typography
                    .titleSmall,
            fontWeight =
                FontWeight.Bold
        )

        Text(
            text =
                "Categories will appear after questions are loaded.",
            style =
                MaterialTheme
                    .typography
                    .bodySmall,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}

@Composable
private fun CheckGlyph(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier =
            modifier
    ) {
        val strokeWidth =
            2.dp.toPx()

        drawLine(
            color =
                color,
            start =
                Offset(
                    x = size.width * 0.17f,
                    y = size.height * 0.53f
                ),
            end =
                Offset(
                    x = size.width * 0.41f,
                    y = size.height * 0.76f
                ),
            strokeWidth =
                strokeWidth,
            cap =
                StrokeCap.Round
        )

        drawLine(
            color =
                color,
            start =
                Offset(
                    x = size.width * 0.41f,
                    y = size.height * 0.76f
                ),
            end =
                Offset(
                    x = size.width * 0.84f,
                    y = size.height * 0.25f
                ),
            strokeWidth =
                strokeWidth,
            cap =
                StrokeCap.Round
        )
    }
}

@Composable
private fun CloseGlyph(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier =
            modifier
    ) {
        val strokeWidth =
            1.8.dp.toPx()

        drawLine(
            color =
                color,
            start =
                Offset(
                    x = 0f,
                    y = 0f
                ),
            end =
                Offset(
                    x = size.width,
                    y = size.height
                ),
            strokeWidth =
                strokeWidth,
            cap =
                StrokeCap.Round
        )

        drawLine(
            color =
                color,
            start =
                Offset(
                    x = size.width,
                    y = 0f
                ),
            end =
                Offset(
                    x = 0f,
                    y = size.height
                ),
            strokeWidth =
                strokeWidth,
            cap =
                StrokeCap.Round
        )
    }
}

private fun QuestionCategory.supportingLabel():
        String {
    return when {
        name.contains(
            "BEHAVIOR",
            ignoreCase = true
        ) ->
            "Experience, teamwork and reflection"

        name.contains(
            "TECH",
            ignoreCase = true
        ) ->
            "Engineering knowledge and implementation"

        name.contains(
            "SYSTEM",
            ignoreCase = true
        ) ->
            "Architecture, scalability and trade-offs"

        name.contains(
            "SITUATION",
            ignoreCase = true
        ) ->
            "Judgement in realistic work scenarios"

        name.contains(
            "LEAD",
            ignoreCase = true
        ) ->
            "Ownership, influence and decision-making"

        name.contains(
            "GENERAL",
            ignoreCase = true
        ) ->
            "Broad interview preparation"

        else ->
            "Questions in this interview category"
    }
}

private fun String.toDisplayLabel():
        String {
    return lowercase()
        .split(
            "_"
        )
        .joinToString(
            " "
        ) { word ->
            word.replaceFirstChar { character ->
                character.uppercase()
            }
        }
}