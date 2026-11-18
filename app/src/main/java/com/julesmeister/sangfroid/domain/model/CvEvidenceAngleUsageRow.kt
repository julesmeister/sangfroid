package com.julesmeister.sangfroid.data.local.model

import com.julesmeister.sangfroid.domain.model.QuestionAngle

data class CvEvidenceAngleUsageRow(
    val evidenceType: String,
    val evidenceId: String,
    val questionAngle: QuestionAngle,
    val usageCount: Int,
    val lastUsedAt: Long
)