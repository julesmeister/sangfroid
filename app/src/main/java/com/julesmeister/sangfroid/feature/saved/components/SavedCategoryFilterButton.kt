package com.julesmeister.sangfroid.feature.saved.components

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
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidAlpha
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidDimensions
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.shape.SangfroidRadius

@Composable
fun SavedCategoryFilterButton(
    activeFilterCount: Int,
    visibleQuestionCount: Int,
    totalQuestionCount: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasActiveFilter =
        activeFilterCount > 0

    val containerColor =
        if (hasActiveFilter) {
            MaterialTheme
                .colorScheme
                .secondary
        } else {
            MaterialTheme
                .colorScheme
                .surfaceVariant
        }

    val contentColor =
        if (hasActiveFilter) {
            MaterialTheme
                .colorScheme
                .onSecondary
        } else {
            MaterialTheme
                .colorScheme
                .onSurface
        }

    Surface(
        onClick =
            onClick,
        enabled =
            enabled,
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
                }
                .semantics {
                    role =
                        Role.Button

                    contentDescription =
                        if (hasActiveFilter) {
                            "Filter saved question categories. " +
                                    "$activeFilterCount selected. " +
                                    "$visibleQuestionCount of " +
                                    "$totalQuestionCount questions shown."
                        } else {
                            "Filter saved question categories. " +
                                    "All $totalQuestionCount questions shown."
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
            FilterGlyph(
                color =
                    contentColor,
                modifier =
                    Modifier.size(
                        17.dp
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
                    if (hasActiveFilter) {
                        "$visibleQuestionCount of $totalQuestionCount shown"
                    } else {
                        "Filter categories"
                    },
                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
                fontWeight =
                    FontWeight.Bold,
                maxLines =
                    1
            )

            if (hasActiveFilter) {
                Box(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )

                ActiveFilterBadge(
                    count =
                        activeFilterCount,
                    contentColor =
                        contentColor
                )
            }
        }
    }
}

@Composable
private fun ActiveFilterBadge(
    count: Int,
    contentColor: Color
) {
    Surface(
        shape =
            CircleShape,
        color =
            contentColor.copy(
                alpha = 0.16f
            ),
        contentColor =
            contentColor
    ) {
        Box(
            modifier =
                Modifier.size(
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