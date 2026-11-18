package com.julesmeister.sangfroid.feature.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidAlpha
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidDimensions
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.theme.QuestionCategoryColors
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.domain.model.Difficulty

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionMetadataRow(
    question: QuestionEntity,
    colors: QuestionCategoryColors,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SangfroidSpacing.Xs),
        verticalArrangement = Arrangement.spacedBy(SangfroidSpacing.Xs)
    ) {
        question.answerFramework
            ?.takeIf { it.isNotBlank() }
            ?.let { framework ->
                MetadataTag(colors = colors) {
                    TagText(
                        text = framework,
                        colors = colors
                    )
                }
            }

        MetadataTag(colors = colors) {
            TagText(
                text = "${question.estimatedMinutes} min",
                colors = colors
            )
        }

        MetadataTag(colors = colors) {
            DifficultyPips(
                difficulty = question.difficulty,
                colors = colors
            )

            TagText(
                text = question.difficulty.label(),
                colors = colors
            )
        }
    }
}

@Composable
private fun MetadataTag(
    colors: QuestionCategoryColors,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(colors.content.copy(alpha = SangfroidAlpha.SubtleFill))
            .padding(
                horizontal = SangfroidSpacing.Sm,
                vertical = SangfroidSpacing.Xs
            ),
        horizontalArrangement = Arrangement.spacedBy(SangfroidSpacing.Xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content()
    }
}

@Composable
private fun TagText(
    text: String,
    colors: QuestionCategoryColors
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = colors.content,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun DifficultyPips(
    difficulty: Difficulty,
    colors: QuestionCategoryColors
) {
    val level = difficulty.ordinal + 1

    Row(
        horizontalArrangement = Arrangement.spacedBy(
            SangfroidDimensions.MetadataPipSpacing
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(SangfroidDimensions.DifficultyLevels) { index ->
            val active = index < level

            Box(
                modifier = Modifier
                    .size(SangfroidDimensions.MetadataPip)
                    .clip(CircleShape)
                    .background(
                        if (active) {
                            colors.accent
                        } else {
                            colors.content.copy(alpha = SangfroidAlpha.PipInactive)
                        }
                    )
            )
        }
    }
}

private fun Difficulty.label(): String {
    return name
        .lowercase()
        .replaceFirstChar { character -> character.uppercase() }
}