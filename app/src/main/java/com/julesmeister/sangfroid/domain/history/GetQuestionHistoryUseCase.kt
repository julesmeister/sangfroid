package com.julesmeister.sangfroid.domain.history

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetQuestionHistoryUseCase @Inject constructor(
    private val repository: PracticeHistoryRepository
) {

    operator fun invoke(
        questionId: String
    ): Flow<List<PracticeAttempt>> {
        val normalizedQuestionId = questionId.trim()

        require(normalizedQuestionId.isNotBlank()) {
            "Question ID cannot be blank."
        }

        return repository
            .observeQuestionHistory(normalizedQuestionId)
            .map { attempts ->
                attempts.sortedByDescending(
                    PracticeAttempt::submittedAt
                )
            }
    }
}