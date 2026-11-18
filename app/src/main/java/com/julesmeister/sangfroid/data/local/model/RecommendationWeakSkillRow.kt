package com.julesmeister.sangfroid.data.local.model

import com.julesmeister.sangfroid.domain.model.SkillCode

data class RecommendationWeakSkillRow(
    val skillCode: SkillCode,
    val averageScore: Double,
    val evidenceCount: Int
)