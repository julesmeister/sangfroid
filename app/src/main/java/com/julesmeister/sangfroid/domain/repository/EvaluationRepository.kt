package com.julesmeister.sangfroid.domain.repository

import com.julesmeister.sangfroid.data.local.entity.PracticeAttemptEntity
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.domain.evaluation.EvaluationResult
import kotlinx.coroutines.flow.Flow

interface EvaluationRepository {

    fun observeAttempt(
        attemptId: String
    ): Flow<PracticeAttemptEntity?>

    fun observeQuestion(
        attemptId: String
    ): Flow<QuestionEntity?>

    fun observeEvaluation(
        attemptId: String
    ): Flow<EvaluationResult?>

    suspend fun evaluateAttempt(
        attemptId: String
    ): Result<EvaluationResult>

    suspend fun retryEvaluation(
        attemptId: String
    ): Result<EvaluationResult>
}