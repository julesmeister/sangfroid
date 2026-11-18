// app/src/main/java/com/julesmeister/sangfroid/feature/profile/ProfileUiMapper.kt

package com.julesmeister.sangfroid.feature.profile

import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidProfileTokens
import com.julesmeister.sangfroid.domain.cv.CvProfile
import com.julesmeister.sangfroid.domain.model.SkillCode
import com.julesmeister.sangfroid.domain.profile.CustomSkill
import com.julesmeister.sangfroid.domain.profile.ProfileSkillSource
import com.julesmeister.sangfroid.feature.profile.ui.CvAchievementModel
import com.julesmeister.sangfroid.feature.profile.ui.CvAnalysisModel
import com.julesmeister.sangfroid.feature.profile.ui.CvCertificationModel
import com.julesmeister.sangfroid.feature.profile.ui.CvChooseButtonModel
import com.julesmeister.sangfroid.feature.profile.ui.CvPanelModel
import com.julesmeister.sangfroid.feature.profile.ui.CvProjectModel
import com.julesmeister.sangfroid.feature.profile.ui.CvSectionModel
import com.julesmeister.sangfroid.feature.profile.ui.CvStrengthModel
import com.julesmeister.sangfroid.feature.profile.ui.CvWorkHighlightModel
import com.julesmeister.sangfroid.feature.profile.ui.ProfileScreenModel
import com.julesmeister.sangfroid.feature.profile.ui.SaveBarModel
import com.julesmeister.sangfroid.feature.profile.ui.SkillChipModel
import com.julesmeister.sangfroid.feature.profile.ui.SkillGroupModel
import com.julesmeister.sangfroid.feature.profile.ui.SkillsSectionModel
import com.julesmeister.sangfroid.feature.profile.ui.SuggestedSkillModel
import com.julesmeister.sangfroid.feature.profile.ui.TargetSectionModel
import com.julesmeister.sangfroid.feature.profile.ui.UndoModel
import java.util.Locale
import kotlin.math.roundToInt

fun ProfileUiState.toScreenModel():
        ProfileScreenModel {
    val analysis =
        activeCvProfile
            ?.toCvAnalysisModel()
            ?.takeUnless(
                CvAnalysisModel::isEmpty
            )

    return ProfileScreenModel(
        isLoading =
            isLoading,
        completeness =
            completeness(),
        cv =
            cvSection(),
        cvAnalysis =
            analysis,
        target =
            targetSection(),
        skills =
            skillsSection(),
        errorMessage =
            errorMessage,
        undo =
            recentlyRemoved?.let { removed ->
                val label =
                    removed.skillCode
                        ?.let(
                            SkillCatalog::label
                        )
                        ?: removed.customSkill
                            ?.name
                        ?: "skill"

                UndoModel(
                    id =
                        removed.id,
                    message =
                        "Removed $label. It won't shape your questions."
                )
            },
        save =
            saveBar()
    )
}

private fun ProfileUiState.completeness():
        Float {
    val role =
        if (targetRole.isNotBlank()) {
            1f
        } else {
            0f
        }

    val level =
        if (experienceLevel.isNotBlank()) {
            1f
        } else {
            0f
        }

    val skillProgress =
        (
                totalSkillCount.toFloat() /
                        SangfroidProfileTokens.TargetSkillCount
                )
            .coerceAtMost(
                1f
            )

    return (
            role +
                    level +
                    skillProgress
            ) / 3f
}

private fun ProfileUiState.targetSection():
        TargetSectionModel {
    val custom =
        experienceLevel.trim()

    val options =
        if (
            custom.isNotBlank() &&
            SkillCatalog
                .experienceLevels
                .none { option ->
                    option.equals(
                        custom,
                        ignoreCase =
                            true
                    )
                }
        ) {
            SkillCatalog.experienceLevels +
                    custom
        } else {
            SkillCatalog.experienceLevels
        }

    return TargetSectionModel(
        role =
            targetRole,
        selectedLevel =
            custom,
        levelOptions =
            options
    )
}

private fun codeChip(
    code: SkillCode,
    fromCv: Boolean = false
): SkillChipModel {
    return SkillChipModel(
        key =
            SkillCatalog.key(
                code
            ),
        label =
            SkillCatalog.label(
                code
            ),
        fromCv =
            fromCv
    )
}

private fun customChip(
    skill: CustomSkill
): SkillChipModel {
    return SkillChipModel(
        key =
            SkillCatalog.customKey(
                skill.normalizedName
            ),
        label =
            skill.name,
        fromCv =
            skill.source ==
                    ProfileSkillSource.CV
    )
}

