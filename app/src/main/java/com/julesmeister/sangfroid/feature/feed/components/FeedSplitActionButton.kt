package com.julesmeister.sangfroid.feature.feed.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidAlpha
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidDimensions
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.shape.SangfroidRadius

@Composable
fun FeedSplitActionButton(
    activeFilterCount: Int,
    enabled: Boolean,
    onPasteQuestions: () -> Unit,
    onOpenCategoryFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor =
        MaterialTheme
            .colorScheme
            .surfaceVariant

    val contentColor =
        MaterialTheme
            .colorScheme
            .onSurface

    val dividerColor =
        MaterialTheme
            .colorScheme
            .outlineVariant
            .copy(
                alpha = 0.72f
            )

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(
                    min =
                        SangfroidDimensions
                            .ButtonHeight
                )
                .graphicsLayer {
                    alpha =
                        if (enabled) {
                            1f
                        } else {
                            SangfroidAlpha.Disabled
                        }
                },
        shape =
            RoundedCornerShape(
                SangfroidRadius.Pill
            ),
        color =
            containerColor,
        contentColor =
            contentColor
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Surface(
                onClick =
                    onPasteQuestions,
                enabled =
                    enabled,
                modifier =
                    Modifier
                        .weight(
                            1.9f
                        )
                        .heightIn(
                            min =
                                SangfroidDimensions
                                    .ButtonHeight
                        )
                        .semantics {
                            role =
                                Role.Button

                            contentDescription =
                                "Paste interview questions"
                        },
                color =
                    Color.Transparent,
                contentColor =
                    contentColor
            ) {
                Row(
                    modifier =
                        Modifier.padding(
                            horizontal =
                                SangfroidSpacing
                                    .ButtonHorizontal,
                            vertical =
                                SangfroidSpacing
                                    .ButtonVertical
                        ),
                    horizontalArrangement =
                        Arrangement.Center,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    PlusGlyph(
                        color =
                            contentColor,
                        modifier =
                            Modifier.size(
                                15.dp
                            )
                    )

                    Box(
                        modifier =
                            Modifier.width(
                                SangfroidSpacing.Xs
                            )
                    )

                    Text(
                        text =
                            "Paste questions",
                        style =
                            MaterialTheme
                                .typography
                                .labelLarge,
                        fontWeight =
                            FontWeight.Bold,
                        maxLines =
                            1,
                        overflow =
                            TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier =
                    Modifier
                        .width(
                            1.dp
                        )
                        .heightIn(
                            min =
                                24.dp
                        )
            ) {
                Canvas(
                    modifier =
                        Modifier
                            .width(
                                1.dp
                            )
                            .size(
                                width = 1.dp,
                                height = 24.dp
                            )
                ) {
                    drawLine(
                        color =
                            dividerColor,
                        start =
                            Offset(
                                x = size.width / 2f,
                                y = 0f
                            ),
                        end =
                            Offset(
                                x = size.width / 2f,
                                y = size.height
                            ),
                        strokeWidth =
                            size.width
                    )
                }
            }

            Surface(
                onClick =
                    onOpenCategoryFilter,
                enabled =
                    enabled,
                modifier =
                    Modifier
                        .weight(
                            1f
                        )
                        .heightIn(
                            min =
                                SangfroidDimensions
                                    .ButtonHeight
                        )
                        .semantics {
                            role =
                                Role.Button

                            contentDescription =
                                if (
                                    activeFilterCount > 0
                                ) {
                                    "Filter question categories. " +
                                            "$activeFilterCount selected."
                                } else {
                                    "Filter question categories"
                                }
                        },
                color =
                    Color.Transparent,
                contentColor =
                    contentColor
            ) {
                Row(
                    modifier =
                        Modifier.padding(
                            horizontal =
                                SangfroidSpacing.Sm,
                            vertical =
                                SangfroidSpacing
                                    .ButtonVertical
                        ),
                    horizontalArrangement =
                        Arrangement.Center,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    FilterGlyph(
                        color =
                            contentColor,
                        modifier =
                            Modifier.size(
                                16.dp
                            )
                    )

                    Box(
                        modifier =
                            Modifier.width(
                                6.dp
                            )
                    )

                    Text(
                        text =
                            "Filter",
                        style =
                            MaterialTheme
                                .typography
                                .labelLarge,
                        fontWeight =
                            FontWeight.Bold,
                        maxLines =
                            1
                    )

                    if (
                        activeFilterCount > 0
                    ) {
                        Box(
                            modifier =
                                Modifier.width(
                                    6.dp
                                )
                        )

                        FilterCountBadge(
                            count =
                                activeFilterCount
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterCountBadge(
    count: Int
) {
    Surface(
        shape =
            CircleShape,
        color =
            MaterialTheme
                .colorScheme
                .secondary,
        contentColor =
            MaterialTheme
                .colorScheme
                .onSecondary
    ) {
        Box(
            modifier =
                Modifier
                    .size(
                        20.dp
                    ),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    count.toString(),
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                fontWeight =
                    FontWeight.ExtraBold,
                maxLines =
                    1
            )
        }
    }
}

@Composable
private fun PlusGlyph(
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
                    x = size.width / 2f,
                    y = size.height * 0.18f
                ),
            end =
                Offset(
                    x = size.width / 2f,
                    y = size.height * 0.82f
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
                    x = size.width * 0.18f,
                    y = size.height / 2f
                ),
            end =
                Offset(
                    x = size.width * 0.82f,
                    y = size.height / 2f
                ),
            strokeWidth =
                strokeWidth,
            cap =
                StrokeCap.Round
        )
    }
}

@Composable
private fun FilterGlyph(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier =
            modifier
    ) {
        val strokeWidth =
            1.7.dp.toPx()

        val topY =
            size.height * 0.22f

        val middleY =
            size.height * 0.50f

        val bottomY =
            size.height * 0.78f

        drawLine(
            color =
                color,
            start =
                Offset(
                    x = size.width * 0.12f,
                    y = topY
                ),
            end =
                Offset(
                    x = size.width * 0.88f,
                    y = topY
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
                    x = size.width * 0.12f,
                    y = middleY
                ),
            end =
                Offset(
                    x = size.width * 0.88f,
                    y = middleY
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
                    x = size.width * 0.12f,
                    y = bottomY
                ),
            end =
                Offset(
                    x = size.width * 0.88f,
                    y = bottomY
                ),
            strokeWidth =
                strokeWidth,
            cap =
                StrokeCap.Round
        )

        drawCircle(
            color =
                color,
            radius =
                2.3.dp.toPx(),
            center =
                Offset(
                    x = size.width * 0.34f,
                    y = topY
                ),
            style =
                Stroke(
                    width =
                        strokeWidth
                )
        )

        drawCircle(
            color =
                color,
            radius =
                2.3.dp.toPx(),
            center =
                Offset(
                    x = size.width * 0.66f,
                    y = middleY
                ),
            style =
                Stroke(
                    width =
                        strokeWidth
                )
        )

        drawCircle(
            color =
                color,
            radius =
                2.3.dp.toPx(),
            center =
                Offset(
                    x = size.width * 0.44f,
                    y = bottomY
                ),
            style =
                Stroke(
                    width =
                        strokeWidth
                )
        )
    }
}