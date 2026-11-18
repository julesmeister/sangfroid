package com.julesmeister.sangfroid.domain.cv

data class CvProject(
    val id: String,
    val name: String,
    val context: String?,
    val candidateRole: String?,
    val objective: String?,
    val actions: List<String>,
    val methods: List<String>,
    val tools: List<String>,
    val outcomes: List<String>,
    val metrics: List<String>,
    val relatedSkills: List<String>,
    val evidence: String
) {

    fun normalized(): CvProject {
        return copy(
            id =
                id
                    .normalizeWhitespace()
                    .take(
                        120
                    ),
            name =
                name
                    .normalizeWhitespace()
                    .take(
                        160
                    ),
            context =
                context.normalizedOptional(
                    200
                ),
            candidateRole =
                candidateRole.normalizedOptional(
                    150
                ),
            objective =
                objective.normalizedOptional(
                    220
                ),
            actions =
                actions
                    .normalizeStringList()
                    .map {
                        it.take(
                            180
                        )
                    }
                    .take(
                        8
                    ),
            methods =
                methods
                    .normalizeStringList()
                    .map {
                        it.take(
                            100
                        )
                    }
                    .take(
                        8
                    ),
            tools =
                tools
                    .normalizeStringList()
                    .map {
                        it.take(
                            100
                        )
                    }
                    .take(
                        8
                    ),
            outcomes =
                outcomes
                    .normalizeStringList()
                    .map {
                        it.take(
                            180
                        )
                    }
                    .take(
                        8
                    ),
            metrics =
                metrics
                    .normalizeStringList()
                    .map {
                        it.take(
                            120
                        )
                    }
                    .take(
                        6
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
                    ),
            evidence =
                evidence
                    .normalizeWhitespace()
                    .take(
                        240
                    )
        )
    }
}

internal fun String?.normalizedOptional(
    maximumLength: Int
): String? {
    return this
        ?.normalizeWhitespace()
        ?.take(
            maximumLength
        )
        ?.takeIf(
            String::isNotBlank
        )
}