// app/src/main/java/com/julesmeister/sangfroid/data/recommendation/RecommendationContextBuilder.kt

package com.julesmeister.sangfroid.data.recommendation

import com.julesmeister.sangfroid.data.local.dao.QuestionDao
import com.julesmeister.sangfroid.data.local.dao.QuestionInteractionDao
import com.julesmeister.sangfroid.domain.cv.CvAnalysisRepository
import com.julesmeister.sangfroid.domain.cv.CvCertificationStatus
import com.julesmeister.sangfroid.domain.cv.CvProfile
import com.julesmeister.sangfroid.domain.model.SwipeDirection
import com.julesmeister.sangfroid.domain.recommendation.CvEvidence
import com.julesmeister.sangfroid.domain.recommendation.CvEvidenceType
import com.julesmeister.sangfroid.domain.recommendation.RecommendationContext
import com.julesmeister.sangfroid.domain.repository.ProfileRepository
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationContextBuilder @Inject constructor(
    private val questionDao: QuestionDao,
    private val questionInteractionDao:
    QuestionInteractionDao,
    private val profileRepository:
    ProfileRepository,
    private val cvAnalysisRepository:
    CvAnalysisRepository
) {

    suspend fun build():
            RecommendationContext {
        val profile =
            profileRepository
                .getProfile()

        val savedCvProfile =
            cvAnalysisRepository
                .getSavedProfile()
                .getOrNull()

        val weakSkills =
            buildWeakSkills()

        val skippedCategories =
            questionInteractionDao
                .getMostInteractedCategories(
                    direction =
                        SwipeDirection.LEFT,
                    minimumCount =
                        MINIMUM_SKIP_EVIDENCE,
                    limit =
                        SKIPPED_CATEGORY_LIMIT
                )
                .map { interaction ->
                    interaction.category
                }
                .distinct()

        val savedCategories =
            questionInteractionDao
                .getMostInteractedCategories(
                    direction =
                        SwipeDirection.RIGHT,
                    minimumCount =
                        MINIMUM_SAVE_EVIDENCE,
                    limit =
                        PREFERRED_CATEGORY_LIMIT
                )
                .map { interaction ->
                    interaction.category
                }
                .distinct()

        val storedPreferredCategories =
            questionDao
                .getPreferredCategories(
                    limit =
                        PREFERRED_CATEGORY_LIMIT
                )
                .distinct()

        val preferredCategories =
            (
                    savedCategories +
                            storedPreferredCategories
                    )
                .distinct()
                .filterNot { category ->
                    category in
                            skippedCategories
                }
                .take(
                    PREFERRED_CATEGORY_LIMIT
                )

        val recentQuestionIds =
            questionDao
                .getRecentQuestionIds(
                    limit =
                        RECENT_QUESTION_LIMIT
                )
                .asSequence()
                .map(
                    String::trim
                )
                .filter(
                    String::isNotBlank
                )
                .distinct()
                .toList()

        val savedSkillCodes =
            questionDao
                .getSavedSkillCodes(
                    limit =
                        SAVED_SKILL_LIMIT
                )
                .distinct()

        val profileSkills =
            profile
                ?.recommendationSkills
                .orEmpty()
                .distinct()

        val cvEvidence =
            savedCvProfile
                ?.let(
                    ::buildCvEvidence
                )
                .orEmpty()
                .take(
                    CV_EVIDENCE_LIMIT
                )

        return RecommendationContext(
            targetRole =
                profile
                    ?.targetRole
                    ?.trim()
                    ?.takeIf(
                        String::isNotBlank
                    ),
            experienceLevel =
                profile
                    ?.experienceLevel
                    ?.trim()
                    ?.takeIf(
                        String::isNotBlank
                    ),
            cvSkills =
                profileSkills,
            cvEvidence =
                cvEvidence,
            weakSkills =
                weakSkills,
            recentQuestionIds =
                recentQuestionIds,
            savedSkillCodes =
                savedSkillCodes,
            skippedCategories =
                skippedCategories,
            preferredCategories =
                preferredCategories,
            explorationRatio =
                calculateExplorationRatio(
                    profileSkillCount =
                        profileSkills.size,
                    cvEvidenceCount =
                        cvEvidence.size,
                    weakSkillCount =
                        weakSkills.size,
                    skippedCategoryCount =
                        skippedCategories.size,
                    preferredCategoryCount =
                        preferredCategories.size
                ),
            languageTag =
                Locale
                    .getDefault()
                    .toLanguageTag()
                    .ifBlank {
                        DEFAULT_LANGUAGE_TAG
                    }
        ).normalized()
    }

    private fun buildCvEvidence(
        profile: CvProfile
    ): List<CvEvidence> {
        val projects =
            profile.projects.map { project ->
                CvEvidence(
                    id =
                        project.id,
                    type =
                        CvEvidenceType.PROJECT,
                    title =
                        project.name,
                    context =
                        combineText(
                            project.context,
                            project.objective
                        ),
                    candidateRole =
                        project.candidateRole,
                    actions =
                        project.actions,
                    methods =
                        project.methods,
                    tools =
                        project.tools,
                    outcomes =
                        project.outcomes,
                    metrics =
                        project.metrics,
                    relatedSkills =
                        project.relatedSkills,
                    evidence =
                        project.evidence
                )
            }

        val achievements =
            profile.achievements.map { achievement ->
                CvEvidence(
                    id =
                        achievement.id,
                    type =
                        CvEvidenceType.ACHIEVEMENT,
                    title =
                        achievement.title,
                    context =
                        achievement.context,
                    actions =
                        listOfNotNull(
                            achievement.candidateAction
                        ),
                    outcomes =
                        listOfNotNull(
                            achievement.result
                        ),
                    metrics =
                        listOfNotNull(
                            achievement.metric
                        ),
                    relatedSkills =
                        achievement.relatedSkills,
                    evidence =
                        achievement.evidence
                )
            }

        val workHighlights =
            profile.workHighlights.map { highlight ->
                CvEvidence(
                    id =
                        highlight.id,
                    type =
                        CvEvidenceType.WORK_HIGHLIGHT,
                    title =
                        highlight.title,
                    context =
                        combineText(
                            highlight.organizationContext,
                            highlight.responsibility
                        ),
                    candidateRole =
                        highlight.candidateRole,
                    actions =
                        listOfNotNull(
                            highlight.action
                        ),
                    outcomes =
                        listOfNotNull(
                            highlight.outcome
                        ),
                    metrics =
                        listOfNotNull(
                            highlight.metric
                        ),
                    relatedSkills =
                        highlight.relatedSkills,
                    evidence =
                        highlight.evidence
                )
            }

        val certifications =
            profile.certifications.map { certification ->
                CvEvidence(
                    id =
                        certification.id,
                    type =
                        CvEvidenceType.CERTIFICATION,
                    title =
                        certification.name,
                    context =
                        buildCertificationContext(
                            issuer =
                                certification.issuer,
                            level =
                                certification.level,
                            status =
                                certification.status,
                            issuedYear =
                                certification.issuedYear
                        ),
                    relatedSkills =
                        certification.relatedSkills,
                    evidence =
                        certification.evidence
                )
            }

        return (
                projects +
                        achievements +
                        workHighlights +
                        certifications
                )
            .asSequence()
            .map(
                CvEvidence::normalized
            )
            .distinctBy { evidence ->
                evidence.type to
                        evidence.id
            }
            .take(
                CV_EVIDENCE_LIMIT
            )
            .toList()
    }

    private fun buildCertificationContext(
        issuer: String?,
        level: String?,
        status: CvCertificationStatus,
        issuedYear: Int?
    ): String? {
        val statusLabel =
            status
                .takeUnless {
                    it ==
                            CvCertificationStatus.UNKNOWN
                }
                ?.name
                ?.lowercase(
                    Locale.ROOT
                )
                ?.split(
                    "_"
                )
                ?.filter(
                    String::isNotBlank
                )
                ?.joinToString(
                    separator =
                        " "
                ) { word ->
                    word.replaceFirstChar { character ->
                        character.titlecase(
                            Locale.getDefault()
                        )
                    }
                }

        return listOfNotNull(
            issuer
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                ),
            level
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                ),
            statusLabel
                ?.takeIf(
                    String::isNotBlank
                ),
            issuedYear
                ?.toString()
        )
            .distinct()
            .joinToString(
                separator =
                    " • "
            )
            .takeIf(
                String::isNotBlank
            )
    }

    private fun combineText(
        first: String?,
        second: String?
    ): String? {
        return listOfNotNull(
            first
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                ),
            second
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )
        )
            .distinct()
            .joinToString(
                separator =
                    " • "
            )
            .takeIf(
                String::isNotBlank
            )
    }

    private suspend fun buildWeakSkills():
            List<RecommendationContext.WeakSkill> {
        return questionDao
            .getWeakSkillRows(
                limit =
                    WEAK_SKILL_LIMIT
            )
            .filter { row ->
                row.evidenceCount >=
                        MINIMUM_WEAK_SKILL_EVIDENCE
            }
            .map { row ->
                val missingPoints =
                    questionDao
                        .getRecentMissingPointsForSkill(
                            skillCode =
                                row.skillCode,
                            limit =
                                MISSING_POINT_ROW_LIMIT
                        )
                        .flatMap { encodedPoints ->
                            encodedPoints
                                .splitStorageString()
                        }
                        .asSequence()
                        .map(
                            String::trim
                        )
                        .filter(
                            String::isNotBlank
                        )
                        .distinct()
                        .take(
                            MISSING_POINT_LIMIT
                        )
                        .toList()

                RecommendationContext.WeakSkill(
                    skillCode =
                        row.skillCode,
                    averageScore =
                        row.averageScore
                            .toFloat()
                            .coerceIn(
                                MINIMUM_SCORE,
                                MAXIMUM_SCORE
                            ),
                    evidenceCount =
                        row.evidenceCount,
                    missingPoints =
                        missingPoints
                )
            }
            .filter { weakSkill ->
                weakSkill.averageScore <
                        WEAK_SKILL_SCORE_THRESHOLD
            }
    }

    private fun calculateExplorationRatio(
        profileSkillCount: Int,
        cvEvidenceCount: Int,
        weakSkillCount: Int,
        skippedCategoryCount: Int,
        preferredCategoryCount: Int
    ): Float {
        val profileEvidenceCount =
            profileSkillCount +
                    cvEvidenceCount

        val behaviorEvidenceCount =
            weakSkillCount +
                    skippedCategoryCount +
                    preferredCategoryCount

        return when {
            profileEvidenceCount == 0 &&
                    behaviorEvidenceCount == 0 -> {
                COLD_START_EXPLORATION_RATIO
            }

            profileEvidenceCount >=
                    ESTABLISHED_PROFILE_SIGNAL_COUNT &&
                    weakSkillCount >=
                    ESTABLISHED_WEAK_SKILL_COUNT -> {
                ESTABLISHED_PROFILE_EXPLORATION_RATIO
            }

            behaviorEvidenceCount >=
                    STRONG_BEHAVIOR_SIGNAL_COUNT -> {
                ESTABLISHED_PROFILE_EXPLORATION_RATIO
            }

            else -> {
                DEFAULT_EXPLORATION_RATIO
            }
        }
    }

    private fun String.splitStorageString():
            List<String> {
        if (isBlank()) {
            return emptyList()
        }

        return split(
            STORAGE_SEPARATOR
        )
            .asSequence()
            .map(
                String::trim
            )
            .filter(
                String::isNotBlank
            )
            .toList()
    }

    companion object {
        private const val RECENT_QUESTION_LIMIT =
            20

        private const val WEAK_SKILL_LIMIT =
            8

        private const val SAVED_SKILL_LIMIT =
            12

        private const val SKIPPED_CATEGORY_LIMIT =
            3

        private const val PREFERRED_CATEGORY_LIMIT =
            3

        private const val CV_EVIDENCE_LIMIT =
            30

        private const val MISSING_POINT_ROW_LIMIT =
            4

        private const val MISSING_POINT_LIMIT =
            6

        private const val MINIMUM_SKIP_EVIDENCE =
            2

        private const val MINIMUM_SAVE_EVIDENCE =
            1

        private const val MINIMUM_WEAK_SKILL_EVIDENCE =
            2

        private const val ESTABLISHED_PROFILE_SIGNAL_COUNT =
            4

        private const val ESTABLISHED_WEAK_SKILL_COUNT =
            2

        private const val STRONG_BEHAVIOR_SIGNAL_COUNT =
            5

        private const val MINIMUM_SCORE =
            0f

        private const val MAXIMUM_SCORE =
            5f

        private const val WEAK_SKILL_SCORE_THRESHOLD =
            3.5f

        private const val COLD_START_EXPLORATION_RATIO =
            0.25f

        private const val DEFAULT_EXPLORATION_RATIO =
            0.15f

        private const val ESTABLISHED_PROFILE_EXPLORATION_RATIO =
            0.10f

        private const val DEFAULT_LANGUAGE_TAG =
            "en-US"

        private const val STORAGE_SEPARATOR =
            "\u001E"
    }
}