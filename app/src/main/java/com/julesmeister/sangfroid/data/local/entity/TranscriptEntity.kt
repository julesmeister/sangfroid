package com.julesmeister.sangfroid.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transcripts",
    foreignKeys = [
        ForeignKey(
            entity = PracticeAttemptEntity::class,
            parentColumns = ["id"],
            childColumns = ["attemptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["attemptId"], unique = true)
    ]
)
data class TranscriptEntity(
    @PrimaryKey
    val id: String,
    val attemptId: String,
    val originalTranscript: String,
    val editedTranscript: String,
    val languageTag: String,
    val speechModelName: String,
    val speechModelVersion: String?,
    val audioFilePath: String?,
    val createdAt: Long = System.currentTimeMillis()
)