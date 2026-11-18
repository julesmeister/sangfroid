package com.julesmeister.sangfroid.feature.feed.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidAlpha
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidDimensions
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.shape.CardShapeStyle
import com.julesmeister.sangfroid.core.designsystem.shape.SangfroidCardShapes
import com.julesmeister.sangfroid.core.designsystem.shape.SangfroidRadius
import com.julesmeister.sangfroid.core.designsystem.shape.SangfroidShapeDimensions
import com.julesmeister.sangfroid.core.designsystem.theme.QuestionCategoryColors
import com.julesmeister.sangfroid.core.designsystem.theme.questionCategoryColors
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.domain.model.QuestionCategory

/*
 * Smallest horizontal room the folder notch must offer
 * before the counter badge is allowed to live there.
 * "99/99" at labelMedium + Sm horizontal padding ≈ 56dp.
 */
private val MinimumNotchSpan =
    64.dp

@Composable
fun InterviewQuestionCard(
    question: QuestionEntity,
    position: Int,
    totalQuestions: Int,
    modifier: Modifier = Modifier,
    depth: Float = 0f
) {
    val colors =
        questionCategoryColors(
            category =
                question.category
        )

    val style =
        remember(question.category) {
            SangfroidCardShapes.styleFor(
                question.category
            )
        }

    val shape =
        remember(style) {
            SangfroidCardShapes.shapeFor(
                style
            )
        }

    val scrimColor =
        MaterialTheme
            .colorScheme
            .background

    val scrimAlpha =
        (depth * SangfroidAlpha.RearCardDimPerDepth)
            .coerceIn(
                0f,
                SangfroidAlpha.RearCardMaxDim
            )

    Box(
        modifier =
            modifier
    ) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .clip(shape)
                    .background(colors.container)
        )

        when (style) {
            is CardShapeStyle.Folder -> {
                FolderCardContent(
                    question = question,
                    position = position,
                    totalQuestions = totalQuestions,
                    colors = colors,
                    style = style
                )
            }

            CardShapeStyle.Stepped -> {
                SteppedCardContent(
                    question = question,
                    position = position,
                    totalQuestions = totalQuestions,
                    colors = colors
                )
            }

            CardShapeStyle.Ticket -> {
                TicketCardContent(
                    question = question,
                    position = position,
                    totalQuestions = totalQuestions,
                    colors = colors
                )
            }
        }

        if (scrimAlpha > 0f) {
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .clip(shape)
                        .background(
                            scrimColor.copy(
                                alpha = scrimAlpha
                            )
                        )
            )
        }
    }
}

/*
 * ===========================================================
 * FOLDER (HR, System Design)
 *
 * The tab occupies [0, tabEnd] across the top tabHeight
 * strip. Everything to the right of tabEnd in that strip is
 * cut away by FolderCardShape. The position badge lives in
 * that notch, mirroring how Stepped cards use their cut-out,
 * so the body no longer spends a full row on it.
 * ===========================================================
 */

@Composable
private fun FolderCardContent(
    question: QuestionEntity,
    position: Int,
    totalQuestions: Int,
    colors: QuestionCategoryColors,
    style: CardShapeStyle.Folder
) {
    BoxWithConstraints(
        modifier =
            Modifier.fillMaxSize()
    ) {
        /*
         * Mirrors FolderCardShape: tabEnd = width × fraction.
         * The shape only clamps this at extreme widths, which
         * the fallback below already covers.
         */
        val tabEnd =
            maxWidth * style.tabWidthFraction

        val tabLabelWidth =
            (tabEnd - style.tabSlant)
                .coerceAtLeast(style.tabHeight)

        val notchSpan =
            maxWidth -
                    tabEnd -
                    SangfroidSpacing.CardContent -
                    SangfroidSpacing.Xs

        val badgeInNotch =
            notchSpan >= MinimumNotchSpan

        Box(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .width(tabLabelWidth)
                    .height(style.tabHeight)
                    .padding(
                        start = SangfroidSpacing.CardContent,
                        end = SangfroidSpacing.Xs
                    ),
            contentAlignment =
                Alignment.CenterStart
        ) {
            CategoryTabLabel(
                category = question.category,
                colors = colors
            )
        }

        if (badgeInNotch) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .widthIn(max = notchSpan)
                        .height(style.tabHeight)
                        .padding(
                            end = SangfroidSpacing.CardContent
                        ),
                contentAlignment =
                    Alignment.CenterEnd
            ) {
                PositionBadge(
                    position = position,
                    totalQuestions = totalQuestions,
                    colors = colors
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = style.tabHeight)
                    .padding(SangfroidSpacing.CardContent)
        ) {
            if (!badgeInNotch) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    PositionBadge(
                        position = position,
                        totalQuestions = totalQuestions,
                        colors = colors
                    )
                }
            }

            CardBody(
                question = question,
                colors = colors,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
            )
        }
    }
}

/*
 * ===========================================================
 * STEPPED (Technical)
 * Counter floats inside the cut-out corner.
 * ===========================================================
 */

