package com.julesmeister.sangfroid.domain.cv

data class CvProfile(
    val requestId: String,
    val profileSummary: String,
    val suggestedRoles: List<String>,
    val suggestedExperienceLevel: String?,
    val skills: List<CvSkill>,
    val skillMappings: List<CvSkillMapping>,
    val certifications: List<CvCertification>,
    val projects: List<CvProject>,
    val achievements: List<CvAchievement>,
    val workHighlights: List<CvWorkHighlight>,
    val experienceSignals: List<String>,
    val modelName: String,
    val modelVersion: String?,
    val analyzedAt: Long
) {

    val mappedSkillCodes:
            Set<com.julesmeister.sangfroid.domain.model.SkillCode>
        get() =
            skillMappings
                .map {
                    it.skillCode
                }
                .toSet()

    val hasEvidence: Boolean
        get() =
            skills.isNotEmpty() ||
                    certifications.isNotEmpty() ||
                    projects.isNotEmpty() ||
                    achievements.isNotEmpty() ||
                    workHighlights.isNotEmpty() ||
                    experienceSignals.isNotEmpty()

    fun normalized(): CvProfile {
        return copy(
            requestId =
                requestId.trim(),
            profileSummary =
                profileSummary
                    .normalizeWhitespace(),
            suggestedRoles =
                suggestedRoles
                    .normalizeStringList(),
            suggestedExperienceLevel =
                suggestedExperienceLevel
                    ?.normalizeWhitespace()
                    ?.takeIf(
                        String::isNotBlank
                    ),
            skills =
                skills
                    .map(
                        CvSkill::normalized
                    )
                    .filter {
                        it.name.isNotBlank() &&
                                it.evidence.isNotBlank()
                    }
                    .distinctBy {
                        it.normalizedName
                    },
            skillMappings =
                skillMappings
                    .map(
                        CvSkillMapping::normalized
                    )
                    .distinctBy {
                        it.extractedSkillName
                            .lowercase() to
                                it.skillCode
                    },
            certifications =
                certifications
                    .map(
                        CvCertification::normalized
                    )
                    .distinctBy {
                        it.id
                    },
            projects =
                projects
                    .map(
                        CvProject::normalized
                    )
                    .distinctBy {
                        it.id
                    },
            achievements =
                achievements
                    .map(
                        CvAchievement::normalized
                    )
                    .distinctBy {
                        it.id
                    },
            workHighlights =
                workHighlights
                    .map(
                        CvWorkHighlight::normalized
                    )
                    .distinctBy {
                        it.id
                    },
            experienceSignals =
                experienceSignals
                    .normalizeStringList(),
            modelName =
                modelName
                    .normalizeWhitespace()
                    .ifBlank {
                        "Qwen"
                    },
            modelVersion =
                modelVersion
                    ?.normalizeWhitespace()
                    ?.takeIf(
                        String::isNotBlank
                    ),
            analyzedAt =
                analyzedAt.takeIf {
                    it > 0L
                } ?: System.currentTimeMillis()
        )
    }
}

internal fun String.normalizeWhitespace(): String {
    return replace(
        "\u0000",
        ""
    )
        .replace(
            Regex("\\s+"),
            " "
        )
        .trim()
}

internal fun List<String>.normalizeStringList(): List<String> {
    return map(
        String::normalizeWhitespace
    )
        .filter(
            String::isNotBlank
        )
        .distinctBy(
            String::lowercase
        )
}