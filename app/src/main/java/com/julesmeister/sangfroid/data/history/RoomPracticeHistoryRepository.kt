// app/src/main/java/com/julesmeister/sangfroid/data/history/RoomPracticeHistoryRepository.kt

package com.julesmeister.sangfroid.data.history

import com.julesmeister.sangfroid.data.local.dao.EvaluationDao
import com.julesmeister.sangfroid.data.local.dao.PracticeDao
import com.julesmeister.sangfroid.data.local.dao.SavedRevisionDao
import com.julesmeister.sangfroid.data.local.entity.AnswerEvaluationEntity
import com.julesmeister.sangfroid.data.local.entity.PracticeAttemptEntity
import com.julesmeister.sangfroid.data.local.entity.SavedRevisionEntity
import com.julesmeister.sangfroid.domain.history.PracticeAnswerEvaluation
import com.julesmeister.sangfroid.domain.history.PracticeAttempt
import com.julesmeister.sangfroid.domain.history.PracticeHistoryRepository
import com.julesmeister.sangfroid.domain.history.RevisionSource
import com.julesmeister.sangfroid.domain.history.SavedRevision
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

@Singleton
class RoomPracticeHistoryRepository @Inject constructor(
    private val practiceDao: PracticeDao,
    private val evaluationDao: EvaluationDao,
    private val savedRevisionDao: SavedRevisionDao
) : PracticeHistoryRepository {

    override suspend fun savePracticeAttempt(
        attempt: PracticeAttempt
    ) {
        practiceDao.submitAttempt(
            attempt = mapAttemptToEntity(
                attempt
            )
        )

        attempt.evaluation?.let { evaluation ->
            evaluationDao.insertEvaluation(
                evaluation = mapEvaluationToEntity(
                    evaluation
                )
            )
        }
    }

    override suspend fun saveRevision(
        attemptId: String,
        questionId: String,
        content: String,
        source: RevisionSource
    ): SavedRevision {
        val existingAttempt =
            evaluationDao.getAttempt(
                attemptId = attemptId
            ) ?: throw IllegalArgumentException(
                "Practice attempt $attemptId does not exist."
            )

        require(
            existingAttempt.questionId ==
                    questionId
        ) {
            "Practice attempt $attemptId does not belong to question $questionId."
        }

        val normalizedContent =
            content.trim()

        require(
            normalizedContent.isNotBlank()
        ) {
            "Revision content cannot be blank."
        }

        val alreadyExists =
            savedRevisionDao
                .revisionContentExistsForQuestion(
                    questionId = questionId,
                    content = normalizedContent
                )

        require(!alreadyExists) {
            "This revision has already been saved for this question."
        }

        val entity =
            savedRevisionDao.insertNextRevision(
                id = UUID.randomUUID().toString(),
                attemptId = attemptId,
                questionId = questionId,
                content = normalizedContent,
                source = source.name,
                createdAt = System.currentTimeMillis()
            )

        return mapRevisionToDomain(
            entity
        )
    }

    override fun observeQuestionHistory(
        questionId: String
    ): Flow<List<PracticeAttempt>> {
        return combine(
            practiceDao.observeAttempts(
                questionId = questionId
            ),
            evaluationDao
                .observeEvaluationEntitiesForQuestion(
                    questionId = questionId
                ),
            savedRevisionDao
                .observeRevisionsForQuestion(
                    questionId = questionId
                )
        ) {
                attempts,
                evaluations,
                revisions ->

            val evaluationsByAttemptId =
                evaluations.associateBy {
                    it.attemptId
                }

            val revisionsByAttemptId =
                revisions.groupBy {
                    it.attemptId
                }

            attempts.map { attempt ->
                mapAttemptToDomain(
                    entity = attempt,
                    evaluation =
                        evaluationsByAttemptId[
                            attempt.id
                        ]?.let {
                            mapEvaluationToDomain(
                                it
                            )
                        },
                    revisions =
                        revisionsByAttemptId[
                            attempt.id
                        ]
                            .orEmpty()
                            .map {
                                mapRevisionToDomain(
                                    it
                                )
                            }
                            .sortedWith(
                                compareByDescending<SavedRevision> {
                                    it.versionNumber
                                }.thenByDescending {
                                    it.createdAt
                                }
                            )
                )
            }
        }
    }

    override fun observeAttempt(
        attemptId: String
    ): Flow<PracticeAttempt?> {
        return combine(
            evaluationDao.observeAttempt(
                attemptId = attemptId
            ),
            evaluationDao.observeEvaluation(
                attemptId = attemptId
            ),
            savedRevisionDao.observeRevisionsForAttempt(
                attemptId = attemptId
            )
        ) {
                attempt,
                storedEvaluation,
                revisions ->

            attempt?.let { entity ->
                mapAttemptToDomain(
                    entity = entity,
                    evaluation =
                        storedEvaluation
                            ?.evaluation
                            ?.let {
                                mapEvaluationToDomain(
                                    it
                                )
                            },
                    revisions =
                        revisions
                            .map {
                                mapRevisionToDomain(
                                    it
                                )
                            }
                            .sortedWith(
                                compareByDescending<SavedRevision> {
                                    it.versionNumber
                                }.thenByDescending {
                                    it.createdAt
                                }
                            )
                )
            }
        }
    }

    override fun observeRevisionsForQuestion(
        questionId: String
    ): Flow<List<SavedRevision>> {
        return savedRevisionDao
            .observeRevisionsForQuestion(
                questionId = questionId
            )
            .map { entities ->
                entities.map {
                    mapRevisionToDomain(
                        it
                    )
                }
            }
    }

    override fun observeRevisionsForAttempt(
        attemptId: String
    ): Flow<List<SavedRevision>> {
        return savedRevisionDao
            .observeRevisionsForAttempt(
                attemptId = attemptId
            )
            .map { entities ->
                entities.map {
                    mapRevisionToDomain(
                        it
                    )
                }
            }
    }

    override suspend fun getAttemptCountForQuestion(
        questionId: String
    ): Int {
        return practiceDao
            .getAttemptCountForQuestion(
                questionId = questionId
            )
    }

    override suspend fun deletePracticeAttempt(
        attemptId: String
    ) {
        require(
            attemptId.isNotBlank()
        ) {
            "Practice attempt ID cannot be blank."
        }

        practiceDao.deleteAttemptById(
            attemptId = attemptId
        )
    }

    override suspend fun deleteSavedRevision(
        revisionId: String
    ) {
        require(
            revisionId.isNotBlank()
        ) {
            "Saved revision ID cannot be blank."
        }

        savedRevisionDao.deleteRevisionById(
            revisionId = revisionId
        )
    }

    private fun mapAttemptToEntity(
        attempt: PracticeAttempt
    ): PracticeAttemptEntity {
        return PracticeAttemptEntity(
            id = attempt.id,
            questionId = attempt.questionId,
            answerText = attempt.answerText,
            inputMethod = attempt.inputMethod,
            startedAt = attempt.startedAt,
            submittedAt = attempt.submittedAt,
            durationSeconds = attempt.durationSeconds,
            wordCount = attempt.wordCount,
            evaluationStatus = attempt.evaluationStatus,
            modelName = attempt.modelName,
            modelVersion = attempt.modelVersion
        )
    }

    private fun mapEvaluationToEntity(
        evaluation: PracticeAnswerEvaluation
    ): AnswerEvaluationEntity {
        return AnswerEvaluationEntity(
            id = evaluation.id,
            attemptId = evaluation.attemptId,
            summary = evaluation.summary,
            strengths = evaluation.strengths,
            missingPoints = evaluation.missingPoints,
            suggestions = evaluation.suggestions,
            improvedOutline = evaluation.improvedOutline,
            improvedAnswerExample =
                evaluation.improvedAnswerExample,
            modelName = evaluation.modelName,
            modelVersion = evaluation.modelVersion,
            processingMode = evaluation.processingMode,
            createdAt = evaluation.createdAt
        )
    }

    private fun mapAttemptToDomain(
        entity: PracticeAttemptEntity,
        evaluation: PracticeAnswerEvaluation?,
        revisions: List<SavedRevision>
    ): PracticeAttempt {
        return PracticeAttempt(
            id = entity.id,
            questionId = entity.questionId,
            answerText = entity.answerText,
            inputMethod = entity.inputMethod,
            startedAt = entity.startedAt,
            submittedAt = entity.submittedAt,
            durationSeconds = entity.durationSeconds,
            wordCount = entity.wordCount,
            evaluationStatus =
                entity.evaluationStatus,
            modelName = entity.modelName,
            modelVersion = entity.modelVersion,
            evaluation = evaluation,
            revisions = revisions
        )
    }

    private fun mapEvaluationToDomain(
        entity: AnswerEvaluationEntity
    ): PracticeAnswerEvaluation {
        return PracticeAnswerEvaluation(
            id = entity.id,
            attemptId = entity.attemptId,
            summary = entity.summary,
            strengths = entity.strengths,
            missingPoints = entity.missingPoints,
            suggestions = entity.suggestions,
            improvedOutline =
                entity.improvedOutline,
            improvedAnswerExample =
                entity.improvedAnswerExample,
            modelName = entity.modelName,
            modelVersion = entity.modelVersion,
            processingMode =
                entity.processingMode,
            createdAt = entity.createdAt
        )
    }

    private fun mapRevisionToDomain(
        entity: SavedRevisionEntity
    ): SavedRevision {
        return SavedRevision(
            id = entity.id,
            attemptId = entity.attemptId,
            questionId = entity.questionId,
            content = entity.content,
            versionNumber = entity.versionNumber,
            source =
                RevisionSource.fromStorageValue(
                    entity.source
                ),
            createdAt = entity.createdAt
        )
    }
}