package com.julesmeister.sangfroid.feature.practice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.sangfroid.data.local.entity.AnswerDraftEntity
import com.julesmeister.sangfroid.data.local.entity.PracticeAttemptEntity
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.domain.model.AnswerInputMethod
import com.julesmeister.sangfroid.domain.model.EvaluationStatus
import com.julesmeister.sangfroid.domain.repository.IdealAnswerRepository
import com.julesmeister.sangfroid.domain.repository.PracticeRepository
import com.julesmeister.sangfroid.feature.practice.speech.AnswerSpeechMerger
import com.julesmeister.sangfroid.feature.practice.speech.PracticeSpeechController
import com.julesmeister.sangfroid.feature.practice.speech.SpeechUiState
import com.julesmeister.sangfroid.navigation.SangfroidDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val DraftSaveDelayMillis =
    600L

@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val savedStateHandle:
    SavedStateHandle,
    private val practiceRepository:
    PracticeRepository,
    private val idealAnswerRepository:
    IdealAnswerRepository,
    private val speechController:
    PracticeSpeechController,
    private val answerSpeechMerger:
    AnswerSpeechMerger
) : ViewModel() {

    private val questionId: String =
        checkNotNull(
            savedStateHandle[
                SangfroidDestination
                    .PracticeArgument
            ]
        )

    private val selectedAttemptId =
        savedStateHandle.getStateFlow<String?>(
            key =
                SangfroidDestination
                    .PracticeSelectedAttemptIdKey,
            initialValue =
                null
        )

    private val suggestedScript =
        savedStateHandle.getStateFlow<String?>(
            key =
                SangfroidDestination
                    .PracticeSuggestedScriptKey,
            initialValue =
                null
        )

    private val answerText =
        MutableStateFlow(
            ""
        )

    private val inputMethod =
        MutableStateFlow(
            AnswerInputMethod.TYPED
        )

    private val startedAt =
        MutableStateFlow(
            System.currentTimeMillis()
        )

    private val isDraftSaved =
        MutableStateFlow(
            true
        )

    private val isSubmitting =
        MutableStateFlow(
            false
        )

    private val submittedAttemptId =
        MutableStateFlow<String?>(
            null
        )

    private val pendingNavigationAttemptId =
        MutableStateFlow<String?>(
            null
        )

    private val idealAnswer =
        MutableStateFlow<
                IdealAnswerRepository.IdealAnswer?
                >(
            null
        )

    private val isGeneratingIdealAnswer =
        MutableStateFlow(
            false
        )

    private val idealAnswerErrorMessage =
        MutableStateFlow<String?>(
            null
        )

    private val errorMessage =
        MutableStateFlow<String?>(
            null
        )

    private val hasSpeechContent =
        MutableStateFlow(
            false
        )

    private var draftSaveJob:
            Job? =
        null

    private var draftRestoreJob:
            Job? =
        null

    private var submissionJob:
            Job? =
        null

    private var idealAnswerJob:
            Job? =
        null

    private var answerBeforeSpeech =
        ""

    private var inputMethodBeforeSpeech =
        AnswerInputMethod.TYPED

    private var hasSpeechContentBeforeSpeech =
        false

    private var isSpeechSessionActive =
        false

    private var hasInitializedComposer =
        false

    val speechUiState:
            StateFlow<SpeechUiState>
        get() =
            speechController.uiState

    private val baseState =
        combine(
            practiceRepository.observeQuestion(
                questionId
            ),
            practiceRepository.observeDraft(
                questionId
            ),
            practiceRepository.observeAttempts(
                questionId
            ),
            answerText,
            inputMethod
        ) {
                question,
                draft,
                attempts,
                currentAnswer,
                method ->

            BasePracticeState(
                question =
                    question,
                draft =
                    draft,
                attempts =
                    attempts,
                currentAnswer =
                    currentAnswer,
                inputMethod =
                    method
            )
        }

    private val processingState =
        combine(
            isDraftSaved,
            isSubmitting,
            submittedAttemptId,
            pendingNavigationAttemptId,
            errorMessage
        ) {
                draftSaved,
                submitting,
                submittedId,
                pendingNavigationId,
                error ->

            ProcessingState(
                isDraftSaved =
                    draftSaved,
                isSubmitting =
                    submitting,
                submittedAttemptId =
                    submittedId,
                pendingNavigationAttemptId =
                    pendingNavigationId,
                errorMessage =
                    error
            )
        }

    private val idealAnswerState =
        combine(
            idealAnswer,
            isGeneratingIdealAnswer,
            idealAnswerErrorMessage
        ) {
                currentIdealAnswer,
                generating,
                idealError ->

            IdealAnswerState(
                idealAnswer =
                    currentIdealAnswer,
                isGenerating =
                    generating,
                errorMessage =
                    idealError
            )
        }

    /*
     * Keep the final uiState combine typed.
     *
     * Kotlin Flow only provides strongly typed combine overloads
     * up to five flows. Combining six directly selects the vararg
     * Array<Any?> overload.
     */
    private val composerState =
        combine(
            startedAt,
            selectedAttemptId,
            suggestedScript
        ) {
                currentStartedAt,
                requestedSelectedAttemptId,
                currentSuggestedScript ->

            ComposerState(
                startedAt =
                    currentStartedAt,
                selectedAttemptId =
                    requestedSelectedAttemptId,
                suggestedScript =
                    currentSuggestedScript
            )
        }

    val uiState:
            StateFlow<PracticeUiState> =
        combine(
            baseState,
            processingState,
            idealAnswerState,
            composerState
        ) {
                base,
                processing,
                ideal,
                composer ->

            val validSelectedAttemptId =
                composer
                    .selectedAttemptId
                    ?.takeIf { attemptId ->
                        base.attempts.any { attempt ->
                            attempt.id ==
                                    attemptId
                        }
                    }

            if (
                composer.selectedAttemptId !=
                null &&
                validSelectedAttemptId ==
                null
            ) {
                savedStateHandle[
                    SangfroidDestination
                        .PracticeSelectedAttemptIdKey
                ] = null
            }

            PracticeUiState(
                isLoading =
                    base.question ==
                            null,
                question =
                    base.question,
                answerText =
                    base.currentAnswer,
                suggestedScript =
                    composer
                        .suggestedScript
                        ?.takeIf(
                            String::isNotBlank
                        ),
                inputMethod =
                    base.inputMethod,
                startedAt =
                    composer.startedAt,
                attempts =
                    base.attempts,
                selectedAttemptId =
                    validSelectedAttemptId,
                isDraftSaved =
                    processing.isDraftSaved,
                isSubmitting =
                    processing.isSubmitting,
                submittedAttemptId =
                    processing.submittedAttemptId,
                pendingNavigationAttemptId =
                    processing
                        .pendingNavigationAttemptId,
                idealAnswer =
                    ideal.idealAnswer,
                isGeneratingIdealAnswer =
                    ideal.isGenerating,
                idealAnswerErrorMessage =
                    ideal.errorMessage,
                errorMessage =
                    processing.errorMessage
            )
        }
            .stateIn(
                scope =
                    viewModelScope,
                started =
                    SharingStarted
                        .WhileSubscribed(
                            stopTimeoutMillis =
                                5_000L
                        ),
                initialValue =
                    PracticeUiState()
            )

    init {
        speechController.bind(
            scope =
                viewModelScope
        )

        speechController.prepare()

        restoreDraft()
    }

    fun showIdealAnswer() {
        generateIdealAnswer(
            refresh =
                false
        )
    }

    fun refreshIdealAnswer() {
        generateIdealAnswer(
            refresh =
                true
        )
    }

    fun dismissIdealAnswerError() {
        idealAnswerErrorMessage.value =
            null
    }

    private fun generateIdealAnswer(
        refresh: Boolean
    ) {
        if (
            isInteractionLocked() ||
            idealAnswerJob?.isActive ==
            true
        ) {
            return
        }

        val question =
            uiState
                .value
                .question
                ?: return

        val previous =
            if (refresh) {
                idealAnswer.value
            } else {
                null
            }

        if (
            refresh &&
            previous ==
            null
        ) {
            return
        }

        val nextRevision =
            previous
                ?.revisionNumber
                ?.plus(
                    1
                )
                ?: 1

        isGeneratingIdealAnswer.value =
            true

        idealAnswerErrorMessage.value =
            null

        idealAnswerJob =
            viewModelScope.launch {
                try {
                    val result =
                        idealAnswerRepository
                            .generate(
                                question =
                                    question,
                                revisionNumber =
                                    nextRevision,
                                previousIdealAnswer =
                                    previous
                                        ?.content,
                                targetRole =
                                    null,
                                experienceLevel =
                                    null,
                                languageTag =
                                    DEFAULT_LANGUAGE_TAG,
                                cvEvidence =
                                    emptyList()
                            )
                            .getOrThrow()

                    idealAnswer.value =
                        result

                    savedStateHandle[
                        SangfroidDestination
                            .PracticeSuggestedScriptKey
                    ] = result.content
                } catch (
                    cancellation:
                    CancellationException
                ) {
                    throw cancellation
                } catch (
                    throwable:
                    Throwable
                ) {
                    idealAnswerErrorMessage.value =
                        throwable
                            .message
                            ?.takeIf(
                                String::isNotBlank
                            )
                            ?: "The ideal answer could not be generated."
                } finally {
                    isGeneratingIdealAnswer.value =
                        false
                }
            }
    }

    fun useRevisionAsGuide(
        script: String
    ) {
        val normalizedScript =
            normalizeScript(
                script
            )

        if (
            normalizedScript.isBlank()
        ) {
            return
        }

        draftSaveJob?.cancel()

        draftSaveJob =
            null

        if (
            speechUiState
                .value
                .isRecording ||
            speechUiState
                .value
                .isProcessing
        ) {
            speechController.cancelRecording()
        }

        speechController.clearTranscript()

        clearSpeechSession()

        savedStateHandle[
            SangfroidDestination
                .PracticeSelectedAttemptIdKey
        ] = null

        savedStateHandle[
            SangfroidDestination
                .PracticeSuggestedScriptKey
        ] = normalizedScript

        answerText.value =
            ""

        inputMethod.value =
            AnswerInputMethod.TYPED

        hasSpeechContent.value =
            false

        startedAt.value =
            System.currentTimeMillis()

        submittedAttemptId.value =
            null

        pendingNavigationAttemptId.value =
            null

        errorMessage.value =
            null

        isDraftSaved.value =
            true

        hasInitializedComposer =
            true

        viewModelScope.launch {
            try {
                practiceRepository.deleteDraft(
                    questionId
                )

                isDraftSaved.value =
                    true
            } catch (
                cancellation:
                CancellationException
            ) {
                throw cancellation
            } catch (
                throwable:
                Throwable
            ) {
                isDraftSaved.value =
                    false

                errorMessage.value =
                    throwable.message
                        ?: "The previous draft could not be cleared."
            }
        }
    }

    fun selectCurrentAttempt() {
        if (
            isInteractionLocked()
        ) {
            return
        }

        savedStateHandle[
            SangfroidDestination
                .PracticeSelectedAttemptIdKey
        ] = null
    }

    fun selectAttempt(
        attemptId: String
    ) {
        if (
            isInteractionLocked()
        ) {
            return
        }

        val normalizedAttemptId =
            attemptId.trim()

        if (
            normalizedAttemptId.isBlank()
        ) {
            return
        }

        val attemptExists =
            uiState
                .value
                .attempts
                .any { attempt ->
                    attempt.id ==
                            normalizedAttemptId
                }

        if (
            !attemptExists
        ) {
            return
        }

        savedStateHandle[
            SangfroidDestination
                .PracticeSelectedAttemptIdKey
        ] = normalizedAttemptId
    }

    fun selectPreviousAttempt() {
        if (
            isInteractionLocked()
        ) {
            return
        }

        val state =
            uiState.value

        val orderedAttempts =
            state
                .attempts
                .sortedByDescending {
                    it.submittedAt
                }

        val currentAttemptId =
            state.selectedAttemptId
                ?: return

        val currentIndex =
            orderedAttempts
                .indexOfFirst {
                    it.id ==
                            currentAttemptId
                }

        if (
            currentIndex <=
            0
        ) {
            selectCurrentAttempt()
        } else {
            selectAttempt(
                orderedAttempts[
                    currentIndex - 1
                ].id
            )
        }
    }

    fun selectNextAttempt() {
        if (
            isInteractionLocked()
        ) {
            return
        }

        val state =
            uiState.value

        val orderedAttempts =
            state
                .attempts
                .sortedByDescending {
                    it.submittedAt
                }

        if (
            orderedAttempts.isEmpty()
        ) {
            return
        }

        val currentAttemptId =
            state.selectedAttemptId

        if (
            currentAttemptId ==
            null
        ) {
            selectAttempt(
                orderedAttempts
                    .first()
                    .id
            )

            return
        }

        val currentIndex =
            orderedAttempts
                .indexOfFirst {
                    it.id ==
                            currentAttemptId
                }

        if (
            currentIndex >=
            0 &&
            currentIndex <
            orderedAttempts.lastIndex
        ) {
            selectAttempt(
                orderedAttempts[
                    currentIndex + 1
                ].id
            )
        }
    }

    fun useAttemptAsNewAnswer(
        attemptId: String
    ) {
        if (
            isInteractionLocked()
        ) {
            return
        }

        val attempt =
            uiState
                .value
                .attempts
                .firstOrNull {
                    it.id ==
                            attemptId
                }
                ?: return

        prepareComposerForNewSession()

        savedStateHandle[
            SangfroidDestination
                .PracticeSuggestedScriptKey
        ] = null

        answerText.value =
            attempt.answerText

        inputMethod.value =
            AnswerInputMethod.TYPED

        hasSpeechContent.value =
            false

        isDraftSaved.value =
            false

        hasInitializedComposer =
            true

        scheduleDraftSave()
    }

    fun clearSuggestedScript() {
        if (
            speechUiState
                .value
                .isRecording ||
            speechUiState
                .value
                .isProcessing
        ) {
            return
        }

        savedStateHandle[
            SangfroidDestination
                .PracticeSuggestedScriptKey
        ] = null
    }

    fun updateAnswer(
        value: String
    ) {
        if (
            uiState
                .value
                .isViewingHistoricalAttempt ||
            speechUiState
                .value
                .isRecording ||
            speechUiState
                .value
                .isProcessing ||
            isSubmitting.value
        ) {
            return
        }

        val previousAnswer =
            answerText.value

        answerText.value =
            value

        hasInitializedComposer =
            true

        if (
            value !=
            previousAnswer
        ) {
            inputMethod.value =
                when {
                    value.isBlank() -> {
                        hasSpeechContent.value =
                            false

                        AnswerInputMethod.TYPED
                    }

                    hasSpeechContent.value ->
                        AnswerInputMethod.MIXED

                    else ->
                        AnswerInputMethod.TYPED
                }
        }

        submittedAttemptId.value =
            null

        pendingNavigationAttemptId.value =
            null

        errorMessage.value =
            null

        isDraftSaved.value =
            false

        scheduleDraftSave()
    }

    fun startSpeechRecording() {
        if (
            uiState
                .value
                .isViewingHistoricalAttempt ||
            isSubmitting.value ||
            isGeneratingIdealAnswer.value ||
            speechUiState
                .value
                .isRecording ||
            speechUiState
                .value
                .isProcessing ||
            isSpeechSessionActive
        ) {
            return
        }

        answerBeforeSpeech =
            answerText.value

        inputMethodBeforeSpeech =
            inputMethod.value

        hasSpeechContentBeforeSpeech =
            hasSpeechContent.value

        isSpeechSessionActive =
            true

        hasInitializedComposer =
            true

        submittedAttemptId.value =
            null

        pendingNavigationAttemptId.value =
            null

        errorMessage.value =
            null

        speechController.startRecording()
    }

    fun stopSpeechRecording() {
        if (
            uiState
                .value
                .isViewingHistoricalAttempt ||
            !isSpeechSessionActive ||
            !speechUiState
                .value
                .isRecording
        ) {
            return
        }

        speechController.stopRecording(
            onTranscriptReady =
                ::applySpeechTranscript
        )
    }

    fun cancelSpeechRecording() {
        speechController.cancelRecording()

        restorePreSpeechState()

        clearSpeechSession()
    }

    fun clearSpeechInput() {
        speechController.clearTranscript()
    }

    fun submitAnswer() {
        if (
            uiState
                .value
                .isViewingHistoricalAttempt ||
            submissionJob?.isActive ==
            true ||
            isGeneratingIdealAnswer.value
        ) {
            return
        }

        val text =
            answerText
                .value
                .trim()

        if (
            text.isBlank() ||
            isSubmitting.value ||
            speechUiState
                .value
                .isRecording ||
            speechUiState
                .value
                .isProcessing
        ) {
            return
        }

        draftSaveJob?.cancel()

        draftSaveJob =
            null

        isSubmitting.value =
            true

        errorMessage.value =
            null

        submissionJob =
            viewModelScope.launch {
                try {
                    val submittedAt =
                        System.currentTimeMillis()

                    val normalizedStartedAt =
                        startedAt
                            .value
                            .coerceAtMost(
                                submittedAt
                            )

                    val durationSeconds =
                        (
                                (
                                        submittedAt -
                                                normalizedStartedAt
                                        ) /
                                        1_000L
                                )
                            .coerceIn(
                                minimumValue =
                                    0L,
                                maximumValue =
                                    Int.MAX_VALUE
                                        .toLong()
                            )
                            .toInt()

                    val attempt =
                        PracticeAttemptEntity(
                            id =
                                UUID.randomUUID()
                                    .toString(),
                            questionId =
                                questionId,
                            answerText =
                                text,
                            inputMethod =
                                inputMethod.value,
                            startedAt =
                                normalizedStartedAt,
                            submittedAt =
                                submittedAt,
                            durationSeconds =
                                durationSeconds,
                            wordCount =
                                calculateWordCount(
                                    text
                                ),
                            evaluationStatus =
                                EvaluationStatus
                                    .NOT_REQUESTED,
                            modelName =
                                null,
                            modelVersion =
                                null
                        )

                    practiceRepository
                        .submitAttempt(
                            attempt
                        )

                    runCatching {
                        practiceRepository
                            .deleteDraft(
                                questionId
                            )
                    }

                    submittedAttemptId.value =
                        attempt.id

                    pendingNavigationAttemptId.value =
                        attempt.id

                    resetComposer()
                } catch (
                    cancellation:
                    CancellationException
                ) {
                    throw cancellation
                } catch (
                    throwable:
                    Throwable
                ) {
                    errorMessage.value =
                        throwable.message
                            ?: "The answer could not be submitted."
                } finally {
                    isSubmitting.value =
                        false
                }
            }
    }

    fun consumeFeedbackNavigation() {
        pendingNavigationAttemptId.value =
            null
    }

    fun dismissError() {
        errorMessage.value =
            null

        idealAnswerErrorMessage.value =
            null

        speechController.dismissError()
    }

    private fun restoreDraft() {
        draftRestoreJob?.cancel()

        draftRestoreJob =
            viewModelScope.launch {
                practiceRepository
                    .observeDraft(
                        questionId
                    )
                    .collect { draft ->
                        if (
                            draft ==
                            null ||
                            hasInitializedComposer ||
                            answerText
                                .value
                                .isNotBlank() ||
                            suggestedScript
                                .value
                                ?.isNotBlank() ==
                            true ||
                            isSpeechSessionActive ||
                            speechUiState
                                .value
                                .isRecording ||
                            speechUiState
                                .value
                                .isProcessing
                        ) {
                            return@collect
                        }

                        answerText.value =
                            draft.answerText

                        inputMethod.value =
                            draft.inputMethod

                        startedAt.value =
                            draft.startedAt

                        hasSpeechContent.value =
                            draft.inputMethod !=
                                    AnswerInputMethod.TYPED

                        isDraftSaved.value =
                            true

                        hasInitializedComposer =
                            true
                    }
            }
    }

    private fun applySpeechTranscript(
        transcript: String
    ) {
        if (
            !isSpeechSessionActive ||
            uiState
                .value
                .isViewingHistoricalAttempt
        ) {
            return
        }

        val cleanTranscript =
            normalizeTranscript(
                transcript
            )

        if (
            cleanTranscript.isBlank()
        ) {
            restorePreSpeechState()

            clearSpeechSession()

            return
        }

        val baseAnswer =
            answerBeforeSpeech

        val mergedAnswer =
            answerSpeechMerger.merge(
                existingAnswer =
                    baseAnswer,
                recognizedSpeech =
                    cleanTranscript
            )

        if (
            mergedAnswer.isBlank()
        ) {
            restorePreSpeechState()

            clearSpeechSession()

            return
        }

        answerText.value =
            mergedAnswer

        hasSpeechContent.value =
            true

        inputMethod.value =
            if (
                baseAnswer.isNotBlank()
            ) {
                AnswerInputMethod.MIXED
            } else {
                AnswerInputMethod.SPEECH
            }

        hasInitializedComposer =
            true

        submittedAttemptId.value =
            null

        pendingNavigationAttemptId.value =
            null

        errorMessage.value =
            null

        isDraftSaved.value =
            false

        clearSpeechSession()

        scheduleDraftSave()
    }

    private fun restorePreSpeechState() {
        if (
            !isSpeechSessionActive
        ) {
            return
        }

        answerText.value =
            answerBeforeSpeech

        inputMethod.value =
            inputMethodBeforeSpeech

        hasSpeechContent.value =
            hasSpeechContentBeforeSpeech
    }

    private fun clearSpeechSession() {
        answerBeforeSpeech =
            ""

        inputMethodBeforeSpeech =
            AnswerInputMethod.TYPED

        hasSpeechContentBeforeSpeech =
            false

        isSpeechSessionActive =
            false
    }

    private fun scheduleDraftSave() {
        draftSaveJob?.cancel()

        draftSaveJob =
            viewModelScope.launch {
                delay(
                    DraftSaveDelayMillis
                )

                val text =
                    answerText.value

                try {
                    if (
                        text.isBlank()
                    ) {
                        practiceRepository
                            .deleteDraft(
                                questionId
                            )

                        isDraftSaved.value =
                            true

                        return@launch
                    }

                    practiceRepository.saveDraft(
                        AnswerDraftEntity(
                            id =
                                questionId,
                            questionId =
                                questionId,
                            answerText =
                                text,
                            inputMethod =
                                inputMethod.value,
                            startedAt =
                                startedAt.value,
                            updatedAt =
                                System.currentTimeMillis()
                        )
                    )

                    isDraftSaved.value =
                        true
                } catch (
                    cancellation:
                    CancellationException
                ) {
                    throw cancellation
                } catch (
                    throwable:
                    Throwable
                ) {
                    isDraftSaved.value =
                        false

                    errorMessage.value =
                        throwable.message
                            ?: "The draft could not be saved."
                }
            }
    }

    private fun prepareComposerForNewSession() {
        draftSaveJob?.cancel()

        draftSaveJob =
            null

        speechController.clearTranscript()

        clearSpeechSession()

        startedAt.value =
            System.currentTimeMillis()

        submittedAttemptId.value =
            null

        pendingNavigationAttemptId.value =
            null

        errorMessage.value =
            null

        savedStateHandle[
            SangfroidDestination
                .PracticeSelectedAttemptIdKey
        ] = null
    }

    private fun resetComposer() {
        draftSaveJob?.cancel()

        draftSaveJob =
            null

        answerText.value =
            ""

        inputMethod.value =
            AnswerInputMethod.TYPED

        hasSpeechContent.value =
            false

        savedStateHandle[
            SangfroidDestination
                .PracticeSuggestedScriptKey
        ] = null

        startedAt.value =
            System.currentTimeMillis()

        isDraftSaved.value =
            true

        hasInitializedComposer =
            true

        savedStateHandle[
            SangfroidDestination
                .PracticeSelectedAttemptIdKey
        ] = null

        clearSpeechSession()

        speechController.clearTranscript()
    }

    private fun isInteractionLocked():
            Boolean {
        return speechUiState
            .value
            .isRecording ||
                speechUiState
                    .value
                    .isProcessing ||
                isSubmitting.value ||
                isGeneratingIdealAnswer.value
    }

    private fun normalizeScript(
        script: String
    ): String {
        return script
            .trim()
            .replace(
                Regex("[\\t\\r ]+"),
                " "
            )
            .replace(
                Regex("\\n{3,}"),
                "\n\n"
            )
    }

    private fun normalizeTranscript(
        transcript: String
    ): String {
        return transcript
            .trim()
            .replace(
                Regex("\\s+"),
                " "
            )
    }

    private fun calculateWordCount(
        text: String
    ): Int {
        return text
            .trim()
            .takeIf(
                String::isNotBlank
            )
            ?.split(
                Regex("\\s+")
            )
            ?.count(
                String::isNotBlank
            )
            ?: 0
    }

    override fun onCleared() {
        draftSaveJob?.cancel()

        draftRestoreJob?.cancel()

        submissionJob?.cancel()

        idealAnswerJob?.cancel()

        if (
            speechUiState
                .value
                .isRecording ||
            speechUiState
                .value
                .isProcessing
        ) {
            speechController.cancelRecording()
        }

        speechController.close()

        super.onCleared()
    }

    private data class BasePracticeState(
        val question:
        QuestionEntity?,
        val draft:
        AnswerDraftEntity?,
        val attempts:
        List<PracticeAttemptEntity>,
        val currentAnswer:
        String,
        val inputMethod:
        AnswerInputMethod
    )

    private data class ProcessingState(
        val isDraftSaved:
        Boolean,
        val isSubmitting:
        Boolean,
        val submittedAttemptId:
        String?,
        val pendingNavigationAttemptId:
        String?,
        val errorMessage:
        String?
    )

    private data class IdealAnswerState(
        val idealAnswer:
        IdealAnswerRepository.IdealAnswer?,
        val isGenerating:
        Boolean,
        val errorMessage:
        String?
    )

    private data class ComposerState(
        val startedAt:
        Long,
        val selectedAttemptId:
        String?,
        val suggestedScript:
        String?
    )

    companion object {
        private const val DEFAULT_LANGUAGE_TAG =
            "en"
    }
}