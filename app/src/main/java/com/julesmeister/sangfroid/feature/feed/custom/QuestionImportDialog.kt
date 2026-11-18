package com.julesmeister.sangfroid.feature.feed.custom

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.julesmeister.sangfroid.core.designsystem.component.SangfroidPillButton

private val DialogShape = RoundedCornerShape(30.dp)
private val DialogMaxHeight = 720.dp
private const val DialogWidthFraction = 0.94f

@Composable
fun QuestionImportDialog(
    state: QuestionImportUiState,
    onContentChanged: (String) -> Unit,
    onExtractQuestions: () -> Unit,
    onImportQuestions: () -> Unit,
    onEditContent: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!state.isVisible) {
        return
    }

    Dialog(
        onDismissRequest = {
            if (!state.isBusy) {
                onDismiss()
            }
        },
        properties =
            DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = !state.isBusy,
                dismissOnClickOutside = !state.isBusy
            )
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth(DialogWidthFraction)
                    .heightIn(max = DialogMaxHeight)
                    .imePadding(),
            shape = DialogShape,
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 2.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(top = 20.dp, bottom = 18.dp)
            ) {
                DialogHeader(
                    stage = state.stage,
                    closeEnabled = !state.isBusy,
                    onClose = onDismiss,
                    modifier = Modifier.padding(horizontal = 22.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .padding(horizontal = 22.dp)
                ) {
                    AnimatedContent(
                        targetState = state.stage,
                        transitionSpec = {
                            fadeIn(tween(200)) togetherWith fadeOut(tween(120))
                        },
                        label = "import-stage"
                    ) { stage ->
                        when (stage) {
                            QuestionImportStage.Editing ->
                                EditingBody(
                                    state = state,
                                    onContentChanged = onContentChanged
                                )

                            QuestionImportStage.Extracting ->
                                WorkingBody(
                                    title = "Reading your paste",
                                    steps =
                                        listOf(
                                            "Ignoring titles, salaries and commentary",
                                            "Turning bullets into clear questions",
                                            "Classifying category and difficulty"
                                        )
                                )

                            QuestionImportStage.Importing ->
                                WorkingBody(
                                    title = "Adding to your feed",
                                    steps =
                                        listOf(
                                            "Saving reviewed questions",
                                            "Linking skills for progress tracking"
                                        )
                                )

                            QuestionImportStage.Preview ->
                                state.preview?.let { preview ->
                                    QuestionImportPreview(
                                        preview = preview,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                            QuestionImportStage.Complete ->
                                CompleteBody(
                                    addedCount =
                                        state.lastImportResult?.addedCount ?: 0,
                                    skippedCount =
                                        state.lastImportResult?.duplicateCount ?: 0
                                )
                        }
                    }
                }

                state.errorMessage
                    ?.takeIf(String::isNotBlank)
                    ?.let { message ->
                        ErrorBanner(
                            message = message,
                            modifier =
                                Modifier
                                    .padding(horizontal = 22.dp)
                                    .padding(top = 12.dp)
                        )
                    }

                DialogFooter(
                    state = state,
                    onExtractQuestions = onExtractQuestions,
                    onImportQuestions = onImportQuestions,
                    onEditContent = onEditContent,
                    onDismiss = onDismiss,
                    modifier =
                        Modifier
                            .padding(horizontal = 22.dp)
                            .padding(top = 18.dp)
                )
            }
        }
    }
}

@Composable
private fun DialogHeader(
    stage: QuestionImportStage,
    closeEnabled: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val step =
        when (stage) {
            QuestionImportStage.Editing,
            QuestionImportStage.Extracting -> 1

            QuestionImportStage.Preview,
            QuestionImportStage.Importing -> 2

            QuestionImportStage.Complete -> 3
        }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "IMPORT QUESTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text =
                        when (stage) {
                            QuestionImportStage.Editing -> "Paste your source"
                            QuestionImportStage.Extracting -> "Extracting questions"
                            QuestionImportStage.Preview -> "Review what we found"
                            QuestionImportStage.Importing -> "Adding questions"
                            QuestionImportStage.Complete -> "All set"
                        },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            CloseButton(
                enabled = closeEnabled,
                onClick = onClose
            )
        }

        StepBar(currentStep = step)
    }
}

