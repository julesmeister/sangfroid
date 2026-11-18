// app/src/main/java/com/julesmeister/sangfroid/feature/profile/ui/ProfileScreenModel.kt

package com.julesmeister.sangfroid.feature.profile.ui

data class ProfileScreenModel(
    val isLoading: Boolean,
    val completeness: Float,
    val cv: CvSectionModel,
    val cvAnalysis: CvAnalysisModel?,
    val target: TargetSectionModel,
    val skills: SkillsSectionModel,
    val errorMessage: String?,
    val undo: UndoModel?,
    val save: SaveBarModel
)

data class TargetSectionModel(
    val role: String,
    val selectedLevel: String,
    val levelOptions: List<String>
)

data class SkillChipModel(
    val key: String,
    val label: String,
    val fromCv: Boolean = false
)

data class SkillGroupModel(
    val title: String,
    val skills: List<SkillChipModel>
)

data class SkillsSectionModel(
    val selected: List<SkillChipModel>,
    val availableGroups: List<SkillGroupModel>,
    val hasCvSkills: Boolean
)

data class UndoModel(
    val id: Long,
    val message: String
)

data class SaveBarModel(
    val label: String,
    val enabled: Boolean,
    val loading: Boolean,
    val showCheck: Boolean,
    val hint: String?
)

data class CvSectionModel(
    val caption: String,
    val chooseButton: CvChooseButtonModel,
    val panel: CvPanelModel?
)

data class CvChooseButtonModel(
    val label: String,
    val enabled: Boolean,
    val loading: Boolean,
    val primary: Boolean
)

data class SuggestedSkillModel(
    val label: String,
    val alreadyAdded: Boolean
)

data class CvStrengthModel(
    val name: String,
    val mappedLabel: String?,
    val typeLabel: String?,
    val confidencePercent: Int,
    val evidence: String
)

data class CvCertificationModel(
    val id: String,
    val name: String,
    val subtitle: String?,
    val status: String?,
    val evidence: String,
    val relatedSkills: List<String>
)

data class CvProjectModel(
    val id: String,
    val name: String,
    val context: String?,
    val candidateRole: String?,
    val objective: String?,
    val actions: List<String>,
    val methods: List<String>,
    val tools: List<String>,
    val outcomes: List<String>,
    val metrics: List<String>,
    val relatedSkills: List<String>,
    val evidence: String
)

data class CvAchievementModel(
    val id: String,
    val title: String,
    val context: String?,
    val action: String?,
    val result: String?,
    val metric: String?,
    val evidence: String,
    val relatedSkills: List<String>
)

data class CvWorkHighlightModel(
    val id: String,
    val title: String,
    val organizationContext: String?,
    val candidateRole: String?,
    val responsibility: String?,
    val action: String?,
    val outcome: String?,
    val metric: String?,
    val evidence: String,
    val relatedSkills: List<String>
)

data class CvAnalysisModel(
    val summary: String,
    val suggestedRoles: List<String>,
    val experienceLevel: String?,
    val strengths: List<CvStrengthModel>,
    val certifications: List<CvCertificationModel>,
    val projects: List<CvProjectModel>,
    val achievements: List<CvAchievementModel>,
    val workHighlights: List<CvWorkHighlightModel>,
    val experienceSignals: List<String>,
    val modelLabel: String
) {

    val structuredEvidenceCount: Int
        get() =
            certifications.size +
                    projects.size +
                    achievements.size +
                    workHighlights.size

    val totalItemCount: Int
        get() =
            strengths.size +
                    structuredEvidenceCount +
                    experienceSignals.size

    val hasExperience: Boolean
        get() =
            projects.isNotEmpty() ||
                    workHighlights.isNotEmpty()

    val hasCredentials: Boolean
        get() =
            certifications.isNotEmpty()

    val hasAchievements: Boolean
        get() =
            achievements.isNotEmpty()

    val hasSignals: Boolean
        get() =
            experienceSignals.isNotEmpty()

    val isEmpty: Boolean
        get() =
            summary.isBlank() &&
                    suggestedRoles.isEmpty() &&
                    experienceLevel.isNullOrBlank() &&
                    strengths.isEmpty() &&
                    certifications.isEmpty() &&
                    projects.isEmpty() &&
                    achievements.isEmpty() &&
                    workHighlights.isEmpty() &&
                    experienceSignals.isEmpty()
}

sealed interface CvPanelModel {

    data class Working(
        val title: String,
        val detail: String
    ) : CvPanelModel

    data class Message(
        val title: String,
        val message: String
    ) : CvPanelModel

    data class Result(
        val role: String,
        val fileName: String?,
        val level: String?,
        val summary: String,
        val suggestedSkills: List<SuggestedSkillModel>,
        val analysis: CvAnalysisModel,
        val appliedNote: String?,
        val applyLabel: String,
        val applyEnabled: Boolean,
        val applyDone: Boolean
    ) : CvPanelModel
}