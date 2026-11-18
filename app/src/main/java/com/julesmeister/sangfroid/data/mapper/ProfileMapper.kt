package com.julesmeister.sangfroid.data.mapper

import com.julesmeister.sangfroid.data.local.entity.CandidateProfileEntity
import com.julesmeister.sangfroid.data.local.entity.ProfileSkillEntity
import com.julesmeister.sangfroid.data.local.model.StoredCandidateProfile
import com.julesmeister.sangfroid.domain.profile.CandidateProfile
import com.julesmeister.sangfroid.domain.profile.ProfileSkill
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileMapper @Inject constructor() {

    fun toDomain(
        stored: StoredCandidateProfile
    ): CandidateProfile {
        return CandidateProfile(
            id = stored.profile.id,
            targetRole =
                stored.profile.targetRole,
            experienceLevel =
                stored.profile.experienceLevel,
            skills = stored.skills
                .map {
                    ProfileSkill(
                        skillCode =
                            it.skillCode,
                        source =
                            it.source
                    )
                }
                .sortedWith(
                    compareBy(
                        { it.source.name },
                        { it.skillCode.name }
                    )
                ),
            updatedAt =
                stored.profile.updatedAt
        )
    }

    fun toProfileEntity(
        profile: CandidateProfile
    ): CandidateProfileEntity {
        val normalized =
            profile.normalized()

        return CandidateProfileEntity(
            id = normalized.id,
            targetRole =
                normalized.targetRole,
            experienceLevel =
                normalized.experienceLevel,
            updatedAt =
                normalized.updatedAt
        )
    }

    fun toSkillEntities(
        profile: CandidateProfile
    ): List<ProfileSkillEntity> {
        val normalized =
            profile.normalized()

        return normalized.skills.map {
            ProfileSkillEntity(
                profileId =
                    normalized.id,
                skillCode =
                    it.skillCode,
                source =
                    it.source
            )
        }
    }
}