package com.julesmeister.sangfroid.data.repository

import com.julesmeister.sangfroid.data.local.dao.ProfileDao
import com.julesmeister.sangfroid.data.local.entity.CandidateProfileEntity
import com.julesmeister.sangfroid.data.local.entity.CustomProfileSkillEntity
import com.julesmeister.sangfroid.data.local.entity.ProfileSkillEntity
import com.julesmeister.sangfroid.data.mapper.ProfileMapper
import com.julesmeister.sangfroid.domain.model.SkillCode
import com.julesmeister.sangfroid.domain.profile.CandidateProfile
import com.julesmeister.sangfroid.domain.profile.CustomSkill
import com.julesmeister.sangfroid.domain.profile.ProfileSkillSource
import com.julesmeister.sangfroid.domain.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class OfflineProfileRepository @Inject constructor(
    private val profileDao: ProfileDao,
    private val profileMapper: ProfileMapper
) : ProfileRepository {

    override fun observeProfile(): Flow<CandidateProfile?> {
        return profileDao
            .observeProfile(CandidateProfile.DEFAULT_PROFILE_ID)
            .map { stored -> stored?.let(profileMapper::toDomain) }
    }

    override fun observeCustomSkills(): Flow<List<CustomSkill>> {
        return profileDao
            .observeCustomSkills(CandidateProfile.DEFAULT_PROFILE_ID)
            .map { entities ->
                entities.map { entity ->
                    CustomSkill(
                        name = entity.name,
                        source = entity.source
                    )
                }
            }
    }

    override suspend fun getProfile(): CandidateProfile? {
        return profileDao
            .getProfile(CandidateProfile.DEFAULT_PROFILE_ID)
            ?.let(profileMapper::toDomain)
    }

    override suspend fun saveProfile(
        profile: CandidateProfile
    ) {
        val normalized = profile.copy(
            id = CandidateProfile.DEFAULT_PROFILE_ID,
            updatedAt = System.currentTimeMillis()
        ).normalized()

        profileDao.replaceProfile(
            profile = profileMapper.toProfileEntity(normalized),
            skills = profileMapper.toSkillEntities(normalized)
        )
    }

    override suspend fun saveProfileSnapshot(
        targetRole: String,
        experienceLevel: String,
        skills: Map<SkillCode, ProfileSkillSource>,
        customSkills: List<CustomSkill>
    ) {
        val profileId = CandidateProfile.DEFAULT_PROFILE_ID

        val profileEntity = CandidateProfileEntity(
            id = profileId,
            targetRole = targetRole.trim(),
            experienceLevel = experienceLevel.trim(),
            updatedAt = System.currentTimeMillis()
        )

        val skillEntities = skills.map { (code, source) ->
            ProfileSkillEntity(
                profileId = profileId,
                skillCode = code,
                source = source
            )
        }

        val customEntities = customSkills
            .mapNotNull { skill ->
                val name = CustomSkill.clean(skill.name)

                if (name.isBlank()) {
                    null
                } else {
                    CustomProfileSkillEntity(
                        profileId = profileId,
                        normalizedName = CustomSkill.normalize(name),
                        name = name,
                        source = skill.source
                    )
                }
            }
            .distinctBy { it.normalizedName }

        profileDao.replaceProfileSnapshot(
            profile = profileEntity,
            skills = skillEntities,
            customSkills = customEntities
        )
    }

    override suspend fun updateProfileDetails(
        targetRole: String,
        experienceLevel: String
    ) {
        val current = getProfile() ?: emptyProfile()

        saveProfile(
            current.copy(
                targetRole = targetRole.trim(),
                experienceLevel = experienceLevel.trim()
            )
        )
    }

    override suspend fun replaceCvSkills(
        skills: List<SkillCode>
    ) {
        replaceSkills(
            source = ProfileSkillSource.CV,
            skills = skills
        )
    }

    override suspend fun replaceManualSkills(
        skills: List<SkillCode>
    ) {
        replaceSkills(
            source = ProfileSkillSource.MANUAL,
            skills = skills
        )
    }

    override suspend fun clearProfile() {
        profileDao.deleteProfile(CandidateProfile.DEFAULT_PROFILE_ID)
    }

    private suspend fun replaceSkills(
        source: ProfileSkillSource,
        skills: List<SkillCode>
    ) {
        val current = getProfile() ?: emptyProfile()

        val profileEntity = CandidateProfileEntity(
            id = CandidateProfile.DEFAULT_PROFILE_ID,
            targetRole = current.targetRole,
            experienceLevel = current.experienceLevel,
            updatedAt = System.currentTimeMillis()
        )

        val skillEntities = skills
            .distinct()
            .map {
                ProfileSkillEntity(
                    profileId = CandidateProfile.DEFAULT_PROFILE_ID,
                    skillCode = it,
                    source = source
                )
            }

        profileDao.replaceSkillsBySource(
            profile = profileEntity,
            source = source,
            skills = skillEntities
        )
    }

    private fun emptyProfile(): CandidateProfile {
        return CandidateProfile(
            id = CandidateProfile.DEFAULT_PROFILE_ID,
            skills = emptyList()
        )
    }
}