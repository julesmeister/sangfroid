package com.julesmeister.sangfroid.domain.evaluation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AnswerEvaluationEngine {

    val state: StateFlow<AnswerEvaluationEngineState>

    val partialOutput: Flow<String>

    suspend fun prepare(): Result<Unit>

    suspend fun evaluate(
        request: EvaluationRequest
    ): Result<EvaluationResult>

    suspend fun cancel()

    suspend fun release()
}

sealed interface AnswerEvaluationEngineState {

    data object Idle : AnswerEvaluationEngineState

    data object LoadingModel : AnswerEvaluationEngineState

    data class Ready(
        val modelName: String,
        val modelVersion: String
    ) : AnswerEvaluationEngineState

    data object Evaluating : AnswerEvaluationEngineState

    data class Completed(
        val evaluationId: String
    ) : AnswerEvaluationEngineState

    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : AnswerEvaluationEngineState
}