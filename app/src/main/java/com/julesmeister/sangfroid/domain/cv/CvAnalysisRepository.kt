package com.julesmeister.sangfroid.domain.cv

import com.julesmeister.sangfroid.domain.model.SkillCode

interface CvAnalysisRepository {

    suspend fun analyze(
        cvText: String,
        currentTargetRole: String?,
        allowedSkillCodes: Set<SkillCode>
    ): Result<CvProfile>

    suspend fun getSavedProfile():
            Result<CvProfile?>

    suspend fun clearSavedProfile():
            Result<Unit>
}