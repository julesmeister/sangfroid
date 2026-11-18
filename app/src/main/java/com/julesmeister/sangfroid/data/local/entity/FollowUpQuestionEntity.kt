package com.julesmeister.sangfroid.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "follow_up_questions",
    foreignKeys = [
        ForeignKey(
            entity = AnswerEvaluationEntity::class,
            parentColumns = ["id"],
            childColumns = ["evaluationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["evaluationId"]),
        Index(value = ["displayOrder"])
    ]
)
data class FollowUpQuestionEntity(
    @PrimaryKey
    val id: String,
    val evaluationId: String,
    val question: String,
    val reason: String?,
    val displayOrder: Int
)