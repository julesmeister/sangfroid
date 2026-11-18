package com.julesmeister.sangfroid.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "cv_profiles"
)
data class CvProfileEntity(
    @PrimaryKey
    @ColumnInfo(
        name = "profile_id"
    )
    val profileId: Long = ACTIVE_PROFILE_ID,

    @ColumnInfo(
        name = "profile_summary"
    )
    val profileSummary: String,

    @ColumnInfo(
        name = "suggested_roles_json"
    )
    val suggestedRolesJson: String,

    @ColumnInfo(
        name = "suggested_experience_level"
    )
    val suggestedExperienceLevel: String?,

    @ColumnInfo(
        name = "experience_signals_json"
    )
    val experienceSignalsJson: String,

    @ColumnInfo(
        name = "model_name"
    )
    val modelName: String,

    @ColumnInfo(
        name = "model_version"
    )
    val modelVersion: String?,

    @ColumnInfo(
        name = "analyzed_at"
    )
    val analyzedAt: Long,

    @ColumnInfo(
        name = "updated_at"
    )
    val updatedAt: Long
) {

    companion object {
        const val ACTIVE_PROFILE_ID =
            1L
    }
}