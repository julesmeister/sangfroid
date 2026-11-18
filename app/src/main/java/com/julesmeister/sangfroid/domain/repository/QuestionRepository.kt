package com.julesmeister.sangfroid.domain.repository

import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.domain.model.QuestionCategory
import kotlinx.coroutines.flow.Flow

interface QuestionRepository {

    fun observeQuestions(): Flow<List<QuestionEntity>>

    fun observeQuestionsByCategory(
        category: QuestionCategory
    ): Flow<List<QuestionEntity>>

    fun observeQuestion(
        questionId: String
    ): Flow<QuestionEntity?>

    fun observeSavedQuestions(): Flow<List<QuestionEntity>>

    fun observeIsQuestionSaved(
        questionId: String
    ): Flow<Boolean>

    fun observeSkippedCount(): Flow<Int>

    suspend fun skipQuestion(
        questionId: String
    )

    suspend fun saveQuestion(
        questionId: String
    )

    suspend fun unsaveQuestion(
        questionId: String
    )

    /**
     * Returns skipped questions to the feed.
     */
    suspend fun restoreSkippedQuestions(): Int

    /**
     * Returns every swiped question to the feed.
     * Saved bookmarks are kept.
     */
    suspend fun restartFeed(): Int

    suspend fun seedQuestionsIfRequired()
}