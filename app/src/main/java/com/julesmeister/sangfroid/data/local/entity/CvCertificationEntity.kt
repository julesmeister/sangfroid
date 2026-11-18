// app/src/main/java/com/julesmeister/sangfroid/data/local/entity/CvCertificationEntity.kt

package com.julesmeister.sangfroid.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "cv_certifications",
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
data class CvCertificationEntity(
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
        name = "issuer"
    )
    val issuer: String?,

    @ColumnInfo(
        name = "level"
    )
    val level: String?,

    @ColumnInfo(
        name = "status"
    )
    val status: String,

    @ColumnInfo(
        name = "issued_year"
    )
    val issuedYear: Int?,

    @ColumnInfo(
        name = "evidence"
    )
    val evidence: String,

    @ColumnInfo(
        name = "related_skills_json"
    )
    val relatedSkillsJson: String
)