private fun ProfileUiState.skillsSection():
        SkillsSectionModel {
    val selected =
        skills.map {
                (
                    code,
                    source
                ) ->

            codeChip(
                code =
                    code,
                fromCv =
                    source ==
                            ProfileSkillSource.CV
            )
        } +
                customSkills.map(
                    ::customChip
                )

    val groups =
        listOf(
            SkillGroupModel(
                title =
                    "People skills",
                skills =
                    SkillCatalog
                        .peopleSkills
                        .filterNot { code ->
                            code in skills
                        }
                        .map { code ->
                            codeChip(
                                code
                            )
                        }
            ),
            SkillGroupModel(
                title =
                    "Technical",
                skills =
                    SkillCatalog
                        .technicalSkills
                        .filterNot { code ->
                            code in skills
                        }
                        .map { code ->
                            codeChip(
                                code
                            )
                        }
            )
        )
            .filter { group ->
                group.skills.isNotEmpty()
            }

    return SkillsSectionModel(
        selected =
            selected,
        availableGroups =
            groups,
        hasCvSkills =
            selected.any { skill ->
                skill.fromCv
            }
    )
}

private fun ProfileUiState.saveBar():
        SaveBarModel {
    val justSaved =
        !isDirty &&
                isSaved

    return SaveBarModel(
        label =
            when {
                isSaving ->
                    "Saving…"

                justSaved ->
                    "Profile saved"

                !isDirty ->
                    "No changes to save"

                else ->
                    "Save profile"
            },
        enabled =
            canSave,
        loading =
            isSaving,
        showCheck =
            justSaved,
        hint =
            if (isDirty) {
                missingRequirement
            } else {
                null
            }
    )
}

private fun ProfileUiState.cvSection():
        CvSectionModel {
    val cv =
        cvImport

    val profile =
        activeCvProfile

    val panel =
        when {
            cv.status ==
                    CvImportStatus.ReadingPdf -> {
                CvPanelModel.Working(
                    title =
                        "Reading your CV",
                    detail =
                        cv.fileName
                            ?: "Extracting text from the PDF"
                )
            }

            cv.status ==
                    CvImportStatus.Analyzing -> {
                CvPanelModel.Working(
                    title =
                        "Analyzing your experience",
                    detail =
                        "Qwen is organizing skills, projects, credentials, achievements, and work evidence."
                )
            }

            cv.status ==
                    CvImportStatus.InsufficientText -> {
                CvPanelModel.Message(
                    title =
                        "We couldn't read enough text",
                    message =
                        cv.errorMessage
                            ?: "Try a text-based PDF instead of a scanned or image-only document."
                )
            }

            cv.status ==
                    CvImportStatus.Error -> {
                CvPanelModel.Message(
                    title =
                        "CV analysis unavailable",
                    message =
                        cv.errorMessage
                            ?: "Sangfroid could not analyze this CV."
                )
            }

            profile != null -> {
                cvResult(
                    profile =
                        profile,
                    fileName =
                        cv.fileName
                )
            }

            else -> {
                null
            }
        }

    return CvSectionModel(
        caption =
            if (profile != null) {
                "Your saved CV analysis is displayed in the dedicated sections below."
            } else {
                "Upload a PDF and Qwen will organize your skills, projects, credentials, achievements, and work evidence."
            },
        chooseButton =
            CvChooseButtonModel(
                label =
                    when {
                        cv.isWorking ->
                            "Analyzing…"

                        profile != null ->
                            "Replace CV"

                        else ->
                            "Choose CV (PDF)"
                    },
                enabled =
                    !cv.isWorking,
                loading =
                    cv.isWorking,
                primary =
                    profile == null
            ),
        panel =
            panel
    )
}

private fun ProfileUiState.cvResult(
    profile: CvProfile,
    fileName: String?
): CvPanelModel.Result {
    val resolved =
        profile.resolveSkillCodes()

    val codesToAdd =
        resolved.skillCodes.count { code ->
            code !in skills
        }

    val customsToAdd =
        resolved.customSkillNames.count { name ->
            CustomSkill.normalize(
                name
            ) !in customSkillKeys
        }

    val totalToAdd =
        codesToAdd +
                customsToAdd

    val alreadyApplied =
        cvApplySummary != null &&
                totalToAdd == 0

    val suggestedSkills =
        resolved.skillCodes.map { code ->
            SuggestedSkillModel(
                label =
                    SkillCatalog.label(
                        code
                    ),
                alreadyAdded =
                    code in skills
            )
        } +
                resolved.customSkillNames.map { name ->
                    SuggestedSkillModel(
                        label =
                            name,
                        alreadyAdded =
                            CustomSkill.normalize(
                                name
                            ) in customSkillKeys
                    )
                }

    val analysis =
        profile.toCvAnalysisModel()

    return CvPanelModel.Result(
        role =
            profile.suggestedRoles
                .firstOrNull()
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )
                ?: "CV analyzed",
        fileName =
            fileName,
        level =
            profile
                .suggestedExperienceLevel
                .normalizeOptional(),
        summary =
            profile.profileSummary
                .trim(),
        suggestedSkills =
            suggestedSkills,
        analysis =
            analysis,
        appliedNote =
            cvApplySummary?.let { summary ->
                "Added ${
                    plural(
                        summary.addedCount,
                        "skill"
                    )
                }. Save to keep it."
            },
        applyLabel =
            when {
                alreadyApplied ->
                    "Added to your profile"

                totalToAdd == 0 ->
                    "Use suggested role & level"

                else ->
                    "Add ${
                        plural(
                            totalToAdd,
                            "skill"
                        )
                    } to profile"
            },
        applyEnabled =
            !alreadyApplied,
        applyDone =
            alreadyApplied
    )
}

