package com.julesmeister.sangfroid.domain.repository

import com.julesmeister.sangfroid.domain.model.SkillCode
import com.julesmeister.sangfroid.domain.profile.CandidateProfile
import com.julesmeister.sangfroid.domain.profile.CustomSkill
import com.julesmeister.sangfroid.domain.profile.ProfileSkillSource
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {

    fun observeProfile(): Flow<CandidateProfile?>

    fun observeCustomSkills(): Flow<List<CustomSkill>>

    suspend fun getProfile(): CandidateProfile?

    suspend fun saveProfile(
        profile: CandidateProfile
    )

    /** Saves role, level, catalogue skills and custom skills in one transaction. */
    suspend fun saveProfileSnapshot(
        targetRole: String,
        experienceLevel: String,
        skills: Map<SkillCode, ProfileSkillSource>,
        customSkills: List<CustomSkill>
    )

    suspend fun updateProfileDetails(
        targetRole: String,
        experienceLevel: String
    )

    suspend fun replaceCvSkills(
        skills: List<SkillCode>
    )

    suspend fun replaceManualSkills(
        skills: List<SkillCode>
    )

    suspend fun clearProfile()
}