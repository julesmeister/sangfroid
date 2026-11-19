// app/src/main/java/com/julesmeister/sangfroid/feature/practice/components/PracticeScrollableContent.kt

package com.julesmeister.sangfroid.feature.practice.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidDimensions
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.feature.practice.PracticeUiState
import com.julesmeister.sangfroid.feature.practice.speech.SpeechUiState

private val IdleAnswerEditorMinHeight =
    220.dp

private val IdleAnswerEditorMaxHeight =
    420.dp

@Composable
fun PracticeScrollableContent(
    uiState: PracticeUiState,
    speechUiState: SpeechUiState,
    onNavigateBack: () -> Unit,
    onAnswerChanged: (String) -> Unit,
    onSelectCurrentAttempt: () -> Unit,
    onSelectAttempt: (String) -> Unit,
    onSelectPreviousAttempt: () -> Unit,
    onSelectNextAttempt: () -> Unit,
    onUseAttemptAsNewAnswer: (String) -> Unit,
    onOpenFeedback: (String) -> Unit,
    onEditorFocusChanged: (Boolean) -> Unit,
    onShowIdealAnswer: () -> Unit,
    onRefreshIdealAnswer: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val question =
        uiState.question
            ?: return

    val scrollState =
        rememberScrollState()

    var isEditorFocused by
    remember {
        mutableStateOf(
            false
        )
    }

    val orderedAttempts =
        remember(
            uiState.attempts
        ) {
            uiState.attempts
                .sortedByDescending {
                    it.submittedAt
                }
        }

    val isExpanded =
        !uiState.isViewingHistoricalAttempt &&
                (
                        compact ||
                                isEditorFocused ||
                                speechUiState.isRecording ||
                                speechUiState.isProcessing
                        )

    LaunchedEffect(
        uiState.isViewingHistoricalAttempt
    ) {
        if (
            uiState.isViewingHistoricalAttempt
        ) {
            isEditorFocused =
                false

            onEditorFocusChanged(
                false
            )
        }
    }

    val contentModifier =
        if (isExpanded) {
            modifier.fillMaxSize()
        } else {
            modifier
                .fillMaxSize()
                .verticalScroll(
                    scrollState
                )
        }

    Column(
        modifier =
            contentModifier
                .padding(
                    top =
                        SangfroidSpacing.Md,
                    bottom =
                        if (isExpanded) {
                            SangfroidSpacing.Xs
                        } else {
                            SangfroidSpacing.Section
                        }
                )
                .animateContentSize()
    ) {
        PracticeHeader(
            onNavigateBack =
                onNavigateBack,
            isDraftSaved =
                uiState.isDraftSaved,
            wordCount =
                uiState.selectedAttempt
                    ?.wordCount
                    ?: uiState.wordCount,
            compact =
                isExpanded,
            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier =
                Modifier.height(
                    if (isExpanded) {
                        SangfroidSpacing.Sm
                    } else {
                        SangfroidSpacing.Md
                    }
                )
        )

        AnimatedVisibility(
            visible =
                !isExpanded,
            enter =
                fadeIn() +
                        expandVertically(),
            exit =
                fadeOut() +
                        shrinkVertically()
        ) {
            Column(
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                PracticeQuestionCard(
                    question =
                        question,
                    attempts =
                        orderedAttempts,
                    selectedAttemptId =
                        uiState.selectedAttemptId,
                    onSelectCurrentAttempt =
                        onSelectCurrentAttempt,
                    onSelectAttempt =
                        onSelectAttempt,
                    onSelectPreviousAttempt =
                        onSelectPreviousAttempt,
                    onSelectNextAttempt =
                        onSelectNextAttempt,
                    onUseAttemptAsNewAnswer =
                        onUseAttemptAsNewAnswer,
                    onOpenFeedback =
                        onOpenFeedback,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(
                                min =
                                    SangfroidDimensions
                                        .PracticeQuestionCardMinHeight
                            )
                )

                Spacer(
                    modifier =
                        Modifier.height(
                            SangfroidSpacing.Section
                        )
                )
            }
        }

        AnimatedVisibility(
            visible =
                isExpanded,
            enter =
                fadeIn() +
                        expandVertically(),
            exit =
                fadeOut() +
                        shrinkVertically()
        ) {
            ExpandedQuestionReminder(
                question =
                    question,
                hasSuggestedScript =
                    uiState.hasSuggestedScript,
                isGeneratingIdealAnswer =
                    uiState.isGeneratingIdealAnswer
            )
        }

        AnimatedVisibility(
            visible =
                !uiState.isViewingHistoricalAttempt,
            enter =
                fadeIn() +
                        expandVertically(),
            exit =
                fadeOut() +
                        shrinkVertically()
        ) {
            CurrentAnswerComposer(
                uiState =
                    uiState,
                speechUiState =
                    speechUiState,
                isExpanded =
                    isExpanded,
                onAnswerChanged =
                    onAnswerChanged,
                onEditorFocusChanged = { focused ->
                    isEditorFocused =
                        focused

                    onEditorFocusChanged(
                        focused
                    )
                },
                onShowIdealAnswer =
                    onShowIdealAnswer,
                onRefreshIdealAnswer =
                    onRefreshIdealAnswer
            )
        }

        PracticeMessages(
            uiState =
                uiState,
            speechUiState =
                speechUiState
        )
    }
}

