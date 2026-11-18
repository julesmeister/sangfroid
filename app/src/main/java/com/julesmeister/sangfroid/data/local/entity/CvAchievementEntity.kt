// app/src/main/java/com/julesmeister/sangfroid/data/local/entity/CvAchievementEntity.kt

package com.julesmeister.sangfroid.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "cv_achievements",
    primaryKeys = [
        "profile_owner_id",
        "evidence_id"
    ],
    foreignKeys = [
        ForeignKey(
            entity =
                CvProfileEntity::class,
            parentColumns = [
                "profile_id"
            ],
            childColumns = [
                "profile_owner_id"
            ],
            onDelete =
                ForeignKey.CASCADE,
            onUpdate =
                ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = [
                "profile_owner_id"
            ]
        )
    ]
)
data class CvAchievementEntity(
    @ColumnInfo(
        name = "profile_owner_id"
    )
    val profileOwnerId: Long,

    @ColumnInfo(
        name = "evidence_id"
    )
    val evidenceId: String,

    @ColumnInfo(
        name = "title"
    )
    val title: String,

    @ColumnInfo(
        name = "context"
    )
    val context: String?,

    @ColumnInfo(
        name = "candidate_action"
    )
    val candidateAction: String?,

    @ColumnInfo(
        name = "result"
    )
    val result: String?,

    @ColumnInfo(
        name = "metric"
    )
    val metric: String?,

    @ColumnInfo(
        name = "evidence"
    )
    val evidence: String,

    @ColumnInfo(
        name = "related_skills_json"
    )
    val relatedSkillsJson: String
)