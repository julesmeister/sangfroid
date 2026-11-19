// app/src/main/java/com/julesmeister/sangfroid/feature/practice/components/PracticeAnswerEditor.kt

package com.julesmeister.sangfroid.feature.practice.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidDimensions
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val BringIntoViewDelayMillis =
    220L

private const val GuideLookAheadWords =
    5

private val GuideWordPattern =
    Regex(
        "[\\p{L}\\p{N}]+(?:['’][\\p{L}\\p{N}]+)*"
    )

@Composable
fun PracticeAnswerEditor(
    value: String,
    partialTranscript: String,
    isRecording: Boolean,
    isProcessing: Boolean,
    elapsedLabel: String,
    onValueChange: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    suggestedScript: String? = null,
    hasIdealAnswer: Boolean = false,
    isGeneratingIdealAnswer: Boolean = false,
    idealAnswerEnabled: Boolean = true,
    onGenerateIdealAnswer: () -> Unit = {},
    onRefreshIdealAnswer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val normalizedSuggestedScript =
        remember(
            suggestedScript
        ) {
            suggestedScript
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )
        }

    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    val isFocused by
    interactionSource
        .collectIsFocusedAsState()

    val bringIntoViewRequester =
        remember {
            BringIntoViewRequester()
        }

    val coroutineScope =
        rememberCoroutineScope()

    val displayedValue =
        remember(
            value,
            partialTranscript,
            isRecording
        ) {
            buildLiveAnswer(
                committedValue =
                    value,
                partialTranscript =
                    partialTranscript,
                isRecording =
                    isRecording
            )
        }

    val ghostColor =
        MaterialTheme
            .colorScheme
            .onSurfaceVariant
            .copy(
                alpha =
                    0.40f
            )

    val guideTransformation:
            VisualTransformation =
        remember(
            normalizedSuggestedScript,
            ghostColor
        ) {
            if (
                normalizedSuggestedScript ==
                null
            ) {
                VisualTransformation.None
            } else {
                RemainingGuideTransformation(
                    script =
                        normalizedSuggestedScript,
                    ghostColor =
                        ghostColor
                )
            }
        }

    val canClear =
        value.isNotBlank() &&
                !isRecording &&
                !isProcessing &&
                !isGeneratingIdealAnswer

    val footerVisible =
        isProcessing ||
                isGeneratingIdealAnswer ||
                normalizedSuggestedScript !=
                null ||
                (
                        !isRecording &&
                                !isProcessing &&
                                isFocused
                        )

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(
                    min =
                        SangfroidDimensions
                            .PracticeAnswerCompactHeight
                )
                .bringIntoViewRequester(
                    bringIntoViewRequester
                )
                .semantics {
                    contentDescription =
                        when {
                            isRecording &&
                                    normalizedSuggestedScript !=
                                    null ->
                                "Answer editor. Voice transcription active. Remaining suggested words appear in lighter text."

                            isRecording ->
                                "Answer editor. Voice transcription active."

                            isProcessing ->
                                "Answer editor. Improving transcript."

                            isGeneratingIdealAnswer ->
                                "Answer editor. Preparing an ideal answer guide."

                            normalizedSuggestedScript !=
                                    null ->
                                "Answer editor. Remaining suggested words appear in lighter text."

                            else ->
                                "Answer editor"
                        }
                }
    ) {
        BasicTextField(
            value =
                displayedValue,
            onValueChange = { updatedValue ->
                if (
                    !isRecording &&
                    !isProcessing &&
                    !isGeneratingIdealAnswer
                ) {
                    onValueChange(
                        updatedValue
                    )
                }
            },
            enabled =
                !isRecording &&
                        !isProcessing &&
                        !isGeneratingIdealAnswer,
            interactionSource =
                interactionSource,
            visualTransformation =
                guideTransformation,
            modifier =
                Modifier
                    .fillMaxSize()
                    .onFocusChanged { focusState ->
                        onFocusChanged(
                            focusState.isFocused
                        )

                        if (
                            focusState.isFocused
                        ) {
                            coroutineScope.launch {
                                delay(
                                    BringIntoViewDelayMillis
                                )

                                bringIntoViewRequester
                                    .bringIntoView()
                            }
                        }
                    }
                    .padding(
                        start =
                            SangfroidSpacing.Xs,
                        top =
                            SangfroidSpacing.Md,
                        end =
                            SangfroidSpacing.Xs,
                        bottom =
                            SangfroidSpacing.Xl
                    ),
            textStyle =
                MaterialTheme
                    .typography
                    .bodyLarge
                    .copy(
                        color =
                            MaterialTheme
                                .colorScheme
                                .onBackground
                    ),
            cursorBrush =
                SolidColor(
                    MaterialTheme
                        .colorScheme
                        .primary
                ),
            decorationBox = { innerTextField ->
                Box(
                    modifier =
                        Modifier.fillMaxSize()
                ) {
                    if (
                        displayedValue.isBlank() &&
                        normalizedSuggestedScript ==
                        null
                    ) {
                        PracticeAnswerPlaceholder(
                            isRecording =
                                isRecording,
                            modifier =
                                Modifier.align(
                                    Alignment.TopStart
                                )
                        )
                    }

                    innerTextField()
                }
            }
        )

        AnimatedVisibility(
            visible =
                footerVisible,
            enter =
                fadeIn(),
            exit =
                fadeOut(),
            modifier =
                Modifier.align(
                    Alignment.BottomStart
                )
        ) {
            Text(
                text =
                    when {
                        isProcessing ->
                            "Refining transcript offline…"

                        isGeneratingIdealAnswer ->
                            "Preparing a guide…"

                        normalizedSuggestedScript !=
                                null ->
                            "Light text is a guide only"

                        else ->
                            "Draft saves automatically"
                    },
                modifier =
                    Modifier.padding(
                        start =
                            SangfroidSpacing.Xs,
                        end =
                            SangfroidSpacing.Xl,
                        bottom =
                            SangfroidSpacing.Xs
                    ),
                style =
                    MaterialTheme
                        .typography
                        .labelSmall,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
                        .copy(
                            alpha =
                                if (
                                    isProcessing ||
                                    isGeneratingIdealAnswer
                                ) {
                                    0.72f
                                } else {
                                    0.58f
                                }
                        )
            )
        }

        Row(
            modifier =
                Modifier
                    .align(
                        Alignment.BottomEnd
                    )
                    .padding(
                        end =
                            SangfroidSpacing.Xs,
                        bottom =
                            SangfroidSpacing.Xs
                    ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            /*
             * Both actions intentionally receive the same outer
             * Row alignment. Neither child adds its own vertical
             * padding, which keeps Refresh / Ideal answer and Clear
             * on exactly the same baseline.
             */
            IdealAnswerEditorAction(
                hasIdealAnswer =
                    hasIdealAnswer,
                isGenerating =
                    isGeneratingIdealAnswer,
                enabled =
                    idealAnswerEnabled &&
                            !isRecording &&
                            !isProcessing,
                onGenerate =
                    onGenerateIdealAnswer,
                onRefresh =
                    onRefreshIdealAnswer,
                modifier =
                    Modifier.padding(
                        horizontal =
                            SangfroidSpacing.Sm,
                        vertical =
                            SangfroidSpacing.Xs
                    )
            )

            AnimatedVisibility(
                visible =
                    canClear,
                enter =
                    fadeIn(),
                exit =
                    fadeOut()
            ) {
                ClearAnswerAction(
                    onClear = {
                        onValueChange(
                            ""
                        )
                    },
                    modifier =
                        Modifier.padding(
                            horizontal =
                                SangfroidSpacing.Sm,
                            vertical =
                                SangfroidSpacing.Xs
                        )
                )
            }
        }
    }
}

