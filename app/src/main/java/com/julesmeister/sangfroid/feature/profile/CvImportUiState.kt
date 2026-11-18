package com.julesmeister.sangfroid.feature.profile

import com.julesmeister.sangfroid.domain.cv.CvProfile

data class CvImportUiState(
    val status: CvImportStatus =
        CvImportStatus.Idle,
    val fileName: String? =
        null,
    val pageCount: Int =
        0,
    val characterCount: Int =
        0,
    val wordCount: Int =
        0,
    val profile: CvProfile? =
        null,
    val errorMessage: String? =
        null
) {

    val isWorking: Boolean
        get() =
            status ==
                    CvImportStatus.ReadingPdf ||
                    status ==
                    CvImportStatus.Analyzing

    val hasProfile: Boolean
        get() =
            profile != null

    val hasAnalysis: Boolean
        get() =
            hasProfile

    val hasStructuredEvidence: Boolean
        get() {
            val currentProfile =
                profile
                    ?: return false

            return currentProfile
                .certifications
                .isNotEmpty() ||
                    currentProfile
                        .projects
                        .isNotEmpty() ||
                    currentProfile
                        .achievements
                        .isNotEmpty() ||
                    currentProfile
                        .workHighlights
                        .isNotEmpty()
        }

    val evidenceCount: Int
        get() {
            val currentProfile =
                profile
                    ?: return 0

            return currentProfile
                .certifications
                .size +
                    currentProfile
                        .projects
                        .size +
                    currentProfile
                        .achievements
                        .size +
                    currentProfile
                        .workHighlights
                        .size
        }

    val hasSkills: Boolean
        get() =
            profile
                ?.skills
                ?.isNotEmpty() ==
                    true

    val hasCertifications: Boolean
        get() =
            profile
                ?.certifications
                ?.isNotEmpty() ==
                    true

    val hasProjects: Boolean
        get() =
            profile
                ?.projects
                ?.isNotEmpty() ==
                    true

    val hasAchievements: Boolean
        get() =
            profile
                ?.achievements
                ?.isNotEmpty() ==
                    true

    val hasWorkHighlights: Boolean
        get() =
            profile
                ?.workHighlights
                ?.isNotEmpty() ==
                    true
}

enum class CvImportStatus {
    Idle,
    ReadingPdf,
    Analyzing,
    Success,
    InsufficientText,
    Error
}