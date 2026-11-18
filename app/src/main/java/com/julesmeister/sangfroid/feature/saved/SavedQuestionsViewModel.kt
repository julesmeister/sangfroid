package com.julesmeister.sangfroid.feature.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.domain.progress.QuestionProgressRepository
import com.julesmeister.sangfroid.domain.repository.QuestionRepository
import com.julesmeister.sangfroid.feature.saved.model.SavedQuestionItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SavedQuestionsViewModel @Inject constructor(
    private val questionRepository:
    QuestionRepository,
    private val questionProgressRepository:
    QuestionProgressRepository
) : ViewModel() {

    private val removingQuestionIds =
        MutableStateFlow<Set<String>>(
            emptySet()
        )

    private val actionError =
        MutableStateFlow<String?>(
            null
        )

    private val savedQuestionItems:
            Flow<List<SavedQuestionItem>> =
        questionRepository
            .observeSavedQuestions()
            .flatMapLatest { questions ->
                observeSavedQuestionsWithProgress(
                    questions =
                        questions
                )
            }

    val uiState:
            StateFlow<SavedQuestionsUiState> =
        combine(
            savedQuestionItems,
            removingQuestionIds,
            actionError
        ) {
                questions,
                removingIds,
                error ->

            SavedQuestionsUiState(
                isLoading =
                    false,
                questions =
                    questions,
                removingQuestionIds =
                    removingIds,
                errorMessage =
                    error
            )
        }
            .catch { throwable ->
                emit(
                    SavedQuestionsUiState(
                        isLoading =
                            false,
                        errorMessage =
                            throwable.message
                                ?: "Saved questions could not be loaded."
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
                    SavedQuestionsUiState()
            )

    fun removeQuestion(
        questionId: String
    ) {
        val normalizedQuestionId =
            questionId
                .trim()
                .takeIf(
                    String::isNotBlank
                )
                ?: return

        if (
            normalizedQuestionId in
            removingQuestionIds.value
        ) {
            return
        }

        viewModelScope.launch {
            removingQuestionIds.value += normalizedQuestionId

            actionError.value =
                null

            try {
                questionRepository
                    .unsaveQuestion(
                        questionId =
                            normalizedQuestionId
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
                        ?: "The question could not be removed from Saved."
            } finally {
                removingQuestionIds.value -= normalizedQuestionId
            }
        }
    }

    fun dismissError() {
        actionError.value =
            null
    }

    private fun observeSavedQuestionsWithProgress(
        questions: List<QuestionEntity>
    ): Flow<List<SavedQuestionItem>> {
        if (questions.isEmpty()) {
            return flowOf(
                emptyList()
            )
        }

        val itemFlows:
                List<Flow<SavedQuestionItem>> =
            questions.map { question ->
                questionProgressRepository
                    .observeQuestionProgress(
                        questionId =
                            question.id
                    )
                    .map { progress ->
                        SavedQuestionItem(
                            question =
                                question,
                            progress =
                                progress
                        )
                    }
            }

        return combine(
            itemFlows
        ) { items ->
            /*
             * QuestionDao.observeSavedQuestions() already
             * returns newest saved questions first. combine()
             * preserves the order of itemFlows, so no second
             * sort is needed here.
             */
            items.toList()
        }
    }
}