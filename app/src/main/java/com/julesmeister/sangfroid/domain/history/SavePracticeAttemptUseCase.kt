package com.julesmeister.sangfroid.domain.history

import com.julesmeister.sangfroid.domain.model.AnswerInputMethod
import com.julesmeister.sangfroid.domain.model.EvaluationStatus
import java.util.UUID
import javax.inject.Inject

class SavePracticeAttemptUseCase @Inject constructor(
    private val repository: PracticeHistoryRepository
) {

    suspend operator fun invoke(
        questionId: String,
        answerText: String,
        inputMethod: AnswerInputMethod,
        startedAt: Long,
        submittedAt: Long = System.currentTimeMillis(),
        evaluationStatus: EvaluationStatus,
        modelName: String? = null,
        modelVersion: String? = null,
        evaluation: PracticeEvaluationInput? = null
    ): PracticeAttempt {
        val normalizedQuestionId = questionId.trim()
        val normalizedAnswerText = answerText.trim()

        require(normalizedQuestionId.isNotBlank()) {
            "Question ID cannot be blank."
        }

        require(normalizedAnswerText.isNotBlank()) {
            "Answer text cannot be blank."
        }

        require(startedAt > 0L) {
            "Started time must be greater than zero."
        }

        require(submittedAt >= startedAt) {
            "Submitted time cannot be earlier than started time."
        }

        val attemptId = UUID.randomUUID().toString()

        val durationMillis = submittedAt - startedAt
        val durationSeconds = (durationMillis / 1_000L)
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()

        val wordCount = normalizedAnswerText
            .split(Regex("\\s+"))
            .count(String::isNotBlank)

        val answerEvaluation = evaluation?.let { input ->
            PracticeAnswerEvaluation(
                id = UUID.randomUUID().toString(),
                attemptId = attemptId,
                summary = input.summary.trim(),
                strengths = input.strengths.trim(),
                missingPoints = input.missingPoints.trim(),
                suggestions = input.suggestions.trim(),
                improvedOutline = input.improvedOutline.trim(),
                improvedAnswerExample = input.improvedAnswerExample
                    ?.trim()
                    ?.takeIf(String::isNotBlank),
                modelName = input.modelName.trim(),
                modelVersion = input.modelVersion
                    ?.trim()
                    ?.takeIf(String::isNotBlank),
                processingMode = input.processingMode.trim(),
                createdAt = submittedAt
            )
        }

        val attempt = PracticeAttempt(
            id = attemptId,
            questionId = normalizedQuestionId,
            answerText = normalizedAnswerText,
            inputMethod = inputMethod,
            startedAt = startedAt,
            submittedAt = submittedAt,
            durationSeconds = durationSeconds,
            wordCount = wordCount,
            evaluationStatus = evaluationStatus,
            modelName = modelName
                ?.trim()
                ?.takeIf(String::isNotBlank),
            modelVersion = modelVersion
                ?.trim()
                ?.takeIf(String::isNotBlank),
            evaluation = answerEvaluation,
            revisions = emptyList()
        )

        repository.savePracticeAttempt(attempt)

        return attempt
    }
}

data class PracticeEvaluationInput(
    val summary: String,
    val strengths: String,
    val missingPoints: String,
    val suggestions: String,
    val improvedOutline: String,
    val improvedAnswerExample: String?,
    val modelName: String,
    val modelVersion: String?,
    val processingMode: String
)