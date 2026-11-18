package com.julesmeister.sangfroid.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rubric_scores",
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
        Index(value = ["criterion"])
    ]
)
data class RubricScoreEntity(
    @PrimaryKey
    val id: String,
    val evaluationId: String,
    val criterion: String,
    val score: Int,
    val maximumScore: Int = 5,
    val explanation: String
)