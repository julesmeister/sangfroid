// app/src/main/java/com/julesmeister/sangfroid/data/local/entity/CvWorkHighlightEntity.kt

package com.julesmeister.sangfroid.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "cv_work_highlights",
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
data class CvWorkHighlightEntity(
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
        name = "organization_context"
    )
    val organizationContext: String?,

    @ColumnInfo(
        name = "candidate_role"
    )
    val candidateRole: String?,

    @ColumnInfo(
        name = "responsibility"
    )
    val responsibility: String?,

    @ColumnInfo(
        name = "action"
    )
    val action: String?,

    @ColumnInfo(
        name = "outcome"
    )
    val outcome: String?,

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