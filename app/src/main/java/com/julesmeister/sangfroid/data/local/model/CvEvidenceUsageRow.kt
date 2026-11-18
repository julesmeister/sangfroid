package com.julesmeister.sangfroid.data.local.model

data class CvEvidenceUsageRow(
    val evidenceType: String,
    val evidenceId: String,
    val usageCount: Int,
    val lastUsedAt: Long?
)