@Composable
private fun CurrentAnswerComposer(
    uiState: PracticeUiState,
    speechUiState: SpeechUiState,
    isExpanded: Boolean,
    onAnswerChanged: (String) -> Unit,
    onEditorFocusChanged: (Boolean) -> Unit,
    onShowIdealAnswer: () -> Unit,
    onRefreshIdealAnswer: () -> Unit
) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
    ) {
        PracticeAnswerEditor(
            value =
                uiState.answerText,
            partialTranscript =
                speechUiState.partialTranscript,
            isRecording =
                speechUiState.isRecording,
            isProcessing =
                speechUiState.isProcessing,
            elapsedLabel =
                speechUiState.elapsedLabel,
            onValueChange =
                onAnswerChanged,
            onFocusChanged =
                onEditorFocusChanged,
            suggestedScript =
                uiState.suggestedScript,
            hasIdealAnswer =
                uiState.hasIdealAnswer,
            isGeneratingIdealAnswer =
                uiState.isGeneratingIdealAnswer,
            idealAnswerEnabled =
                uiState.canGenerateIdealAnswer,
            onGenerateIdealAnswer =
                onShowIdealAnswer,
            onRefreshIdealAnswer =
                onRefreshIdealAnswer,
            modifier =
                if (isExpanded) {
                    Modifier
                        .fillMaxWidth()
                        .heightIn(
                            min =
                                IdleAnswerEditorMinHeight
                        )
                } else {
                    Modifier
                        .fillMaxWidth()
                        .heightIn(
                            min =
                                IdleAnswerEditorMinHeight,
                            max =
                                IdleAnswerEditorMaxHeight
                        )
                }
        )

        AnimatedVisibility(
            visible =
                isExpanded,
            enter =
                fadeIn(),
            exit =
                fadeOut()
        ) {
            ExpandedEditorFooter(
                wordCount =
                    displayedWordCount(
                        storedAnswer =
                            uiState.answerText,
                        partialTranscript =
                            speechUiState.partialTranscript,
                        isRecording =
                            speechUiState.isRecording
                    ),
                isDraftSaved =
                    uiState.isDraftSaved,
                isRecording =
                    speechUiState.isRecording,
                isProcessing =
                    speechUiState.isProcessing,
                hasSuggestedScript =
                    uiState.hasSuggestedScript
            )
        }

        AnimatedVisibility(
            visible =
                !isExpanded,
            enter =
                fadeIn() +
                        expandVertically(),
            exit =
                fadeOut() +
                        shrinkVertically()
        ) {
            Column(
                modifier =
                    Modifier.fillMaxWidth()
            ) {
                Spacer(
                    modifier =
                        Modifier.height(
                            SangfroidSpacing.Md
                        )
                )

                PracticeAnswerStatistics(
                    wordCount =
                        uiState.wordCount,
                    attemptCount =
                        uiState.attempts.size,
                    isSaved =
                        uiState.isDraftSaved,
                    inputMethod =
                        uiState.inputMethod
                )
            }
        }
    }
}

