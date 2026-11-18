package com.julesmeister.sangfroid.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.julesmeister.sangfroid.domain.model.AnswerInputMethod
import com.julesmeister.sangfroid.domain.model.EvaluationStatus

@Entity(
    tableName = "practice_attempts",
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["questionId"]),
        Index(value = ["submittedAt"]),
        Index(value = ["evaluationStatus"])
    ]
)
data class PracticeAttemptEntity(
    @PrimaryKey
    val id: String,
    val questionId: String,
    val answerText: String,
    val inputMethod: AnswerInputMethod,
    val startedAt: Long,
    val submittedAt: Long,
    val durationSeconds: Int,
    val wordCount: Int,
    val evaluationStatus: EvaluationStatus,
    val modelName: String?,
    val modelVersion: String?
)