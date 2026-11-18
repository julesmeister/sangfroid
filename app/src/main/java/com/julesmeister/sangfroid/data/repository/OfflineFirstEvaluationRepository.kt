package com.julesmeister.sangfroid.data.repository

import com.julesmeister.sangfroid.data.local.dao.EvaluationDao
import com.julesmeister.sangfroid.data.local.entity.AnswerEvaluationEntity
import com.julesmeister.sangfroid.data.local.entity.FollowUpQuestionEntity
import com.julesmeister.sangfroid.data.local.entity.RubricScoreEntity
import com.julesmeister.sangfroid.data.local.model.StoredEvaluation
import com.julesmeister.sangfroid.domain.evaluation.AnswerEvaluationEngine
import com.julesmeister.sangfroid.domain.evaluation.EvaluationProcessingMode
import com.julesmeister.sangfroid.domain.evaluation.EvaluationRequest
import com.julesmeister.sangfroid.domain.evaluation.EvaluationResult
import com.julesmeister.sangfroid.domain.evaluation.EvaluationRubricScore
import com.julesmeister.sangfroid.domain.evaluation.FollowUpQuestion
import com.julesmeister.sangfroid.domain.model.EvaluationStatus
import com.julesmeister.sangfroid.domain.repository.EvaluationRepository
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class OfflineFirstEvaluationRepository @Inject constructor(
    private val evaluationDao: EvaluationDao,
    private val evaluationEngine: AnswerEvaluationEngine
) : EvaluationRepository {

    override fun observeAttempt(
        attemptId: String
    ) = evaluationDao.observeAttempt(
        attemptId
    )

    override fun observeQuestion(
        attemptId: String
    ) = evaluationDao.observeQuestionForAttempt(
        attemptId
    )

    override fun observeEvaluation(
        attemptId: String
    ): Flow<EvaluationResult?> {
        return evaluationDao
            .observeEvaluation(attemptId)
            .map { stored ->
                stored?.toDomain()
            }
    }

    override suspend fun evaluateAttempt(
        attemptId: String
    ): Result<EvaluationResult> {
        val existing =
            evaluationDao.getEvaluation(
                attemptId
            )

        if (existing != null) {
            return Result.success(
                existing.toDomain()
            )
        }

        return performEvaluation(
            attemptId = attemptId
        )
    }

    override suspend fun retryEvaluation(
        attemptId: String
    ): Result<EvaluationResult> {
        val existing =
            evaluationDao.getEvaluation(
                attemptId
            )

        if (existing != null) {
            return Result.success(
                existing.toDomain()
            )
        }

        return performEvaluation(
            attemptId = attemptId
        )
    }

    private suspend fun performEvaluation(
        attemptId: String
    ): Result<EvaluationResult> {
        val attempt =
            evaluationDao.getAttempt(
                attemptId
            ) ?: return Result.failure(
                IllegalArgumentException(
                    "Practice attempt was not found."
                )
            )

        val question =
            evaluationDao
                .getQuestionForAttempt(
                    attemptId
                ) ?: return Result.failure(
                IllegalArgumentException(
                    "Interview question was not found."
                )
            )

        evaluationDao.updateEvaluationStatus(
            attemptId = attemptId,
            status =
                EvaluationStatus.PROCESSING
        )

        return try {
            evaluationEngine
                .prepare()
                .getOrThrow()

            val result =
                evaluationEngine
                    .evaluate(
                        EvaluationRequest(
                            attemptId =
                                attempt.id,
                            questionId =
                                question.id,
                            question =
                                question.question,
                            answer =
                                attempt.answerText,
                            category =
                                question.category,
                            difficulty =
                                question.difficulty,
                            answerFramework =
                                question.answerFramework,
                            whyAsked =
                                question.whyAsked,
                            inputMethod =
                                attempt.inputMethod
                        )
                    )
                    .getOrThrow()

            require(
                result.attemptId ==
                        attempt.id
            ) {
                "The evaluation response does not match the submitted attempt."
            }

            saveEvaluation(result)

            Result.success(result)
        } catch (
            cancellation:
            CancellationException
        ) {
            evaluationDao
                .updateEvaluationStatus(
                    attemptId = attemptId,
                    status =
                        EvaluationStatus
                            .NOT_REQUESTED
                )

            throw cancellation
        } catch (throwable: Throwable) {
            val status =
                if (
                    throwable
                        .isLikelyConnectivityFailure()
                ) {
                    EvaluationStatus.QUEUED
                } else {
                    EvaluationStatus.FAILED
                }

            evaluationDao
                .updateEvaluationStatus(
                    attemptId = attemptId,
                    status = status
                )

            Result.failure(throwable)
        }
    }

    private suspend fun saveEvaluation(
        result: EvaluationResult
    ) {
        val evaluation =
            AnswerEvaluationEntity(
                id =
                    result.id,
                attemptId =
                    result.attemptId,
                summary =
                    result.summary,
                strengths =
                    result.strengths
                        .joinToStorageString(),
                missingPoints =
                    result.missingPoints
                        .joinToStorageString(),
                suggestions =
                    result.suggestions
                        .joinToStorageString(),
                improvedOutline =
                    result.improvedOutline
                        .joinToStorageString(),
                improvedAnswerExample =
                    result.improvedAnswerExample,
                modelName =
                    result.modelName,
                modelVersion =
                    result.modelVersion,
                processingMode =
                    result.processingMode.name,
                createdAt =
                    result.createdAt
            )

        val rubricScores =
            result.rubricScores.map { score ->
                RubricScoreEntity(
                    id =
                        UUID.randomUUID()
                            .toString(),
                    evaluationId =
                        result.id,
                    criterion =
                        score.criterion,
                    score =
                        score.score,
                    maximumScore =
                        score.maximumScore,
                    explanation =
                        score.explanation
                )
            }

        val followUpQuestions =
            result.followUpQuestions
                .mapIndexed { index, followUp ->
                    FollowUpQuestionEntity(
                        id =
                            UUID.randomUUID()
                                .toString(),
                        evaluationId =
                            result.id,
                        question =
                            followUp.question,
                        reason =
                            followUp.reason,
                        displayOrder =
                            index
                    )
                }

        evaluationDao.saveEvaluation(
            evaluation = evaluation,
            rubricScores = rubricScores,
            followUpQuestions =
                followUpQuestions
        )
    }

    private fun StoredEvaluation.toDomain():
            EvaluationResult {
        return EvaluationResult(
            id =
                evaluation.id,
            attemptId =
                evaluation.attemptId,
            summary =
                evaluation.summary,
            strengths =
                evaluation.strengths
                    .splitStorageString(),
            missingPoints =
                evaluation.missingPoints
                    .splitStorageString(),
            suggestions =
                evaluation.suggestions
                    .splitStorageString(),
            improvedOutline =
                evaluation.improvedOutline
                    .splitStorageString(),
            improvedAnswerExample =
                evaluation
                    .improvedAnswerExample,
            followUpQuestions =
                followUpQuestions
                    .sortedBy {
                        it.displayOrder
                    }
                    .map {
                        FollowUpQuestion(
                            question =
                                it.question,
                            reason =
                                it.reason
                        )
                    },
            rubricScores =
                rubricScores.map {
                    EvaluationRubricScore(
                        criterion =
                            it.criterion,
                        score =
                            it.score,
                        maximumScore =
                            it.maximumScore,
                        explanation =
                            it.explanation
                    )
                },
            modelName =
                evaluation.modelName,
            modelVersion =
                evaluation.modelVersion
                    ?: "unknown",
            processingMode =
                runCatching {
                    EvaluationProcessingMode
                        .valueOf(
                            evaluation
                                .processingMode
                        )
                }.getOrDefault(
                    EvaluationProcessingMode
                        .CLOUD_QWEN
                ),
            createdAt =
                evaluation.createdAt
        )
    }

    private fun List<String>.joinToStorageString():
            String {
        return asSequence()
            .map {
                it.trim()
            }
            .filter {
                it.isNotBlank()
            }
            .joinToString(
                separator =
                    STORAGE_SEPARATOR
            )
    }

    private fun String.splitStorageString():
            List<String> {
        if (isBlank()) {
            return emptyList()
        }

        return split(
            STORAGE_SEPARATOR
        )
            .map {
                it.trim()
            }
            .filter {
                it.isNotBlank()
            }
    }

    private fun Throwable
            .isLikelyConnectivityFailure():
            Boolean {
        var current: Throwable? =
            this

        while (current != null) {
            when (current) {
                is ConnectException,
                is UnknownHostException,
                is SocketTimeoutException,
                is NoRouteToHostException -> {
                    return true
                }
            }

            current =
                current.cause
        }

        return false
    }

    companion object {
        private const val STORAGE_SEPARATOR =
            "\u001E"
    }
}