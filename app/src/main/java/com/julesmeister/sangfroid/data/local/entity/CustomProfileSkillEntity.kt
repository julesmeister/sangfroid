package com.julesmeister.sangfroid.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.julesmeister.sangfroid.domain.profile.ProfileSkillSource

@Entity(
    tableName = "custom_profile_skills",
    primaryKeys = [
        "profileId",
        "normalizedName"
    ],
    foreignKeys = [
        ForeignKey(
            entity = CandidateProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profileId"])
    ]
)
data class CustomProfileSkillEntity(
    val profileId: String,
    val normalizedName: String,
    val name: String,
    val source: ProfileSkillSource
)