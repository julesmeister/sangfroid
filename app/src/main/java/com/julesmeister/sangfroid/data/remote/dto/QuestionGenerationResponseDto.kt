package com.julesmeister.sangfroid.data.remote.dto

import org.json.JSONArray
import org.json.JSONObject

data class QuestionGenerationResponseDto(
    val requestId: String,
    val questions: List<GeneratedQuestionDto>,
    val modelName: String,
    val modelVersion: String,
    val generatedAt: Long
) {

    init {
        require(
            questions.size <=
                    MAXIMUM_QUESTION_COUNT
        ) {
            "A generation response cannot contain more than 20 questions."
        }
    }

    companion object {

        fun fromJson(
            json: JSONObject
        ): QuestionGenerationResponseDto {
            return QuestionGenerationResponseDto(
                requestId =
                    json
                        .optString(
                            JSON_REQUEST_ID
                        )
                        .trim(),
                questions =
                    json
                        .optJSONArray(
                            JSON_QUESTIONS
                        )
                        .toGeneratedQuestions(),
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
                generatedAt =
                    json
                        .optLong(
                            JSON_GENERATED_AT,
                            System.currentTimeMillis()
                        )
                        .takeIf { value ->
                            value > 0L
                        }
                        ?: System.currentTimeMillis()
            )
        }

        private fun JSONArray?.toGeneratedQuestions():
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

                val questionJson =
                    optJSONObject(
                        index
                    ) ?: continue

                questions +=
                    GeneratedQuestionDto.fromJson(
                        questionJson
                    )
            }

            return questions
        }

        private const val JSON_REQUEST_ID =
            "requestId"

        private const val JSON_QUESTIONS =
            "questions"

        private const val JSON_MODEL_NAME =
            "modelName"

        private const val JSON_MODEL_VERSION =
            "modelVersion"

        private const val JSON_GENERATED_AT =
            "generatedAt"

        private const val DEFAULT_MODEL_NAME =
            "Qwen"

        private const val DEFAULT_MODEL_VERSION =
            "remote"

        private const val MAXIMUM_QUESTION_COUNT =
            20
    }
}