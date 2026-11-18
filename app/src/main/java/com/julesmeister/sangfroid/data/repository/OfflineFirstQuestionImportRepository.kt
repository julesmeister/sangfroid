package com.julesmeister.sangfroid.data.repository

import com.julesmeister.sangfroid.data.local.dao.QuestionDao
import com.julesmeister.sangfroid.data.mapper.GeneratedQuestionMapper
import com.julesmeister.sangfroid.data.recommendation.QuestionSimilarityChecker
import com.julesmeister.sangfroid.data.remote.api.SangfroidApi
import com.julesmeister.sangfroid.data.remote.dto.QuestionImportRequestDto
import com.julesmeister.sangfroid.domain.repository.QuestionImportRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class OfflineFirstQuestionImportRepository
@Inject constructor(
    private val sangfroidApi:
    SangfroidApi,
    private val questionDao:
    QuestionDao,
    private val generatedQuestionMapper:
    GeneratedQuestionMapper,
    private val questionSimilarityChecker:
    QuestionSimilarityChecker
) : QuestionImportRepository {

    override suspend fun extractQuestions(
        content: String,
        languageTag: String
    ): Result<QuestionImportRepository.ImportPreview> {
        require(
            content.trim().length >=
                    MINIMUM_IMPORT_CONTENT_LENGTH
        ) {
            "Paste at least 20 characters of interview-question content."
        }

        return try {
            val requestId =
                UUID.randomUUID()
                    .toString()

            val existingQuestions =
                questionDao.getAllQuestions()

            val response =
                sangfroidApi
                    .importQuestions(
                        QuestionImportRequestDto(
                            requestId =
                                requestId,
                            content =
                                content,
                            languageTag =
                                languageTag
                        )
                    )
                    .getOrThrow()

            require(
                response.requestId.isBlank() ||
                        response.requestId ==
                        requestId
            ) {
                "The imported-question response does not match the request."
            }

            var invalidCandidateCount =
                0

            val mappedCandidates =
                response.questions
                    .mapNotNull { generatedQuestion ->
                        runCatching {
                            generatedQuestionMapper.map(
                                generatedQuestion =
                                    generatedQuestion,
                                generatedAt =
                                    response.processedAt,
                                source =
                                    GeneratedQuestionMapper
                                        .SOURCE_CUSTOM
                            )
                        }
                            .onFailure {
                                invalidCandidateCount++
                            }
                            .getOrNull()
                    }

            val review =
                questionSimilarityChecker.review(
                    candidates =
                        mappedCandidates.map { mapped ->
                            mapped.question
                        },
                    existingQuestions =
                        existingQuestions,
                    maximumAcceptedCount =
                        MAXIMUM_IMPORT_COUNT
                )

            val acceptedIds =
                review.accepted
                    .mapTo(
                        mutableSetOf()
                    ) { question ->
                        question.id
                    }

            val acceptedCandidates =
                mappedCandidates
                    .filter { mapped ->
                        mapped.question.id in
                                acceptedIds
                    }
                    .map { mapped ->
                        QuestionImportRepository
                            .ImportCandidate(
                                question =
                                    mapped.question,
                                skillCrossReferences =
                                    mapped
                                        .skillCrossReferences
                            )
                    }

            Result.success(
                QuestionImportRepository
                    .ImportPreview(
                        requestId =
                            requestId,
                        candidates =
                            acceptedCandidates,
                        detectedCount =
                            response.detectedCount,
                        exactDuplicateCount =
                            review.exactDuplicates.size,
                        similarDuplicateCount =
                            review.similarDuplicates.size,
                        invalidCandidateCount =
                            invalidCandidateCount,
                        modelName =
                            response.modelName,
                        modelVersion =
                            response.modelVersion,
                        processedAt =
                            response.processedAt
                    )
            )
        } catch (
            cancellation: CancellationException
        ) {
            throw cancellation
        } catch (
            throwable: Throwable
        ) {
            Result.failure(
                throwable
            )
        }
    }

    override suspend fun storeQuestions(
        preview: QuestionImportRepository.ImportPreview
    ): Result<QuestionImportRepository.ImportResult> {
        return try {
            val uniqueCandidates =
                preview.candidates
                    .distinctBy { candidate ->
                        candidate.question.id
                    }

            val questions =
                uniqueCandidates.map { candidate ->
                    candidate.question
                }

            val skillCrossReferences =
                uniqueCandidates
                    .flatMap { candidate ->
                        candidate.skillCrossReferences
                    }
                    .distinctBy { reference ->
                        reference.questionId to
                                reference.skillCode
                    }

            questionDao.insertQuestionCatalogue(
                questions =
                    questions,
                skills =
                    skillCrossReferences
            )

            Result.success(
                QuestionImportRepository
                    .ImportResult(
                        addedCount =
                            questions.size,
                        addedQuestionIds =
                            questions.map { question ->
                                question.id
                            },
                        duplicateCount =
                            preview.duplicateCount,
                        invalidCandidateCount =
                            preview.invalidCandidateCount
                    )
            )
        } catch (
            cancellation: CancellationException
        ) {
            throw cancellation
        } catch (
            throwable: Throwable
        ) {
            Result.failure(
                throwable
            )
        }
    }

    companion object {
        private const val MINIMUM_IMPORT_CONTENT_LENGTH =
            20

        private const val MAXIMUM_IMPORT_COUNT =
            50
    }
}