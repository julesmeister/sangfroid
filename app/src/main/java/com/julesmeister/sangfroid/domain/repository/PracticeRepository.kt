package com.julesmeister.sangfroid.domain.repository

import com.julesmeister.sangfroid.data.local.entity.AnswerDraftEntity
import com.julesmeister.sangfroid.data.local.entity.PracticeAttemptEntity
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

interface PracticeRepository {

    fun observeQuestion(
        questionId: String
    ): Flow<QuestionEntity?>

    fun observeDraft(
        questionId: String
    ): Flow<AnswerDraftEntity?>

    fun observeAttempts(
        questionId: String
    ): Flow<List<PracticeAttemptEntity>>

    suspend fun saveDraft(
        draft: AnswerDraftEntity
    )

    suspend fun deleteDraft(
        questionId: String
    )

    suspend fun submitAttempt(
        attempt: PracticeAttemptEntity
    )
}