package com.julesmeister.sangfroid.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_revisions",
    foreignKeys = [
        ForeignKey(
            entity = PracticeAttemptEntity::class,
            parentColumns = ["id"],
            childColumns = ["attemptId"],
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(
            value = ["attemptId"]
        ),
        Index(
            value = ["questionId"]
        ),
        Index(
            value = [
                "questionId",
                "createdAt"
            ]
        ),
        Index(
            value = [
                "questionId",
                "versionNumber"
            ],
            unique = true
        )
    ]
)
data class SavedRevisionEntity(
    @PrimaryKey
    val id: String,
    val attemptId: String,
    val questionId: String,
    val content: String,
    val versionNumber: Int,
    val source: String,
    val createdAt: Long =
        System.currentTimeMillis()
)