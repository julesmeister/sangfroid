package com.julesmeister.sangfroid.data.cv.remote

data class CvAnalysisRequestDto(
    val cvText: String,
    val allowedSkillCodes: List<String>
)