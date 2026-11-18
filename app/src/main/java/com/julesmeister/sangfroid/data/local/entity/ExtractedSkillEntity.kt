package com.julesmeister.sangfroid.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.julesmeister.sangfroid.domain.model.SkillCode

@Entity(
    tableName = "extracted_skills",
    indices = [
        Index(value = ["skillCode"], unique = true),
        Index(value = ["isConfirmed"])
    ]
)
data class ExtractedSkillEntity(
    @PrimaryKey
    val id: String,
    val skillCode: SkillCode,
    val displayName: String,
    val sourceKeyword: String,
    val confidence: Float,
    val isConfirmed: Boolean,
    val isEnabled: Boolean = true,
    val extractedAt: Long = System.currentTimeMillis()
)