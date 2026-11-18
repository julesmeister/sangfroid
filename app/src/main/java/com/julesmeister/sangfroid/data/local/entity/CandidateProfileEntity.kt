package com.julesmeister.sangfroid.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "candidate_profiles"
)
data class CandidateProfileEntity(
    @PrimaryKey
    val id: String,
    val targetRole: String,
    val experienceLevel: String,
    val updatedAt: Long
)
