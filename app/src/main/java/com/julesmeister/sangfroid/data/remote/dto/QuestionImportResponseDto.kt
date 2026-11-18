// app/src/main/java/com/julesmeister/sangfroid/data/remote/dto/QuestionImportResponseDto.kt

package com.julesmeister.sangfroid.data.remote.dto

import org.json.JSONArray
import org.json.JSONObject

data class QuestionImportResponseDto(
    val requestId: String,
    val questions: List<GeneratedQuestionDto>,
    val detectedCount: Int,
    val modelName: String,
    val modelVersion: String,
    val processedAt: Long
) {

    init {
        require(
            detectedCount >= 0
        ) {
            "detectedCount cannot be negative."
        }

        require(
            questions.size <=
                    MAXIMUM_QUESTION_COUNT
        ) {
            "An import response cannot contain more than 50 questions."
        }
    }

    companion object {

        fun fromJson(
            json: JSONObject
        ): QuestionImportResponseDto {
            val questions =
                json
                    .optJSONArray(
                        JSON_QUESTIONS
                    )
                    .toQuestions()

            return QuestionImportResponseDto(
                requestId =
                    json
                        .optString(
                            JSON_REQUEST_ID
                        )
                        .trim(),
                questions =
                    questions,
                detectedCount =
                    json
                        .optInt(
                            JSON_DETECTED_COUNT,
                            questions.size
                        )
                        .coerceAtLeast(
                            minimumValue = 0
                        ),
                modelName =
                    json
                        .optString(
                            JSON_MODEL_NAME,
                            DEFAULT_MODEL_NAME
                        )
                        .trim()
                        .takeIf(
                            String::isNotBlank
                        )
                        ?: DEFAULT_MODEL_NAME,
                modelVersion =
                    json
                        .optString(
                            JSON_MODEL_VERSION,
                            DEFAULT_MODEL_VERSION
                        )
                        .trim()
                        .takeIf(
                            String::isNotBlank
                        )
                        ?: DEFAULT_MODEL_VERSION,
                processedAt =
                    json
                        .optLong(
                            JSON_PROCESSED_AT,
                            System.currentTimeMillis()
                        )
                        .takeIf { value ->
                            value > 0L
                        }
                        ?: System.currentTimeMillis()
            )
        }

        private fun JSONArray?.toQuestions():
                List<GeneratedQuestionDto> {
            if (this == null) {
                return emptyList()
            }

            val questions =
                mutableListOf<GeneratedQuestionDto>()

            for (
            index in
            0 until length()
            ) {
                if (
                    questions.size >=
                    MAXIMUM_QUESTION_COUNT
                ) {
                    break
                }

                val item =
                    optJSONObject(
                        index
                    ) ?: continue

                questions +=
                    GeneratedQuestionDto.fromJson(
                        item
                    )
            }

            return questions
        }

        private const val JSON_REQUEST_ID =
            "requestId"

        private const val JSON_QUESTIONS =
            "questions"

        private const val JSON_DETECTED_COUNT =
            "detectedCount"

        private const val JSON_MODEL_NAME =
            "modelName"

        private const val JSON_MODEL_VERSION =
            "modelVersion"

        private const val JSON_PROCESSED_AT =
            "processedAt"

        private const val DEFAULT_MODEL_NAME =
            "Qwen"

        private const val DEFAULT_MODEL_VERSION =
            "remote"

        private const val MAXIMUM_QUESTION_COUNT =
            50
    }
}