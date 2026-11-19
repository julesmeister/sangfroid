package com.julesmeister.sangfroid.data.remote.dto

import org.json.JSONArray
import org.json.JSONObject

data class IdealAnswerResponseDto(
    val requestId: String,
    val questionId: String,
    val idealAnswer: String,
    val revisionNumber: Int,
    val usedEvidenceIds: List<String>,
    val modelName: String,
    val modelVersion: String,
    val generatedAt: Long
) {

    init {
        require(requestId.isNotBlank()) {
            "requestId cannot be blank."
        }

        require(questionId.isNotBlank()) {
            "questionId cannot be blank."
        }

        require(idealAnswer.isNotBlank()) {
            "idealAnswer cannot be blank."
        }

        require(revisionNumber >= 1) {
            "revisionNumber must be at least one."
        }
    }

    companion object {

        fun fromJson(
            json: JSONObject
        ): IdealAnswerResponseDto {
            return IdealAnswerResponseDto(
                requestId =
                    json
                        .optString(
                            JSON_REQUEST_ID
                        )
                        .trim()
                        .takeIf(
                            String::isNotBlank
                        )
                        ?: throw IllegalArgumentException(
                            "Ideal-answer requestId is missing."
                        ),

                questionId =
                    json
                        .optString(
                            JSON_QUESTION_ID
                        )
                        .trim()
                        .takeIf(
                            String::isNotBlank
                        )
                        ?: throw IllegalArgumentException(
                            "Ideal-answer questionId is missing."
                        ),

                idealAnswer =
                    json
                        .optString(
                            JSON_IDEAL_ANSWER
                        )
                        .replace(
                            '\u0000',
                            ' '
                        )
                        .trim()
                        .takeIf(
                            String::isNotBlank
                        )
                        ?: throw IllegalArgumentException(
                            "Ideal-answer content is missing."
                        ),

                revisionNumber =
                    json
                        .optInt(
                            JSON_REVISION_NUMBER,
                            1
                        )
                        .coerceAtLeast(
                            1
                        ),

                usedEvidenceIds =
                    json
                        .optJSONArray(
                            JSON_USED_EVIDENCE_IDS
                        )
                        .toStringList(),

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

        private fun JSONArray?.toStringList():
                List<String> {
            if (this == null) {
                return emptyList()
            }

            return buildList {
                for (
                index in
                0 until length()
                ) {
                    val value =
                        optString(
                            index
                        )
                            .replace(
                                '\u0000',
                                ' '
                            )
                            .trim()

                    if (
                        value.isNotBlank() &&
                        value !in this
                    ) {
                        add(
                            value
                        )
                    }
                }
            }
        }

        private const val JSON_REQUEST_ID =
            "requestId"

        private const val JSON_QUESTION_ID =
            "questionId"

        private const val JSON_IDEAL_ANSWER =
            "idealAnswer"

        private const val JSON_REVISION_NUMBER =
            "revisionNumber"

        private const val JSON_USED_EVIDENCE_IDS =
            "usedEvidenceIds"

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
    }
}