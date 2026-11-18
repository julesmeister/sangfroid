package com.julesmeister.sangfroid.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.julesmeister.sangfroid.domain.model.SkillCode

@Entity(
    tableName = "question_skills",
    primaryKeys = ["questionId", "skillCode"],
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
        Index(value = ["skillCode"])
    ]
)
data class QuestionSkillCrossRef(
    val questionId: String,
    val skillCode: SkillCode
)