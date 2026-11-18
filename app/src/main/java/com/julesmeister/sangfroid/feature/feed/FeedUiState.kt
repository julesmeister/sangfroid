package com.julesmeister.sangfroid.feature.feed

import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.data.recommendation.QuestionRecommendationScore
import com.julesmeister.sangfroid.feature.feed.custom.QuestionImportUiState

data class FeedUiState(
    val isLoading: Boolean = true,
    val questions:
    List<QuestionEntity> =
        emptyList(),
    val recommendationScores:
    Map<String, QuestionRecommendationScore> =
        emptyMap(),
    val skippedCount: Int = 0,
    val isProcessingAction: Boolean = false,
    val freshGeneration:
    FreshQuestionGenerationState =
        FreshQuestionGenerationState.Idle,
    val questionImport:
    QuestionImportUiState =
        QuestionImportUiState(),
    val errorMessage: String? = null
) {

    val currentQuestion:
            QuestionEntity?
        get() =
            questions.firstOrNull()

    val currentRecommendationScore:
            QuestionRecommendationScore?
        get() =
            currentQuestion
                ?.id
                ?.let { questionId ->
                    recommendationScores[
                        questionId
                    ]
                }

    val recommendationLabel: String?
        get() =
            currentRecommendationScore
                ?.displayLabel

    val recommendationSupportingLabel:
            String?
        get() =
            currentRecommendationScore
                ?.supportingLabel

    val isGeneratingFreshQuestions: Boolean
        get() =
            freshGeneration is
                    FreshQuestionGenerationState.Preparing ||
                    freshGeneration is
                            FreshQuestionGenerationState.Generating ||
                    freshGeneration is
                            FreshQuestionGenerationState.Reviewing

    val isImportingQuestions: Boolean
        get() =
            questionImport.isBusy

    val canGenerateFresh: Boolean
        get() =
            !isProcessingAction &&
                    !isGeneratingFreshQuestions &&
                    !isImportingQuestions

    val canRestoreSkipped: Boolean
        get() =
            skippedCount > 0 &&
                    !isProcessingAction &&
                    !isGeneratingFreshQuestions &&
                    !isImportingQuestions

    val canOpenQuestionImport: Boolean
        get() =
            !isProcessingAction &&
                    !isGeneratingFreshQuestions &&
                    !isImportingQuestions

    val isEmpty: Boolean
        get() =
            !isLoading &&
                    questions.isEmpty()

    val hasQuestions: Boolean
        get() =
            questions.isNotEmpty()
}

sealed interface FreshQuestionGenerationState {

    data object Idle :
        FreshQuestionGenerationState

    data class Preparing(
        val cvEvidenceCount: Int
    ) : FreshQuestionGenerationState

    data class Generating(
        val requestedCount: Int
    ) : FreshQuestionGenerationState

    data class Reviewing(
        val candidateCount: Int
    ) : FreshQuestionGenerationState

    data class Complete(
        val addedCount: Int,
        val duplicateCount: Int
    ) : FreshQuestionGenerationState

    data class Failed(
        val message: String
    ) : FreshQuestionGenerationState
}