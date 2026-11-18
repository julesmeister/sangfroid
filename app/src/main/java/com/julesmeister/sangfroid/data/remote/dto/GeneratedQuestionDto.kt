package com.julesmeister.sangfroid.data.remote.dto

import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

data class GeneratedQuestionDto(
    val id: String,
    val question: String,
    val category: String,
    val difficulty: String,
    val questionAngle: String,
    val whyAsked: String,
    val answerFramework: String?,
    val estimatedMinutes: Int,
    val skillCodes: List<String>,
    val generationReason: String,
    val evidenceType: String?,
    val evidenceId: String?
) {

    init {
        require(id.isNotBlank()) {
            "Generated question ID cannot be blank."
        }

        require(question.isNotBlank()) {
            "Generated question text cannot be blank."
        }

        require(category.isNotBlank()) {
            "Generated question category cannot be blank."
        }

        require(difficulty.isNotBlank()) {
            "Generated question difficulty cannot be blank."
        }

        require(questionAngle.isNotBlank()) {
            "Generated question angle cannot be blank."
        }

        require(whyAsked.isNotBlank()) {
            "Generated question whyAsked cannot be blank."
        }

        require(generationReason.isNotBlank()) {
            "Generated question generationReason cannot be blank."
        }

        require(
            estimatedMinutes in 1..30
        ) {
            "Generated question estimatedMinutes must be between 1 and 30."
        }

        require(
            skillCodes.size <=
                    MAXIMUM_SKILL_COUNT
        ) {
            "A generated question cannot contain more than four skill codes."
        }

        require(
            (evidenceType == null) ==
                    (evidenceId == null)
        ) {
            "Generated evidenceType and evidenceId must both be present or both be null."
        }
    }

    companion object {

        fun fromJson(
            json: JSONObject
        ): GeneratedQuestionDto {
            val evidenceType =
                json
                    .nullableString(
                        JSON_EVIDENCE_TYPE
                    )
                    ?.normalizeEnumValue()

            val evidenceId =
                json
                    .nullableString(
                        JSON_EVIDENCE_ID
                    )

            require(
                (evidenceType == null) ==
                        (evidenceId == null)
            ) {
                "Generated evidenceType and evidenceId must both be present or both be null."
            }

            val angle =
                json
                    .nullableString(
                        JSON_QUESTION_ANGLE
                    )
                    ?.normalizeEnumValue()
                    ?.takeIf { value ->
                        value in ALLOWED_QUESTION_ANGLES
                    }
                    ?: DEFAULT_QUESTION_ANGLE

            return GeneratedQuestionDto(
                id =
                    json.requiredString(
                        JSON_ID
                    ),
                question =
                    json.requiredString(
                        JSON_QUESTION
                    ),
                category =
                    json
                        .requiredString(
                            JSON_CATEGORY
                        )
                        .normalizeEnumValue(),
                difficulty =
                    json
                        .requiredString(
                            JSON_DIFFICULTY
                        )
                        .normalizeDifficulty(),
                questionAngle =
                    angle,
                whyAsked =
                    json.requiredString(
                        JSON_WHY_ASKED
                    ),
                answerFramework =
                    json.nullableString(
                        JSON_ANSWER_FRAMEWORK
                    ),
                estimatedMinutes =
                    json
                        .optInt(
                            JSON_ESTIMATED_MINUTES,
                            DEFAULT_ESTIMATED_MINUTES
                        )
                        .coerceIn(
                            minimumValue = 1,
                            maximumValue = 30
                        ),
                skillCodes =
                    json
                        .optJSONArray(
                            JSON_SKILL_CODES
                        )
                        .toNormalizedStringList(
                            maximumItems =
                                MAXIMUM_SKILL_COUNT
                        )
                        .ifEmpty {
                            listOf(
                                DEFAULT_SKILL_CODE
                            )
                        },
                generationReason =
                    json
                        .nullableString(
                            JSON_GENERATION_REASON
                        )
                        ?: DEFAULT_GENERATION_REASON,
                evidenceType =
                    evidenceType,
                evidenceId =
                    evidenceId
            )
        }

        private fun JSONObject.requiredString(
            name: String
        ): String {
            return nullableString(
                name
            ) ?: throw IllegalArgumentException(
                "$name is missing from a generated question."
            )
        }

        private fun JSONObject.nullableString(
            name: String
        ): String? {
            if (
                !has(name) ||
                isNull(name)
            ) {
                return null
            }

            return optString(name)
                .replace(
                    '\u0000',
                    ' '
                )
                .replace(
                    WHITESPACE_REGEX,
                    " "
                )
                .trim()
                .takeIf(
                    String::isNotBlank
                )
                ?.takeUnless { value ->
                    value.equals(
                        other = "null",
                        ignoreCase = true
                    )
                }
        }

        private fun JSONArray?.toNormalizedStringList(
            maximumItems: Int
        ): List<String> {
            if (this == null) {
                return emptyList()
            }

            val values =
                mutableListOf<String>()

            for (
            index in
            0 until length()
            ) {
                if (
                    values.size >=
                    maximumItems
                ) {
                    break
                }

                val value =
                    optString(index)
                        .normalizeEnumValue()

                if (
                    value.isNotBlank() &&
                    value !in values
                ) {
                    values += value
                }
            }

            return values
        }

        private fun String.normalizeDifficulty():
                String {
            val normalized =
                normalizeEnumValue()

            return if (
                normalized == "BEGINNER"
            ) {
                "FOUNDATION"
            } else {
                normalized
            }
        }

        private fun String.normalizeEnumValue():
                String {
            return trim()
                .uppercase(
                    Locale.ROOT
                )
                .replace(
                    ENUM_SEPARATOR_REGEX,
                    "_"
                )
                .trim('_')
        }

        private const val JSON_ID =
            "id"

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

        private const val JSON_ESTIMATED_MINUTES =
            "estimatedMinutes"

        private const val JSON_SKILL_CODES =
            "skillCodes"

        private const val JSON_GENERATION_REASON =
            "generationReason"

        private const val JSON_EVIDENCE_TYPE =
            "evidenceType"

        private const val JSON_EVIDENCE_ID =
            "evidenceId"

        private const val DEFAULT_ESTIMATED_MINUTES =
            5

        private const val DEFAULT_QUESTION_ANGLE =
            "GENERAL"

        private const val DEFAULT_SKILL_CODE =
            "GENERAL"

        private const val DEFAULT_GENERATION_REASON =
            "Generated from the candidate's interview-practice context."

        private const val MAXIMUM_SKILL_COUNT =
            4

        private val ALLOWED_QUESTION_ANGLES =
            setOf(
                "OVERVIEW",
                "OWNERSHIP",
                "CHALLENGE",
                "DECISION",
                "TRADE_OFF",
                "FAILURE",
                "IMPACT",
                "COLLABORATION",
                "TECHNICAL_DEPTH",
                "SCALABILITY",
                "SECURITY",
                "TESTING",
                "DEPLOYMENT",
                "LEARNING",
                "REFLECTION",
                "GENERAL"
            )

        private val ENUM_SEPARATOR_REGEX =
            Regex(
                "[^A-Z0-9]+"
            )

        private val WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )
    }
}