@Composable
private fun SteppedCardContent(
    question: QuestionEntity,
    position: Int,
    totalQuestions: Int,
    colors: QuestionCategoryColors
) {
    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .size(
                        width = SangfroidShapeDimensions.StepWidth,
                        height = SangfroidShapeDimensions.StepHeight
                    ),
            contentAlignment =
                Alignment.Center
        ) {
            PositionBadge(
                position = position,
                totalQuestions = totalQuestions,
                colors = colors
            )
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(SangfroidSpacing.CardContent)
        ) {
            CategoryChip(
                category = question.category,
                colors = colors
            )

            CardBody(
                question = question,
                colors = colors,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
            )
        }
    }
}

/*
 * ===========================================================
 * TICKET (Behavioral)
 * Perforation aligns exactly with the side notches.
 * ===========================================================
 */

@Composable
private fun TicketCardContent(
    question: QuestionEntity,
    position: Int,
    totalQuestions: Int,
    colors: QuestionCategoryColors
) {
    BoxWithConstraints(
        modifier =
            Modifier.fillMaxSize()
    ) {
        val perforationY =
            maxHeight * SangfroidShapeDimensions.TicketNotchFraction

        Column(
            modifier =
                Modifier.fillMaxSize()
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(perforationY)
                        .padding(
                            start = SangfroidSpacing.CardContent,
                            end = SangfroidSpacing.CardContent,
                            top = SangfroidSpacing.CardContent,
                            bottom = SangfroidSpacing.Lg
                        )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryChip(
                        category = question.category,
                        colors = colors
                    )

                    PositionBadge(
                        position = position,
                        totalQuestions = totalQuestions,
                        colors = colors
                    )
                }

                Spacer(
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = question.question,
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.content,
                    maxLines = SangfroidDimensions.QuestionTicketMaxLines,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(
                    modifier = Modifier.height(SangfroidSpacing.Sm)
                )

                Text(
                    text = question.whyAsked,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.secondaryContent,
                    maxLines = SangfroidDimensions.SupportingTextMaxLines,
                    overflow = TextOverflow.Ellipsis
                )
            }

            TicketPerforation(
                color =
                    colors.content.copy(
                        alpha = SangfroidAlpha.Perforation
                    ),
                modifier =
                    Modifier.padding(
                        horizontal =
                            SangfroidShapeDimensions.TicketNotchRadius +
                                    SangfroidSpacing.Sm
                    )
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(
                            horizontal = SangfroidSpacing.CardContent
                        ),
                verticalArrangement =
                    Arrangement.Center
            ) {
                QuestionMetadataRow(
                    question = question,
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun TicketPerforation(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(SangfroidDimensions.TicketPerforationStroke)
    ) {
        val dash = SangfroidDimensions.TicketPerforationDash.toPx()
        val gap = SangfroidDimensions.TicketPerforationGap.toPx()
        val centerY = size.height / 2f

        drawLine(
            color = color,
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = size.height,
            cap = StrokeCap.Round,
            pathEffect =
                PathEffect.dashPathEffect(
                    floatArrayOf(dash, gap)
                )
        )
    }
}

/*
 * ===========================================================
 * SHARED PARTS
 * ===========================================================
 */

@Composable
private fun CardBody(
    question: QuestionEntity,
    colors: QuestionCategoryColors,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
    ) {
        Spacer(
            modifier = Modifier.weight(1f)
        )

        Text(
            text = question.question,
            style = MaterialTheme.typography.headlineLarge,
            color = colors.content,
            maxLines = SangfroidDimensions.QuestionMaxLines,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(
            modifier = Modifier.height(SangfroidSpacing.Lg)
        )

        Text(
            text = question.whyAsked,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.secondaryContent,
            maxLines = SangfroidDimensions.SupportingTextMaxLines,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(
            modifier = Modifier.height(SangfroidSpacing.Section)
        )

        QuestionMetadataRow(
            question = question,
            colors = colors
        )
    }
}

@Composable
private fun CategoryTabLabel(
    category: QuestionCategory,
    colors: QuestionCategoryColors
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SangfroidSpacing.Xs)
    ) {
        Box(
            modifier =
                Modifier
                    .size(SangfroidDimensions.CategoryDot)
                    .clip(CircleShape)
                    .background(colors.accent)
        )

        Text(
            text = category.displayName(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = colors.content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CategoryChip(
    category: QuestionCategory,
    colors: QuestionCategoryColors
) {
    Surface(
        color = colors.tagContainer,
        contentColor = colors.tagContent,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            modifier =
                Modifier.padding(
                    horizontal = SangfroidSpacing.Sm,
                    vertical = SangfroidSpacing.Xs
                ),
            text = category.displayName(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun PositionBadge(
    position: Int,
    totalQuestions: Int,
    colors: QuestionCategoryColors
) {
    Box(
        modifier =
            Modifier
                .heightIn(min = SangfroidDimensions.CounterBadgeHeight)
                .widthIn(min = SangfroidDimensions.CounterBadgeHeight)
                .clip(RoundedCornerShape(SangfroidRadius.Pill))
                .background(colors.accent)
                .padding(
                    horizontal = SangfroidSpacing.Sm,
                    vertical = SangfroidSpacing.Xxs
                ),
        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text = "$position/$totalQuestions",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = colors.tagContent,
            maxLines = 1,
            softWrap = false
        )
    }
}

private fun QuestionCategory.displayName(): String {
    return name.replace("_", " ")
}