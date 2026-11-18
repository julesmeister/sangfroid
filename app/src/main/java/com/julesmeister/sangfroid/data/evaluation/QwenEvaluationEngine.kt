package com.julesmeister.sangfroid.data.evaluation

import com.julesmeister.sangfroid.data.remote.api.SangfroidApi
import com.julesmeister.sangfroid.data.remote.dto.EvaluationRequestDto
import com.julesmeister.sangfroid.domain.evaluation.AnswerEvaluationEngine
import com.julesmeister.sangfroid.domain.evaluation.AnswerEvaluationEngineState
import com.julesmeister.sangfroid.domain.evaluation.EvaluationRequest
import com.julesmeister.sangfroid.domain.evaluation.EvaluationResult
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class QwenEvaluationEngine @Inject constructor(
    private val sangfroidApi:
    SangfroidApi
) : AnswerEvaluationEngine {

    private val mutableState =
        MutableStateFlow<
                AnswerEvaluationEngineState
                >(
            AnswerEvaluationEngineState.Idle
        )

    override val state =
        mutableState.asStateFlow()

    private val mutablePartialOutput =
        MutableSharedFlow<String>(
            extraBufferCapacity = 1
        )

    override val partialOutput:
            Flow<String> =
        mutablePartialOutput
            .asSharedFlow()

    override suspend fun prepare():
            Result<Unit> {
        mutableState.value =
            AnswerEvaluationEngineState.Ready(
                modelName = "Qwen API",
                modelVersion = "remote"
            )

        return Result.success(Unit)
    }

    override suspend fun evaluate(
        request: EvaluationRequest
    ): Result<EvaluationResult> {
        mutableState.value =
            AnswerEvaluationEngineState
                .Evaluating

        val requestId =
            UUID.randomUUID()
                .toString()

        val result =
            sangfroidApi
                .evaluateAnswer(
                    EvaluationRequestDto
                        .fromDomain(
                            requestId =
                                requestId,
                            request = request
                        )
                )
                .map { response ->
                    response.toDomain()
                }

        result.onSuccess {
                evaluation ->
            mutableState.value =
                AnswerEvaluationEngineState
                    .Completed(
                        evaluationId =
                            evaluation.id
                    )
        }.onFailure { throwable ->
            mutableState.value =
                AnswerEvaluationEngineState
                    .Error(
                        message =
                            throwable.message
                                ?: "Unable to evaluate the answer.",
                        cause = throwable
                    )
        }

        return result
    }

    override suspend fun cancel() {
        mutableState.value =
            AnswerEvaluationEngineState
                .Ready(
                    modelName =
                        "Qwen API",
                    modelVersion =
                        "remote"
                )
    }

    override suspend fun release() {
        mutableState.value =
            AnswerEvaluationEngineState.Idle
    }
}