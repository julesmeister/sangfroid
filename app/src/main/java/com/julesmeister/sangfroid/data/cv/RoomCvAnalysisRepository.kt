package com.julesmeister.sangfroid.data.cv

import com.julesmeister.sangfroid.data.local.dao.CvProfileDao
import com.julesmeister.sangfroid.data.local.dao.StoredCvProfile
import com.julesmeister.sangfroid.data.local.entity.CvAchievementEntity
import com.julesmeister.sangfroid.data.local.entity.CvCertificationEntity
import com.julesmeister.sangfroid.data.local.entity.CvProfileEntity
import com.julesmeister.sangfroid.data.local.entity.CvProjectEntity
import com.julesmeister.sangfroid.data.local.entity.CvSkillEntity
import com.julesmeister.sangfroid.data.local.entity.CvSkillMappingEntity
import com.julesmeister.sangfroid.data.local.entity.CvWorkHighlightEntity
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
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException

@Singleton
class RoomCvAnalysisRepository @Inject constructor(
    private val remoteRepository:
    RemoteCvAnalysisRepository,
    private val cvProfileDao:
    CvProfileDao
) : CvAnalysisRepository {

    override suspend fun analyze(
        cvText: String,
        currentTargetRole: String?,
        allowedSkillCodes: Set<SkillCode>
    ): Result<CvProfile> {
        return try {
            val profile =
                remoteRepository
                    .analyze(
                        cvText =
                            cvText,
                        currentTargetRole =
                            currentTargetRole,
                        allowedSkillCodes =
                            allowedSkillCodes
                    )
                    .getOrThrow()
                    .normalized()

            saveProfile(
                profile
            )

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

    override suspend fun getSavedProfile():
            Result<CvProfile?> {
        return try {
            val storedProfile =
                withContext(
                    Dispatchers.IO
                ) {
                    cvProfileDao
                        .getCompleteProfile()
                }

            Result.success(
                storedProfile
                    ?.let(
                        ::storedProfileToDomain
                    )
                    ?.normalized()
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

    override suspend fun clearSavedProfile():
            Result<Unit> {
        return try {
            withContext(
                Dispatchers.IO
            ) {
                cvProfileDao
                    .clearProfile()
            }

            Result.success(
                Unit
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

    private suspend fun saveProfile(
        profile: CvProfile
    ) {
        withContext(
            Dispatchers.IO
        ) {
            val profileId =
                CvProfileEntity
                    .ACTIVE_PROFILE_ID

            cvProfileDao.replaceProfile(
                profile =
                    profile.toEntity(
                        profileId
                    ),
                skills =
                    profile.skills
                        .map { skill ->
                            skill.toEntity(
                                profileId
                            )
                        },
                mappings =
                    profile.skillMappings
                        .map { mapping ->
                            mapping.toEntity(
                                profileId
                            )
                        },
                certifications =
                    profile.certifications
                        .map { certification ->
                            certification.toEntity(
                                profileId
                            )
                        },
                projects =
                    profile.projects
                        .map { project ->
                            project.toEntity(
                                profileId
                            )
                        },
                achievements =
                    profile.achievements
                        .map { achievement ->
                            achievement.toEntity(
                                profileId
                            )
                        },
                workHighlights =
                    profile.workHighlights
                        .map { highlight ->
                            highlight.toEntity(
                                profileId
                            )
                        }
            )
        }
    }

    private fun CvProfile.toEntity(
        profileId: Long
    ): CvProfileEntity {
        return CvProfileEntity(
            profileId =
                profileId,
            profileSummary =
                profileSummary.cleanText(),
            suggestedRolesJson =
                stringsToJson(
                    suggestedRoles
                ),
            suggestedExperienceLevel =
                suggestedExperienceLevel
                    ?.cleanText()
                    ?.takeIf(
                        String::isNotBlank
                    ),
            experienceSignalsJson =
                stringsToJson(
                    experienceSignals
                ),
            modelName =
                modelName
                    .cleanText()
                    .ifBlank {
                        DEFAULT_MODEL_NAME
                    },
            modelVersion =
                modelVersion
                    ?.cleanText()
                    ?.takeIf(
                        String::isNotBlank
                    ),
            analyzedAt =
                analyzedAt.takeIf {
                    it > 0L
                } ?: System.currentTimeMillis(),
            updatedAt =
                System.currentTimeMillis()
        )
    }

    private fun CvSkill.toEntity(
        profileId: Long
    ): CvSkillEntity {
        return CvSkillEntity(
            profileOwnerId =
                profileId,
            name =
                name.cleanText(),
            normalizedName =
                normalizedName
                    .cleanText()
                    .lowercase(
                        Locale.ROOT
                    ),
            type =
                type.name,
            confidence =
                normalizeConfidence(
                    confidence
                ),
            evidence =
                evidence.cleanText()
        )
    }

    private fun CvSkillMapping.toEntity(
        profileId: Long
    ): CvSkillMappingEntity {
        return CvSkillMappingEntity(
            profileOwnerId =
                profileId,
            extractedSkillName =
                extractedSkillName
                    .cleanText(),
            skillCode =
                skillCode.name,
            confidence =
                normalizeConfidence(
                    confidence
                ),
            reason =
                reason.cleanText()
        )
    }

    private fun CvCertification.toEntity(
        profileId: Long
    ): CvCertificationEntity {
        return CvCertificationEntity(
            profileOwnerId =
                profileId,
            evidenceId =
                id.cleanText(),
            name =
                name.cleanText(),
            issuer =
                issuer
                    ?.cleanText()
                    ?.takeIf(
                        String::isNotBlank
                    ),
            level =
                level
                    ?.cleanText()
                    ?.takeIf(
                        String::isNotBlank
                    ),
            status =
                status.name,
            issuedYear =
                issuedYear
                    ?.takeIf {
                        it in
                                MINIMUM_CREDENTIAL_YEAR..
                                MAXIMUM_CREDENTIAL_YEAR
                    },
            evidence =
                evidence.cleanText(),
            relatedSkillsJson =
                stringsToJson(
                    relatedSkills
                )
        )
    }

    private fun CvProject.toEntity(
        profileId: Long
    ): CvProjectEntity {
        return CvProjectEntity(
            profileOwnerId =
                profileId,
            evidenceId =
                id.cleanText(),
            name =
                name.cleanText(),
            context =
                context.cleanOptional(),
            candidateRole =
                candidateRole.cleanOptional(),
            objective =
                objective.cleanOptional(),
            actionsJson =
                stringsToJson(
                    actions
                ),
            methodsJson =
                stringsToJson(
                    methods
                ),
            toolsJson =
                stringsToJson(
                    tools
                ),
            outcomesJson =
                stringsToJson(
                    outcomes
                ),
            metricsJson =
                stringsToJson(
                    metrics
                ),
            relatedSkillsJson =
                stringsToJson(
                    relatedSkills
                ),
            evidence =
                evidence.cleanText()
        )
    }

    private fun CvAchievement.toEntity(
        profileId: Long
    ): CvAchievementEntity {
        return CvAchievementEntity(
            profileOwnerId =
                profileId,
            evidenceId =
                id.cleanText(),
            title =
                title.cleanText(),
            context =
                context.cleanOptional(),
            candidateAction =
                candidateAction.cleanOptional(),
            result =
                result.cleanOptional(),
            metric =
                metric.cleanOptional(),
            evidence =
                evidence.cleanText(),
            relatedSkillsJson =
                stringsToJson(
                    relatedSkills
                )
        )
    }

    private fun CvWorkHighlight.toEntity(
        profileId: Long
    ): CvWorkHighlightEntity {
        return CvWorkHighlightEntity(
            profileOwnerId =
                profileId,
            evidenceId =
                id.cleanText(),
            title =
                title.cleanText(),
            organizationContext =
                organizationContext.cleanOptional(),
            candidateRole =
                candidateRole.cleanOptional(),
            responsibility =
                responsibility.cleanOptional(),
            action =
                action.cleanOptional(),
            outcome =
                outcome.cleanOptional(),
            metric =
                metric.cleanOptional(),
            evidence =
                evidence.cleanText(),
            relatedSkillsJson =
                stringsToJson(
                    relatedSkills
                )
        )
    }

    private fun storedProfileToDomain(
        stored: StoredCvProfile
    ): CvProfile {
        return CvProfile(
            requestId =
                createStoredRequestId(
                    stored.profile
                ),
            profileSummary =
                stored.profile
                    .profileSummary,
            suggestedRoles =
                jsonToStrings(
                    stored.profile
                        .suggestedRolesJson
                ),
            suggestedExperienceLevel =
                stored.profile
                    .suggestedExperienceLevel,
            skills =
                stored.skills
                    .map(
                        ::skillEntityToDomain
                    ),
            skillMappings =
                stored.mappings
                    .mapNotNull(
                        ::mappingEntityToDomain
                    ),
            certifications =
                stored.certifications
                    .map(
                        ::certificationEntityToDomain
                    ),
            projects =
                stored.projects
                    .map(
                        ::projectEntityToDomain
                    ),
            achievements =
                stored.achievements
                    .map(
                        ::achievementEntityToDomain
                    ),
            workHighlights =
                stored.workHighlights
                    .map(
                        ::workHighlightEntityToDomain
                    ),
            experienceSignals =
                jsonToStrings(
                    stored.profile
                        .experienceSignalsJson
                ),
            modelName =
                stored.profile
                    .modelName,
            modelVersion =
                stored.profile
                    .modelVersion,
            analyzedAt =
                stored.profile
                    .analyzedAt
        )
    }

    private fun skillEntityToDomain(
        entity: CvSkillEntity
    ): CvSkill {
        return CvSkill(
            name =
                entity.name,
            normalizedName =
                entity.normalizedName,
            type =
                CvSkillType.entries
                    .firstOrNull { type ->
                        type.name.equals(
                            other =
                                entity.type,
                            ignoreCase =
                                true
                        )
                    }
                    ?: CvSkillType.PROFESSIONAL,
            confidence =
                normalizeConfidence(
                    entity.confidence
                ),
            evidence =
                entity.evidence
        )
    }

    private fun mappingEntityToDomain(
        entity: CvSkillMappingEntity
    ): CvSkillMapping? {
        val skillCode =
            SkillCode.entries
                .firstOrNull { code ->
                    code.name.equals(
                        other =
                            entity.skillCode,
                        ignoreCase =
                            true
                    )
                }
                ?: return null

        return CvSkillMapping(
            extractedSkillName =
                entity.extractedSkillName,
            skillCode =
                skillCode,
            confidence =
                normalizeConfidence(
                    entity.confidence
                ),
            reason =
                entity.reason
        )
    }

    private fun certificationEntityToDomain(
        entity: CvCertificationEntity
    ): CvCertification {
        return CvCertification(
            id =
                entity.evidenceId,
            name =
                entity.name,
            issuer =
                entity.issuer,
            level =
                entity.level,
            status =
                CvCertificationStatus.entries
                    .firstOrNull { status ->
                        status.name.equals(
                            other =
                                entity.status,
                            ignoreCase =
                                true
                        )
                    }
                    ?: CvCertificationStatus.UNKNOWN,
            issuedYear =
                entity.issuedYear,
            evidence =
                entity.evidence,
            relatedSkills =
                jsonToStrings(
                    entity.relatedSkillsJson
                )
        )
    }

    private fun projectEntityToDomain(
        entity: CvProjectEntity
    ): CvProject {
        return CvProject(
            id =
                entity.evidenceId,
            name =
                entity.name,
            context =
                entity.context,
            candidateRole =
                entity.candidateRole,
            objective =
                entity.objective,
            actions =
                jsonToStrings(
                    entity.actionsJson
                ),
            methods =
                jsonToStrings(
                    entity.methodsJson
                ),
            tools =
                jsonToStrings(
                    entity.toolsJson
                ),
            outcomes =
                jsonToStrings(
                    entity.outcomesJson
                ),
            metrics =
                jsonToStrings(
                    entity.metricsJson
                ),
            relatedSkills =
                jsonToStrings(
                    entity.relatedSkillsJson
                ),
            evidence =
                entity.evidence
        )
    }

    private fun achievementEntityToDomain(
        entity: CvAchievementEntity
    ): CvAchievement {
        return CvAchievement(
            id =
                entity.evidenceId,
            title =
                entity.title,
            context =
                entity.context,
            candidateAction =
                entity.candidateAction,
            result =
                entity.result,
            metric =
                entity.metric,
            evidence =
                entity.evidence,
            relatedSkills =
                jsonToStrings(
                    entity.relatedSkillsJson
                )
        )
    }

    private fun workHighlightEntityToDomain(
        entity: CvWorkHighlightEntity
    ): CvWorkHighlight {
        return CvWorkHighlight(
            id =
                entity.evidenceId,
            title =
                entity.title,
            organizationContext =
                entity.organizationContext,
            candidateRole =
                entity.candidateRole,
            responsibility =
                entity.responsibility,
            action =
                entity.action,
            outcome =
                entity.outcome,
            metric =
                entity.metric,
            evidence =
                entity.evidence,
            relatedSkills =
                jsonToStrings(
                    entity.relatedSkillsJson
                )
        )
    }

    private fun stringsToJson(
        values: List<String>
    ): String {
        val output =
            JSONArray()

        values
            .asSequence()
            .map { value ->
                value.cleanText()
            }
            .filter(
                String::isNotBlank
            )
            .distinctBy { value ->
                value.lowercase(
                    Locale.ROOT
                )
            }
            .forEach(
                output::put
            )

        return output.toString()
    }

    private fun jsonToStrings(
        value: String
    ): List<String> {
        if (value.isBlank()) {
            return emptyList()
        }

        return try {
            val jsonArray =
                JSONArray(
                    value
                )

            buildList {
                repeat(
                    jsonArray.length()
                ) { index ->
                    val item =
                        jsonArray
                            .optString(
                                index,
                                ""
                            )
                            .cleanText()

                    if (item.isNotBlank()) {
                        add(
                            item
                        )
                    }
                }
            }
                .distinctBy { item ->
                    item.lowercase(
                        Locale.ROOT
                    )
                }
        } catch (
            exception: JSONException
        ) {
            emptyList()
        }
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

    private fun String?.cleanOptional():
            String? {
        return this
            ?.cleanText()
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

    private fun createStoredRequestId(
        profile: CvProfileEntity
    ): String {
        return "stored-cv-${profile.analyzedAt}"
    }

    companion object {
        private const val DEFAULT_MODEL_NAME =
            "Qwen"

        private const val MINIMUM_CREDENTIAL_YEAR =
            1900

        private const val MAXIMUM_CREDENTIAL_YEAR =
            2200

        private val WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )
    }
}