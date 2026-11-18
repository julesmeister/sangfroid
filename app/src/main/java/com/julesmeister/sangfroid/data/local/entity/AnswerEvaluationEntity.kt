package com.julesmeister.sangfroid.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "answer_evaluations",
    foreignKeys = [
        ForeignKey(
            entity = PracticeAttemptEntity::class,
            parentColumns = ["id"],
            childColumns = ["attemptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["attemptId"], unique = true),
        Index(value = ["createdAt"])
    ]
)
data class AnswerEvaluationEntity(
    @PrimaryKey
    val id: String,
    val attemptId: String,
    val summary: String,
    val strengths: String,
    val missingPoints: String,
    val suggestions: String,
    val improvedOutline: String,
    val improvedAnswerExample: String?,
    val modelName: String,
    val modelVersion: String?,
    val processingMode: String,
    val createdAt: Long = System.currentTimeMillis()
)