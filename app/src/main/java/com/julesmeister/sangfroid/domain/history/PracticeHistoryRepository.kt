package com.julesmeister.sangfroid.domain.history

import kotlinx.coroutines.flow.Flow

interface PracticeHistoryRepository {

    suspend fun savePracticeAttempt(
        attempt: PracticeAttempt
    )

    suspend fun saveRevision(
        attemptId: String,
        questionId: String,
        content: String,
        source: RevisionSource
    ): SavedRevision

    fun observeQuestionHistory(
        questionId: String
    ): Flow<List<PracticeAttempt>>

    fun observeAttempt(
        attemptId: String
    ): Flow<PracticeAttempt?>

    fun observeRevisionsForQuestion(
        questionId: String
    ): Flow<List<SavedRevision>>

    fun observeRevisionsForAttempt(
        attemptId: String
    ): Flow<List<SavedRevision>>

    suspend fun getAttemptCountForQuestion(
        questionId: String
    ): Int

    suspend fun deletePracticeAttempt(
        attemptId: String
    )

    suspend fun deleteSavedRevision(
        revisionId: String
    )
}