@Composable
private fun StepBar(
    currentStep: Int
) {
    val labels = listOf("Paste", "Review", "Add")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val active = index + 1 <= currentStep

            val barColor by
            animateColorAsState(
                targetValue =
                    if (active) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    },
                animationSpec = tween(220),
                label = "step-bar"
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(barColor)
                )

                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight =
                        if (index + 1 == currentStep) FontWeight.Bold
                        else FontWeight.Medium,
                    color =
                        if (active) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EditingBody(
    state: QuestionImportUiState,
    onContentChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text =
                "Paste a LinkedIn post, interview list, article or notes. " +
                        "Only the questions are kept, and anything you already have is skipped.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        TextField(
            value = state.pastedContent,
            onValueChange = { value ->
                onContentChanged(
                    value.take(QuestionImportUiState.MAXIMUM_CONTENT_LENGTH)
                )
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp, max = 420.dp),
            placeholder = {
                Text(
                    text = "Paste the full post here…",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium,
            enabled = !state.isBusy,
            shape = RoundedCornerShape(20.dp),
            colors =
                TextFieldDefaults.colors(
                    focusedContainerColor =
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    unfocusedContainerColor =
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    disabledContainerColor =
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text =
                    "%,d / %,d characters".format(
                        state.contentCharacterCount,
                        QuestionImportUiState.MAXIMUM_CONTENT_LENGTH
                    ),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (state.pastedContent.isNotEmpty() && !state.isBusy) {
                Text(
                    text = "Clear",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(role = Role.Button) {
                                onContentChanged("")
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun WorkingBody(
    title: String,
    steps: List<String>
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(
            modifier =
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(36.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            steps.forEach { step ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                    )

                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CompleteBody(
    addedCount: Int,
    skippedCount: Int
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier =
                Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            CheckGlyph(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            )
        }

        Text(
            text =
                if (addedCount == 1) "1 question added"
                else "$addedCount questions added",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text =
                buildString {
                    append("They're in your feed now, with the same practice, feedback and progress tracking as every other question.")

                    if (skippedCount > 0) {
                        append(" $skippedCount similar ")
                        append(if (skippedCount == 1) "question was" else "questions were")
                        append(" skipped.")
                    }
                },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    val errorColor = MaterialTheme.colorScheme.error

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(errorColor.copy(alpha = 0.08f))
                .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier =
                Modifier
                    .padding(top = 6.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(errorColor)
        )

        SelectionContainer(modifier = Modifier.weight(1f)) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = errorColor
            )
        }
    }
}

@Composable
private fun DialogFooter(
    state: QuestionImportUiState,
    onExtractQuestions: () -> Unit,
    onImportQuestions: () -> Unit,
    onEditContent: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isBusy) {
        return
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (state.stage) {
            QuestionImportStage.Editing -> {
                SecondaryAction(
                    text = "Cancel",
                    onClick = onDismiss
                )

                SangfroidPillButton(
                    text = "Extract questions",
                    onClick = onExtractQuestions,
                    enabled = state.canAnalyze,
                    emphasized = true,
                    modifier = Modifier.weight(1f)
                )
            }

            QuestionImportStage.Preview -> {
                SecondaryAction(
                    text = "Edit paste",
                    onClick = onEditContent
                )

                val count = state.preview?.importableCount ?: 0

                SangfroidPillButton(
                    text =
                        when (count) {
                            0 -> "Nothing to add"
                            1 -> "Add 1 question"
                            else -> "Add $count questions"
                        },
                    onClick = onImportQuestions,
                    enabled = state.canImport,
                    emphasized = true,
                    modifier = Modifier.weight(1f)
                )
            }

            QuestionImportStage.Complete -> {
                SangfroidPillButton(
                    text = "Done",
                    onClick = onDismiss,
                    enabled = true,
                    emphasized = true,
                    modifier = Modifier.weight(1f)
                )
            }

            QuestionImportStage.Extracting,
            QuestionImportStage.Importing -> Unit
        }
    }
}

@Composable
private fun SecondaryAction(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            Modifier
                .clip(RoundedCornerShape(50))
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun CloseButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier =
            Modifier
                .size(36.dp)
                .semantics { contentDescription = "Close" },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) {
            val color =
                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = if (enabled) 1f else 0.4f
                )

            Canvas(modifier = Modifier.size(12.dp)) {
                val stroke = 1.8.dp.toPx()

                drawLine(
                    color = color,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )

                drawLine(
                    color = color,
                    start = Offset(size.width, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun CheckGlyph(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = 3.dp.toPx()

        drawLine(
            color = color,
            start = Offset(size.width * 0.18f, size.height * 0.52f),
            end = Offset(size.width * 0.42f, size.height * 0.76f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = Offset(size.width * 0.42f, size.height * 0.76f),
            end = Offset(size.width * 0.84f, size.height * 0.28f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Suppress("unused")
private val SpacerWidth = Modifier.width(0.dp)