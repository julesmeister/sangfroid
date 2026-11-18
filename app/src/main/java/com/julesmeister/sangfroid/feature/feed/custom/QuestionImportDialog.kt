package com.julesmeister.sangfroid.feature.feed.custom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.component.SangfroidPillButton

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

    AlertDialog(
        onDismissRequest = {
            if (!state.isBusy) {
                onDismiss()
            }
        },
        title = {
            Text(
                text =
                    when (state.stage) {
                        QuestionImportStage.Editing ->
                            "Paste interview questions"

                        QuestionImportStage.Extracting ->
                            "Extracting questions"

                        QuestionImportStage.Preview ->
                            "Review import"

                        QuestionImportStage.Importing ->
                            "Adding to your feed"

                        QuestionImportStage.Complete ->
                            "Questions added"
                    },
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.ExtraBold
            )
        },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(
                            max = 560.dp
                        ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {
                when (
                    state.stage
                ) {
                    QuestionImportStage.Editing -> {
                        Text(
                            text =
                                "Paste a LinkedIn post, interview list, article, or notes. Sangfroid will extract only the interview questions and check them against your existing library.",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )

                        OutlinedTextField(
                            value =
                                state.pastedContent,
                            onValueChange = { value ->
                                onContentChanged(
                                    value.take(
                                        QuestionImportUiState
                                            .MAXIMUM_CONTENT_LENGTH
                                    )
                                )
                            },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(
                                        min = 240.dp,
                                        max = 400.dp
                                    ),
                            placeholder = {
                                Text(
                                    text =
                                        "Paste the complete post here…"
                                )
                            },
                            supportingText = {
                                Text(
                                    text =
                                        "${state.contentCharacterCount} / " +
                                                QuestionImportUiState
                                                    .MAXIMUM_CONTENT_LENGTH
                                )
                            },
                            enabled =
                                !state.isBusy,
                            shape =
                                RoundedCornerShape(
                                    18.dp
                                )
                        )
                    }

                    QuestionImportStage.Extracting,
                    QuestionImportStage.Importing -> {
                        Surface(
                            modifier =
                                Modifier.fillMaxWidth(),
                            shape =
                                RoundedCornerShape(
                                    20.dp
                                ),
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .surfaceVariant
                                    .copy(
                                        alpha = 0.55f
                                    )
                        ) {
                            Column(
                                modifier =
                                    Modifier.padding(
                                        20.dp
                                    ),
                                verticalArrangement =
                                    Arrangement.spacedBy(
                                        12.dp
                                    )
                            ) {
                                CircularProgressIndicator(
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .primary
                                )

                                Text(
                                    text =
                                        if (
                                            state.stage ==
                                            QuestionImportStage
                                                .Extracting
                                        ) {
                                            "Qwen is extracting, formatting, and classifying the interview questions."
                                        } else {
                                            "Saving the reviewed questions into your existing feed."
                                        },
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodyMedium,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant
                                )
                            }
                        }
                    }

                    QuestionImportStage.Preview -> {
                        val preview =
                            state.preview

                        if (preview != null) {
                            QuestionImportPreview(
                                preview =
                                    preview,
                                modifier =
                                    Modifier.fillMaxWidth()
                            )
                        }
                    }

                    QuestionImportStage.Complete -> {
                        val result =
                            state.lastImportResult

                        Surface(
                            modifier =
                                Modifier.fillMaxWidth(),
                            shape =
                                RoundedCornerShape(
                                    20.dp
                                ),
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primaryContainer
                                    .copy(
                                        alpha = 0.55f
                                    )
                        ) {
                            Column(
                                modifier =
                                    Modifier.padding(
                                        18.dp
                                    ),
                                verticalArrangement =
                                    Arrangement.spacedBy(
                                        8.dp
                                    )
                            ) {
                                Text(
                                    text =
                                        "${result?.addedCount ?: 0} " +
                                                if (
                                                    result?.addedCount ==
                                                    1
                                                ) {
                                                    "question was added"
                                                } else {
                                                    "questions were added"
                                                },
                                    style =
                                        MaterialTheme
                                            .typography
                                            .titleMedium,
                                    fontWeight =
                                        FontWeight.ExtraBold
                                )

                                Text(
                                    text =
                                        "The new questions now use the same feed, practice, feedback, history, and progress flow as your other questions.",
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodyMedium,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                state.errorMessage
                    ?.takeIf(
                        String::isNotBlank
                    )
                    ?.let { message ->
                        SelectionContainer {
                            Text(
                                text =
                                    message,
                                style =
                                    MaterialTheme
                                        .typography
                                        .bodySmall,
                                color =
                                    MaterialTheme
                                        .colorScheme
                                        .error
                            )
                        }
                    }
            }
        },
        confirmButton = {
            when (
                state.stage
            ) {
                QuestionImportStage.Editing -> {
                    SangfroidPillButton(
                        text =
                            "Extract questions",
                        onClick =
                            onExtractQuestions,
                        enabled =
                            state.canAnalyze,
                        emphasized =
                            true
                    )
                }

                QuestionImportStage.Preview -> {
                    SangfroidPillButton(
                        text =
                            "Add ${state.preview?.importableCount ?: 0} questions",
                        onClick =
                            onImportQuestions,
                        enabled =
                            state.canImport,
                        emphasized =
                            true
                    )
                }

                QuestionImportStage.Complete -> {
                    SangfroidPillButton(
                        text =
                            "Done",
                        onClick =
                            onDismiss,
                        enabled =
                            true,
                        emphasized =
                            true
                    )
                }

                QuestionImportStage.Extracting,
                QuestionImportStage.Importing -> {
                    Unit
                }
            }
        },
        dismissButton = {
            when (
                state.stage
            ) {
                QuestionImportStage.Preview -> {
                    TextButton(
                        onClick =
                            onEditContent,
                        enabled =
                            !state.isBusy
                    ) {
                        Text(
                            text =
                                "Edit paste"
                        )
                    }
                }

                QuestionImportStage.Editing -> {
                    TextButton(
                        onClick =
                            onDismiss,
                        enabled =
                            !state.isBusy
                    ) {
                        Text(
                            text =
                                "Cancel"
                        )
                    }
                }

                QuestionImportStage.Extracting,
                QuestionImportStage.Importing,
                QuestionImportStage.Complete -> {
                    Unit
                }
            }
        },
        shape =
            RoundedCornerShape(
                28.dp
            )
    )
}