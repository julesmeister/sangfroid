package com.julesmeister.sangfroid.data.repository

import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.data.remote.api.SangfroidApi
import com.julesmeister.sangfroid.data.remote.dto.CvEvidenceDto
import com.julesmeister.sangfroid.data.remote.dto.IdealAnswerRequestDto
import com.julesmeister.sangfroid.domain.repository.IdealAnswerRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class RemoteIdealAnswerRepository
@Inject constructor(
    private val sangfroidApi:
    SangfroidApi
) : IdealAnswerRepository {

    override suspend fun generate(
        question: QuestionEntity,
        revisionNumber: Int,
        previousIdealAnswer: String?,
        targetRole: String?,
        experienceLevel: String?,
        languageTag: String,
        cvEvidence: List<CvEvidenceDto>
    ): Result<IdealAnswerRepository.IdealAnswer> {
        return try {
            require(
                revisionNumber >= 1
            ) {
                "revisionNumber must be at least one."
            }

            val requestId =
                UUID.randomUUID()
                    .toString()

            val response =
                sangfroidApi
                    .generateIdealAnswer(
                        IdealAnswerRequestDto(
                            requestId =
                                requestId,
                            questionId =
                                question.id,
                            question =
                                question.question,
                            category =
                                question.category.name,
                            difficulty =
                                question.difficulty.name,
                            questionAngle =
                                question.questionAngle.name,
                            whyAsked =
                                question.whyAsked,
                            answerFramework =
                                question.answerFramework,
                            targetRole =
                                targetRole,
                            experienceLevel =
                                experienceLevel,
                            languageTag =
                                languageTag,
                            revisionNumber =
                                revisionNumber,
                            previousIdealAnswer =
                                previousIdealAnswer,
                            cvEvidence =
                                cvEvidence
                        )
                    )
                    .getOrThrow()

            require(
                response.requestId ==
                        requestId
            ) {
                "The ideal-answer response does not match the request."
            }

            require(
                response.questionId ==
                        question.id
            ) {
                "The ideal-answer response does not match the question."
            }

            Result.success(
                IdealAnswerRepository.IdealAnswer(
                    requestId =
                        response.requestId,
                    questionId =
                        response.questionId,
                    content =
                        response.idealAnswer,
                    revisionNumber =
                        response.revisionNumber,
                    usedEvidenceIds =
                        response.usedEvidenceIds,
                    modelName =
                        response.modelName,
                    modelVersion =
                        response.modelVersion,
                    generatedAt =
                        response.generatedAt
                )
            )
        } catch (
            cancellation:
            CancellationException
        ) {
            throw cancellation
        } catch (
            throwable:
            Throwable
        ) {
            Result.failure(
                throwable
            )
        }
    }
}