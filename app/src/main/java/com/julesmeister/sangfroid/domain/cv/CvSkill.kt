package com.julesmeister.sangfroid.domain.cv

data class CvSkill(
    val name: String,
    val normalizedName: String,
    val type: CvSkillType,
    val confidence: Double,
    val evidence: String
) {

    fun normalized(): CvSkill {
        val normalizedDisplayName =
            name
                .normalizeWhitespace()
                .take(
                    MAXIMUM_NAME_LENGTH
                )

        val canonicalName =
            normalizedName
                .normalizeWhitespace()
                .lowercase()
                .ifBlank {
                    normalizedDisplayName
                        .lowercase()
                }
                .take(
                    MAXIMUM_NAME_LENGTH
                )

        return copy(
            name =
                normalizedDisplayName,
            normalizedName =
                canonicalName,
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
            evidence =
                evidence
                    .normalizeWhitespace()
                    .take(
                        MAXIMUM_EVIDENCE_LENGTH
                    )
        )
    }

    companion object {
        private const val MAXIMUM_NAME_LENGTH =
            100

        private const val MAXIMUM_EVIDENCE_LENGTH =
            240
    }
}

enum class CvSkillType {
    DOMAIN,
    TECHNICAL,
    TOOL,
    PROFESSIONAL,
    BEHAVIORAL;

    companion object {

        fun fromApi(
            value: String
        ): CvSkillType {
            return entries.firstOrNull {
                it.name.equals(
                    other =
                        value.trim(),
                    ignoreCase =
                        true
                )
            } ?: PROFESSIONAL
        }
    }
}