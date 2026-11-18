package com.julesmeister.sangfroid.domain.cv

import com.julesmeister.sangfroid.domain.model.SkillCode

data class CvSkillMapping(
    val extractedSkillName: String,
    val skillCode: SkillCode,
    val confidence: Double,
    val reason: String
) {

    fun normalized(): CvSkillMapping {
        return copy(
            extractedSkillName =
                extractedSkillName
                    .normalizeWhitespace()
                    .take(
                        MAXIMUM_SKILL_NAME_LENGTH
                    ),
            confidence =
                confidence
                    .takeIf {
                        it.isFinite()
                    }
                    ?.coerceIn(
                        0.0,
                        1.0
                    )
                    ?: 0.0,
            reason =
                reason
                    .normalizeWhitespace()
                    .take(
                        MAXIMUM_REASON_LENGTH
                    )
        )
    }

    companion object {
        private const val MAXIMUM_SKILL_NAME_LENGTH =
            100

        private const val MAXIMUM_REASON_LENGTH =
            240
    }
}