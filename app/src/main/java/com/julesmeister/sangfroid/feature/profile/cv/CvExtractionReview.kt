package com.julesmeister.sangfroid.feature.profile.cv

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.shape.SangfroidRadius
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidCharcoal
import com.julesmeister.sangfroid.core.designsystem.theme.SangfroidLime
import com.julesmeister.sangfroid.domain.cv.CvExtractionResult
import com.julesmeister.sangfroid.domain.model.SkillCode

@OptIn(
    ExperimentalLayoutApi::class
)
@Composable
fun CvExtractionReview(
    extraction: CvExtractionResult,
    selectedSkillCodes: Set<SkillCode>,
    onSkillToggled: (SkillCode) -> Unit,
    onApply: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier =
            modifier.fillMaxWidth(),
        shape =
            MaterialTheme
                .shapes
                .extraLarge,
        color =
            MaterialTheme
                .colorScheme
                .surface
    ) {
        Column(
            modifier =
                Modifier.padding(
                    SangfroidSpacing.CardContent
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    SangfroidSpacing.Md
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
                        "Review CV skills",
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "Only selected skills will be saved. Your manually added skills will remain.",
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

            Text(
                text =
                    "${extraction.document.displayName} · " +
                            "${extraction.pageCount} " +
                            if (extraction.pageCount == 1) {
                                "page"
                            } else {
                                "pages"
                            },
                style =
                    MaterialTheme
                        .typography
                        .labelMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            if (extraction.skills.isEmpty()) {
                Text(
                    text =
                        "No known Sangfroid skills were detected. You can continue adding skills manually.",
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            } else {
                FlowRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            SangfroidSpacing.Xs
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            SangfroidSpacing.Xs
                        )
                ) {
                    extraction.skills.forEach { match ->
                        CvReviewSkillPill(
                            skillCode =
                                match.skillCode,
                            confidence =
                                match.confidence,
                            selected =
                                match.skillCode in
                                        selectedSkillCodes,
                            onClick = {
                                onSkillToggled(
                                    match.skillCode
                                )
                            }
                        )
                    }
                }
            }

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        SangfroidSpacing.Sm
                    )
            ) {
                ReviewAction(
                    label =
                        "Cancel",
                    emphasized =
                        false,
                    onClick =
                        onCancel,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                ReviewAction(
                    label =
                        "Use ${selectedSkillCodes.size} skills",
                    emphasized =
                        true,
                    enabled =
                        selectedSkillCodes.isNotEmpty(),
                    onClick =
                        onApply,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )
            }
        }
    }
}

@Composable
private fun CvReviewSkillPill(
    skillCode: SkillCode,
    confidence: Float,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val containerColor =
        if (selected) {
            SangfroidLime
        } else {
            MaterialTheme
                .colorScheme
                .surfaceVariant
        }

    val contentColor =
        if (selected) {
            SangfroidCharcoal
        } else {
            MaterialTheme
                .colorScheme
                .onSurfaceVariant
        }

    Row(
        modifier =
            Modifier
                .background(
                    color =
                        containerColor,
                    shape =
                        RoundedCornerShape(
                            SangfroidRadius.Pill
                        )
                )
                .clickable(
                    interactionSource =
                        interactionSource,
                    indication =
                        null,
                    role =
                        Role.Checkbox,
                    onClickLabel =
                        if (selected) {
                            "Remove ${skillCode.displayName()}"
                        } else {
                            "Add ${skillCode.displayName()}"
                        },
                    onClick =
                        onClick
                )
                .padding(
                    horizontal =
                        SangfroidSpacing.Md,
                    vertical =
                        SangfroidSpacing.Sm
                ),
        horizontalArrangement =
            Arrangement.spacedBy(
                SangfroidSpacing.Xs
            )
    ) {
        Text(
            text =
                skillCode.displayName(),
            style =
                MaterialTheme
                    .typography
                    .labelLarge,
            fontWeight =
                FontWeight.SemiBold,
            color =
                contentColor
        )

        Text(
            text =
                "${(confidence * 100).toInt()}%",
            style =
                MaterialTheme
                    .typography
                    .labelSmall,
            color =
                contentColor.copy(
                    alpha =
                        0.68f
                )
        )
    }
}

@Composable
private fun ReviewAction(
    label: String,
    emphasized: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val containerColor =
        when {
            !enabled -> {
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
                    .copy(
                        alpha =
                            0.5f
                    )
            }

            emphasized -> {
                MaterialTheme
                    .colorScheme
                    .onSurface
            }

            else -> {
                MaterialTheme
                    .colorScheme
                    .surfaceVariant
            }
        }

    val contentColor =
        when {
            !enabled -> {
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
                    .copy(
                        alpha =
                            0.5f
                    )
            }

            emphasized -> {
                MaterialTheme
                    .colorScheme
                    .surface
            }

            else -> {
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
            }
        }

    androidx.compose.foundation.layout.Box(
        modifier =
            modifier
                .background(
                    color =
                        containerColor,
                    shape =
                        RoundedCornerShape(
                            SangfroidRadius.Pill
                        )
                )
                .clickable(
                    interactionSource =
                        interactionSource,
                    indication =
                        null,
                    enabled =
                        enabled,
                    role =
                        Role.Button,
                    onClickLabel =
                        label,
                    onClick =
                        onClick
                )
                .padding(
                    horizontal =
                        SangfroidSpacing.Md,
                    vertical =
                        SangfroidSpacing.Sm
                ),
        contentAlignment =
            androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text =
                label,
            style =
                MaterialTheme
                    .typography
                    .labelLarge,
            fontWeight =
                FontWeight.Bold,
            color =
                contentColor
        )
    }
}

private fun SkillCode.displayName(): String {
    return when (this) {
        SkillCode.CI_CD -> {
            "CI/CD"
        }

        SkillCode.REST_API -> {
            "REST API"
        }

        SkillCode.NOSQL -> {
            "NoSQL"
        }

        SkillCode.DYNAMODB -> {
            "DynamoDB"
        }

        SkillCode.AWS -> {
            "AWS"
        }

        else -> {
            name
                .lowercase()
                .split(
                    "_"
                )
                .joinToString(
                    " "
                ) { word ->
                    word.replaceFirstChar {
                            character ->
                        character.uppercase()
                    }
                }
        }
    }
}