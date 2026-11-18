// app/src/main/java/com/julesmeister/sangfroid/data/local/dao/CvProfileDao.kt

package com.julesmeister.sangfroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.julesmeister.sangfroid.data.local.entity.CvAchievementEntity
import com.julesmeister.sangfroid.data.local.entity.CvCertificationEntity
import com.julesmeister.sangfroid.data.local.entity.CvProfileEntity
import com.julesmeister.sangfroid.data.local.entity.CvProjectEntity
import com.julesmeister.sangfroid.data.local.entity.CvSkillEntity
import com.julesmeister.sangfroid.data.local.entity.CvSkillMappingEntity
import com.julesmeister.sangfroid.data.local.entity.CvWorkHighlightEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CvProfileDao {

    @Query(
        """
        SELECT *
        FROM cv_profiles
        WHERE profile_id = :profileId
        LIMIT 1
        """
    )
    abstract fun observeProfile(
        profileId: Long =
            CvProfileEntity.ACTIVE_PROFILE_ID
    ): Flow<CvProfileEntity?>

    @Query(
        """
        SELECT *
        FROM cv_profiles
        WHERE profile_id = :profileId
        LIMIT 1
        """
    )
    abstract suspend fun getProfile(
        profileId: Long =
            CvProfileEntity.ACTIVE_PROFILE_ID
    ): CvProfileEntity?

    @Query(
        """
        SELECT *
        FROM cv_skills
        WHERE profile_owner_id = :profileId
        ORDER BY confidence DESC, name ASC
        """
    )
    abstract suspend fun getSkills(
        profileId: Long =
            CvProfileEntity.ACTIVE_PROFILE_ID
    ): List<CvSkillEntity>

    @Query(
        """
        SELECT *
        FROM cv_skill_mappings
        WHERE profile_owner_id = :profileId
        ORDER BY confidence DESC, skill_code ASC
        """
    )
    abstract suspend fun getSkillMappings(
        profileId: Long =
            CvProfileEntity.ACTIVE_PROFILE_ID
    ): List<CvSkillMappingEntity>

    @Query(
        """
        SELECT *
        FROM cv_certifications
        WHERE profile_owner_id = :profileId
        ORDER BY name ASC
        """
    )
    abstract suspend fun getCertifications(
        profileId: Long =
            CvProfileEntity.ACTIVE_PROFILE_ID
    ): List<CvCertificationEntity>

    @Query(
        """
        SELECT *
        FROM cv_projects
        WHERE profile_owner_id = :profileId
        ORDER BY name ASC
        """
    )
    abstract suspend fun getProjects(
        profileId: Long =
            CvProfileEntity.ACTIVE_PROFILE_ID
    ): List<CvProjectEntity>

    @Query(
        """
        SELECT *
        FROM cv_achievements
        WHERE profile_owner_id = :profileId
        ORDER BY title ASC
        """
    )
    abstract suspend fun getAchievements(
        profileId: Long =
            CvProfileEntity.ACTIVE_PROFILE_ID
    ): List<CvAchievementEntity>

    @Query(
        """
        SELECT *
        FROM cv_work_highlights
        WHERE profile_owner_id = :profileId
        ORDER BY title ASC
        """
    )
    abstract suspend fun getWorkHighlights(
        profileId: Long =
            CvProfileEntity.ACTIVE_PROFILE_ID
    ): List<CvWorkHighlightEntity>

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    protected abstract suspend fun insertProfile(
        profile: CvProfileEntity
    )

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    protected abstract suspend fun insertSkills(
        skills: List<CvSkillEntity>
    )

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    protected abstract suspend fun insertSkillMappings(
        mappings: List<CvSkillMappingEntity>
    )

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    protected abstract suspend fun insertCertifications(
        certifications: List<CvCertificationEntity>
    )

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    protected abstract suspend fun insertProjects(
        projects: List<CvProjectEntity>
    )

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    protected abstract suspend fun insertAchievements(
        achievements: List<CvAchievementEntity>
    )

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    protected abstract suspend fun insertWorkHighlights(
        workHighlights: List<CvWorkHighlightEntity>
    )

    @Query(
        """
        DELETE FROM cv_skills
        WHERE profile_owner_id = :profileId
        """
    )
    protected abstract suspend fun deleteSkills(
        profileId: Long
    )

    @Query(
        """
        DELETE FROM cv_skill_mappings
        WHERE profile_owner_id = :profileId
        """
    )
    protected abstract suspend fun deleteSkillMappings(
        profileId: Long
    )

    @Query(
        """
        DELETE FROM cv_certifications
        WHERE profile_owner_id = :profileId
        """
    )
    protected abstract suspend fun deleteCertifications(
        profileId: Long
    )

    @Query(
        """
        DELETE FROM cv_projects
        WHERE profile_owner_id = :profileId
        """
    )
    protected abstract suspend fun deleteProjects(
        profileId: Long
    )

    @Query(
        """
        DELETE FROM cv_achievements
        WHERE profile_owner_id = :profileId
        """
    )
    protected abstract suspend fun deleteAchievements(
        profileId: Long
    )

    @Query(
        """
        DELETE FROM cv_work_highlights
        WHERE profile_owner_id = :profileId
        """
    )
    protected abstract suspend fun deleteWorkHighlights(
        profileId: Long
    )

    @Query(
        """
        DELETE FROM cv_profiles
        WHERE profile_id = :profileId
        """
    )
    protected abstract suspend fun deleteProfile(
        profileId: Long
    )

    @Transaction
    open suspend fun replaceProfile(
        profile: CvProfileEntity,
        skills: List<CvSkillEntity>,
        mappings: List<CvSkillMappingEntity>,
        certifications: List<CvCertificationEntity>,
        projects: List<CvProjectEntity>,
        achievements: List<CvAchievementEntity>,
        workHighlights: List<CvWorkHighlightEntity>
    ) {
        val profileId =
            profile.profileId

        deleteSkillMappings(
            profileId
        )

        deleteSkills(
            profileId
        )

        deleteCertifications(
            profileId
        )

        deleteProjects(
            profileId
        )

        deleteAchievements(
            profileId
        )

        deleteWorkHighlights(
            profileId
        )

        insertProfile(
            profile
        )

        if (skills.isNotEmpty()) {
            insertSkills(
                skills
            )
        }

        if (mappings.isNotEmpty()) {
            insertSkillMappings(
                mappings
            )
        }

        if (certifications.isNotEmpty()) {
            insertCertifications(
                certifications
            )
        }

        if (projects.isNotEmpty()) {
            insertProjects(
                projects
            )
        }

        if (achievements.isNotEmpty()) {
            insertAchievements(
                achievements
            )
        }

        if (workHighlights.isNotEmpty()) {
            insertWorkHighlights(
                workHighlights
            )
        }
    }

    @Transaction
    open suspend fun getCompleteProfile(
        profileId: Long =
            CvProfileEntity.ACTIVE_PROFILE_ID
    ): StoredCvProfile? {
        val profile =
            getProfile(
                profileId
            )
                ?: return null

        return StoredCvProfile(
            profile =
                profile,
            skills =
                getSkills(
                    profileId
                ),
            mappings =
                getSkillMappings(
                    profileId
                ),
            certifications =
                getCertifications(
                    profileId
                ),
            projects =
                getProjects(
                    profileId
                ),
            achievements =
                getAchievements(
                    profileId
                ),
            workHighlights =
                getWorkHighlights(
                    profileId
                )
        )
    }

    @Transaction
    open suspend fun clearProfile(
        profileId: Long =
            CvProfileEntity.ACTIVE_PROFILE_ID
    ) {
        deleteSkillMappings(
            profileId
        )

        deleteSkills(
            profileId
        )

        deleteCertifications(
            profileId
        )

        deleteProjects(
            profileId
        )

        deleteAchievements(
            profileId
        )

        deleteWorkHighlights(
            profileId
        )

        deleteProfile(
            profileId
        )
    }
}

data class StoredCvProfile(
    val profile: CvProfileEntity,
    val skills: List<CvSkillEntity>,
    val mappings: List<CvSkillMappingEntity>,
    val certifications: List<CvCertificationEntity>,
    val projects: List<CvProjectEntity>,
    val achievements: List<CvAchievementEntity>,
    val workHighlights: List<CvWorkHighlightEntity>
)