private class RemainingGuideTransformation(
    private val script: String,
    private val ghostColor: Color
) : VisualTransformation {

    override fun filter(
        text: AnnotatedString
    ): TransformedText {
        val suffix =
            remainingGuideSuffix(
                script =
                    script,
                answer =
                    text.text
            )

        if (
            suffix.isEmpty()
        ) {
            return TransformedText(
                text =
                    text,
                offsetMapping =
                    OffsetMapping.Identity
            )
        }

        val transformedText =
            buildAnnotatedString {
                append(
                    text
                )

                val ghostStart =
                    length

                append(
                    suffix
                )

                addStyle(
                    style =
                        SpanStyle(
                            color =
                                ghostColor,
                            fontWeight =
                                FontWeight.Normal,
                            fontStyle =
                                FontStyle.Normal
                        ),
                    start =
                        ghostStart,
                    end =
                        length
                )
            }

        val actualLength =
            text.length

        return TransformedText(
            text =
                transformedText,
            offsetMapping =
                object : OffsetMapping {
                    override fun originalToTransformed(
                        offset: Int
                    ): Int {
                        return offset.coerceIn(
                            minimumValue =
                                0,
                            maximumValue =
                                actualLength
                        )
                    }

                    override fun transformedToOriginal(
                        offset: Int
                    ): Int {
                        return offset.coerceIn(
                            minimumValue =
                                0,
                            maximumValue =
                                actualLength
                        )
                    }
                }
        )
    }
}

private data class GuideWord(
    val start: Int,
    val endExclusive: Int,
    val normalized: String
)

private fun tokenizeGuide(
    text: String
): List<GuideWord> {
    return GuideWordPattern
        .findAll(
            text
        )
        .map { match ->
            GuideWord(
                start =
                    match.range.first,
                endExclusive =
                    match.range.last +
                            1,
                normalized =
                    match.value
                        .replace(
                            '’',
                            '\''
                        )
                        .lowercase(
                            Locale.ROOT
                        )
            )
        }
        .toList()
}

