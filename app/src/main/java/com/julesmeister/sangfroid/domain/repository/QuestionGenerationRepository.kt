package com.julesmeister.sangfroid.domain.repository

import com.julesmeister.sangfroid.domain.recommendation.RecommendationContext

interface QuestionGenerationRepository {

    suspend fun generateAndStore(
        context: RecommendationContext,
        requestedCount: Int
    ): Result<GenerationResult>

    data class GenerationResult(
        val requestId: String,
        val requestedCount: Int,
        val candidateCount: Int,
        val storedQuestionCount: Int,
        val storedQuestionIds: List<String>,
        val exactDuplicateCount: Int,
        val similarDuplicateCount: Int,
        val modelName: String,
        val modelVersion: String,
        val generatedAt: Long
    ) {
        init {
            require(requestId.isNotBlank()) {
                "requestId cannot be blank."
            }

            require(requestedCount in 1..10) {
                "requestedCount must be between one and ten."
            }

            require(candidateCount >= 0) {
                "candidateCount cannot be negative."
            }

            require(storedQuestionCount >= 0) {
                "storedQuestionCount cannot be negative."
            }

            require(
                storedQuestionCount ==
                        storedQuestionIds.size
            ) {
                "storedQuestionCount must match storedQuestionIds."
            }

            require(exactDuplicateCount >= 0) {
                "exactDuplicateCount cannot be negative."
            }

            require(similarDuplicateCount >= 0) {
                "similarDuplicateCount cannot be negative."
            }

            require(
                storedQuestionCount +
                        exactDuplicateCount +
                        similarDuplicateCount <=
                        candidateCount
            ) {
                "Generation metrics cannot exceed candidateCount."
            }

            require(
                storedQuestionIds.none(String::isBlank)
            ) {
                "storedQuestionIds cannot contain blank IDs."
            }

            require(
                storedQuestionIds.distinct().size ==
                        storedQuestionIds.size
            ) {
                "storedQuestionIds must be unique."
            }

            require(modelName.isNotBlank()) {
                "modelName cannot be blank."
            }

            require(modelVersion.isNotBlank()) {
                "modelVersion cannot be blank."
            }

            require(generatedAt > 0L) {
                "generatedAt must be positive."
            }
        }

        val rejectedDuplicateCount: Int
            get() =
                exactDuplicateCount +
                        similarDuplicateCount

        val complete: Boolean
            get() =
                storedQuestionCount >=
                        requestedCount
    }
}