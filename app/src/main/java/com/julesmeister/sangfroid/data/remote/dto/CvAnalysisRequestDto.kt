package com.julesmeister.sangfroid.data.remote.dto

import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

data class CvAnalysisRequestDto(
    val requestId: String,
    val cvText: String,
    val currentTargetRole: String? = null,
    val allowedSkillCodes: List<String>
) {

    fun toJson(): JSONObject {
        val normalizedRequestId =
            requestId.trim()

        val normalizedCvText =
            cvText
                .replace(
                    "\u0000",
                    ""
                )
                .replace(
                    HORIZONTAL_WHITESPACE_REGEX,
                    " "
                )
                .replace(
                    LINE_SPACING_REGEX,
                    "\n"
                )
                .replace(
                    EXCESSIVE_NEWLINES_REGEX,
                    "\n\n"
                )
                .trim()

        val normalizedCurrentTargetRole =
            currentTargetRole
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }

        val normalizedAllowedSkillCodes =
            allowedSkillCodes
                .map { skillCode ->
                    skillCode
                        .trim()
                        .uppercase(
                            Locale.ROOT
                        )
                }
                .filter { skillCode ->
                    skillCode.isNotBlank()
                }
                .distinct()
                .take(
                    MAXIMUM_SKILL_CODE_COUNT
                )

        require(
            normalizedRequestId.isNotBlank()
        ) {
            "requestId is required."
        }

        require(
            normalizedRequestId.length <=
                    MAXIMUM_REQUEST_ID_LENGTH
        ) {
            "requestId cannot exceed 100 characters."
        }

        require(
            normalizedCvText.length in
                    MINIMUM_CV_TEXT_LENGTH..
                    MAXIMUM_CV_TEXT_LENGTH
        ) {
            "cvText must contain between 80 and 30000 characters."
        }

        require(
            normalizedCurrentTargetRole == null ||
                    normalizedCurrentTargetRole.length <=
                    MAXIMUM_TARGET_ROLE_LENGTH
        ) {
            "currentTargetRole cannot exceed 150 characters."
        }

        require(
            normalizedAllowedSkillCodes.isNotEmpty()
        ) {
            "allowedSkillCodes cannot be empty."
        }

        return JSONObject().apply {
            put(
                JSON_REQUEST_ID,
                normalizedRequestId
            )

            put(
                JSON_CV_TEXT,
                normalizedCvText
            )

            if (
                normalizedCurrentTargetRole == null
            ) {
                put(
                    JSON_CURRENT_TARGET_ROLE,
                    JSONObject.NULL
                )
            } else {
                put(
                    JSON_CURRENT_TARGET_ROLE,
                    normalizedCurrentTargetRole
                )
            }

            put(
                JSON_ALLOWED_SKILL_CODES,
                JSONArray(
                    normalizedAllowedSkillCodes
                )
            )
        }
    }

    companion object {
        private const val JSON_REQUEST_ID =
            "requestId"

        private const val JSON_CV_TEXT =
            "cvText"

        private const val JSON_CURRENT_TARGET_ROLE =
            "currentTargetRole"

        private const val JSON_ALLOWED_SKILL_CODES =
            "allowedSkillCodes"

        private const val MAXIMUM_REQUEST_ID_LENGTH =
            100

        private const val MINIMUM_CV_TEXT_LENGTH =
            80

        private const val MAXIMUM_CV_TEXT_LENGTH =
            30_000

        private const val MAXIMUM_TARGET_ROLE_LENGTH =
            150

        private const val MAXIMUM_SKILL_CODE_COUNT =
            100

        private val HORIZONTAL_WHITESPACE_REGEX =
            Regex(
                "[\\t\\x0B\\f\\r ]+"
            )

        private val LINE_SPACING_REGEX =
            Regex(
                " *\\n *"
            )

        private val EXCESSIVE_NEWLINES_REGEX =
            Regex(
                "\\n{3,}"
            )
    }
}