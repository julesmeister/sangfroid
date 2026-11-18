package com.julesmeister.sangfroid.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cv_skill_mappings",
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
                "extracted_skill_name",
                "skill_code"
            ],
            unique = true
        )
    ]
)
data class CvSkillMappingEntity(
    @PrimaryKey(
        autoGenerate = true
    )
    @ColumnInfo(
        name = "mapping_id"
    )
    val mappingId: Long = 0L,

    @ColumnInfo(
        name = "profile_owner_id"
    )
    val profileOwnerId: Long,

    @ColumnInfo(
        name = "extracted_skill_name"
    )
    val extractedSkillName: String,

    @ColumnInfo(
        name = "skill_code"
    )
    val skillCode: String,

    @ColumnInfo(
        name = "confidence"
    )
    val confidence: Double,

    @ColumnInfo(
        name = "reason"
    )
    val reason: String
)