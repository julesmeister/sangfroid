package com.julesmeister.sangfroid.domain.evaluation

import com.julesmeister.sangfroid.domain.model.AnswerInputMethod
import com.julesmeister.sangfroid.domain.model.Difficulty
import com.julesmeister.sangfroid.domain.model.QuestionCategory

data class EvaluationRequest(
    val attemptId: String,
    val questionId: String,
    val question: String,
    val answer: String,
    val category: QuestionCategory,
    val difficulty: Difficulty,
    val answerFramework: String?,
    val whyAsked: String,
    val inputMethod: AnswerInputMethod,
    val targetRole: String? = null,
    val relevantSkills: List<String> = emptyList(),
    val previousWeaknesses: List<String> = emptyList(),
    val languageTag: String = "en-US"
) {
    init {
        require(attemptId.isNotBlank()) {
            "Attempt ID cannot be blank."
        }

        require(questionId.isNotBlank()) {
            "Question ID cannot be blank."
        }

        require(question.isNotBlank()) {
            "Interview question cannot be blank."
        }

        require(answer.isNotBlank()) {
            "Interview answer cannot be blank."
        }
    }
}