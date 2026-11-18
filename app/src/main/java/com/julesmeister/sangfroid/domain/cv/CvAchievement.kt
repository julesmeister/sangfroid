package com.julesmeister.sangfroid.domain.cv

data class CvAchievement(
    val id: String,
    val title: String,
    val context: String?,
    val candidateAction: String?,
    val result: String?,
    val metric: String?,
    val evidence: String,
    val relatedSkills: List<String>
) {

    fun normalized(): CvAchievement {
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
            context =
                context.normalizedOptional(
                    200
                ),
            candidateAction =
                candidateAction.normalizedOptional(
                    220
                ),
            result =
                result.normalizedOptional(
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