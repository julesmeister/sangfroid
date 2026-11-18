package com.julesmeister.sangfroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.julesmeister.sangfroid.data.local.entity.CandidateProfileEntity
import com.julesmeister.sangfroid.data.local.entity.CustomProfileSkillEntity
import com.julesmeister.sangfroid.data.local.entity.ProfileSkillEntity
import com.julesmeister.sangfroid.data.local.model.StoredCandidateProfile
import com.julesmeister.sangfroid.domain.profile.ProfileSkillSource
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Transaction
    @Query(
        """
        SELECT *
        FROM candidate_profiles
        WHERE id = :profileId
        LIMIT 1
        """
    )
    fun observeProfile(
        profileId: String
    ): Flow<StoredCandidateProfile?>

    @Transaction
    @Query(
        """
        SELECT *
        FROM candidate_profiles
        WHERE id = :profileId
        LIMIT 1
        """
    )
    suspend fun getProfile(
        profileId: String
    ): StoredCandidateProfile?

    @Query(
        """
        SELECT *
        FROM custom_profile_skills
        WHERE profileId = :profileId
        ORDER BY rowid
        """
    )
    fun observeCustomSkills(
        profileId: String
    ): Flow<List<CustomProfileSkillEntity>>

    /*
     * @Upsert updates the existing row in place.
     *
     * Do NOT use @Insert(onConflict = REPLACE) here: SQLite implements
     * REPLACE as DELETE + INSERT, and the DELETE cascades to
     * profile_skills and custom_profile_skills, wiping every skill.
     */
    @Upsert
    suspend fun upsertProfile(
        profile: CandidateProfileEntity
    )

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertSkills(
        skills: List<ProfileSkillEntity>
    )

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertCustomSkills(
        skills: List<CustomProfileSkillEntity>
    )

    @Query(
        """
        DELETE FROM profile_skills
        WHERE profileId = :profileId
        AND source = :source
        """
    )
    suspend fun deleteSkillsBySource(
        profileId: String,
        source: ProfileSkillSource
    )

    @Query(
        """
        DELETE FROM profile_skills
        WHERE profileId = :profileId
        """
    )
    suspend fun deleteAllSkills(
        profileId: String
    )

    @Query(
        """
        DELETE FROM custom_profile_skills
        WHERE profileId = :profileId
        """
    )
    suspend fun deleteAllCustomSkills(
        profileId: String
    )

    @Query(
        """
        DELETE FROM candidate_profiles
        WHERE id = :profileId
        """
    )
    suspend fun deleteProfile(
        profileId: String
    )

    @Transaction
    suspend fun replaceProfile(
        profile: CandidateProfileEntity,
        skills: List<ProfileSkillEntity>
    ) {
        upsertProfile(profile)
        deleteAllSkills(profile.id)

        if (skills.isNotEmpty()) {
            insertSkills(skills)
        }
    }

    @Transaction
    suspend fun replaceSkillsBySource(
        profile: CandidateProfileEntity,
        source: ProfileSkillSource,
        skills: List<ProfileSkillEntity>
    ) {
        upsertProfile(profile)

        deleteSkillsBySource(
            profileId = profile.id,
            source = source
        )

        if (skills.isNotEmpty()) {
            insertSkills(skills)
        }
    }

    /** Writes the whole editable profile atomically: one commit, one Flow emission. */
    @Transaction
    suspend fun replaceProfileSnapshot(
        profile: CandidateProfileEntity,
        skills: List<ProfileSkillEntity>,
        customSkills: List<CustomProfileSkillEntity>
    ) {
        upsertProfile(profile)

        deleteAllSkills(profile.id)
        deleteAllCustomSkills(profile.id)

        if (skills.isNotEmpty()) {
            insertSkills(skills)
        }

        if (customSkills.isNotEmpty()) {
            insertCustomSkills(customSkills)
        }
    }
}