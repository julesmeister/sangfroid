package com.julesmeister.sangfroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.julesmeister.sangfroid.data.local.entity.AnswerDraftEntity
import com.julesmeister.sangfroid.data.local.entity.PracticeAttemptEntity
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PracticeDao {

    @Query(
        """
        SELECT *
        FROM questions
        WHERE id = :questionId
        LIMIT 1
        """
    )
    fun observeQuestion(
        questionId: String
    ): Flow<QuestionEntity?>

    @Query(
        """
        SELECT *
        FROM answer_drafts
        WHERE questionId = :questionId
        LIMIT 1
        """
    )
    fun observeDraft(
        questionId: String
    ): Flow<AnswerDraftEntity?>

    @Upsert
    suspend fun upsertDraft(
        draft: AnswerDraftEntity
    )

    @Query(
        """
        DELETE FROM answer_drafts
        WHERE questionId = :questionId
        """
    )
    suspend fun deleteDraft(
        questionId: String
    )

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAttempt(
        attempt: PracticeAttemptEntity
    )

    @Query(
        """
        SELECT *
        FROM practice_attempts
        WHERE questionId = :questionId
        ORDER BY submittedAt DESC
        """
    )
    fun observeAttempts(
        questionId: String
    ): Flow<List<PracticeAttemptEntity>>

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
        DELETE FROM practice_attempts
        WHERE id = :attemptId
        """
    )
    suspend fun deleteAttemptById(
        attemptId: String
    )

    @Transaction
    suspend fun submitAttempt(
        attempt: PracticeAttemptEntity
    ) {
        insertAttempt(attempt)
        deleteDraft(attempt.questionId)
    }
}