package com.julesmeister.sangfroid.domain.repository

import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.data.local.entity.QuestionSkillCrossRef

interface QuestionImportRepository {

    suspend fun extractQuestions(
        content: String,
        languageTag: String
    ): Result<ImportPreview>

    suspend fun storeQuestions(
        preview: ImportPreview
    ): Result<ImportResult>

    data class ImportCandidate(
        val question: QuestionEntity,
        val skillCrossReferences:
        List<QuestionSkillCrossRef>
    )

    data class ImportPreview(
        val requestId: String,
        val candidates: List<ImportCandidate>,
        val detectedCount: Int,
        val exactDuplicateCount: Int,
        val similarDuplicateCount: Int,
        val invalidCandidateCount: Int,
        val modelName: String,
        val modelVersion: String,
        val processedAt: Long
    ) {
        val questions: List<QuestionEntity>
            get() =
                candidates.map(
                    ImportCandidate::question
                )

        val importableCount: Int
            get() =
                candidates.size

        val duplicateCount: Int
            get() =
                exactDuplicateCount +
                        similarDuplicateCount
    }

    data class ImportResult(
        val addedCount: Int,
        val addedQuestionIds: List<String>,
        val duplicateCount: Int,
        val invalidCandidateCount: Int
    )
}