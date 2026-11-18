package com.julesmeister.sangfroid.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.julesmeister.sangfroid.domain.model.AnswerInputMethod

@Entity(
    tableName = "answer_drafts",
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["questionId"], unique = true),
        Index(value = ["updatedAt"])
    ]
)
data class AnswerDraftEntity(
    @PrimaryKey
    val id: String,
    val questionId: String,
    val answerText: String,
    val inputMethod: AnswerInputMethod,
    val startedAt: Long,
    val updatedAt: Long = System.currentTimeMillis()
)