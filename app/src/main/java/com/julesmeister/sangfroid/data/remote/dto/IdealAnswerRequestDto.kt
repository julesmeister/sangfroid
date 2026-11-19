package com.julesmeister.sangfroid.data.remote.dto

import org.json.JSONArray
import org.json.JSONObject

data class IdealAnswerRequestDto(
    val requestId: String,
    val questionId: String,
    val question: String,
    val category: String,
    val difficulty: String,
    val questionAngle: String?,
    val whyAsked: String?,
    val answerFramework: String?,
    val targetRole: String?,
    val experienceLevel: String?,
    val languageTag: String,
    val revisionNumber: Int,
    val previousIdealAnswer: String?,
    val cvEvidence: List<CvEvidenceDto>
) {

    init {
        require(requestId.isNotBlank()) {
            "requestId cannot be blank."
        }

        require(questionId.isNotBlank()) {
            "questionId cannot be blank."
        }

        require(question.isNotBlank()) {
            "question cannot be blank."
        }

        require(category.isNotBlank()) {
            "category cannot be blank."
        }

        require(difficulty.isNotBlank()) {
            "difficulty cannot be blank."
        }

        require(languageTag.isNotBlank()) {
            "languageTag cannot be blank."
        }

        require(revisionNumber >= 1) {
            "revisionNumber must be at least one."
        }

        require(
            cvEvidence.size <=
                    MAXIMUM_EVIDENCE_COUNT
        ) {
            "cvEvidence cannot contain more than 20 entries."
        }
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put(
                JSON_REQUEST_ID,
                requestId
                    .normalizeRequired(
                        MAXIMUM_REQUEST_ID_LENGTH
                    )
            )

            put(
                JSON_QUESTION_ID,
                questionId
                    .normalizeRequired(
                        MAXIMUM_QUESTION_ID_LENGTH
                    )
            )

            put(
                JSON_QUESTION,
                question
                    .normalizeRequired(
                        MAXIMUM_QUESTION_LENGTH
                    )
            )

            put(
                JSON_CATEGORY,
                category
                    .normalizeRequired(
                        MAXIMUM_ENUM_LENGTH
                    )
            )

            put(
                JSON_DIFFICULTY,
                difficulty
                    .normalizeRequired(
                        MAXIMUM_ENUM_LENGTH
                    )
            )

            putNullable(
                JSON_QUESTION_ANGLE,
                questionAngle,
                MAXIMUM_ANGLE_LENGTH
            )

            putNullable(
                JSON_WHY_ASKED,
                whyAsked,
                MAXIMUM_WHY_ASKED_LENGTH
            )

            putNullable(
                JSON_ANSWER_FRAMEWORK,
                answerFramework,
                MAXIMUM_ANSWER_FRAMEWORK_LENGTH
            )

            putNullable(
                JSON_TARGET_ROLE,
                targetRole,
                MAXIMUM_TARGET_ROLE_LENGTH
            )

            putNullable(
                JSON_EXPERIENCE_LEVEL,
                experienceLevel,
                MAXIMUM_EXPERIENCE_LEVEL_LENGTH
            )

            put(
                JSON_LANGUAGE_TAG,
                languageTag
                    .normalizeRequired(
                        MAXIMUM_LANGUAGE_TAG_LENGTH
                    )
            )

            put(
                JSON_REVISION_NUMBER,
                revisionNumber
            )

            putNullablePreservingLines(
                JSON_PREVIOUS_IDEAL_ANSWER,
                previousIdealAnswer,
                MAXIMUM_PREVIOUS_ANSWER_LENGTH
            )

            put(
                JSON_CV_EVIDENCE,
                JSONArray().apply {
                    cvEvidence
                        .map(
                            CvEvidenceDto::normalized
                        )
                        .filter { evidence ->
                            evidence.id.isNotBlank() &&
                                    evidence.title.isNotBlank()
                        }
                        .distinctBy { evidence ->
                            evidence.type.name to
                                    evidence.id
                        }
                        .take(
                            MAXIMUM_EVIDENCE_COUNT
                        )
                        .forEach { evidence ->
                            put(
                                evidence.toJson()
                            )
                        }
                }
            )
        }
    }

    private fun JSONObject.putNullable(
        name: String,
        value: String?,
        maximumLength: Int
    ) {
        put(
            name,
            value
                ?.normalizeRequired(
                    maximumLength
                )
                ?.takeIf(
                    String::isNotBlank
                )
                ?: JSONObject.NULL
        )
    }

    private fun JSONObject.putNullablePreservingLines(
        name: String,
        value: String?,
        maximumLength: Int
    ) {
        val normalized =
            value
                ?.replace(
                    '\u0000',
                    ' '
                )
                ?.replace(
                    Regex("[\\t ]+"),
                    " "
                )
                ?.replace(
                    Regex("\\n{3,}"),
                    "\n\n"
                )
                ?.trim()
                ?.take(
                    maximumLength
                )
                ?.takeIf(
                    String::isNotBlank
                )

        put(
            name,
            normalized
                ?: JSONObject.NULL
        )
    }

    private fun String.normalizeRequired(
        maximumLength: Int
    ): String {
        return replace(
            '\u0000',
            ' '
        )
            .replace(
                WHITESPACE_REGEX,
                " "
            )
            .trim()
            .take(
                maximumLength
            )
    }

    companion object {
        private const val JSON_REQUEST_ID =
            "requestId"

        private const val JSON_QUESTION_ID =
            "questionId"

        private const val JSON_QUESTION =
            "question"

        private const val JSON_CATEGORY =
            "category"

        private const val JSON_DIFFICULTY =
            "difficulty"

        private const val JSON_QUESTION_ANGLE =
            "questionAngle"

        private const val JSON_WHY_ASKED =
            "whyAsked"

        private const val JSON_ANSWER_FRAMEWORK =
            "answerFramework"

        private const val JSON_TARGET_ROLE =
            "targetRole"

        private const val JSON_EXPERIENCE_LEVEL =
            "experienceLevel"

        private const val JSON_LANGUAGE_TAG =
            "languageTag"

        private const val JSON_REVISION_NUMBER =
            "revisionNumber"

        private const val JSON_PREVIOUS_IDEAL_ANSWER =
            "previousIdealAnswer"

        private const val JSON_CV_EVIDENCE =
            "cvEvidence"

        private const val MAXIMUM_REQUEST_ID_LENGTH =
            100

        private const val MAXIMUM_QUESTION_ID_LENGTH =
            200

        private const val MAXIMUM_QUESTION_LENGTH =
            2_000

        private const val MAXIMUM_ENUM_LENGTH =
            50

        private const val MAXIMUM_ANGLE_LENGTH =
            100

        private const val MAXIMUM_WHY_ASKED_LENGTH =
            2_000

        private const val MAXIMUM_ANSWER_FRAMEWORK_LENGTH =
            3_000

        private const val MAXIMUM_TARGET_ROLE_LENGTH =
            200

        private const val MAXIMUM_EXPERIENCE_LEVEL_LENGTH =
            100

        private const val MAXIMUM_LANGUAGE_TAG_LENGTH =
            20

        private const val MAXIMUM_PREVIOUS_ANSWER_LENGTH =
            8_000

        private const val MAXIMUM_EVIDENCE_COUNT =
            20

        private val WHITESPACE_REGEX =
            Regex("\\s+")
    }
}