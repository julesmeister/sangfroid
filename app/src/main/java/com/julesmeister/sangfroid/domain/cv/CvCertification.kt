package com.julesmeister.sangfroid.domain.cv

data class CvCertification(
    val id: String,
    val name: String,
    val issuer: String?,
    val level: String?,
    val status: CvCertificationStatus,
    val issuedYear: Int?,
    val evidence: String,
    val relatedSkills: List<String>
) {

    fun normalized(): CvCertification {
        return copy(
            id =
                id
                    .normalizeWhitespace()
                    .take(
                        MAXIMUM_ID_LENGTH
                    ),
            name =
                name
                    .normalizeWhitespace()
                    .take(
                        MAXIMUM_NAME_LENGTH
                    ),
            issuer =
                issuer
                    ?.normalizeWhitespace()
                    ?.take(
                        MAXIMUM_ISSUER_LENGTH
                    )
                    ?.takeIf(
                        String::isNotBlank
                    ),
            level =
                level
                    ?.normalizeWhitespace()
                    ?.take(
                        MAXIMUM_LEVEL_LENGTH
                    )
                    ?.takeIf(
                        String::isNotBlank
                    ),
            issuedYear =
                issuedYear
                    ?.takeIf {
                        it in
                                MINIMUM_YEAR..
                                MAXIMUM_YEAR
                    },
            evidence =
                evidence
                    .normalizeWhitespace()
                    .take(
                        MAXIMUM_EVIDENCE_LENGTH
                    ),
            relatedSkills =
                relatedSkills
                    .normalizeStringList()
                    .take(
                        MAXIMUM_RELATED_SKILLS
                    )
        )
    }

    companion object {
        private const val MAXIMUM_ID_LENGTH =
            120

        private const val MAXIMUM_NAME_LENGTH =
            150

        private const val MAXIMUM_ISSUER_LENGTH =
            150

        private const val MAXIMUM_LEVEL_LENGTH =
            100

        private const val MAXIMUM_EVIDENCE_LENGTH =
            240

        private const val MAXIMUM_RELATED_SKILLS =
            8

        private const val MINIMUM_YEAR =
            1900

        private const val MAXIMUM_YEAR =
            2200
    }
}

enum class CvCertificationStatus {
    COMPLETED,
    IN_PROGRESS,
    EXPIRED,
    UNKNOWN;

    companion object {

        fun fromApi(
            value: String
        ): CvCertificationStatus {
            return entries.firstOrNull {
                it.name.equals(
                    other =
                        value.trim(),
                    ignoreCase =
                        true
                )
            } ?: UNKNOWN
        }
    }
}