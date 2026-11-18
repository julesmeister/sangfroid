// app/src/main/java/com/julesmeister/sangfroid/data/local/dao/EvaluationDao.kt

package com.julesmeister.sangfroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.julesmeister.sangfroid.data.local.entity.AnswerEvaluationEntity
import com.julesmeister.sangfroid.data.local.entity.FollowUpQuestionEntity
import com.julesmeister.sangfroid.data.local.entity.PracticeAttemptEntity
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.data.local.entity.RubricScoreEntity
import com.julesmeister.sangfroid.data.local.model.StoredEvaluation
import com.julesmeister.sangfroid.domain.model.EvaluationStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface EvaluationDao {

    @Query(
        """
        SELECT *
        FROM practice_attempts
        WHERE id = :attemptId
        LIMIT 1
        """
    )
    fun observeAttempt(
        attemptId: String
    ): Flow<PracticeAttemptEntity?>

    @Query(
        """
        SELECT *
        FROM practice_attempts
        WHERE id = :attemptId
        LIMIT 1
        """
    )
    suspend fun getAttempt(
        attemptId: String
    ): PracticeAttemptEntity?

    @Query(
        """
        SELECT questions.*
        FROM questions
        INNER JOIN practice_attempts
            ON practice_attempts.questionId = questions.id
        WHERE practice_attempts.id = :attemptId
        LIMIT 1
        """
    )
    fun observeQuestionForAttempt(
        attemptId: String
    ): Flow<QuestionEntity?>

    @Query(
        """
        SELECT questions.*
        FROM questions
        INNER JOIN practice_attempts
            ON practice_attempts.questionId = questions.id
        WHERE practice_attempts.id = :attemptId
        LIMIT 1
        """
    )
    suspend fun getQuestionForAttempt(
        attemptId: String
    ): QuestionEntity?

    @Transaction
    @Query(
        """
        SELECT *
        FROM answer_evaluations
        WHERE attemptId = :attemptId
        LIMIT 1
        """
    )
    fun observeEvaluation(
        attemptId: String
    ): Flow<StoredEvaluation?>

    @Transaction
    @Query(
        """
        SELECT *
        FROM answer_evaluations
        WHERE attemptId = :attemptId
        LIMIT 1
        """
    )
    suspend fun getEvaluation(
        attemptId: String
    ): StoredEvaluation?

    @Query(
        """
        SELECT answer_evaluations.*
        FROM answer_evaluations
        INNER JOIN practice_attempts
            ON practice_attempts.id = answer_evaluations.attemptId
        WHERE practice_attempts.questionId = :questionId
        ORDER BY answer_evaluations.createdAt DESC
        """
    )
    fun observeEvaluationEntitiesForQuestion(
        questionId: String
    ): Flow<List<AnswerEvaluationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluation(
        evaluation: AnswerEvaluationEntity
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRubricScores(
        scores: List<RubricScoreEntity>
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUpQuestions(
        questions: List<FollowUpQuestionEntity>
    )

    @Query(
        """
        DELETE FROM rubric_scores
        WHERE evaluationId = :evaluationId
        """
    )
    suspend fun deleteRubricScores(
        evaluationId: String
    )

    @Query(
        """
        DELETE FROM follow_up_questions
        WHERE evaluationId = :evaluationId
        """
    )
    suspend fun deleteFollowUpQuestions(
        evaluationId: String
    )

    @Query(
        """
        UPDATE practice_attempts
        SET evaluationStatus = :status
        WHERE id = :attemptId
        """
    )
    suspend fun updateEvaluationStatus(
        attemptId: String,
        status: EvaluationStatus
    )

    @Transaction
    suspend fun saveEvaluation(
        evaluation: AnswerEvaluationEntity,
        rubricScores: List<RubricScoreEntity>,
        followUpQuestions: List<FollowUpQuestionEntity>
    ) {
        deleteRubricScores(
            evaluationId = evaluation.id
        )

        deleteFollowUpQuestions(
            evaluationId = evaluation.id
        )

        insertEvaluation(
            evaluation
        )

        if (rubricScores.isNotEmpty()) {
            insertRubricScores(
                rubricScores
            )
        }

        if (followUpQuestions.isNotEmpty()) {
            insertFollowUpQuestions(
                followUpQuestions
            )
        }

        updateEvaluationStatus(
            attemptId = evaluation.attemptId,
            status = EvaluationStatus.COMPLETED
        )
    }
}