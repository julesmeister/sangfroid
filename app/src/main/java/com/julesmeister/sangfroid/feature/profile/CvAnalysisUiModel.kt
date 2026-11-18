// app/src/main/java/com/julesmeister/sangfroid/feature/profile/CvAnalysisUiModel.kt

package com.julesmeister.sangfroid.feature.profile

import androidx.compose.runtime.Immutable
import com.julesmeister.sangfroid.domain.cv.CvProfile
import java.util.Locale

@Immutable
data class CvAnalysisUiModel(
    val summary: String,
    val suggestedRoles: List<String>,
    val experienceLevel: String?,
    val skills: List<CvSkillUiModel>,
    val certifications: List<CvCertificationUiModel>,
    val projects: List<CvProjectUiModel>,
    val achievements: List<CvAchievementUiModel>,
    val workHighlights: List<CvWorkHighlightUiModel>,
    val experienceSignals: List<String>,
    val modelLabel: String
) {

    val hasSkills: Boolean
        get() =
            skills.isNotEmpty()

    val hasCertifications: Boolean
        get() =
            certifications.isNotEmpty()

    val hasProjects: Boolean
        get() =
            projects.isNotEmpty()

    val hasAchievements: Boolean
        get() =
            achievements.isNotEmpty()

    val hasWorkHighlights: Boolean
        get() =
            workHighlights.isNotEmpty()

    val hasExperienceSignals: Boolean
        get() =
            experienceSignals.isNotEmpty()

    val evidenceCount: Int
        get() =
            certifications.size +
                    projects.size +
                    achievements.size +
                    workHighlights.size

    companion object {

        fun from(
            profile: CvProfile
        ): CvAnalysisUiModel {
            return CvAnalysisUiModel(
                summary =
                    profile.profileSummary
                        .trim(),
                suggestedRoles =
                    profile.suggestedRoles
                        .normalizeTextList(),
                experienceLevel =
                    profile
                        .suggestedExperienceLevel
                        .normalizeOptional(),
                skills =
                    profile.skills
                        .asSequence()
                        .filter { skill ->
                            skill.name.isNotBlank()
                        }
                        .sortedWith(
                            compareByDescending {
                                it.confidence
                            }
                        )
                        .map { skill ->
                            CvSkillUiModel(
                                name =
                                    skill.name
                                        .trim(),
                                typeLabel =
                                    skill.type
                                        .name
                                        .toDisplayLabel(),
                                confidence =
                                    skill.confidence
                                        .coerceIn(
                                            0.0,
                                            1.0
                                        ),
                                evidence =
                                    skill.evidence
                                        .trim()
                            )
                        }
                        .toList(),
                certifications =
                    profile.certifications
                        .map { certification ->
                            CvCertificationUiModel(
                                id =
                                    certification.id
                                        .trim(),
                                title =
                                    certification.name
                                        .trim(),
                                subtitle =
                                    listOfNotNull(
                                        certification
                                            .issuer
                                            .normalizeOptional(),
                                        certification
                                            .level
                                            .normalizeOptional(),
                                        certification
                                            .issuedYear
                                            ?.toString()
                                    )
                                        .distinct()
                                        .joinToString(
                                            separator =
                                                " • "
                                        )
                                        .takeIf(
                                            String::isNotBlank
                                        ),
                                status =
                                    certification.status
                                        .name
                                        .toDisplayLabel(),
                                evidence =
                                    certification.evidence
                                        .trim(),
                                relatedSkills =
                                    certification
                                        .relatedSkills
                                        .normalizeTextList()
                            )
                        },
                projects =
                    profile.projects
                        .map { project ->
                            CvProjectUiModel(
                                id =
                                    project.id
                                        .trim(),
                                title =
                                    project.name
                                        .trim(),
                                context =
                                    project.context
                                        .normalizeOptional(),
                                candidateRole =
                                    project.candidateRole
                                        .normalizeOptional(),
                                objective =
                                    project.objective
                                        .normalizeOptional(),
                                actions =
                                    project.actions
                                        .normalizeTextList(),
                                methods =
                                    project.methods
                                        .normalizeTextList(),
                                tools =
                                    project.tools
                                        .normalizeTextList(),
                                outcomes =
                                    project.outcomes
                                        .normalizeTextList(),
                                metrics =
                                    project.metrics
                                        .normalizeTextList(),
                                relatedSkills =
                                    project.relatedSkills
                                        .normalizeTextList(),
                                evidence =
                                    project.evidence
                                        .trim()
                            )
                        },
                achievements =
                    profile.achievements
                        .map { achievement ->
                            CvAchievementUiModel(
                                id =
                                    achievement.id
                                        .trim(),
                                title =
                                    achievement.title
                                        .trim(),
                                context =
                                    achievement.context
                                        .normalizeOptional(),
                                action =
                                    achievement.candidateAction
                                        .normalizeOptional(),
                                result =
                                    achievement.result
                                        .normalizeOptional(),
                                metric =
                                    achievement.metric
                                        .normalizeOptional(),
                                evidence =
                                    achievement.evidence
                                        .trim(),
                                relatedSkills =
                                    achievement
                                        .relatedSkills
                                        .normalizeTextList()
                            )
                        },
                workHighlights =
                    profile.workHighlights
                        .map { highlight ->
                            CvWorkHighlightUiModel(
                                id =
                                    highlight.id
                                        .trim(),
                                title =
                                    highlight.title
                                        .trim(),
                                organizationContext =
                                    highlight
                                        .organizationContext
                                        .normalizeOptional(),
                                candidateRole =
                                    highlight
                                        .candidateRole
                                        .normalizeOptional(),
                                responsibility =
                                    highlight
                                        .responsibility
                                        .normalizeOptional(),
                                action =
                                    highlight.action
                                        .normalizeOptional(),
                                outcome =
                                    highlight.outcome
                                        .normalizeOptional(),
                                metric =
                                    highlight.metric
                                        .normalizeOptional(),
                                evidence =
                                    highlight.evidence
                                        .trim(),
                                relatedSkills =
                                    highlight
                                        .relatedSkills
                                        .normalizeTextList()
                            )
                        },
                experienceSignals =
                    profile.experienceSignals
                        .normalizeTextList(),
                modelLabel =
                    buildString {
                        append(
                            profile.modelName
                                .trim()
                                .ifBlank {
                                    "Qwen"
                                }
                        )

                        profile.modelVersion
                            .normalizeOptional()
                            ?.let { version ->
                                append(
                                    " • "
                                )

                                append(
                                    version
                                )
                            }
                    }
            )
        }

        private fun String?.normalizeOptional():
                String? {
            return this
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )
        }

        private fun List<String>.normalizeTextList():
                List<String> {
            return asSequence()
                .map(
                    String::trim
                )
                .filter(
                    String::isNotBlank
                )
                .distinct()
                .toList()
        }

        private fun String.toDisplayLabel():
                String {
            return trim()
                .lowercase(
                    Locale.ROOT
                )
                .split(
                    "_",
                    "-",
                    " "
                )
                .filter(
                    String::isNotBlank
                )
                .joinToString(
                    separator =
                        " "
                ) { word ->
                    word.replaceFirstChar { character ->
                        character.titlecase(
                            Locale.getDefault()
                        )
                    }
                }
        }
    }
}

@Immutable
data class CvSkillUiModel(
    val name: String,
    val typeLabel: String,
    val confidence: Double,
    val evidence: String
) {
    val confidencePercent: Int
        get() =
            (
                    confidence
                        .coerceIn(
                            0.0,
                            1.0
                        ) *
                            100.0
                    )
                .toInt()
                .coerceIn(
                    0,
                    100
                )
}

@Immutable
data class CvCertificationUiModel(
    val id: String,
    val title: String,
    val subtitle: String?,
    val status: String,
    val evidence: String,
    val relatedSkills: List<String>
)

@Immutable
data class CvProjectUiModel(
    val id: String,
    val title: String,
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

@Immutable
data class CvAchievementUiModel(
    val id: String,
    val title: String,
    val context: String?,
    val action: String?,
    val result: String?,
    val metric: String?,
    val evidence: String,
    val relatedSkills: List<String>
)

@Immutable
data class CvWorkHighlightUiModel(
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