package com.julesmeister.sangfroid.data.repository

import com.julesmeister.sangfroid.data.local.dao.QuestionDao
import com.julesmeister.sangfroid.data.local.dao.QuestionInteractionDao
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.data.local.entity.SavedQuestionEntity
import com.julesmeister.sangfroid.data.local.entity.SwipeDecisionEntity
import com.julesmeister.sangfroid.data.local.seed.QuestionSeedData
import com.julesmeister.sangfroid.domain.model.QuestionCategory
import com.julesmeister.sangfroid.domain.model.SwipeDirection
import com.julesmeister.sangfroid.domain.repository.QuestionRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class OfflineQuestionRepository @Inject constructor(
    private val questionDao:
    QuestionDao,
    private val questionInteractionDao:
    QuestionInteractionDao
) : QuestionRepository {

    override fun observeQuestions():
            Flow<List<QuestionEntity>> {
        return questionDao
            .observeUnswipedQuestions()
    }

    override fun observeQuestionsByCategory(
        category: QuestionCategory
    ): Flow<List<QuestionEntity>> {
        return questionDao
            .observeUnswipedQuestionsByCategory(
                category =
                    category
            )
    }

    override fun observeQuestion(
        questionId: String
    ): Flow<QuestionEntity?> {
        return questionDao
            .observeQuestion(
                questionId =
                    questionId
            )
    }

    override fun observeSavedQuestions():
            Flow<List<QuestionEntity>> {
        return questionDao
            .observeSavedQuestions()
    }

    override fun observeIsQuestionSaved(
        questionId: String
    ): Flow<Boolean> {
        return questionInteractionDao
            .observeIsQuestionSaved(
                questionId =
                    questionId
            )
    }

    override fun observeSkippedCount():
            Flow<Int> {
        return questionInteractionDao
            .observeSwipeCount(
                direction =
                    SwipeDirection.LEFT
            )
    }

    override suspend fun skipQuestion(
        questionId: String
    ) {
        recordSwipe(
            questionId =
                questionId,
            direction =
                SwipeDirection.LEFT
        )
    }

    override suspend fun saveQuestion(
        questionId: String
    ) {
        val timestamp =
            System.currentTimeMillis()

        questionInteractionDao
            .saveAndRecordSwipe(
                decision =
                    SwipeDecisionEntity(
                        id =
                            UUID
                                .randomUUID()
                                .toString(),
                        questionId =
                            questionId,
                        direction =
                            SwipeDirection.RIGHT,
                        swipedAt =
                            timestamp
                    ),
                savedQuestion =
                    SavedQuestionEntity(
                        id =
                            UUID
                                .randomUUID()
                                .toString(),
                        questionId =
                            questionId,
                        savedAt =
                            timestamp
                    )
            )
    }

    override suspend fun unsaveQuestion(
        questionId: String
    ) {
        questionInteractionDao
            .deleteSavedQuestion(
                questionId =
                    questionId
            )
    }

    override suspend fun restoreSkippedQuestions():
            Int {
        return questionInteractionDao
            .deleteSwipeDecisions(
                direction =
                    SwipeDirection.LEFT
            )
    }

    override suspend fun restartFeed():
            Int {
        return questionInteractionDao
            .deleteAllSwipeDecisions()
    }

    override suspend fun seedQuestionsIfRequired() {
        if (
            questionDao.countQuestions() >
            0
        ) {
            return
        }

        questionDao
            .insertQuestionCatalogue(
                questions =
                    QuestionSeedData.questions,
                skills =
                    QuestionSeedData
                        .questionSkills
            )
    }

    private suspend fun recordSwipe(
        questionId: String,
        direction: SwipeDirection
    ) {
        questionInteractionDao
            .insertSwipeDecision(
                decision =
                    SwipeDecisionEntity(
                        id =
                            UUID
                                .randomUUID()
                                .toString(),
                        questionId =
                            questionId,
                        direction =
                            direction
                    )
            )
    }
}