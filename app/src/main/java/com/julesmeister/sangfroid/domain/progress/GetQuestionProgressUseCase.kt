package com.julesmeister.sangfroid.domain.progress

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

class GetQuestionProgressUseCase @Inject constructor(
    private val repository:
    QuestionProgressRepository
) {

    operator fun invoke(
        questionId: String
    ): Flow<QuestionProgress> {
        val normalizedQuestionId =
            questionId.trim()

        require(
            normalizedQuestionId.isNotBlank()
        ) {
            "Question ID cannot be blank."
        }

        return repository
            .observeQuestionProgress(
                questionId =
                    normalizedQuestionId
            )
            .distinctUntilChanged()
    }

    suspend fun once(
        questionId: String
    ): QuestionProgress {
        val normalizedQuestionId =
            questionId.trim()

        require(
            normalizedQuestionId.isNotBlank()
        ) {
            "Question ID cannot be blank."
        }

        return repository.getQuestionProgress(
            questionId =
                normalizedQuestionId
        )
    }
}