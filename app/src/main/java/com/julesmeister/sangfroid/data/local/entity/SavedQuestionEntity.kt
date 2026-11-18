package com.julesmeister.sangfroid.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_questions",
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
        Index(value = ["savedAt"])
    ]
)
data class SavedQuestionEntity(
    @PrimaryKey
    val id: String,
    val questionId: String,
    val savedAt: Long = System.currentTimeMillis()
)