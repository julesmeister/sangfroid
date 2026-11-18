package com.julesmeister.sangfroid.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cv_skills",
    foreignKeys = [
        ForeignKey(
            entity = CvProfileEntity::class,
            parentColumns = [
                "profile_id"
            ],
            childColumns = [
                "profile_owner_id"
            ],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = [
                "profile_owner_id"
            ]
        ),
        Index(
            value = [
                "profile_owner_id",
                "normalized_name"
            ],
            unique = true
        )
    ]
)
data class CvSkillEntity(
    @PrimaryKey(
        autoGenerate = true
    )
    @ColumnInfo(
        name = "skill_id"
    )
    val skillId: Long = 0L,

    @ColumnInfo(
        name = "profile_owner_id"
    )
    val profileOwnerId: Long,

    @ColumnInfo(
        name = "name"
    )
    val name: String,

    @ColumnInfo(
        name = "normalized_name"
    )
    val normalizedName: String,

    @ColumnInfo(
        name = "skill_type"
    )
    val type: String,

    @ColumnInfo(
        name = "confidence"
    )
    val confidence: Double,

    @ColumnInfo(
        name = "evidence"
    )
    val evidence: String
)