private fun remainingGuideSuffix(
    script: String,
    answer: String
): String {
    val input =
        answer.trimStart()

    if (
        input.isEmpty()
    ) {
        return script
    }

    if (
        script.startsWith(
            input,
            ignoreCase =
                true
        )
    ) {
        return script.substring(
            input.length
        )
    }

    val expectedWords =
        tokenizeGuide(
            script
        )

    val actualWords =
        tokenizeGuide(
            input
        )

    if (
        expectedWords.isEmpty()
    ) {
        return ""
    }

    if (
        actualWords.isEmpty()
    ) {
        return appendGuideSeparator(
            answer =
                answer,
            remaining =
                script,
            partialWord =
                false
        )
    }

    var expectedIndex =
        0

    var consumedEnd =
        0

    var partialWord =
        false

    actualWords.forEachIndexed {
            actualIndex,
            actual ->

        if (
            expectedIndex >=
            expectedWords.size
        ) {
            return@forEachIndexed
        }

        val searchEnd =
            minOf(
                expectedWords.size,
                expectedIndex +
                        GuideLookAheadWords +
                        1
            )

        val matchingIndex =
            (
                    expectedIndex until
                            searchEnd
                    )
                .firstOrNull { index ->
                    expectedWords[index]
                        .normalized ==
                            actual.normalized
                }

        if (
            matchingIndex !=
            null
        ) {
            consumedEnd =
                expectedWords[
                    matchingIndex
                ].endExclusive

            expectedIndex =
                matchingIndex +
                        1

            partialWord =
                false
        } else {
            val isLastUnfinishedWord =
                actualIndex ==
                        actualWords.lastIndex &&
                        actual.endExclusive ==
                        input.length

            val expected =
                expectedWords[
                    expectedIndex
                ]

            if (
                isLastUnfinishedWord &&
                actual.normalized.length <
                expected.normalized.length &&
                expected.normalized.startsWith(
                    actual.normalized
                )
            ) {
                consumedEnd =
                    (
                            expected.start +
                                    actual.normalized.length
                            )
                        .coerceAtMost(
                            expected.endExclusive
                        )

                partialWord =
                    true
            }
        }
    }

    if (
        expectedIndex >=
        expectedWords.size &&
        !partialWord
    ) {
        return ""
    }

    val remaining =
        script.substring(
            consumedEnd
        )

    return appendGuideSeparator(
        answer =
            answer,
        remaining =
            remaining,
        partialWord =
            partialWord
    )
}

private fun appendGuideSeparator(
    answer: String,
    remaining: String,
    partialWord: Boolean
): String {
    if (
        remaining.isEmpty() ||
        answer.isEmpty()
    ) {
        return remaining
    }

    if (
        partialWord
    ) {
        return remaining
    }

    if (
        answer.last()
            .isWhitespace()
    ) {
        return remaining.trimStart()
    }

    if (
        remaining.first()
            .isWhitespace()
    ) {
        return remaining
    }

    return if (
        remaining.first()
            .isLetterOrDigit()
    ) {
        " $remaining"
    } else {
        remaining
    }
}

@Composable
private fun ClearAnswerAction(
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource =
        remember {
            MutableInteractionSource()
        }

    Row(
        modifier =
            modifier
                .semantics {
                    contentDescription =
                        "Clear answer"

                    role =
                        Role.Button
                }
                .clickable(
                    interactionSource =
                        interactionSource,
                    indication =
                        null,
                    role =
                        Role.Button,
                    onClickLabel =
                        "Clear answer",
                    onClick =
                        onClear
                ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text =
                "Clear",
            style =
                MaterialTheme
                    .typography
                    .labelSmall,
            fontWeight =
                FontWeight.SemiBold,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
                    .copy(
                        alpha =
                            0.58f
                    )
        )
    }
}

@Composable
private fun PracticeAnswerPlaceholder(
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
    ) {
        Text(
            text =
                if (isRecording) {
                    "Listening…"
                } else {
                    "Start writing your answer…"
                },
            style =
                MaterialTheme
                    .typography
                    .bodyLarge,
            fontWeight =
                FontWeight.Medium,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
                    .copy(
                        alpha =
                            0.72f
                    )
        )

        if (
            !isRecording
        ) {
            Text(
                text =
                    "Situation  •  Action  •  Result",
                modifier =
                    Modifier.padding(
                        top =
                            SangfroidSpacing.Sm
                    ),
                style =
                    MaterialTheme
                        .typography
                        .bodySmall,
                fontStyle =
                    FontStyle.Italic,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
                        .copy(
                            alpha =
                                0.48f
                        )
            )
        }
    }
}

private fun buildLiveAnswer(
    committedValue: String,
    partialTranscript: String,
    isRecording: Boolean
): String {
    val committed =
        committedValue.trim()

    val partial =
        partialTranscript.trim()

    if (
        !isRecording ||
        partial.isBlank()
    ) {
        return committedValue
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
        return committedValue
    }

    return "$committed $partial"
}