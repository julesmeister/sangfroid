package com.julesmeister.sangfroid.domain.profile

import com.julesmeister.sangfroid.domain.model.SkillCode

data class CandidateProfile(
    val id: String = DEFAULT_PROFILE_ID,
    val targetRole: String = "",
    val experienceLevel: String = "",
    val skills: List<ProfileSkill> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
) {

    val cvSkills: List<SkillCode>
        get() = skills
            .filter {
                it.source == ProfileSkillSource.CV
            }
            .map {
                it.skillCode
            }
            .distinct()

    val manualSkills: List<SkillCode>
        get() = skills
            .filter {
                it.source == ProfileSkillSource.MANUAL
            }
            .map {
                it.skillCode
            }
            .distinct()

    val recommendationSkills: List<SkillCode>
        get() = skills
            .map {
                it.skillCode
            }
            .distinct()

    fun normalized(): CandidateProfile {
        return copy(
            id = id.trim().ifBlank {
                DEFAULT_PROFILE_ID
            },
            targetRole = targetRole.trim(),
            experienceLevel =
                experienceLevel.trim(),
            skills = skills
                .distinctBy {
                    it.skillCode to it.source
                },
            updatedAt = updatedAt.takeIf {
                it > 0L
            } ?: System.currentTimeMillis()
        )
    }

    companion object {
        const val DEFAULT_PROFILE_ID =
            "local-candidate-profile"
    }
}

data class ProfileSkill(
    val skillCode: SkillCode,
    val source: ProfileSkillSource
)

enum class ProfileSkillSource {
    CV,
    MANUAL
}