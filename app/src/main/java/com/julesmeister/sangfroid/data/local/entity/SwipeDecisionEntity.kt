package com.julesmeister.sangfroid.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.julesmeister.sangfroid.domain.model.SwipeDirection

@Entity(
    tableName = "swipe_decisions",
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
        Index(value = ["swipedAt"])
    ]
)
data class SwipeDecisionEntity(
    @PrimaryKey
    val id: String,
    val questionId: String,
    val direction: SwipeDirection,
    val swipedAt: Long = System.currentTimeMillis()
)