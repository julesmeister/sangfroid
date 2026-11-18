package com.julesmeister.sangfroid.feature.progress.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.feature.progress.CriterionProgressItem

@Composable
fun SkillProgressSection(
    title: String,
    supportingText: String,
    items: List<CriterionProgressItem>,
    emphasized: Boolean,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        return
    }

    Column(
        modifier =
            modifier.fillMaxWidth(),
        verticalArrangement =
            Arrangement.spacedBy(
                SangfroidSpacing.Sm
            )
    ) {
        Column(
            verticalArrangement =
                Arrangement.spacedBy(
                    SangfroidSpacing.Xxs
                )
        ) {
            Text(
                text =
                    title,
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground
            )

            Text(
                text =
                    supportingText,
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

        items.forEach { item ->
            CriterionRow(
                item =
                    item,
                emphasized =
                    emphasized
            )
        }
    }
}

@Composable
private fun CriterionRow(
    item: CriterionProgressItem,
    emphasized: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier =
            modifier.fillMaxWidth(),
        color =
            if (emphasized) {
                MaterialTheme
                    .colorScheme
                    .tertiaryContainer
            } else {
                MaterialTheme
                    .colorScheme
                    .errorContainer
                    .copy(
                        alpha = 0.72f
                    )
            },
        contentColor =
            if (emphasized) {
                MaterialTheme
                    .colorScheme
                    .onTertiaryContainer
            } else {
                MaterialTheme
                    .colorScheme
                    .onErrorContainer
            },
        shape =
            MaterialTheme
                .shapes
                .large
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        SangfroidSpacing.Md
                    ),
            horizontalArrangement =
                Arrangement.SpaceBetween,
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Column(
                modifier =
                    Modifier.weight(
                        1f
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        SangfroidSpacing.Xxs
                    )
            ) {
                Text(
                    text =
                        item.criterion,
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "${item.evidenceCount} " +
                                if (item.evidenceCount == 1) {
                                    "feedback score"
                                } else {
                                    "feedback scores"
                                },
                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                )
            }

            Text(
                text =
                    "${item.averagePercentage}%",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.ExtraBold
            )
        }
    }
}