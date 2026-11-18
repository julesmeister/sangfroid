package com.julesmeister.sangfroid.domain.history

import javax.inject.Inject

class SaveRevisionUseCase @Inject constructor(
    private val repository: PracticeHistoryRepository
) {

    suspend operator fun invoke(
        attemptId: String,
        questionId: String,
        content: String,
        source: RevisionSource =
            RevisionSource.QWEN_EXAMPLE
    ): SavedRevision {
        val normalizedAttemptId =
            attemptId.trim()

        val normalizedQuestionId =
            questionId.trim()

        val normalizedContent =
            content.trim()

        require(normalizedAttemptId.isNotBlank()) {
            "Practice attempt ID cannot be blank."
        }

        require(normalizedQuestionId.isNotBlank()) {
            "Question ID cannot be blank."
        }

        require(normalizedContent.isNotBlank()) {
            "Revision content cannot be blank."
        }

        return repository.saveRevision(
            attemptId = normalizedAttemptId,
            questionId = normalizedQuestionId,
            content = normalizedContent,
            source = source
        )
    }
}