private fun CvProfile.toCvAnalysisModel():
        CvAnalysisModel {
    val resolved =
        resolveSkillCodes()

    return CvAnalysisModel(
        summary =
            profileSummary.trim(),
        suggestedRoles =
            suggestedRoles.normalizeList(),
        experienceLevel =
            suggestedExperienceLevel
                .normalizeOptional(),
        strengths =
            skills.map { skill ->
                CvStrengthModel(
                    name =
                        skill.name.trim(),
                    mappedLabel =
                        resolved
                            .matchFor(
                                skill.name
                            )
                            ?.let(
                                SkillCatalog::label
                            )
                            ?: if (
                                resolved.isCustom(
                                    skill.name
                                )
                            ) {
                                "Custom skill"
                            } else {
                                null
                            },
                    typeLabel =
                        skill.type
                            .name
                            .toDisplayLabel()
                            .takeIf(
                                String::isNotBlank
                            ),
                    confidencePercent =
                        (
                                skill.confidence
                                    .coerceIn(
                                        0.0,
                                        1.0
                                    ) *
                                        100.0
                                )
                            .roundToInt(),
                    evidence =
                        skill.evidence.trim()
                )
            },
        certifications =
            certifications.map { certification ->
                CvCertificationModel(
                    id =
                        certification.id.trim(),
                    name =
                        certification.name.trim(),
                    subtitle =
                        listOfNotNull(
                            certification.issuer
                                .normalizeOptional(),
                            certification.level
                                .normalizeOptional(),
                            certification.issuedYear
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
                            .toDisplayLabel()
                            .takeUnless { status ->
                                status.equals(
                                    "Unknown",
                                    ignoreCase =
                                        true
                                )
                            }
                            ?.takeIf(
                                String::isNotBlank
                            ),
                    evidence =
                        certification.evidence
                            .trim(),
                    relatedSkills =
                        certification
                            .relatedSkills
                            .normalizeList()
                )
            },
        projects =
            projects.map { project ->
                CvProjectModel(
                    id =
                        project.id.trim(),
                    name =
                        project.name.trim(),
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
                            .normalizeList(),
                    methods =
                        project.methods
                            .normalizeList(),
                    tools =
                        project.tools
                            .normalizeList(),
                    outcomes =
                        project.outcomes
                            .normalizeList(),
                    metrics =
                        project.metrics
                            .normalizeList(),
                    relatedSkills =
                        project.relatedSkills
                            .normalizeList(),
                    evidence =
                        project.evidence.trim()
                )
            },
        achievements =
            achievements.map { achievement ->
                CvAchievementModel(
                    id =
                        achievement.id.trim(),
                    title =
                        achievement.title.trim(),
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
                        achievement.relatedSkills
                            .normalizeList()
                )
            },
        workHighlights =
            workHighlights.map { highlight ->
                CvWorkHighlightModel(
                    id =
                        highlight.id.trim(),
                    title =
                        highlight.title.trim(),
                    organizationContext =
                        highlight.organizationContext
                            .normalizeOptional(),
                    candidateRole =
                        highlight.candidateRole
                            .normalizeOptional(),
                    responsibility =
                        highlight.responsibility
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
                        highlight.relatedSkills
                            .normalizeList()
                )
            },
        experienceSignals =
            experienceSignals.normalizeList(),
        modelLabel =
            buildString {
                append(
                    modelName
                        .trim()
                        .ifBlank {
                            "Qwen"
                        }
                )

                modelVersion
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

private fun List<String>.normalizeList():
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

private fun plural(
    count: Int,
    word: String
): String {
    return if (count == 1) {
        "1 $word"
    } else {
        "$count ${word}s"
    }
}