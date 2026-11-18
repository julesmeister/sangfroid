package com.julesmeister.sangfroid.domain.cv

data class CvWorkHighlight(
    val id: String,
    val title: String,
    val organizationContext: String?,
    val candidateRole: String?,
    val responsibility: String?,
    val action: String?,
    val outcome: String?,
    val metric: String?,
    val evidence: String,
    val relatedSkills: List<String>
) {

    fun normalized(): CvWorkHighlight {
        return copy(
            id =
                id
                    .normalizeWhitespace()
                    .take(
                        120
                    ),
            title =
                title
                    .normalizeWhitespace()
                    .take(
                        180
                    ),
            organizationContext =
                organizationContext.normalizedOptional(
                    180
                ),
            candidateRole =
                candidateRole.normalizedOptional(
                    150
                ),
            responsibility =
                responsibility.normalizedOptional(
                    220
                ),
            action =
                action.normalizedOptional(
                    220
                ),
            outcome =
                outcome.normalizedOptional(
                    220
                ),
            metric =
                metric.normalizedOptional(
                    120
                ),
            evidence =
                evidence
                    .normalizeWhitespace()
                    .take(
                        240
                    ),
            relatedSkills =
                relatedSkills
                    .normalizeStringList()
                    .map {
                        it.take(
                            100
                        )
                    }
                    .take(
                        8
                    )
        )
    }
}