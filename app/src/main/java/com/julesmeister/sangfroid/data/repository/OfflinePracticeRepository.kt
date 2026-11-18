package com.julesmeister.sangfroid.data.repository

import com.julesmeister.sangfroid.data.local.dao.PracticeDao
import com.julesmeister.sangfroid.data.local.entity.AnswerDraftEntity
import com.julesmeister.sangfroid.data.local.entity.PracticeAttemptEntity
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.domain.repository.PracticeRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class OfflinePracticeRepository @Inject constructor(
    private val practiceDao: PracticeDao
) : PracticeRepository {

    override fun observeQuestion(
        questionId: String
    ): Flow<QuestionEntity?> {
        return practiceDao.observeQuestion(
            questionId
        )
    }

    override fun observeDraft(
        questionId: String
    ): Flow<AnswerDraftEntity?> {
        return practiceDao.observeDraft(
            questionId
        )
    }

    override fun observeAttempts(
        questionId: String
    ): Flow<List<PracticeAttemptEntity>> {
        return practiceDao.observeAttempts(
            questionId
        )
    }

    override suspend fun saveDraft(
        draft: AnswerDraftEntity
    ) {
        practiceDao.upsertDraft(
            draft
        )
    }

    override suspend fun deleteDraft(
        questionId: String
    ) {
        practiceDao.deleteDraft(
            questionId
        )
    }

    override suspend fun submitAttempt(
        attempt: PracticeAttemptEntity
    ) {
        practiceDao.submitAttempt(
            attempt
        )
    }
}