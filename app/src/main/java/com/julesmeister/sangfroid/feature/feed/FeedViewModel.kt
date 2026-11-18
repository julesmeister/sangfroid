package com.julesmeister.sangfroid.feature.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.data.recommendation.QuestionRecommendationScore
import com.julesmeister.sangfroid.data.recommendation.RankedQuestion
import com.julesmeister.sangfroid.data.recommendation.ReactiveQuestionRecommendationRanker
import com.julesmeister.sangfroid.data.recommendation.RecommendationContextBuilder
import com.julesmeister.sangfroid.domain.repository.QuestionGenerationRepository
import com.julesmeister.sangfroid.domain.repository.QuestionImportRepository
import com.julesmeister.sangfroid.domain.repository.QuestionRepository
import com.julesmeister.sangfroid.feature.feed.custom.QuestionImportStage
import com.julesmeister.sangfroid.feature.feed.custom.QuestionImportUiState
import com.julesmeister.sangfroid.feature.feed.model.SwipeAction
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val questionRepository:
    QuestionRepository,
    private val questionGenerationRepository:
    QuestionGenerationRepository,
    private val questionImportRepository:
    QuestionImportRepository,
    private val recommendationContextBuilder:
    RecommendationContextBuilder,
    private val reactiveRecommendationRanker:
    ReactiveQuestionRecommendationRanker
) : ViewModel() {

    private val isProcessingAction =
        MutableStateFlow(false)

    private val actionError =
        MutableStateFlow<String?>(null)

    private val freshGeneration =
        MutableStateFlow<
                FreshQuestionGenerationState
                >(
            FreshQuestionGenerationState.Idle
        )

    private val questionImport =
        MutableStateFlow(
            QuestionImportUiState()
        )

    private var generationJob: Job? =
        null

    private var questionImportJob: Job? =
        null

    private val rawQuestionsFlow =
        questionRepository.observeQuestions()

    private val rankedFeedFlow:
            Flow<RankedFeed> =
        rawQuestionsFlow
            .flatMapLatest { questions ->
                reactiveRecommendationRanker
                    .observeRankedQuestions(
                        questions = questions
                    )
            }
            .combine(
                rawQuestionsFlow
            ) {
                    rankedQuestions,
                    currentQuestions ->

                buildRankedFeed(
                    rankedQuestions =
                        rankedQuestions,
                    fallbackQuestions =
                        currentQuestions
                )
            }

    private val actionState =
        combine(
            isProcessingAction,
            freshGeneration,
            questionImport,
            actionError
        ) {
                processing,
                generation,
                importState,
                error ->

            FeedActionState(
                isProcessingAction =
                    processing,
                freshGeneration =
                    generation,
                questionImport =
                    importState,
                errorMessage =
                    error
            )
        }

    val uiState:
            StateFlow<FeedUiState> =
        combine(
            rankedFeedFlow,
            questionRepository
                .observeSkippedCount(),
            actionState
        ) {
                rankedFeed,
                skippedCount,
                actions ->

            FeedUiState(
                isLoading =
                    false,
                questions =
                    rankedFeed.questions,
                recommendationScores =
                    rankedFeed
                        .scoresByQuestionId,
                skippedCount =
                    skippedCount,
                isProcessingAction =
                    actions
                        .isProcessingAction,
                freshGeneration =
                    actions.freshGeneration,
                questionImport =
                    actions.questionImport,
                errorMessage =
                    actions.errorMessage
            )
        }
            .catch { throwable ->
                Log.e(
                    LOG_TAG,
                    "Unable to build the recommended feed.",
                    throwable
                )

                emit(
                    FeedUiState(
                        isLoading =
                            false,
                        freshGeneration =
                            freshGeneration.value,
                        questionImport =
                            questionImport.value,
                        errorMessage =
                            throwable.message
                                ?: "Unable to load interview questions."
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
                    FeedUiState()
            )

    init {
        initialiseFeed()
    }

    fun resolveSwipe(
        action: SwipeAction,
        questionId: String,
        onPracticeRequested:
            (String) -> Unit
    ) {
        if (
            isProcessingAction.value ||
            isGenerating() ||
            questionImport.value.isBusy
        ) {
            return
        }

        when (action) {
            SwipeAction.PRACTICE -> {
                onPracticeRequested(
                    questionId
                )
            }

            SwipeAction.SKIP -> {
                runAction(
                    fallbackError =
                        "The question could not be skipped."
                ) {
                    questionRepository
                        .skipQuestion(
                            questionId
                        )
                }
            }

            SwipeAction.SAVE -> {
                runAction(
                    fallbackError =
                        "The question could not be saved."
                ) {
                    questionRepository
                        .saveQuestion(
                            questionId
                        )
                }
            }
        }
    }

    fun restoreSkippedQuestions() {
        runAction(
            fallbackError =
                "Skipped questions could not be restored."
        ) {
            questionRepository
                .restoreSkippedQuestions()
        }
    }

    fun restartFeed() {
        runAction(
            fallbackError =
                "The deck could not be restarted."
        ) {
            questionRepository
                .restartFeed()
        }
    }

    fun generateFreshQuestions() {
        if (
            generationJob?.isActive ==
            true ||
            isGenerating() ||
            questionImport.value.isBusy ||
            isProcessingAction.value
        ) {
            return
        }

        generationJob =
            viewModelScope.launch {
                actionError.value =
                    null

                try {
                    freshGeneration.value =
                        FreshQuestionGenerationState
                            .Preparing(
                                cvEvidenceCount =
                                    0
                            )

                    val context =
                        recommendationContextBuilder
                            .build()

                    freshGeneration.value =
                        FreshQuestionGenerationState
                            .Preparing(
                                cvEvidenceCount =
                                    context
                                        .cvEvidence
                                        .size
                            )

                    freshGeneration.value =
                        FreshQuestionGenerationState
                            .Generating(
                                requestedCount =
                                    GENERATION_BATCH_SIZE
                            )

                    val result =
                        questionGenerationRepository
                            .generateAndStore(
                                context =
                                    context,
                                requestedCount =
                                    GENERATION_BATCH_SIZE
                            )
                            .getOrThrow()

                    freshGeneration.value =
                        FreshQuestionGenerationState
                            .Reviewing(
                                candidateCount =
                                    result.candidateCount
                            )

                    freshGeneration.value =
                        if (
                            result.storedQuestionCount >
                            0
                        ) {
                            FreshQuestionGenerationState
                                .Complete(
                                    addedCount =
                                        result
                                            .storedQuestionCount,
                                    duplicateCount =
                                        result
                                            .rejectedDuplicateCount
                                )
                        } else {
                            FreshQuestionGenerationState
                                .Failed(
                                    message =
                                        if (
                                            result.rejectedDuplicateCount >
                                            0
                                        ) {
                                            "Qwen returned questions that were already covered or too similar to your existing questions."
                                        } else {
                                            "No new interview questions were generated."
                                        }
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
                        LOG_TAG,
                        "Fresh-question generation failed.",
                        throwable
                    )

                    freshGeneration.value =
                        FreshQuestionGenerationState
                            .Failed(
                                message =
                                    friendlyGenerationError(
                                        throwable
                                    )
                            )
                }
            }
    }

    fun dismissGenerationMessage() {
        if (!isGenerating()) {
            freshGeneration.value =
                FreshQuestionGenerationState
                    .Idle
        }
    }

    fun openQuestionImport() {
        if (
            isProcessingAction.value ||
            isGenerating() ||
            questionImport.value.isBusy
        ) {
            return
        }

        questionImport.value =
            QuestionImportUiState(
                isVisible =
                    true
            )
    }

    fun closeQuestionImport() {
        if (
            questionImport.value.isBusy
        ) {
            return
        }

        questionImport.value =
            QuestionImportUiState()
    }

    fun updateQuestionImportContent(
        content: String
    ) {
        val currentState =
            questionImport.value

        if (
            currentState.isBusy
        ) {
            return
        }

        questionImport.value =
            currentState.copy(
                pastedContent =
                    content.take(
                        QuestionImportUiState
                            .MAXIMUM_CONTENT_LENGTH
                    ),
                stage =
                    QuestionImportStage
                        .Editing,
                preview =
                    null,
                lastImportResult =
                    null,
                errorMessage =
                    null
            )
    }

    fun editQuestionImportContent() {
        val currentState =
            questionImport.value

        if (
            currentState.isBusy
        ) {
            return
        }

        questionImport.value =
            currentState.copy(
                stage =
                    QuestionImportStage
                        .Editing,
                preview =
                    null,
                lastImportResult =
                    null,
                errorMessage =
                    null
            )
    }

    fun extractImportedQuestions() {
        val currentState =
            questionImport.value

        if (
            questionImportJob?.isActive ==
            true ||
            currentState.isBusy ||
            !currentState.canAnalyze ||
            isGenerating() ||
            isProcessingAction.value
        ) {
            return
        }

        questionImportJob =
            viewModelScope.launch {
                questionImport.value =
                    currentState.copy(
                        stage =
                            QuestionImportStage
                                .Extracting,
                        preview =
                            null,
                        lastImportResult =
                            null,
                        errorMessage =
                            null
                    )

                try {
                    val preview =
                        questionImportRepository
                            .extractQuestions(
                                content =
                                    currentState
                                        .pastedContent,
                                languageTag =
                                    Locale
                                        .getDefault()
                                        .toLanguageTag()
                                        .ifBlank {
                                            DEFAULT_LANGUAGE_TAG
                                        }
                            )
                            .getOrThrow()

                    questionImport.value =
                        questionImport.value
                            .copy(
                                stage =
                                    QuestionImportStage
                                        .Preview,
                                preview =
                                    preview,
                                lastImportResult =
                                    null,
                                errorMessage =
                                    null
                            )
                } catch (
                    cancellation:
                    CancellationException
                ) {
                    throw cancellation
                } catch (
                    throwable: Throwable
                ) {
                    Log.e(
                        LOG_TAG,
                        "Question import extraction failed.",
                        throwable
                    )

                    questionImport.value =
                        questionImport.value
                            .copy(
                                stage =
                                    QuestionImportStage
                                        .Editing,
                                preview =
                                    null,
                                lastImportResult =
                                    null,
                                errorMessage =
                                    friendlyImportError(
                                        throwable
                                    )
                            )
                }
            }
    }

    fun confirmQuestionImport() {
        val currentState =
            questionImport.value

        val preview =
            currentState.preview
                ?: return

        if (
            questionImportJob?.isActive ==
            true ||
            currentState.isBusy ||
            !currentState.canImport ||
            isGenerating() ||
            isProcessingAction.value
        ) {
            return
        }

        questionImportJob =
            viewModelScope.launch {
                questionImport.value =
                    currentState.copy(
                        stage =
                            QuestionImportStage
                                .Importing,
                        errorMessage =
                            null
                    )

                try {
                    val result =
                        questionImportRepository
                            .storeQuestions(
                                preview =
                                    preview
                            )
                            .getOrThrow()

                    questionImport.value =
                        questionImport.value
                            .copy(
                                stage =
                                    QuestionImportStage
                                        .Complete,
                                lastImportResult =
                                    result,
                                errorMessage =
                                    null
                            )
                } catch (
                    cancellation:
                    CancellationException
                ) {
                    throw cancellation
                } catch (
                    throwable: Throwable
                ) {
                    Log.e(
                        LOG_TAG,
                        "Question import storage failed.",
                        throwable
                    )

                    questionImport.value =
                        questionImport.value
                            .copy(
                                stage =
                                    QuestionImportStage
                                        .Preview,
                                lastImportResult =
                                    null,
                                errorMessage =
                                    throwable.message
                                        ?.trim()
                                        ?.takeIf(
                                            String::isNotBlank
                                        )
                                        ?: "The extracted questions could not be added."
                            )
                }
            }
    }

    fun dismissError() {
        actionError.value =
            null
    }

    private fun initialiseFeed() {
        viewModelScope.launch {
            runCatching {
                questionRepository
                    .seedQuestionsIfRequired()
            }
                .onFailure { throwable ->
                    if (
                        throwable is
                                CancellationException
                    ) {
                        throw throwable
                    }

                    Log.e(
                        LOG_TAG,
                        "Unable to seed the initial question catalogue.",
                        throwable
                    )

                    actionError.value =
                        throwable.message
                            ?: "Initial questions could not be prepared."
                }
        }
    }

    private fun isGenerating():
            Boolean {
        return when (
            freshGeneration.value
        ) {
            is FreshQuestionGenerationState
            .Preparing,
            is FreshQuestionGenerationState
            .Generating,
            is FreshQuestionGenerationState
            .Reviewing -> {
                true
            }

            else -> {
                false
            }
        }
    }

    private fun friendlyGenerationError(
        throwable: Throwable
    ): String {
        val message =
            throwable.message
                ?.trim()
                .orEmpty()

        val normalized =
            message.lowercase(
                Locale.ROOT
            )

        return when {
            "too long" in normalized ||
                    "timed out" in normalized -> {
                "Qwen is taking longer than expected. Your existing feed is unchanged. Try generating again in a moment."
            }

            "connect" in normalized ||
                    "network" in normalized ||
                    "host" in normalized -> {
                "Sangfroid could not reach the question-generation service. Check the connection and try again."
            }

            message.isNotBlank() -> {
                message
            }

            else -> {
                "Fresh questions could not be generated."
            }
        }
    }

    private fun friendlyImportError(
        throwable: Throwable
    ): String {
        val message =
            throwable.message
                ?.trim()
                .orEmpty()

        val normalized =
            message.lowercase(
                Locale.ROOT
            )

        return when {
            "too long" in normalized ||
                    "timed out" in normalized -> {
                "Question extraction is taking longer than expected. Your existing feed is unchanged. Try again in a moment."
            }

            "connect" in normalized ||
                    "network" in normalized ||
                    "host" in normalized -> {
                "Sangfroid could not reach the question-import service. Check the connection and try again."
            }

            "no questions" in normalized ||
                    "contains no questions" in
                    normalized -> {
                "No interview questions were detected in the pasted content."
            }

            message.isNotBlank() -> {
                message
            }

            else -> {
                "The pasted questions could not be extracted."
            }
        }
    }

    private fun buildRankedFeed(
        rankedQuestions:
        List<RankedQuestion>,
        fallbackQuestions:
        List<QuestionEntity>
    ): RankedFeed {
        if (
            rankedQuestions.isEmpty()
        ) {
            return RankedFeed(
                questions =
                    fallbackQuestions,
                scoresByQuestionId =
                    emptyMap()
            )
        }

        val availableQuestionIds =
            fallbackQuestions
                .mapTo(
                    mutableSetOf(),
                    QuestionEntity::id
                )

        val validRankedQuestions =
            rankedQuestions.filter {
                    rankedQuestion ->
                rankedQuestion.question.id in
                        availableQuestionIds
            }

        val rankedQuestionIds =
            validRankedQuestions
                .mapTo(
                    mutableSetOf()
                ) { rankedQuestion ->
                    rankedQuestion
                        .question
                        .id
                }

        val unrankedQuestions =
            fallbackQuestions.filter {
                    question ->
                question.id !in
                        rankedQuestionIds
            }

        return RankedFeed(
            questions =
                validRankedQuestions
                    .map(
                        RankedQuestion::question
                    ) +
                        unrankedQuestions,
            scoresByQuestionId =
                validRankedQuestions
                    .associate {
                            rankedQuestion ->
                        rankedQuestion.question.id to
                                rankedQuestion.score
                    }
        )
    }

    private fun runAction(
        fallbackError: String,
        block: suspend () -> Unit
    ) {
        if (
            isProcessingAction.value ||
            isGenerating() ||
            questionImport.value.isBusy
        ) {
            return
        }

        viewModelScope.launch {
            isProcessingAction.value =
                true

            actionError.value =
                null

            try {
                block()
            } catch (
                cancellation:
                CancellationException
            ) {
                throw cancellation
            } catch (
                throwable: Throwable
            ) {
                Log.e(
                    LOG_TAG,
                    fallbackError,
                    throwable
                )

                actionError.value =
                    throwable.message
                        ?.takeIf(
                            String::isNotBlank
                        )
                        ?: fallbackError
            } finally {
                isProcessingAction.value =
                    false
            }
        }
    }

    override fun onCleared() {
        generationJob?.cancel()
        questionImportJob?.cancel()
        super.onCleared()
    }

    private data class RankedFeed(
        val questions:
        List<QuestionEntity> =
            emptyList(),
        val scoresByQuestionId:
        Map<
                String,
                QuestionRecommendationScore
                > =
            emptyMap()
    )

    private data class FeedActionState(
        val isProcessingAction:
        Boolean,
        val freshGeneration:
        FreshQuestionGenerationState,
        val questionImport:
        QuestionImportUiState,
        val errorMessage:
        String?
    )

    companion object {
        private const val LOG_TAG =
            "SangfroidFeed"

        private const val GENERATION_BATCH_SIZE =
            10

        private const val DEFAULT_LANGUAGE_TAG =
            "en"
    }
}