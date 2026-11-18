package com.julesmeister.sangfroid.feature.feedback

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.sangfroid.data.local.entity.PracticeAttemptEntity
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.domain.evaluation.EvaluationResult
import com.julesmeister.sangfroid.domain.history.DeleteSavedRevisionUseCase
import com.julesmeister.sangfroid.domain.history.PracticeHistoryRepository
import com.julesmeister.sangfroid.domain.history.RevisionSource
import com.julesmeister.sangfroid.domain.history.SaveRevisionUseCase
import com.julesmeister.sangfroid.domain.history.SavedRevision
import com.julesmeister.sangfroid.domain.progress.GetQuestionProgressUseCase
import com.julesmeister.sangfroid.domain.progress.QuestionProgress
import com.julesmeister.sangfroid.domain.repository.EvaluationRepository
import com.julesmeister.sangfroid.navigation.SangfroidDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val EvaluationLogTag =
    "SangfroidEval"

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val evaluationRepository:
    EvaluationRepository,
    practiceHistoryRepository:
    PracticeHistoryRepository,
    private val saveRevisionUseCase:
    SaveRevisionUseCase,
    private val deleteSavedRevisionUseCase:
    DeleteSavedRevisionUseCase,
    private val getQuestionProgress:
    GetQuestionProgressUseCase
) : ViewModel() {

    private val attemptId: String =
        checkNotNull(
            savedStateHandle[
                SangfroidDestination.FeedbackArgument
            ]
        )

    private val isEvaluating =
        MutableStateFlow(false)

    private val isSavingRevision =
        MutableStateFlow(false)

    private val isDeletingRevision =
        MutableStateFlow(false)

    private val revisionSaved =
        MutableStateFlow(false)

    private val isRevisionHistoryVisible =
        MutableStateFlow(false)

    private val selectedRevision =
        MutableStateFlow<SavedRevision?>(null)

    private val revisionPendingDeletion =
        MutableStateFlow<SavedRevision?>(null)

    private val errorMessage =
        MutableStateFlow<String?>(null)

    private val revisionErrorMessage =
        MutableStateFlow<String?>(null)

    private var evaluationJob:
            Job? = null

    private var revisionSaveJob:
            Job? = null

    private var revisionDeleteJob:
            Job? = null

    private val attemptFlow =
        evaluationRepository.observeAttempt(
            attemptId =
                attemptId
        )

    private val questionFlow =
        evaluationRepository.observeQuestion(
            attemptId =
                attemptId
        )

    private val evaluationFlow =
        evaluationRepository.observeEvaluation(
            attemptId =
                attemptId
        )

    private val revisionsFlow:
            Flow<List<SavedRevision>> =
        questionFlow.flatMapLatest { question ->
            if (question == null) {
                flowOf(
                    emptyList()
                )
            } else {
                practiceHistoryRepository
                    .observeRevisionsForQuestion(
                        questionId =
                            question.id
                    )
            }
        }

    private val progressFlow:
            Flow<QuestionProgress?> =
        questionFlow.flatMapLatest { question ->
            if (question == null) {
                flowOf(
                    null
                )
            } else {
                getQuestionProgress(
                    questionId =
                        question.id
                )
            }
        }

    private val feedbackData =
        combine(
            attemptFlow,
            questionFlow,
            evaluationFlow,
            revisionsFlow,
            progressFlow
        ) {
                attempt,
                question,
                evaluation,
                revisions,
                progress ->

            FeedbackData(
                attempt =
                    attempt,
                question =
                    question,
                evaluation =
                    evaluation,
                revisions =
                    revisions,
                questionProgress =
                    progress
            )
        }

    private val operationFlags =
        combine(
            isEvaluating,
            isSavingRevision,
            isDeletingRevision,
            revisionSaved
        ) {
                evaluating,
                savingRevision,
                deletingRevision,
                saved ->

            OperationFlags(
                isEvaluating =
                    evaluating,
                isSavingRevision =
                    savingRevision,
                isDeletingRevision =
                    deletingRevision,
                revisionSaved =
                    saved
            )
        }

    private val operationState =
        combine(
            operationFlags,
            errorMessage,
            revisionErrorMessage
        ) {
                flags,
                evaluationError,
                revisionError ->

            OperationState(
                isEvaluating =
                    flags.isEvaluating,
                isSavingRevision =
                    flags.isSavingRevision,
                isDeletingRevision =
                    flags.isDeletingRevision,
                revisionSaved =
                    flags.revisionSaved,
                errorMessage =
                    evaluationError,
                revisionErrorMessage =
                    revisionError
            )
        }

    private val overlayState =
        combine(
            isRevisionHistoryVisible,
            selectedRevision,
            revisionPendingDeletion
        ) {
                historyVisible,
                selected,
                pendingDeletion ->

            OverlayState(
                isRevisionHistoryVisible =
                    historyVisible,
                selectedRevision =
                    selected,
                revisionPendingDeletion =
                    pendingDeletion
            )
        }

    val uiState:
            StateFlow<FeedbackUiState> =
        combine(
            feedbackData,
            operationState,
            overlayState
        ) {
                data,
                operation,
                overlay ->

            FeedbackUiState(
                attempt =
                    data.attempt,
                question =
                    data.question,
                evaluation =
                    data.evaluation,
                questionProgress =
                    data.questionProgress,
                revisions =
                    data.revisions
                        .sortedWith(
                            compareByDescending<
                                    SavedRevision
                                    > {
                                it.versionNumber
                            }
                                .thenByDescending {
                                    it.createdAt
                                }
                        ),
                isLoading =
                    data.attempt == null ||
                            data.question == null,
                isEvaluating =
                    operation.isEvaluating,
                isSavingRevision =
                    operation.isSavingRevision,
                isDeletingRevision =
                    operation.isDeletingRevision,
                revisionSaved =
                    operation.revisionSaved,
                isRevisionHistoryVisible =
                    overlay
                        .isRevisionHistoryVisible,
                selectedRevision =
                    overlay.selectedRevision,
                revisionPendingDeletion =
                    overlay
                        .revisionPendingDeletion,
                errorMessage =
                    operation.errorMessage,
                revisionErrorMessage =
                    operation
                        .revisionErrorMessage
            )
        }
            .stateIn(
                scope =
                    viewModelScope,
                started =
                    SharingStarted.WhileSubscribed(
                        stopTimeoutMillis =
                            5_000L
                    ),
                initialValue =
                    FeedbackUiState()
            )

    init {
        evaluateIfRequired()
    }

    fun retry() {
        runEvaluation(
            forceRetry =
                true
        )
    }

    fun saveImprovedRevision() {
        if (
            isSavingRevision.value ||
            isDeletingRevision.value ||
            revisionSaveJob?.isActive == true
        ) {
            return
        }

        val state =
            uiState.value

        val attempt =
            state.attempt
                ?: return

        val question =
            state.question
                ?: return

        val content =
            state.improvedAnswerExample
                ?: return

        if (state.isCurrentRevisionSaved) {
            revisionSaved.value =
                true

            revisionErrorMessage.value =
                null

            return
        }

        revisionSaveJob =
            viewModelScope.launch {
                isSavingRevision.value =
                    true

                revisionSaved.value =
                    false

                revisionErrorMessage.value =
                    null

                try {
                    saveRevisionUseCase(
                        attemptId =
                            attempt.id,
                        questionId =
                            question.id,
                        content =
                            content,
                        source =
                            RevisionSource
                                .QWEN_EXAMPLE
                    )

                    revisionSaved.value =
                        true
                } catch (
                    cancellation:
                    CancellationException
                ) {
                    throw cancellation
                } catch (
                    throwable: Throwable
                ) {
                    revisionErrorMessage.value =
                        throwable.toUserMessage(
                            fallback =
                                "The revision could not be saved."
                        )
                } finally {
                    isSavingRevision.value =
                        false
                }
            }
    }

    fun showRevisionHistory() {
        if (
            uiState.value.revisions.isEmpty() ||
            isDeletingRevision.value
        ) {
            return
        }

        revisionErrorMessage.value =
            null

        revisionPendingDeletion.value =
            null

        selectedRevision.value =
            null

        isRevisionHistoryVisible.value =
            true
    }

    fun hideRevisionHistory() {
        if (isDeletingRevision.value) {
            return
        }

        revisionErrorMessage.value =
            null

        revisionPendingDeletion.value =
            null

        selectedRevision.value =
            null

        isRevisionHistoryVisible.value =
            false
    }

    fun openRevision(
        revision: SavedRevision
    ) {
        if (isDeletingRevision.value) {
            return
        }

        revisionErrorMessage.value =
            null

        revisionPendingDeletion.value =
            null

        selectedRevision.value =
            revision

        isRevisionHistoryVisible.value =
            true
    }

    fun closeRevision() {
        if (isDeletingRevision.value) {
            return
        }

        revisionErrorMessage.value =
            null

        revisionPendingDeletion.value =
            null

        selectedRevision.value =
            null
    }

    fun requestRevisionDeletion(
        revision: SavedRevision
    ) {
        if (
            isDeletingRevision.value ||
            revisionDeleteJob?.isActive == true
        ) {
            return
        }

        val currentRevision =
            uiState.value.revisions
                .firstOrNull { savedRevision ->
                    savedRevision.id ==
                            revision.id
                }
                ?: return

        revisionErrorMessage.value =
            null

        revisionPendingDeletion.value =
            currentRevision
    }

    fun cancelRevisionDeletion() {
        if (isDeletingRevision.value) {
            return
        }

        revisionErrorMessage.value =
            null

        revisionPendingDeletion.value =
            null
    }

    fun confirmRevisionDeletion() {
        if (
            isDeletingRevision.value ||
            revisionDeleteJob?.isActive == true
        ) {
            return
        }

        val revision =
            revisionPendingDeletion.value
                ?: return

        revisionDeleteJob =
            viewModelScope.launch {
                isDeletingRevision.value =
                    true

                revisionErrorMessage.value =
                    null

                try {
                    deleteSavedRevisionUseCase(
                        revisionId =
                            revision.id
                    )

                    if (
                        selectedRevision.value
                            ?.id ==
                        revision.id
                    ) {
                        selectedRevision.value =
                            null
                    }

                    revisionPendingDeletion.value =
                        null

                    revisionSaved.value =
                        false
                } catch (
                    cancellation:
                    CancellationException
                ) {
                    throw cancellation
                } catch (
                    throwable: Throwable
                ) {
                    revisionPendingDeletion.value =
                        null

                    revisionErrorMessage.value =
                        throwable.toUserMessage(
                            fallback =
                                "The revision could not be deleted."
                        )
                } finally {
                    isDeletingRevision.value =
                        false
                }
            }
    }

    fun dismissError() {
        errorMessage.value =
            null
    }

    fun dismissRevisionMessage() {
        revisionSaved.value =
            false

        revisionErrorMessage.value =
            null
    }

    private fun evaluateIfRequired() {
        runEvaluation(
            forceRetry =
                false
        )
    }

    private fun runEvaluation(
        forceRetry: Boolean
    ) {
        if (
            isEvaluating.value ||
            evaluationJob?.isActive == true
        ) {
            return
        }

        evaluationJob =
            viewModelScope.launch {
                isEvaluating.value =
                    true

                errorMessage.value =
                    null

                revisionSaved.value =
                    false

                revisionErrorMessage.value =
                    null

                Log.d(
                    EvaluationLogTag,
                    "Starting evaluation. " +
                            "attemptId=$attemptId " +
                            "forceRetry=$forceRetry"
                )

                try {
                    val result =
                        if (forceRetry) {
                            evaluationRepository
                                .retryEvaluation(
                                    attemptId =
                                        attemptId
                                )
                        } else {
                            evaluationRepository
                                .evaluateAttempt(
                                    attemptId =
                                        attemptId
                                )
                        }

                    result
                        .onSuccess {
                            Log.d(
                                EvaluationLogTag,
                                "Evaluation completed. " +
                                        "attemptId=$attemptId"
                            )
                        }
                        .onFailure { throwable ->
                            Log.e(
                                EvaluationLogTag,
                                "Evaluation failed. " +
                                        "attemptId=$attemptId",
                                throwable
                            )

                            errorMessage.value =
                                throwable.toUserMessage(
                                    fallback =
                                        "Feedback could not be generated."
                                )
                        }
                } catch (
                    cancellation:
                    CancellationException
                ) {
                    throw cancellation
                } catch (
                    throwable: Throwable
                ) {
                    Log.e(
                        EvaluationLogTag,
                        "Unexpected evaluation failure. " +
                                "attemptId=$attemptId",
                        throwable
                    )

                    errorMessage.value =
                        throwable.toUserMessage(
                            fallback =
                                "Feedback could not be generated."
                        )
                } finally {
                    isEvaluating.value =
                        false
                }
            }
    }

    private fun Throwable.toUserMessage(
        fallback: String
    ): String {
        message
            ?.trim()
            ?.takeIf(
                String::isNotBlank
            )
            ?.let { directMessage ->
                return directMessage
            }

        var currentCause =
            cause

        while (currentCause != null) {
            currentCause
                .message
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )
                ?.let { causeMessage ->
                    return causeMessage
                }

            currentCause =
                currentCause.cause
        }

        return fallback
    }

    override fun onCleared() {
        evaluationJob?.cancel()

        revisionSaveJob?.cancel()

        revisionDeleteJob?.cancel()

        super.onCleared()
    }

    private data class FeedbackData(
        val attempt:
        PracticeAttemptEntity?,
        val question:
        QuestionEntity?,
        val evaluation:
        EvaluationResult?,
        val revisions:
        List<SavedRevision>,
        val questionProgress:
        QuestionProgress?
    )

    private data class OperationFlags(
        val isEvaluating:
        Boolean,
        val isSavingRevision:
        Boolean,
        val isDeletingRevision:
        Boolean,
        val revisionSaved:
        Boolean
    )

    private data class OperationState(
        val isEvaluating:
        Boolean,
        val isSavingRevision:
        Boolean,
        val isDeletingRevision:
        Boolean,
        val revisionSaved:
        Boolean,
        val errorMessage:
        String?,
        val revisionErrorMessage:
        String?
    )

    private data class OverlayState(
        val isRevisionHistoryVisible:
        Boolean,
        val selectedRevision:
        SavedRevision?,
        val revisionPendingDeletion:
        SavedRevision?
    )
}