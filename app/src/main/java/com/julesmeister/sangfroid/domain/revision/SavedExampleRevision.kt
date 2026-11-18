package com.julesmeister.sangfroid.domain.revision

data class SavedExampleRevision(
    val id: String,
    val questionId: String,
    val attemptId: String,
    val evaluationId: String,
    val content: String,
    val version: Int,
    val modelName: String,
    val modelVersion: String?,
    val savedAt: Long
) {

    init {
        require(
            id.isNotBlank()
        ) {
            "Revision ID cannot be blank."
        }

        require(
            questionId.isNotBlank()
        ) {
            "Question ID cannot be blank."
        }

        require(
            attemptId.isNotBlank()
        ) {
            "Attempt ID cannot be blank."
        }

        require(
            evaluationId.isNotBlank()
        ) {
            "Evaluation ID cannot be blank."
        }

        require(
            content.isNotBlank()
        ) {
            "Revision content cannot be blank."
        }

        require(
            version > 0
        ) {
            "Revision version must be positive."
        }

        require(
            modelName.isNotBlank()
        ) {
            "Model name cannot be blank."
        }

        require(
            savedAt > 0L
        ) {
            "Revision savedAt must be positive."
        }
    }
}