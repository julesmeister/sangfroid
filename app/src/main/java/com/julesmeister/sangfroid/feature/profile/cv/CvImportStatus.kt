package com.julesmeister.sangfroid.feature.profile.cv

import com.julesmeister.sangfroid.domain.cv.CvDocument
import com.julesmeister.sangfroid.domain.cv.CvExtractionResult

sealed interface CvImportStatus {

    data object Idle :
        CvImportStatus

    data class Reading(
        val document: CvDocument
    ) : CvImportStatus

    data class ExtractingSkills(
        val document: CvDocument
    ) : CvImportStatus

    data class Review(
        val extraction: CvExtractionResult,
        val selectedSkillCodes:
        Set<com.julesmeister.sangfroid.domain.model.SkillCode>
    ) : CvImportStatus

    data class Applying(
        val extraction: CvExtractionResult
    ) : CvImportStatus

    data class Imported(
        val fileName: String,
        val importedSkillCount: Int
    ) : CvImportStatus

    data class Error(
        val fileName: String?,
        val message: String
    ) : CvImportStatus
}