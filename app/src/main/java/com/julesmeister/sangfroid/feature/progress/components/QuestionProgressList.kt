package com.julesmeister.sangfroid.feature.progress.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.theme.questionCategoryColors
import com.julesmeister.sangfroid.domain.model.QuestionCategory
import com.julesmeister.sangfroid.domain.progress.QuestionMasteryLevel
import com.julesmeister.sangfroid.feature.progress.model.ProgressQuestionItem

private const val CollapsedRowCount = 5

private enum class ProgressFilter(val label: String) {
    ALL("All"),
    NEEDS_WORK("Needs work"),
    DEVELOPING("Developing"),
    STRONG("Strong");

    fun matches(level: QuestionMasteryLevel): Boolean =
        when (this) {
            ALL -> true
            NEEDS_WORK -> level == QuestionMasteryLevel.NEEDS_WORK
            DEVELOPING -> level == QuestionMasteryLevel.DEVELOPING
            STRONG ->
                level == QuestionMasteryLevel.STRONG ||
                        level == QuestionMasteryLevel.MASTERED
        }
}

@Composable
fun QuestionProgressList(
    items: List<ProgressQuestionItem>,
    onQuestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return

    var filter by rememberSaveable { mutableStateOf(ProgressFilter.ALL) }
    var showAll by rememberSaveable { mutableStateOf(false) }

    val filtered = items.filter { filter.matches(it.masteryLevel) }
    val shown = if (showAll) filtered else filtered.take(CollapsedRowCount)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SangfroidSpacing.Xxs)
            ) {
                Text(
                    text = "Question progress",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Tap a question to practise it again.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ProgressFilter.entries.forEach { option ->
                val count = items.count { option.matches(it.masteryLevel) }

                if (option == ProgressFilter.ALL || count > 0) {
                    FilterChip(
                        label = option.label,
                        count = count,
                        selected = filter == option,
                        onClick = {
                            filter = option
                            showAll = false
                        }
                    )
                }
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .animateContentSize()
        ) {
            if (shown.isEmpty()) {
                Text(
                    text = "No questions in this group yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(20.dp)
                )
            }

            shown.forEachIndexed { index, item ->
                if (index > 0) {
                    Box(
                        modifier =
                            Modifier
                                .padding(start = 76.dp, end = 16.dp)
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                                )
                    )
                }

                QuestionProgressRow(
                    item = item,
                    onClick = { onQuestionClick(item.question.id) }
                )
            }

            if (filtered.size > CollapsedRowCount) {
                Text(
                    text = if (showAll) "Show less" else "Show all ${filtered.size}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button) { showAll = !showAll }
                            .padding(vertical = 14.dp, horizontal = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        targetValue =
            if (selected) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        animationSpec = tween(180),
        label = "filter-bg"
    )

    val content by animateColorAsState(
        targetValue =
            if (selected) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(180),
        label = "filter-content"
    )

    Row(
        modifier =
            Modifier
                .clip(RoundedCornerShape(50))
                .background(background)
                .clickable(role = Role.Tab, onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = content
        )

        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = content.copy(alpha = 0.65f)
        )
    }
}

@Composable
private fun QuestionProgressRow(
    item: ProgressQuestionItem,
    onClick: () -> Unit
) {
    val colors = questionCategoryColors(item.question.category)

    val ringColor =
        if (item.masteryLevel == QuestionMasteryLevel.NEEDS_WORK) {
            MaterialTheme.colorScheme.error
        } else {
            colors.accent
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScoreRing(
            percentage = item.latestPercentage,
            color = ringColor
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(colors.accent)
                )

                Text(
                    text = item.question.category.displayName(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = item.question.question,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val attempts = item.progress.attemptCount

                Text(
                    text = if (attempts == 1) "1 attempt" else "$attempts attempts",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                item.progress.scoreChangePercentagePoints
                    ?.takeIf { it != 0 }
                    ?.let { change ->
                        Text(
                            text = if (change > 0) "↑$change" else "↓${-change}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color =
                                if (change > 0) colors.accent
                                else MaterialTheme.colorScheme.error
                        )
                    }
            }
        }

        Chevron(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    }
}

@Composable
private fun ScoreRing(
    percentage: Int?,
    color: Color
) {
    val track = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val fraction = (percentage ?: 0).coerceIn(0, 100) / 100f

    Box(
        modifier = Modifier.size(46.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 4.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)

            drawArc(
                color = track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke)
            )

            if (fraction > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * fraction,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }

        Text(
            text = percentage?.toString() ?: "—",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun Chevron(color: Color) {
    Canvas(modifier = Modifier.size(14.dp)) {
        val stroke = 1.8.dp.toPx()

        drawLine(
            color = color,
            start = Offset(size.width * 0.38f, size.height * 0.22f),
            end = Offset(size.width * 0.66f, size.height * 0.5f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(size.width * 0.66f, size.height * 0.5f),
            end = Offset(size.width * 0.38f, size.height * 0.78f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

private fun QuestionCategory.displayName(): String =
    when (this) {
        QuestionCategory.HR -> "HR"
        QuestionCategory.BEHAVIORAL -> "Behavioral"
        QuestionCategory.TECHNICAL -> "Technical"
        QuestionCategory.SYSTEM_DESIGN -> "System design"
        QuestionCategory.DOMAIN -> "Domain"
        QuestionCategory.PROJECT -> "Project"
    }