@Composable
private fun ExpandedQuestionReminder(
    question: QuestionEntity,
    hasSuggestedScript: Boolean,
    isGeneratingIdealAnswer: Boolean
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start =
                        SangfroidSpacing.Xs,
                    end =
                        SangfroidSpacing.Xs,
                    bottom =
                        SangfroidSpacing.Sm
                ),
        verticalArrangement =
            Arrangement.spacedBy(
                SangfroidSpacing.Xxs
            )
    ) {
        Text(
            text =
                when {
                    isGeneratingIdealAnswer ->
                        "PREPARING IDEAL ANSWER"

                    hasSuggestedScript ->
                        "GUIDED PRACTICE"

                    else ->
                        "ANSWERING"
                },
            style =
                MaterialTheme
                    .typography
                    .labelSmall,
            fontWeight =
                FontWeight.Bold,
            color =
                MaterialTheme
                    .colorScheme
                    .primary
        )

        Text(
            text =
                question.question.ifBlank {
                    "Answer the interview question"
                },
            style =
                MaterialTheme
                    .typography
                    .titleSmall,
            fontWeight =
                FontWeight.SemiBold,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
            maxLines =
                2,
            overflow =
                TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ExpandedEditorFooter(
    wordCount: Int,
    isDraftSaved: Boolean,
    isRecording: Boolean,
    isProcessing: Boolean,
    hasSuggestedScript: Boolean
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        SangfroidSpacing.Xs,
                    vertical =
                        SangfroidSpacing.Xs
                ),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text =
                if (
                    wordCount ==
                    1
                ) {
                    "1 word"
                } else {
                    "$wordCount words"
                },
            style =
                MaterialTheme
                    .typography
                    .labelSmall,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )

        Text(
            text =
                when {
                    isProcessing ->
                        "Refining offline…"

                    isRecording &&
                            hasSuggestedScript ->
                        "Guided listening…"

                    isRecording ->
                        "Listening…"

                    isDraftSaved ->
                        "Saved"

                    else ->
                        "Saving…"
                },
            style =
                MaterialTheme
                    .typography
                    .labelSmall,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}

@Composable
private fun PracticeMessages(
    uiState: PracticeUiState,
    speechUiState: SpeechUiState
) {
    AnimatedVisibility(
        visible =
            speechUiState.errorMessage !=
                    null,
        enter =
            fadeIn() +
                    expandVertically(),
        exit =
            fadeOut() +
                    shrinkVertically()
    ) {
        Column {
            Spacer(
                modifier =
                    Modifier.height(
                        SangfroidSpacing.Md
                    )
            )

            PracticeErrorPanel(
                title =
                    "Voice input unavailable",
                message =
                    speechUiState
                        .errorMessage
                        .orEmpty()
            )
        }
    }

    AnimatedVisibility(
        visible =
            uiState.idealAnswerErrorMessage !=
                    null,
        enter =
            fadeIn() +
                    expandVertically(),
        exit =
            fadeOut() +
                    shrinkVertically()
    ) {
        Column {
            Spacer(
                modifier =
                    Modifier.height(
                        SangfroidSpacing.Md
                    )
            )

            PracticeErrorPanel(
                title =
                    "Ideal answer unavailable",
                message =
                    uiState
                        .idealAnswerErrorMessage
                        .orEmpty()
            )
        }
    }

    AnimatedVisibility(
        visible =
            uiState.errorMessage !=
                    null,
        enter =
            fadeIn() +
                    expandVertically(),
        exit =
            fadeOut() +
                    shrinkVertically()
    ) {
        Column {
            Spacer(
                modifier =
                    Modifier.height(
                        SangfroidSpacing.Md
                    )
            )

            PracticeErrorPanel(
                title =
                    "Answer not saved",
                message =
                    uiState
                        .errorMessage
                        .orEmpty()
            )
        }
    }

    AnimatedVisibility(
        visible =
            uiState.submittedAttemptId !=
                    null,
        enter =
            fadeIn() +
                    scaleIn(
                        initialScale =
                            0.94f
                    ) +
                    expandVertically(),
        exit =
            fadeOut() +
                    scaleOut(
                        targetScale =
                            0.94f
                    ) +
                    shrinkVertically()
    ) {
        Column {
            Spacer(
                modifier =
                    Modifier.height(
                        SangfroidSpacing.Md
                    )
            )

            PracticeSubmittedPanel()
        }
    }
}

private fun displayedWordCount(
    storedAnswer: String,
    partialTranscript: String,
    isRecording: Boolean
): Int {
    val displayedText =
        buildDisplayedAnswerForCount(
            storedAnswer =
                storedAnswer,
            partialTranscript =
                partialTranscript,
            isRecording =
                isRecording
        )

    return displayedText
        .trim()
        .takeIf(
            String::isNotBlank
        )
        ?.split(
            Regex("\\s+")
        )
        ?.size
        ?: 0
}

private fun buildDisplayedAnswerForCount(
    storedAnswer: String,
    partialTranscript: String,
    isRecording: Boolean
): String {
    if (
        !isRecording
    ) {
        return storedAnswer
    }

    val committed =
        storedAnswer.trim()

    val partial =
        partialTranscript.trim()

    if (
        partial.isBlank()
    ) {
        return storedAnswer
    }

    if (
        committed.isBlank()
    ) {
        return partial
    }

    if (
        partial.startsWith(
            prefix =
                committed,
            ignoreCase =
                false
        )
    ) {
        return partial
    }

    if (
        committed.endsWith(
            suffix =
                partial,
            ignoreCase =
                false
        )
    ) {
        return storedAnswer
    }

    return "$committed $partial"
}