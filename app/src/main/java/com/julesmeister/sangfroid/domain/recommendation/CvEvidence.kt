// app/src/main/java/com/julesmeister/sangfroid/domain/recommendation/CvEvidence.kt

package com.julesmeister.sangfroid.domain.recommendation

import java.util.Locale

data class CvEvidence(
    val id: String,
    val type: CvEvidenceType,
    val title: String,
    val context: String? = null,
    val candidateRole: String? = null,
    val actions: List<String> = emptyList(),
    val methods: List<String> = emptyList(),
    val tools: List<String> = emptyList(),
    val outcomes: List<String> = emptyList(),
    val metrics: List<String> = emptyList(),
    val relatedSkills: List<String> = emptyList(),
    val evidence: String? = null
) {

    init {
        require(
            id.isNotBlank()
        ) {
            "CV evidence ID cannot be blank."
        }

        require(
            title.isNotBlank()
        ) {
            "CV evidence title cannot be blank."
        }
    }

    fun normalized():
            CvEvidence {
        return copy(
            id =
                id
                    .trim()
                    .take(
                        MAXIMUM_ID_LENGTH
                    ),
            title =
                title
                    .trim()
                    .take(
                        MAXIMUM_TITLE_LENGTH
                    ),
            context =
                context.normalizeOptional(
                    MAXIMUM_CONTEXT_LENGTH
                ),
            candidateRole =
                candidateRole.normalizeOptional(
                    MAXIMUM_ROLE_LENGTH
                ),
            actions =
                actions.normalizeList(
                    maximumItems =
                        MAXIMUM_GENERAL_LIST_SIZE,
                    maximumLength =
                        MAXIMUM_DETAIL_LENGTH
                ),
            methods =
                methods.normalizeList(
                    maximumItems =
                        MAXIMUM_GENERAL_LIST_SIZE,
                    maximumLength =
                        MAXIMUM_SHORT_DETAIL_LENGTH
                ),
            tools =
                tools.normalizeList(
                    maximumItems =
                        MAXIMUM_GENERAL_LIST_SIZE,
                    maximumLength =
                        MAXIMUM_SHORT_DETAIL_LENGTH
                ),
            outcomes =
                outcomes.normalizeList(
                    maximumItems =
                        MAXIMUM_GENERAL_LIST_SIZE,
                    maximumLength =
                        MAXIMUM_DETAIL_LENGTH
                ),
            metrics =
                metrics.normalizeList(
                    maximumItems =
                        MAXIMUM_METRIC_LIST_SIZE,
                    maximumLength =
                        MAXIMUM_SHORT_DETAIL_LENGTH
                ),
            relatedSkills =
                relatedSkills.normalizeList(
                    maximumItems =
                        MAXIMUM_GENERAL_LIST_SIZE,
                    maximumLength =
                        MAXIMUM_SHORT_DETAIL_LENGTH
                ),
            evidence =
                evidence.normalizeOptional(
                    MAXIMUM_EVIDENCE_LENGTH
                )
        )
    }

    companion object {
        private const val MAXIMUM_ID_LENGTH =
            120

        private const val MAXIMUM_TITLE_LENGTH =
            180

        private const val MAXIMUM_CONTEXT_LENGTH =
            240

        private const val MAXIMUM_ROLE_LENGTH =
            160

        private const val MAXIMUM_DETAIL_LENGTH =
            240

        private const val MAXIMUM_SHORT_DETAIL_LENGTH =
            120

        private const val MAXIMUM_EVIDENCE_LENGTH =
            240

        private const val MAXIMUM_GENERAL_LIST_SIZE =
            8

        private const val MAXIMUM_METRIC_LIST_SIZE =
            6
    }
}

enum class CvEvidenceType {
    CERTIFICATION,
    PROJECT,
    ACHIEVEMENT,
    WORK_HIGHLIGHT,
    EDUCATION;

    companion object {

        fun fromName(
            value: String
        ): CvEvidenceType? {
            val normalized =
                value
                    .trim()
                    .uppercase(
                        Locale.ROOT
                    )
                    .replace(
                        Regex(
                            "[^A-Z0-9]+"
                        ),
                        "_"
                    )
                    .trim(
                        '_'
                    )

            return entries.firstOrNull { type ->
                type.name ==
                        normalized
            }
        }
    }
}

private fun String?.normalizeOptional(
    maximumLength: Int
): String? {
    return this
        ?.trim()
        ?.take(
            maximumLength
        )
        ?.takeIf(
            String::isNotBlank
        )
}

private fun List<String>.normalizeList(
    maximumItems: Int,
    maximumLength: Int
): List<String> {
    return asSequence()
        .map { value ->
            value
                .trim()
                .take(
                    maximumLength
                )
        }
        .filter(
            String::isNotBlank
        )
        .distinct()
        .take(
            maximumItems
        )
        .toList()
}