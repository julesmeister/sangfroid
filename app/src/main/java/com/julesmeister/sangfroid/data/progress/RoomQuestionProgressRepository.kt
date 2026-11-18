package com.julesmeister.sangfroid.data.progress

import com.julesmeister.sangfroid.data.local.dao.PracticeDao
import com.julesmeister.sangfroid.data.local.dao.SavedRevisionDao
import com.julesmeister.sangfroid.data.local.entity.PracticeAttemptEntity
import com.julesmeister.sangfroid.domain.evaluation.EvaluationResult
import com.julesmeister.sangfroid.domain.evaluation.EvaluationRubricScore
import com.julesmeister.sangfroid.domain.progress.QuestionMasteryLevel
import com.julesmeister.sangfroid.domain.progress.QuestionProgress
import com.julesmeister.sangfroid.domain.progress.QuestionProgressRepository
import com.julesmeister.sangfroid.domain.progress.RubricProgress
import com.julesmeister.sangfroid.domain.repository.EvaluationRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@Singleton
class RoomQuestionProgressRepository @Inject constructor(
    private val practiceDao: PracticeDao,
    private val savedRevisionDao:
    SavedRevisionDao,
    private val evaluationRepository:
    EvaluationRepository
) : QuestionProgressRepository {

    override fun observeQuestionProgress(
        questionId: String
    ): Flow<QuestionProgress> {
        val attemptsFlow =
            practiceDao.observeAttempts(
                questionId = questionId
            )

        val evaluationsFlow =
            attemptsFlow.flatMapLatest {
                    attempts ->

                observeEvaluations(
                    attempts = attempts
                )
            }

        val revisionCountFlow =
            savedRevisionDao
                .observeRevisionsForQuestion(
                    questionId = questionId
                )
                .map { revisions ->
                    revisions.size
                }

        return combine(
            attemptsFlow,
            evaluationsFlow,
            revisionCountFlow
        ) {
                attempts,
                evaluations,
                revisionCount ->

            calculateQuestionProgress(
                questionId = questionId,
                attempts = attempts,
                evaluations = evaluations,
                savedRevisionCount =
                    revisionCount
            )
        }
    }

    override suspend fun getQuestionProgress(
        questionId: String
    ): QuestionProgress {
        return observeQuestionProgress(
            questionId = questionId
        ).first()
    }

    private fun observeEvaluations(
        attempts: List<PracticeAttemptEntity>
    ): Flow<List<EvaluatedAttempt>> {
        if (attempts.isEmpty()) {
            return flowOf(
                emptyList()
            )
        }

        val evaluationFlows =
            attempts.map { attempt ->
                evaluationRepository
                    .observeEvaluation(
                        attemptId = attempt.id
                    )
                    .map { evaluation ->
                        EvaluatedAttempt(
                            attempt = attempt,
                            evaluation = evaluation
                        )
                    }
            }

        return combine(
            evaluationFlows
        ) { evaluatedAttempts ->
            evaluatedAttempts.toList()
        }
    }

    private fun calculateQuestionProgress(
        questionId: String,
        attempts: List<PracticeAttemptEntity>,
        evaluations: List<EvaluatedAttempt>,
        savedRevisionCount: Int
    ): QuestionProgress {
        val completedEvaluations =
            evaluations
                .mapNotNull { evaluatedAttempt ->
                    val evaluation =
                        evaluatedAttempt.evaluation
                            ?: return@mapNotNull null

                    EvaluationSnapshot(
                        attempt =
                            evaluatedAttempt.attempt,
                        evaluation =
                            evaluation,
                        normalizedScore =
                            calculateNormalizedScore(
                                rubricScores =
                                    evaluation
                                        .rubricScores
                            )
                    )
                }
                .filter { snapshot ->
                    snapshot.normalizedScore != null
                }
                .sortedWith(
                    compareByDescending<EvaluationSnapshot> {
                        it.evaluation.createdAt
                    }
                        .thenByDescending {
                            it.attempt.submittedAt
                        }
                )

        val normalizedScores =
            completedEvaluations
                .mapNotNull { snapshot ->
                    snapshot.normalizedScore
                }

        val latestNormalizedScore =
            normalizedScores.getOrNull(0)

        val previousNormalizedScore =
            normalizedScores.getOrNull(1)

        val averageNormalizedScore =
            normalizedScores
                .takeIf(List<Float>::isNotEmpty)
                ?.average()
                ?.toFloat()

        val scoreChange =
            if (
                latestNormalizedScore != null &&
                previousNormalizedScore != null
            ) {
                latestNormalizedScore -
                        previousNormalizedScore
            } else {
                null
            }

        val rubricProgress =
            calculateRubricProgress(
                snapshots =
                    completedEvaluations
            )

        val strongestCriteria =
            rubricProgress
                .sortedWith(
                    compareByDescending<RubricProgress> {
                        it.latestNormalizedScore
                    }
                        .thenByDescending {
                            it.averageNormalizedScore
                        }
                        .thenBy {
                            it.criterion
                        }
                )
                .take(
                    CriteriaSummaryLimit
                )

        val weakestCriteria =
            rubricProgress
                .sortedWith(
                    compareBy<RubricProgress> {
                        it.latestNormalizedScore
                    }
                        .thenBy {
                            it.averageNormalizedScore
                        }
                        .thenBy {
                            it.criterion
                        }
                )
                .take(
                    CriteriaSummaryLimit
                )

        val masteryLevel =
            QuestionMasteryLevel.calculate(
                normalizedScore =
                    averageNormalizedScore,
                completedEvaluationCount =
                    completedEvaluations.size
            )

        return QuestionProgress(
            questionId = questionId,
            attemptCount = attempts.size,
            completedEvaluationCount =
                completedEvaluations.size,
            savedRevisionCount =
                savedRevisionCount,
            averageNormalizedScore =
                averageNormalizedScore,
            latestNormalizedScore =
                latestNormalizedScore,
            previousNormalizedScore =
                previousNormalizedScore,
            scoreChange =
                scoreChange,
            masteryLevel =
                masteryLevel,
            rubricProgress =
                rubricProgress,
            strongestCriteria =
                strongestCriteria,
            weakestCriteria =
                weakestCriteria,
            latestEvaluatedAt =
                completedEvaluations
                    .firstOrNull()
                    ?.evaluation
                    ?.createdAt,
            lastPracticedAt =
                attempts
                    .maxOfOrNull {
                        it.submittedAt
                    }
        )
    }

    private fun calculateRubricProgress(
        snapshots: List<EvaluationSnapshot>
    ): List<RubricProgress> {
        val criterionScores =
            linkedMapOf<
                    String,
                    MutableList<CriterionScore>
                    >()

        snapshots.forEach {
                snapshot ->

            snapshot
                .evaluation
                .rubricScores
                .forEach { rubricScore ->
                    val criterion =
                        rubricScore
                            .criterion
                            .trim()

                    if (criterion.isBlank()) {
                        return@forEach
                    }

                    val normalizedScore =
                        normalizeScore(
                            score = rubricScore
                        ) ?: return@forEach

                    criterionScores
                        .getOrPut(
                            criterion
                        ) {
                            mutableListOf()
                        }
                        .add(
                            CriterionScore(
                                evaluationCreatedAt =
                                    snapshot
                                        .evaluation
                                        .createdAt,
                                attemptSubmittedAt =
                                    snapshot
                                        .attempt
                                        .submittedAt,
                                normalizedScore =
                                    normalizedScore
                            )
                        )
                }
        }

        return criterionScores
            .mapNotNull {
                    (criterion, scores) ->

                val orderedScores =
                    scores.sortedWith(
                        compareByDescending<CriterionScore> {
                            it.evaluationCreatedAt
                        }
                            .thenByDescending {
                                it.attemptSubmittedAt
                            }
                    )

                val latest =
                    orderedScores
                        .getOrNull(0)
                        ?.normalizedScore
                        ?: return@mapNotNull null

                val previous =
                    orderedScores
                        .getOrNull(1)
                        ?.normalizedScore

                val average =
                    orderedScores
                        .map {
                            it.normalizedScore
                        }
                        .average()
                        .toFloat()

                RubricProgress(
                    criterion = criterion,
                    attemptCount =
                        orderedScores.size,
                    averageNormalizedScore =
                        average,
                    latestNormalizedScore =
                        latest,
                    previousNormalizedScore =
                        previous,
                    change =
                        if (previous != null) {
                            latest - previous
                        } else {
                            null
                        }
                )
            }
            .sortedBy {
                it.criterion.lowercase()
            }
    }

    private fun calculateNormalizedScore(
        rubricScores:
        List<EvaluationRubricScore>
    ): Float? {
        if (rubricScores.isEmpty()) {
            return null
        }

        val validScores =
            rubricScores.filter { score ->
                score.maximumScore > 0
            }

        if (validScores.isEmpty()) {
            return null
        }

        val earnedPoints =
            validScores.sumOf { score ->
                score.score
                    .coerceIn(
                        minimumValue = 0,
                        maximumValue =
                            score.maximumScore
                    )
            }

        val maximumPoints =
            validScores.sumOf { score ->
                score.maximumScore
            }

        if (maximumPoints <= 0) {
            return null
        }

        return (
                earnedPoints.toFloat() /
                        maximumPoints.toFloat()
                )
            .coerceIn(
                minimumValue = 0f,
                maximumValue = 1f
            )
    }

    private fun normalizeScore(
        score: EvaluationRubricScore
    ): Float? {
        if (score.maximumScore <= 0) {
            return null
        }

        return (
                score.score
                    .coerceIn(
                        minimumValue = 0,
                        maximumValue =
                            score.maximumScore
                    )
                    .toFloat() /
                        score.maximumScore.toFloat()
                )
            .coerceIn(
                minimumValue = 0f,
                maximumValue = 1f
            )
    }

    private data class EvaluatedAttempt(
        val attempt:
        PracticeAttemptEntity,
        val evaluation:
        EvaluationResult?
    )

    private data class EvaluationSnapshot(
        val attempt:
        PracticeAttemptEntity,
        val evaluation:
        EvaluationResult,
        val normalizedScore:
        Float?
    )

    private data class CriterionScore(
        val evaluationCreatedAt:
        Long,
        val attemptSubmittedAt:
        Long,
        val normalizedScore:
        Float
    )

    companion object {
        private const val CriteriaSummaryLimit =
            3
    }
}