package com.julesmeister.sangfroid.data.cv.remote

data class CvAnalysisResponseDto(
    val requestId: String,
    val profileSummary: String,
    val suggestedRoles: List<String>,
    val suggestedExperienceLevel: String?,
    val skills: List<CvSkillDto>,
    val skillMappings: List<CvSkillMappingDto>,
    val experienceSignals: List<String>,
    val modelName: String,
    val modelVersion: String?,
    val analyzedAt: Long
)

data class CvSkillDto(
    val name: String,
    val normalizedName: String,
    val type: String,
    val confidence: Double,
    val evidence: String
)

data class CvSkillMappingDto(
    val extractedSkillName: String,
    val skillCode: String,
    val confidence: Double,
    val reason: String
)