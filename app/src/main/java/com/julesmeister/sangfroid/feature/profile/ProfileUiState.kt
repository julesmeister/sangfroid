package com.julesmeister.sangfroid.feature.profile

import com.julesmeister.sangfroid.domain.cv.CvProfile
import com.julesmeister.sangfroid.domain.model.SkillCode
import com.julesmeister.sangfroid.domain.profile.CustomSkill
import com.julesmeister.sangfroid.domain.profile.ProfileSkillSource

data class ProfileUiState(
    val targetRole: String = "",
    val experienceLevel: String = "",
    val skills: Map<SkillCode, ProfileSkillSource> =
        emptyMap(),
    val customSkills: List<CustomSkill> =
        emptyList(),
    val savedCvProfile: CvProfile? =
        null,
    val cvImport: CvImportUiState =
        CvImportUiState(),
    val cvApplySummary: CvApplySummary? =
        null,
    val recentlyRemoved: RemovedSkill? =
        null,
    val isLoading: Boolean =
        true,
    val isSaving: Boolean =
        false,
    val isSaved: Boolean =
        false,
    val isDirty: Boolean =
        false,
    val errorMessage: String? =
        null
) {

    val selectedSkills: Set<SkillCode>
        get() =
            skills.keys

    val customSkillKeys: Set<String>
        get() =
            customSkills
                .map { customSkill ->
                    customSkill.normalizedName
                }
                .toSet()

    val activeCvProfile: CvProfile?
        get() =
            cvImport.profile
                ?: savedCvProfile

    val hasCvProfile: Boolean
        get() =
            activeCvProfile != null

    val hasCvSkills: Boolean
        get() =
            activeCvProfile
                ?.skills
                ?.isNotEmpty() ==
                    true

    val hasCvProjects: Boolean
        get() =
            activeCvProfile
                ?.projects
                ?.isNotEmpty() ==
                    true

    val hasCvCertifications: Boolean
        get() =
            activeCvProfile
                ?.certifications
                ?.isNotEmpty() ==
                    true

    val hasCvAchievements: Boolean
        get() =
            activeCvProfile
                ?.achievements
                ?.isNotEmpty() ==
                    true

    val hasCvWorkHighlights: Boolean
        get() =
            activeCvProfile
                ?.workHighlights
                ?.isNotEmpty() ==
                    true

    val hasCvExperienceSignals: Boolean
        get() =
            activeCvProfile
                ?.experienceSignals
                ?.isNotEmpty() ==
                    true

    val cvStructuredEvidenceCount: Int
        get() {
            val profile =
                activeCvProfile
                    ?: return 0

            return profile.projects.size +
                    profile.certifications.size +
                    profile.achievements.size +
                    profile.workHighlights.size
        }

    val totalSkillCount: Int
        get() =
            skills.size +
                    customSkills.size

    val missingRequirement: String?
        get() =
            when {
                targetRole.isBlank() ->
                    "Add a target role to save."

                experienceLevel.isBlank() ->
                    "Pick an experience level to save."

                else ->
                    null
            }

    val canSave: Boolean
        get() =
            !isLoading &&
                    !isSaving &&
                    !cvImport.isWorking &&
                    isDirty &&
                    missingRequirement == null
}

data class CvApplySummary(
    val addedCount: Int,
    val totalCount: Int
)

/**
 * Exactly one of [skillCode] or [customSkill] is set.
 */
data class RemovedSkill(
    val skillCode: SkillCode? =
        null,
    val customSkill: CustomSkill? =
        null,
    val source: ProfileSkillSource,
    val id: Long =
        System.nanoTime()
)