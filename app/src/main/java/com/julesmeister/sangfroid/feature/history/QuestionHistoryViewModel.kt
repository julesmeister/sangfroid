package com.julesmeister.sangfroid.feature.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.sangfroid.data.local.dao.QuestionDao
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.domain.history.PracticeAttempt
import com.julesmeister.sangfroid.domain.history.PracticeHistoryRepository
import com.julesmeister.sangfroid.domain.history.SavedRevision
import com.julesmeister.sangfroid.domain.progress.QuestionProgress
import com.julesmeister.sangfroid.domain.progress.QuestionProgressRepository
import com.julesmeister.sangfroid.feature.history.model.AttemptHistoryItem
import com.julesmeister.sangfroid.feature.history.model.RevisionHistoryItem
import com.julesmeister.sangfroid.navigation.SangfroidDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class QuestionHistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    questionDao: QuestionDao,
    questionProgressRepository:
    QuestionProgressRepository,
    private val practiceHistoryRepository:
    PracticeHistoryRepository
) : ViewModel() {

    private val questionId =
        requireNotNull(
            savedStateHandle.get<String>(
                SangfroidDestination
                    .QuestionHistoryArgument
            )
        ) {
            "questionId is required for question history."
        }

    private val deletingAttemptIds =
        MutableStateFlow<Set<String>>(
            emptySet()
        )

    private val deletingRevisionIds =
        MutableStateFlow<Set<String>>(
            emptySet()
        )

    private val actionError =
        MutableStateFlow<String?>(
            null
        )

    private val historyData =
        combine(
            questionDao.observeQuestion(
                questionId =
                    questionId
            ),
            questionProgressRepository
                .observeQuestionProgress(
                    questionId =
                        questionId
                ),
            practiceHistoryRepository
                .observeQuestionHistory(
                    questionId =
                        questionId
                ),
            practiceHistoryRepository
                .observeRevisionsForQuestion(
                    questionId =
                        questionId
                )
        ) {
                question,
                progress,
                attempts,
                revisions ->

            QuestionHistoryData(
                question =
                    question,
                progress =
                    progress,
                attempts =
                    attempts,
                revisions =
                    revisions
            )
        }

    val uiState:
            StateFlow<QuestionHistoryUiState> =
        combine(
            historyData,
            deletingAttemptIds,
            deletingRevisionIds,
            actionError
        ) {
                data,
                currentDeletingAttemptIds,
                currentDeletingRevisionIds,
                currentError ->

            QuestionHistoryUiState(
                isLoading =
                    false,
                question =
                    data.question,
                progress =
                    data.progress,
                attempts =
                    createAttemptItems(
                        attempts =
                            data.attempts
                    ),
                revisions =
                    createRevisionItems(
                        revisions =
                            data.revisions
                    ),
                deletingAttemptIds =
                    currentDeletingAttemptIds,
                deletingRevisionIds =
                    currentDeletingRevisionIds,
                errorMessage =
                    currentError
                        ?: if (
                            data.question ==
                            null
                        ) {
                            "This interview question could not be found."
                        } else {
                            null
                        }
            )
        }
            .catch { throwable ->
                emit(
                    QuestionHistoryUiState(
                        isLoading =
                            false,
                        errorMessage =
                            throwable.message
                                ?: "Question history could not be loaded."
                    )
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
                    QuestionHistoryUiState()
            )

    fun deleteAttempt(
        attemptId: String
    ) {
        val normalizedAttemptId =
            attemptId
                .trim()
                .takeIf(
                    String::isNotBlank
                )
                ?: return

        if (
            normalizedAttemptId in
            deletingAttemptIds.value
        ) {
            return
        }

        viewModelScope.launch {
            deletingAttemptIds.value =
                deletingAttemptIds.value +
                        normalizedAttemptId

            actionError.value =
                null

            try {
                practiceHistoryRepository
                    .deletePracticeAttempt(
                        attemptId =
                            normalizedAttemptId
                    )
            } catch (
                cancellation:
                CancellationException
            ) {
                throw cancellation
            } catch (
                throwable: Throwable
            ) {
                actionError.value =
                    throwable.message
                        ?: "The practice attempt could not be deleted."
            } finally {
                deletingAttemptIds.value =
                    deletingAttemptIds.value -
                            normalizedAttemptId
            }
        }
    }

    fun deleteRevision(
        revisionId: String
    ) {
        val normalizedRevisionId =
            revisionId
                .trim()
                .takeIf(
                    String::isNotBlank
                )
                ?: return

        if (
            normalizedRevisionId in
            deletingRevisionIds.value
        ) {
            return
        }

        viewModelScope.launch {
            deletingRevisionIds.value =
                deletingRevisionIds.value +
                        normalizedRevisionId

            actionError.value =
                null

            try {
                practiceHistoryRepository
                    .deleteSavedRevision(
                        revisionId =
                            normalizedRevisionId
                    )
            } catch (
                cancellation:
                CancellationException
            ) {
                throw cancellation
            } catch (
                throwable: Throwable
            ) {
                actionError.value =
                    throwable.message
                        ?: "The saved revision could not be deleted."
            } finally {
                deletingRevisionIds.value =
                    deletingRevisionIds.value -
                            normalizedRevisionId
            }
        }
    }

    fun dismissError() {
        actionError.value =
            null
    }

    private fun createAttemptItems(
        attempts: List<PracticeAttempt>
    ): List<AttemptHistoryItem> {
        val orderedAttempts =
            attempts.sortedWith(
                compareBy<PracticeAttempt> {
                    it.submittedAt
                }
                    .thenBy {
                        it.startedAt
                    }
                    .thenBy {
                        it.id
                    }
            )

        val sequenceByAttemptId =
            orderedAttempts
                .mapIndexed {
                        index,
                        attempt ->

                    attempt.id to
                            index + 1
                }
                .toMap()

        return orderedAttempts
            .asReversed()
            .map { attempt ->
                AttemptHistoryItem(
                    attempt =
                        attempt,
                    sequenceNumber =
                        sequenceByAttemptId[
                            attempt.id
                        ] ?: 1
                )
            }
    }

    private fun createRevisionItems(
        revisions: List<SavedRevision>
    ): List<RevisionHistoryItem> {
        return revisions
            .sortedWith(
                compareByDescending<
                        SavedRevision
                        > {
                    it.versionNumber
                }
                    .thenByDescending {
                        it.createdAt
                    }
                    .thenBy {
                        it.id
                    }
            )
            .map { revision ->
                RevisionHistoryItem(
                    revision =
                        revision
                )
            }
    }

    private data class QuestionHistoryData(
        val question: QuestionEntity?,
        val progress: QuestionProgress,
        val attempts: List<PracticeAttempt>,
        val revisions: List<SavedRevision>
    )
}