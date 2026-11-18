package com.julesmeister.sangfroid.domain.progress

import kotlinx.coroutines.flow.Flow

interface QuestionProgressRepository {

    fun observeQuestionProgress(
        questionId: String
    ): Flow<QuestionProgress>

    suspend fun getQuestionProgress(
        questionId: String
    ): QuestionProgress
}