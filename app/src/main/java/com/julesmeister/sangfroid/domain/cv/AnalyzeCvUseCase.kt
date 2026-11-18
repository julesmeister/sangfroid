package com.julesmeister.sangfroid.domain.cv

import com.julesmeister.sangfroid.domain.model.SkillCode
import javax.inject.Inject

class AnalyzeCvUseCase @Inject constructor(
    private val repository:
    CvAnalysisRepository
) {

    suspend operator fun invoke(
        cvText: String,
        currentTargetRole: String?,
        allowedSkillCodes: Set<SkillCode> =
            SkillCode.entries.toSet()
    ): Result<CvProfile> {
        val normalizedText =
            normalizeCvText(
                cvText
            )

        if (
            normalizedText.length !in
            MINIMUM_CV_TEXT_LENGTH..
            MAXIMUM_CV_TEXT_LENGTH
        ) {
            return Result.failure(
                IllegalArgumentException(
                    "CV text must contain between " +
                            "$MINIMUM_CV_TEXT_LENGTH and " +
                            "$MAXIMUM_CV_TEXT_LENGTH characters."
                )
            )
        }

        val normalizedTargetRole =
            currentTargetRole
                ?.replace(
                    "\u0000",
                    ""
                )
                ?.replace(
                    WHITESPACE_REGEX,
                    " "
                )
                ?.trim()
                ?.take(
                    MAXIMUM_TARGET_ROLE_LENGTH
                )
                ?.takeIf(
                    String::isNotBlank
                )

        val normalizedSkillCodes =
            allowedSkillCodes
                .ifEmpty {
                    SkillCode.entries.toSet()
                }

        if (normalizedSkillCodes.isEmpty()) {
            return Result.failure(
                IllegalArgumentException(
                    "At least one Sangfroid skill code is required for CV analysis."
                )
            )
        }

        return repository.analyze(
            cvText =
                normalizedText,
            currentTargetRole =
                normalizedTargetRole,
            allowedSkillCodes =
                normalizedSkillCodes
        )
    }

    private fun normalizeCvText(
        value: String
    ): String {
        return value
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
            .take(
                MAXIMUM_CV_TEXT_LENGTH
            )
    }

    companion object {
        private const val MINIMUM_CV_TEXT_LENGTH =
            80

        private const val MAXIMUM_CV_TEXT_LENGTH =
            30_000

        private const val MAXIMUM_TARGET_ROLE_LENGTH =
            150

        private val WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )

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