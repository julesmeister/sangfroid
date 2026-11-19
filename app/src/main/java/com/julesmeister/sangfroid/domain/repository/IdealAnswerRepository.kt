package com.julesmeister.sangfroid.domain.repository

import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.data.remote.dto.CvEvidenceDto

interface IdealAnswerRepository {

    suspend fun generate(
        question: QuestionEntity,
        revisionNumber: Int,
        previousIdealAnswer: String?,
        targetRole: String?,
        experienceLevel: String?,
        languageTag: String,
        cvEvidence: List<CvEvidenceDto>
    ): Result<IdealAnswer>

    data class IdealAnswer(
        val requestId: String,
        val questionId: String,
        val content: String,
        val revisionNumber: Int,
        val usedEvidenceIds: List<String>,
        val modelName: String,
        val modelVersion: String,
        val generatedAt: Long
    ) {
        init {
            require(
                requestId.isNotBlank()
            )

            require(
                questionId.isNotBlank()
            )

            require(
                content.isNotBlank()
            )

            require(
                revisionNumber >= 1
            )
        }
    }
}