// app/src/main/java/com/julesmeister/sangfroid/data/local/entity/CvProjectEntity.kt

package com.julesmeister.sangfroid.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "cv_projects",
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
data class CvProjectEntity(
    @ColumnInfo(
        name = "profile_owner_id"
    )
    val profileOwnerId: Long,

    @ColumnInfo(
        name = "evidence_id"
    )
    val evidenceId: String,

    @ColumnInfo(
        name = "name"
    )
    val name: String,

    @ColumnInfo(
        name = "context"
    )
    val context: String?,

    @ColumnInfo(
        name = "candidate_role"
    )
    val candidateRole: String?,

    @ColumnInfo(
        name = "objective"
    )
    val objective: String?,

    @ColumnInfo(
        name = "actions_json"
    )
    val actionsJson: String,

    @ColumnInfo(
        name = "methods_json"
    )
    val methodsJson: String,

    @ColumnInfo(
        name = "tools_json"
    )
    val toolsJson: String,

    @ColumnInfo(
        name = "outcomes_json"
    )
    val outcomesJson: String,

    @ColumnInfo(
        name = "metrics_json"
    )
    val metricsJson: String,

    @ColumnInfo(
        name = "related_skills_json"
    )
    val relatedSkillsJson: String,

    @ColumnInfo(
        name = "evidence"
    )
    val evidence: String
)