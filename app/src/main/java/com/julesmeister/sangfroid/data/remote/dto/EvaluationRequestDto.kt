package com.julesmeister.sangfroid.data.remote.dto

import com.julesmeister.sangfroid.domain.evaluation.EvaluationRequest
import org.json.JSONArray
import org.json.JSONObject

data class EvaluationRequestDto(
    val requestId: String,
    val attemptId: String,
    val questionId: String,
    val question: String,
    val answer: String,
    val category: String,
    val difficulty: String,
    val answerFramework: String?,
    val whyAsked: String,
    val inputMethod: String,
    val targetRole: String?,
    val relevantSkills: List<String>,
    val previousWeaknesses: List<String>,
    val languageTag: String
) {

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put(
                "requestId",
                requestId
            )

            put(
                "attemptId",
                attemptId
            )

            put(
                "questionId",
                questionId
            )

            put(
                "question",
                question
            )

            put(
                "answer",
                answer
            )

            put(
                "category",
                category
            )

            put(
                "difficulty",
                difficulty
            )

            put(
                "answerFramework",
                answerFramework
                    ?: JSONObject.NULL
            )

            put(
                "whyAsked",
                whyAsked
            )

            put(
                "inputMethod",
                inputMethod
            )

            put(
                "targetRole",
                targetRole
                    ?: JSONObject.NULL
            )

            put(
                "relevantSkills",
                JSONArray(
                    relevantSkills
                )
            )

            put(
                "previousWeaknesses",
                JSONArray(
                    previousWeaknesses
                )
            )

            put(
                "languageTag",
                languageTag
            )
        }
    }

    companion object {

        fun fromDomain(
            requestId: String,
            request: EvaluationRequest
        ): EvaluationRequestDto {
            return EvaluationRequestDto(
                requestId =
                    requestId,
                attemptId =
                    request.attemptId,
                questionId =
                    request.questionId,
                question =
                    request.question,
                answer =
                    request.answer,
                category =
                    request.category.name,
                difficulty =
                    request.difficulty.name,
                answerFramework =
                    request.answerFramework,
                whyAsked =
                    request.whyAsked,
                inputMethod =
                    request.inputMethod.name,
                targetRole =
                    request.targetRole,
                relevantSkills =
                    request.relevantSkills,
                previousWeaknesses =
                    request.previousWeaknesses,
                languageTag =
                    request.languageTag
            )
        }
    }
}