package com.julesmeister.sangfroid.data.cv

import com.julesmeister.sangfroid.data.remote.api.SangfroidApi
import com.julesmeister.sangfroid.data.remote.dto.CvAchievementDto
import com.julesmeister.sangfroid.data.remote.dto.CvAnalysisRequestDto
import com.julesmeister.sangfroid.data.remote.dto.CvAnalysisResponseDto
import com.julesmeister.sangfroid.data.remote.dto.CvCertificationDto
import com.julesmeister.sangfroid.data.remote.dto.CvProjectDto
import com.julesmeister.sangfroid.data.remote.dto.CvSkillDto
import com.julesmeister.sangfroid.data.remote.dto.CvSkillMappingDto
import com.julesmeister.sangfroid.data.remote.dto.CvWorkHighlightDto
import com.julesmeister.sangfroid.domain.cv.CvAchievement
import com.julesmeister.sangfroid.domain.cv.CvAnalysisRepository
import com.julesmeister.sangfroid.domain.cv.CvCertification
import com.julesmeister.sangfroid.domain.cv.CvCertificationStatus
import com.julesmeister.sangfroid.domain.cv.CvProfile
import com.julesmeister.sangfroid.domain.cv.CvProject
import com.julesmeister.sangfroid.domain.cv.CvSkill
import com.julesmeister.sangfroid.domain.cv.CvSkillMapping
import com.julesmeister.sangfroid.domain.cv.CvSkillType
import com.julesmeister.sangfroid.domain.cv.CvWorkHighlight
import com.julesmeister.sangfroid.domain.model.SkillCode
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class RemoteCvAnalysisRepository @Inject constructor(
    private val sangfroidApi:
    SangfroidApi
) : CvAnalysisRepository {

    override suspend fun analyze(
        cvText: String,
        currentTargetRole: String?,
        allowedSkillCodes: Set<SkillCode>
    ): Result<CvProfile> {
        val normalizedText =
            normalizeCvText(
                cvText
            )

        if (
            normalizedText.length !in
            MINIMUM_CV_TEXT_LENGTH..
            MAXIMUM_CV_TEXT_LENGTH
        ) {
            return Result.failure(
                IllegalArgumentException(
                    "CV text must contain between " +
                            "$MINIMUM_CV_TEXT_LENGTH and " +
                            "$MAXIMUM_CV_TEXT_LENGTH characters."
                )
            )
        }

        val normalizedTargetRole =
            normalizeOptional(
                value =
                    currentTargetRole,
                maximumLength =
                    MAXIMUM_TARGET_ROLE_LENGTH
            )

        val normalizedSkillCodes =
            allowedSkillCodes
                .ifEmpty {
                    SkillCode.entries.toSet()
                }
                .asSequence()
                .map { skillCode ->
                    skillCode.name
                }
                .distinct()
                .take(
                    MAXIMUM_SKILL_CODE_COUNT
                )
                .toList()

        if (normalizedSkillCodes.isEmpty()) {
            return Result.failure(
                IllegalArgumentException(
                    "At least one Sangfroid skill code is required for CV analysis."
                )
            )
        }

        val request =
            CvAnalysisRequestDto(
                requestId =
                    createRequestId(),
                cvText =
                    normalizedText,
                currentTargetRole =
                    normalizedTargetRole,
                allowedSkillCodes =
                    normalizedSkillCodes
            )

        return try {
            val response =
                sangfroidApi
                    .analyzeCv(
                        request
                    )
                    .getOrThrow()

            require(
                response.requestId ==
                        request.requestId
            ) {
                "The CV-analysis response does not match the request."
            }

            val profile =
                responseDtoToDomain(
                    response
                )

            require(
                profile.profileSummary
                    .isNotBlank()
            ) {
                "The CV-analysis response did not contain a profile summary."
            }

            require(
                profile.hasEvidence
            ) {
                "The CV-analysis response did not contain usable CV evidence."
            }

            Result.success(
                profile
            )
        } catch (
            cancellation: CancellationException
        ) {
            throw cancellation
        } catch (
            throwable: Throwable
        ) {
            Result.failure(
                throwable
            )
        }
    }

    private fun responseDtoToDomain(
        response: CvAnalysisResponseDto
    ): CvProfile {
        return CvProfile(
            requestId =
                response.requestId
                    .cleanText(),
            profileSummary =
                response.profileSummary
                    .cleanText(),
            suggestedRoles =
                normalizeStrings(
                    values =
                        response.suggestedRoles,
                    maximumItems =
                        MAXIMUM_SUGGESTED_ROLE_COUNT,
                    maximumItemLength =
                        MAXIMUM_TARGET_ROLE_LENGTH
                ),
            suggestedExperienceLevel =
                normalizeOptional(
                    value =
                        response.suggestedExperienceLevel,
                    maximumLength =
                        MAXIMUM_EXPERIENCE_LEVEL_LENGTH
                ),
            skills =
                response.skills
                    .mapNotNull(
                        ::skillDtoToDomain
                    )
                    .distinctBy { skill ->
                        skill.normalizedName
                            .lowercase(
                                Locale.ROOT
                            )
                    }
                    .sortedByDescending { skill ->
                        skill.confidence
                    }
                    .take(
                        MAXIMUM_SKILL_COUNT
                    ),
            skillMappings =
                response.skillMappings
                    .mapNotNull(
                        ::mappingDtoToDomain
                    )
                    .distinctBy { mapping ->
                        mapping.extractedSkillName
                            .lowercase(
                                Locale.ROOT
                            ) to
                                mapping.skillCode
                    }
                    .sortedByDescending { mapping ->
                        mapping.confidence
                    },
            certifications =
                response.certifications
                    .mapNotNull(
                        ::certificationDtoToDomain
                    )
                    .distinctBy { certification ->
                        certification.id
                    }
                    .take(
                        MAXIMUM_CERTIFICATION_COUNT
                    ),
            projects =
                response.projects
                    .mapNotNull(
                        ::projectDtoToDomain
                    )
                    .distinctBy { project ->
                        project.id
                    }
                    .take(
                        MAXIMUM_PROJECT_COUNT
                    ),
            achievements =
                response.achievements
                    .mapNotNull(
                        ::achievementDtoToDomain
                    )
                    .distinctBy { achievement ->
                        achievement.id
                    }
                    .take(
                        MAXIMUM_ACHIEVEMENT_COUNT
                    ),
            workHighlights =
                response.workHighlights
                    .mapNotNull(
                        ::workHighlightDtoToDomain
                    )
                    .distinctBy { highlight ->
                        highlight.id
                    }
                    .take(
                        MAXIMUM_WORK_HIGHLIGHT_COUNT
                    ),
            experienceSignals =
                normalizeStrings(
                    values =
                        response.experienceSignals,
                    maximumItems =
                        MAXIMUM_EXPERIENCE_SIGNAL_COUNT,
                    maximumItemLength =
                        MAXIMUM_DETAIL_LENGTH
                ),
            modelName =
                response.modelName
                    .cleanText()
                    .ifBlank {
                        DEFAULT_MODEL_NAME
                    },
            modelVersion =
                normalizeOptional(
                    value =
                        response.modelVersion,
                    maximumLength =
                        MAXIMUM_MODEL_VERSION_LENGTH
                ),
            analyzedAt =
                response.analyzedAt
                    .takeIf { analyzedAt ->
                        analyzedAt > 0L
                    }
                    ?: System.currentTimeMillis()
        ).normalized()
    }

    private fun skillDtoToDomain(
        skill: CvSkillDto
    ): CvSkill? {
        val name =
            skill.name
                .cleanText()
                .take(
                    MAXIMUM_SKILL_NAME_LENGTH
                )

        val evidence =
            skill.evidence
                .cleanText()
                .take(
                    MAXIMUM_EVIDENCE_LENGTH
                )

        if (
            name.isBlank() ||
            evidence.isBlank()
        ) {
            return null
        }

        val normalizedName =
            skill.normalizedName
                .cleanText()
                .lowercase(
                    Locale.ROOT
                )
                .ifBlank {
                    name.lowercase(
                        Locale.ROOT
                    )
                }
                .take(
                    MAXIMUM_SKILL_NAME_LENGTH
                )

        return CvSkill(
            name =
                name,
            normalizedName =
                normalizedName,
            type =
                CvSkillType.fromApi(
                    skill.type
                ),
            confidence =
                normalizeConfidence(
                    skill.confidence
                ),
            evidence =
                evidence
        ).normalized()
    }

    private fun mappingDtoToDomain(
        mapping: CvSkillMappingDto
    ): CvSkillMapping? {
        val extractedSkillName =
            mapping.extractedSkillName
                .cleanText()
                .take(
                    MAXIMUM_SKILL_NAME_LENGTH
                )

        if (extractedSkillName.isBlank()) {
            return null
        }

        val skillCode =
            SkillCode.entries
                .firstOrNull { candidate ->
                    candidate.name.equals(
                        other =
                            mapping.skillCode
                                .trim(),
                        ignoreCase =
                            true
                    )
                }
                ?: return null

        return CvSkillMapping(
            extractedSkillName =
                extractedSkillName,
            skillCode =
                skillCode,
            confidence =
                normalizeConfidence(
                    mapping.confidence
                ),
            reason =
                mapping.reason
                    .cleanText()
                    .take(
                        MAXIMUM_EVIDENCE_LENGTH
                    )
        ).normalized()
    }

    private fun certificationDtoToDomain(
        certification: CvCertificationDto
    ): CvCertification? {
        val id =
            certification.id
                .cleanText()
                .take(
                    MAXIMUM_EVIDENCE_ID_LENGTH
                )

        val name =
            certification.name
                .cleanText()
                .take(
                    MAXIMUM_CERTIFICATION_NAME_LENGTH
                )

        val evidence =
            certification.evidence
                .cleanText()
                .take(
                    MAXIMUM_EVIDENCE_LENGTH
                )

        if (
            id.isBlank() ||
            name.isBlank() ||
            evidence.isBlank()
        ) {
            return null
        }

        return CvCertification(
            id =
                id,
            name =
                name,
            issuer =
                normalizeOptional(
                    value =
                        certification.issuer,
                    maximumLength =
                        MAXIMUM_ORGANIZATION_LENGTH
                ),
            level =
                normalizeOptional(
                    value =
                        certification.level,
                    maximumLength =
                        MAXIMUM_LEVEL_LENGTH
                ),
            status =
                CvCertificationStatus.fromApi(
                    certification.status
                ),
            issuedYear =
                certification.issuedYear
                    ?.takeIf { year ->
                        year in
                                MINIMUM_CREDENTIAL_YEAR..
                                MAXIMUM_CREDENTIAL_YEAR
                    },
            evidence =
                evidence,
            relatedSkills =
                normalizeStrings(
                    values =
                        certification.relatedSkills,
                    maximumItems =
                        MAXIMUM_RELATED_SKILL_COUNT,
                    maximumItemLength =
                        MAXIMUM_SKILL_NAME_LENGTH
                )
        ).normalized()
    }

    private fun projectDtoToDomain(
        project: CvProjectDto
    ): CvProject? {
        val id =
            project.id
                .cleanText()
                .take(
                    MAXIMUM_EVIDENCE_ID_LENGTH
                )

        val name =
            project.name
                .cleanText()
                .take(
                    MAXIMUM_PROJECT_NAME_LENGTH
                )

        val evidence =
            project.evidence
                .cleanText()
                .take(
                    MAXIMUM_EVIDENCE_LENGTH
                )

        if (
            id.isBlank() ||
            name.isBlank() ||
            evidence.isBlank()
        ) {
            return null
        }

        return CvProject(
            id =
                id,
            name =
                name,
            context =
                normalizeOptional(
                    value =
                        project.context,
                    maximumLength =
                        MAXIMUM_CONTEXT_LENGTH
                ),
            candidateRole =
                normalizeOptional(
                    value =
                        project.candidateRole,
                    maximumLength =
                        MAXIMUM_TARGET_ROLE_LENGTH
                ),
            objective =
                normalizeOptional(
                    value =
                        project.objective,
                    maximumLength =
                        MAXIMUM_DETAIL_LENGTH
                ),
            actions =
                normalizeStrings(
                    values =
                        project.actions,
                    maximumItems =
                        MAXIMUM_PROJECT_LIST_ITEM_COUNT,
                    maximumItemLength =
                        MAXIMUM_ACTION_LENGTH
                ),
            methods =
                normalizeStrings(
                    values =
                        project.methods,
                    maximumItems =
                        MAXIMUM_PROJECT_LIST_ITEM_COUNT,
                    maximumItemLength =
                        MAXIMUM_SKILL_NAME_LENGTH
                ),
            tools =
                normalizeStrings(
                    values =
                        project.tools,
                    maximumItems =
                        MAXIMUM_PROJECT_LIST_ITEM_COUNT,
                    maximumItemLength =
                        MAXIMUM_SKILL_NAME_LENGTH
                ),
            outcomes =
                normalizeStrings(
                    values =
                        project.outcomes,
                    maximumItems =
                        MAXIMUM_PROJECT_LIST_ITEM_COUNT,
                    maximumItemLength =
                        MAXIMUM_ACTION_LENGTH
                ),
            metrics =
                normalizeStrings(
                    values =
                        project.metrics,
                    maximumItems =
                        MAXIMUM_METRIC_COUNT,
                    maximumItemLength =
                        MAXIMUM_METRIC_LENGTH
                ),
            relatedSkills =
                normalizeStrings(
                    values =
                        project.relatedSkills,
                    maximumItems =
                        MAXIMUM_RELATED_SKILL_COUNT,
                    maximumItemLength =
                        MAXIMUM_SKILL_NAME_LENGTH
                ),
            evidence =
                evidence
        ).normalized()
    }

    private fun achievementDtoToDomain(
        achievement: CvAchievementDto
    ): CvAchievement? {
        val id =
            achievement.id
                .cleanText()
                .take(
                    MAXIMUM_EVIDENCE_ID_LENGTH
                )

        val title =
            achievement.title
                .cleanText()
                .take(
                    MAXIMUM_TITLE_LENGTH
                )

        val evidence =
            achievement.evidence
                .cleanText()
                .take(
                    MAXIMUM_EVIDENCE_LENGTH
                )

        if (
            id.isBlank() ||
            title.isBlank() ||
            evidence.isBlank()
        ) {
            return null
        }

        return CvAchievement(
            id =
                id,
            title =
                title,
            context =
                normalizeOptional(
                    value =
                        achievement.context,
                    maximumLength =
                        MAXIMUM_CONTEXT_LENGTH
                ),
            candidateAction =
                normalizeOptional(
                    value =
                        achievement.candidateAction,
                    maximumLength =
                        MAXIMUM_DETAIL_LENGTH
                ),
            result =
                normalizeOptional(
                    value =
                        achievement.result,
                    maximumLength =
                        MAXIMUM_DETAIL_LENGTH
                ),
            metric =
                normalizeOptional(
                    value =
                        achievement.metric,
                    maximumLength =
                        MAXIMUM_METRIC_LENGTH
                ),
            evidence =
                evidence,
            relatedSkills =
                normalizeStrings(
                    values =
                        achievement.relatedSkills,
                    maximumItems =
                        MAXIMUM_RELATED_SKILL_COUNT,
                    maximumItemLength =
                        MAXIMUM_SKILL_NAME_LENGTH
                )
        ).normalized()
    }

    private fun workHighlightDtoToDomain(
        highlight: CvWorkHighlightDto
    ): CvWorkHighlight? {
        val id =
            highlight.id
                .cleanText()
                .take(
                    MAXIMUM_EVIDENCE_ID_LENGTH
                )

        val title =
            highlight.title
                .cleanText()
                .take(
                    MAXIMUM_TITLE_LENGTH
                )

        val evidence =
            highlight.evidence
                .cleanText()
                .take(
                    MAXIMUM_EVIDENCE_LENGTH
                )

        if (
            id.isBlank() ||
            title.isBlank() ||
            evidence.isBlank()
        ) {
            return null
        }

        return CvWorkHighlight(
            id =
                id,
            title =
                title,
            organizationContext =
                normalizeOptional(
                    value =
                        highlight.organizationContext,
                    maximumLength =
                        MAXIMUM_ORGANIZATION_CONTEXT_LENGTH
                ),
            candidateRole =
                normalizeOptional(
                    value =
                        highlight.candidateRole,
                    maximumLength =
                        MAXIMUM_TARGET_ROLE_LENGTH
                ),
            responsibility =
                normalizeOptional(
                    value =
                        highlight.responsibility,
                    maximumLength =
                        MAXIMUM_DETAIL_LENGTH
                ),
            action =
                normalizeOptional(
                    value =
                        highlight.action,
                    maximumLength =
                        MAXIMUM_DETAIL_LENGTH
                ),
            outcome =
                normalizeOptional(
                    value =
                        highlight.outcome,
                    maximumLength =
                        MAXIMUM_DETAIL_LENGTH
                ),
            metric =
                normalizeOptional(
                    value =
                        highlight.metric,
                    maximumLength =
                        MAXIMUM_METRIC_LENGTH
                ),
            evidence =
                evidence,
            relatedSkills =
                normalizeStrings(
                    values =
                        highlight.relatedSkills,
                    maximumItems =
                        MAXIMUM_RELATED_SKILL_COUNT,
                    maximumItemLength =
                        MAXIMUM_SKILL_NAME_LENGTH
                )
        ).normalized()
    }

    private fun normalizeCvText(
        value: String
    ): String {
        return value
            .replace(
                "\u0000",
                ""
            )
            .replace(
                HORIZONTAL_WHITESPACE_REGEX,
                " "
            )
            .replace(
                LINE_SPACING_REGEX,
                "\n"
            )
            .replace(
                EXCESSIVE_NEWLINES_REGEX,
                "\n\n"
            )
            .trim()
            .take(
                MAXIMUM_CV_TEXT_LENGTH
            )
    }

    private fun normalizeStrings(
        values: List<String>,
        maximumItems: Int,
        maximumItemLength: Int
    ): List<String> {
        return values
            .asSequence()
            .map { value ->
                value
                    .cleanText()
                    .take(
                        maximumItemLength
                    )
            }
            .filter(
                String::isNotBlank
            )
            .distinctBy { value ->
                value.lowercase(
                    Locale.ROOT
                )
            }
            .take(
                maximumItems
            )
            .toList()
    }

    private fun normalizeOptional(
        value: String?,
        maximumLength: Int
    ): String? {
        return value
            ?.cleanText()
            ?.take(
                maximumLength
            )
            ?.takeIf(
                String::isNotBlank
            )
    }

    private fun normalizeConfidence(
        value: Double
    ): Double {
        if (!value.isFinite()) {
            return 0.0
        }

        return value.coerceIn(
            minimumValue =
                0.0,
            maximumValue =
                1.0
        )
    }

    private fun String.cleanText():
            String {
        return replace(
            "\u0000",
            ""
        )
            .replace(
                WHITESPACE_REGEX,
                " "
            )
            .trim()
    }

    private fun createRequestId():
            String {
        return "cv-${UUID.randomUUID()}"
    }

    companion object {
        private const val MINIMUM_CV_TEXT_LENGTH =
            80

        private const val MAXIMUM_CV_TEXT_LENGTH =
            30_000

        private const val MAXIMUM_TARGET_ROLE_LENGTH =
            150

        private const val MAXIMUM_EXPERIENCE_LEVEL_LENGTH =
            50

        private const val MAXIMUM_MODEL_VERSION_LENGTH =
            150

        private const val MAXIMUM_SKILL_CODE_COUNT =
            100

        private const val MAXIMUM_SKILL_NAME_LENGTH =
            100

        private const val MAXIMUM_SKILL_COUNT =
            30

        private const val MAXIMUM_EVIDENCE_ID_LENGTH =
            120

        private const val MAXIMUM_CERTIFICATION_NAME_LENGTH =
            150

        private const val MAXIMUM_ORGANIZATION_LENGTH =
            150

        private const val MAXIMUM_ORGANIZATION_CONTEXT_LENGTH =
            180

        private const val MAXIMUM_LEVEL_LENGTH =
            100

        private const val MAXIMUM_PROJECT_NAME_LENGTH =
            160

        private const val MAXIMUM_TITLE_LENGTH =
            180

        private const val MAXIMUM_CONTEXT_LENGTH =
            200

        private const val MAXIMUM_DETAIL_LENGTH =
            220

        private const val MAXIMUM_ACTION_LENGTH =
            180

        private const val MAXIMUM_METRIC_LENGTH =
            120

        private const val MAXIMUM_EVIDENCE_LENGTH =
            240

        private const val MAXIMUM_RELATED_SKILL_COUNT =
            8

        private const val MAXIMUM_PROJECT_LIST_ITEM_COUNT =
            8

        private const val MAXIMUM_METRIC_COUNT =
            6

        private const val MAXIMUM_SUGGESTED_ROLE_COUNT =
            5

        private const val MAXIMUM_EXPERIENCE_SIGNAL_COUNT =
            15

        private const val MAXIMUM_CERTIFICATION_COUNT =
            12

        private const val MAXIMUM_PROJECT_COUNT =
            12

        private const val MAXIMUM_ACHIEVEMENT_COUNT =
            15

        private const val MAXIMUM_WORK_HIGHLIGHT_COUNT =
            15

        private const val MINIMUM_CREDENTIAL_YEAR =
            1900

        private const val MAXIMUM_CREDENTIAL_YEAR =
            2200

        private const val DEFAULT_MODEL_NAME =
            "Qwen"

        private val WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )

        private val HORIZONTAL_WHITESPACE_REGEX =
            Regex(
                "[\\t\\x0B\\f\\r ]+"
            )

        private val LINE_SPACING_REGEX =
            Regex(
                " *\\n *"
            )

        private val EXCESSIVE_NEWLINES_REGEX =
            Regex(
                "\\n{3,}"
            )
    }

    override suspend fun getSavedProfile():
            Result<CvProfile?> {
        return Result.success(
            null
        )
    }

    override suspend fun clearSavedProfile():
            Result<Unit> {
        return Result.success(
            Unit
        )
    }
}