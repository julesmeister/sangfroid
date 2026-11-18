package com.julesmeister.sangfroid.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.julesmeister.sangfroid.domain.model.ReminderStatus

@Entity(
    tableName = "reminders",
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
        Index(value = ["scheduledAt"]),
        Index(value = ["status"])
    ]
)
data class ReminderEntity(
    @PrimaryKey
    val id: String,
    val questionId: String,
    val scheduledAt: Long,
    val status: ReminderStatus,
    val workRequestId: String?,
    val createdAt: Long = System.currentTimeMillis()
)