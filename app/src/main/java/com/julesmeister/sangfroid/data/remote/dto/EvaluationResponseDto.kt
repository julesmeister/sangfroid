package com.julesmeister.sangfroid.data.remote.dto

import com.julesmeister.sangfroid.domain.evaluation.EvaluationProcessingMode
import com.julesmeister.sangfroid.domain.evaluation.EvaluationResult
import com.julesmeister.sangfroid.domain.evaluation.EvaluationRubricScore
import com.julesmeister.sangfroid.domain.evaluation.FollowUpQuestion
import org.json.JSONArray
import org.json.JSONObject

data class EvaluationResponseDto(
    val evaluationId: String,
    val attemptId: String,
    val summary: String,
    val strengths: List<String>,
    val missingPoints: List<String>,
    val suggestions: List<String>,
    val improvedOutline: List<String>,
    val improvedAnswerExample: String?,
    val followUpQuestions:
    List<FollowUpQuestionDto>,
    val rubricScores:
    List<RubricScoreDto>,
    val modelName: String,
    val modelVersion: String,
    val createdAt: Long
) {

    fun toDomain(): EvaluationResult {
        return EvaluationResult(
            id =
                evaluationId,
            attemptId =
                attemptId,
            summary =
                summary,
            strengths =
                strengths,
            missingPoints =
                missingPoints,
            suggestions =
                suggestions,
            improvedOutline =
                improvedOutline,
            improvedAnswerExample =
                improvedAnswerExample,
            followUpQuestions =
                followUpQuestions.map {
                        followUp ->
                    followUp.toDomain()
                },
            rubricScores =
                rubricScores.map {
                        score ->
                    score.toDomain()
                },
            modelName =
                modelName,
            modelVersion =
                modelVersion,
            processingMode =
                EvaluationProcessingMode
                    .CLOUD_QWEN,
            createdAt =
                createdAt
        )
    }

    companion object {

        fun fromJson(
            json: JSONObject
        ): EvaluationResponseDto {
            return EvaluationResponseDto(
                evaluationId =
                    json.requireText(
                        "evaluationId"
                    ),
                attemptId =
                    json.requireText(
                        "attemptId"
                    ),
                summary =
                    json.requireText(
                        "summary"
                    ),
                strengths =
                    json.requireTextList(
                        "strengths"
                    ),
                missingPoints =
                    json.requireTextList(
                        "missingPoints"
                    ),
                suggestions =
                    json.requireTextList(
                        "suggestions"
                    ),
                improvedOutline =
                    json.requireTextList(
                        "improvedOutline"
                    ),
                improvedAnswerExample =
                    json.optionalText(
                        "improvedAnswerExample"
                    ),
                followUpQuestions =
                    json
                        .getJSONArray(
                            "followUpQuestions"
                        )
                        .toFollowUpQuestions(),
                rubricScores =
                    json
                        .getJSONArray(
                            "rubricScores"
                        )
                        .toRubricScores(),
                modelName =
                    json.requireText(
                        "modelName"
                    ),
                modelVersion =
                    json.requireText(
                        "modelVersion"
                    ),
                createdAt =
                    json.getLong(
                        "createdAt"
                    )
            )
        }
    }
}

data class FollowUpQuestionDto(
    val question: String,
    val reason: String?
) {

    fun toDomain():
            FollowUpQuestion {
        return FollowUpQuestion(
            question =
                question,
            reason =
                reason
        )
    }
}

data class RubricScoreDto(
    val criterion: String,
    val score: Int,
    val maximumScore: Int,
    val explanation: String
) {

    fun toDomain():
            EvaluationRubricScore {
        return EvaluationRubricScore(
            criterion =
                criterion,
            score =
                score,
            maximumScore =
                maximumScore,
            explanation =
                explanation
        )
    }
}

private fun JSONObject.requireText(
    key: String
): String {
    val value =
        getString(key)
            .trim()

    require(value.isNotBlank()) {
        "$key cannot be blank."
    }

    return value
}

private fun JSONObject.optionalText(
    key: String
): String? {
    if (
        !has(key) ||
        isNull(key)
    ) {
        return null
    }

    return optString(key)
        .trim()
        .takeIf {
                value ->
            value.isNotBlank()
        }
}

private fun JSONObject.requireTextList(
    key: String
): List<String> {
    val values =
        getJSONArray(key)
            .toTextList()

    require(values.isNotEmpty()) {
        "$key cannot be empty."
    }

    return values
}

private fun JSONArray.toTextList():
        List<String> {
    return buildList {
        repeat(length()) { index ->
            val value =
                optString(index)
                    .trim()

            if (value.isNotBlank()) {
                add(value)
            }
        }
    }
}

private fun JSONArray.toFollowUpQuestions():
        List<FollowUpQuestionDto> {
    return buildList {
        repeat(length()) { index ->
            val item =
                getJSONObject(index)

            add(
                FollowUpQuestionDto(
                    question =
                        item.requireText(
                            "question"
                        ),
                    reason =
                        item.optionalText(
                            "reason"
                        )
                )
            )
        }
    }
}

private fun JSONArray.toRubricScores():
        List<RubricScoreDto> {
    return buildList {
        repeat(length()) { index ->
            val item =
                getJSONObject(index)

            val score =
                item.getInt(
                    "score"
                )

            val maximumScore =
                item.getInt(
                    "maximumScore"
                )

            require(maximumScore > 0) {
                "maximumScore must be positive."
            }

            require(
                score in 0..maximumScore
            ) {
                "score must be between zero and maximumScore."
            }

            add(
                RubricScoreDto(
                    criterion =
                        item.requireText(
                            "criterion"
                        ),
                    score =
                        score,
                    maximumScore =
                        maximumScore,
                    explanation =
                        item.requireText(
                            "explanation"
                        )
                )
            )
        }
    }
}