package com.julesmeister.sangfroid.data.cv

import com.julesmeister.sangfroid.data.remote.dto.CvAchievementDto
import com.julesmeister.sangfroid.data.remote.dto.CvAnalysisResponseDto
import com.julesmeister.sangfroid.data.remote.dto.CvCertificationDto
import com.julesmeister.sangfroid.data.remote.dto.CvProjectDto
import com.julesmeister.sangfroid.data.remote.dto.CvSkillDto
import com.julesmeister.sangfroid.data.remote.dto.CvSkillMappingDto
import com.julesmeister.sangfroid.data.remote.dto.CvWorkHighlightDto
import com.julesmeister.sangfroid.domain.cv.CvAchievement
import com.julesmeister.sangfroid.domain.cv.CvCertification
import com.julesmeister.sangfroid.domain.cv.CvCertificationStatus
import com.julesmeister.sangfroid.domain.cv.CvProfile
import com.julesmeister.sangfroid.domain.cv.CvProject
import com.julesmeister.sangfroid.domain.cv.CvSkill
import com.julesmeister.sangfroid.domain.cv.CvSkillMapping
import com.julesmeister.sangfroid.domain.cv.CvSkillType
import com.julesmeister.sangfroid.domain.cv.CvWorkHighlight
import com.julesmeister.sangfroid.domain.model.SkillCode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CvAnalysisDtoMapper @Inject constructor() {

    fun toDomain(
        response: CvAnalysisResponseDto
    ): CvProfile {
        val validMappings =
            response
                .skillMappings
                .mapNotNull(
                    ::toDomain
                )

        return CvProfile(
            requestId =
                response.requestId,
            profileSummary =
                response.profileSummary,
            suggestedRoles =
                response.suggestedRoles,
            suggestedExperienceLevel =
                response.suggestedExperienceLevel,
            skills =
                response.skills.map(
                    ::toDomain
                ),
            skillMappings =
                validMappings,
            certifications =
                response.certifications.map(
                    ::toDomain
                ),
            projects =
                response.projects.map(
                    ::toDomain
                ),
            achievements =
                response.achievements.map(
                    ::toDomain
                ),
            workHighlights =
                response.workHighlights.map(
                    ::toDomain
                ),
            experienceSignals =
                response.experienceSignals,
            modelName =
                response.modelName,
            modelVersion =
                response.modelVersion,
            analyzedAt =
                response.analyzedAt
        ).normalized()
    }

    private fun toDomain(
        dto: CvSkillDto
    ): CvSkill {
        return CvSkill(
            name =
                dto.name,
            normalizedName =
                dto.normalizedName,
            type =
                CvSkillType.fromApi(
                    dto.type
                ),
            confidence =
                dto.confidence,
            evidence =
                dto.evidence
        )
    }

    private fun toDomain(
        dto: CvSkillMappingDto
    ): CvSkillMapping? {
        val skillCode =
            SkillCode.entries.firstOrNull {
                it.name.equals(
                    other =
                        dto.skillCode.trim(),
                    ignoreCase =
                        true
                )
            } ?: return null

        return CvSkillMapping(
            extractedSkillName =
                dto.extractedSkillName,
            skillCode =
                skillCode,
            confidence =
                dto.confidence,
            reason =
                dto.reason
        )
    }

    private fun toDomain(
        dto: CvCertificationDto
    ): CvCertification {
        return CvCertification(
            id =
                dto.id,
            name =
                dto.name,
            issuer =
                dto.issuer,
            level =
                dto.level,
            status =
                CvCertificationStatus.fromApi(
                    dto.status
                ),
            issuedYear =
                dto.issuedYear,
            evidence =
                dto.evidence,
            relatedSkills =
                dto.relatedSkills
        )
    }

    private fun toDomain(
        dto: CvProjectDto
    ): CvProject {
        return CvProject(
            id =
                dto.id,
            name =
                dto.name,
            context =
                dto.context,
            candidateRole =
                dto.candidateRole,
            objective =
                dto.objective,
            actions =
                dto.actions,
            methods =
                dto.methods,
            tools =
                dto.tools,
            outcomes =
                dto.outcomes,
            metrics =
                dto.metrics,
            relatedSkills =
                dto.relatedSkills,
            evidence =
                dto.evidence
        )
    }

    private fun toDomain(
        dto: CvAchievementDto
    ): CvAchievement {
        return CvAchievement(
            id =
                dto.id,
            title =
                dto.title,
            context =
                dto.context,
            candidateAction =
                dto.candidateAction,
            result =
                dto.result,
            metric =
                dto.metric,
            evidence =
                dto.evidence,
            relatedSkills =
                dto.relatedSkills
        )
    }

    private fun toDomain(
        dto: CvWorkHighlightDto
    ): CvWorkHighlight {
        return CvWorkHighlight(
            id =
                dto.id,
            title =
                dto.title,
            organizationContext =
                dto.organizationContext,
            candidateRole =
                dto.candidateRole,
            responsibility =
                dto.responsibility,
            action =
                dto.action,
            outcome =
                dto.outcome,
            metric =
                dto.metric,
            evidence =
                dto.evidence,
            relatedSkills =
                dto.relatedSkills
        )
    }
}