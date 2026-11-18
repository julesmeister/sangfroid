package com.julesmeister.sangfroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.julesmeister.sangfroid.data.local.entity.SavedQuestionEntity
import com.julesmeister.sangfroid.data.local.entity.SwipeDecisionEntity
import com.julesmeister.sangfroid.data.local.model.CategoryInteractionRow
import com.julesmeister.sangfroid.domain.model.SwipeDirection
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionInteractionDao {

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun insertSwipeDecision(
        decision: SwipeDecisionEntity
    )

    @Insert(
        onConflict =
            OnConflictStrategy.REPLACE
    )
    suspend fun insertSavedQuestion(
        savedQuestion: SavedQuestionEntity
    )

    @Query(
        """
        DELETE FROM saved_questions
        WHERE questionId = :questionId
        """
    )
    suspend fun deleteSavedQuestion(
        questionId: String
    )

    @Query(
        """
        DELETE FROM swipe_decisions
        WHERE direction = :direction
        """
    )
    suspend fun deleteSwipeDecisions(
        direction: SwipeDirection
    ): Int

    @Query(
        """
        DELETE FROM swipe_decisions
        """
    )
    suspend fun deleteAllSwipeDecisions():
            Int

    @Query(
        """
        SELECT COUNT(DISTINCT questionId)
        FROM swipe_decisions
        WHERE direction = :direction
        """
    )
    fun observeSwipeCount(
        direction: SwipeDirection
    ): Flow<Int>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1
            FROM saved_questions
            WHERE questionId = :questionId
        )
        """
    )
    fun observeIsQuestionSaved(
        questionId: String
    ): Flow<Boolean>

    @Query(
        """
        SELECT questionId
        FROM saved_questions
        ORDER BY savedAt DESC
        """
    )
    fun observeSavedQuestionIds():
            Flow<List<String>>

    @Query(
        """
        SELECT
            questions.category AS category,
            COUNT(DISTINCT swipe_decisions.questionId)
                AS interactionCount
        FROM swipe_decisions
        INNER JOIN questions
            ON questions.id =
               swipe_decisions.questionId
        WHERE swipe_decisions.direction =
              :direction
        GROUP BY questions.category
        ORDER BY interactionCount DESC,
                 questions.category ASC
        """
    )
    suspend fun getCategoryInteractions(
        direction: SwipeDirection
    ): List<CategoryInteractionRow>

    @Query(
        """
        SELECT
            questions.category AS category,
            COUNT(DISTINCT swipe_decisions.questionId)
                AS interactionCount
        FROM swipe_decisions
        INNER JOIN questions
            ON questions.id =
               swipe_decisions.questionId
        WHERE swipe_decisions.direction =
              :direction
        GROUP BY questions.category
        HAVING COUNT(
            DISTINCT swipe_decisions.questionId
        ) >= :minimumCount
        ORDER BY interactionCount DESC,
                 questions.category ASC
        LIMIT :limit
        """
    )
    suspend fun getMostInteractedCategories(
        direction: SwipeDirection,
        minimumCount: Int,
        limit: Int
    ): List<CategoryInteractionRow>

    @Transaction
    suspend fun saveAndRecordSwipe(
        decision: SwipeDecisionEntity,
        savedQuestion: SavedQuestionEntity
    ) {
        insertSavedQuestion(
            savedQuestion
        )

        insertSwipeDecision(
            decision
        )
    }
}