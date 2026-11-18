package com.julesmeister.sangfroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.julesmeister.sangfroid.data.local.entity.AnswerEvaluationEntity
import com.julesmeister.sangfroid.data.local.entity.PracticeAttemptEntity
import com.julesmeister.sangfroid.domain.model.EvaluationStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeAttemptDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAttempt(
        attempt: PracticeAttemptEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluation(
        evaluation: AnswerEvaluationEntity
    )

    @Transaction
    suspend fun insertAttemptWithEvaluation(
        attempt: PracticeAttemptEntity,
        evaluation: AnswerEvaluationEntity?
    ) {
        insertAttempt(attempt)

        if (evaluation != null) {
            insertEvaluation(evaluation)
        }
    }

    @Query(
        """
        SELECT *
        FROM practice_attempts
        WHERE id = :attemptId
        LIMIT 1
        """
    )
    suspend fun getAttemptById(
        attemptId: String
    ): PracticeAttemptEntity?

    @Query(
        """
        SELECT *
        FROM practice_attempts
        WHERE id = :attemptId
        LIMIT 1
        """
    )
    fun observeAttemptById(
        attemptId: String
    ): Flow<PracticeAttemptEntity?>

    @Query(
        """
        SELECT *
        FROM answer_evaluations
        WHERE attemptId = :attemptId
        LIMIT 1
        """
    )
    suspend fun getEvaluationByAttemptId(
        attemptId: String
    ): AnswerEvaluationEntity?

    @Query(
        """
        SELECT *
        FROM answer_evaluations
        WHERE attemptId = :attemptId
        LIMIT 1
        """
    )
    fun observeEvaluationByAttemptId(
        attemptId: String
    ): Flow<AnswerEvaluationEntity?>

    @Query(
        """
        SELECT *
        FROM practice_attempts
        WHERE questionId = :questionId
        ORDER BY submittedAt DESC
        """
    )
    fun observeAttemptsForQuestion(
        questionId: String
    ): Flow<List<PracticeAttemptEntity>>

    @Query(
        """
        SELECT *
        FROM answer_evaluations
        WHERE attemptId IN (
            SELECT id
            FROM practice_attempts
            WHERE questionId = :questionId
        )
        ORDER BY createdAt DESC
        """
    )
    fun observeEvaluationsForQuestion(
        questionId: String
    ): Flow<List<AnswerEvaluationEntity>>

    @Query(
        """
        SELECT *
        FROM practice_attempts
        ORDER BY submittedAt DESC
        """
    )
    fun observeAllAttempts(): Flow<List<PracticeAttemptEntity>>

    @Query(
        """
        SELECT COUNT(*)
        FROM practice_attempts
        WHERE questionId = :questionId
        """
    )
    suspend fun getAttemptCountForQuestion(
        questionId: String
    ): Int

    @Query(
        """
        UPDATE practice_attempts
        SET evaluationStatus = :status,
            modelName = :modelName,
            modelVersion = :modelVersion
        WHERE id = :attemptId
        """
    )
    suspend fun updateEvaluationState(
        attemptId: String,
        status: EvaluationStatus,
        modelName: String?,
        modelVersion: String?
    )

    @Transaction
    suspend fun saveEvaluationAndUpdateAttempt(
        evaluation: AnswerEvaluationEntity,
        status: EvaluationStatus,
        modelName: String?,
        modelVersion: String?
    ) {
        insertEvaluation(evaluation)

        updateEvaluationState(
            attemptId = evaluation.attemptId,
            status = status,
            modelName = modelName,
            modelVersion = modelVersion
        )
    }

    @Query(
        """
        DELETE FROM practice_attempts
        WHERE id = :attemptId
        """
    )
    suspend fun deleteAttemptById(
        attemptId: String
    )

    @Query(
        """
        DELETE FROM practice_attempts
        WHERE questionId = :questionId
        """
    )
    suspend fun deleteAttemptsForQuestion(
        questionId: String
    )

    @Query("DELETE FROM practice_attempts")
    suspend fun deleteAllAttempts()
}