// app/src/main/java/com/julesmeister/sangfroid/domain/recommendation/RecommendationContext.kt

package com.julesmeister.sangfroid.domain.recommendation

import com.julesmeister.sangfroid.domain.model.QuestionCategory
import com.julesmeister.sangfroid.domain.model.SkillCode
import java.util.Locale

data class RecommendationContext(
    val targetRole: String? = null,
    val experienceLevel: String? = null,
    val cvSkills: List<SkillCode> =
        emptyList(),
    val cvEvidence: List<CvEvidence> =
        emptyList(),
    val weakSkills: List<WeakSkill> =
        emptyList(),
    val recentQuestionIds: List<String> =
        emptyList(),
    val savedSkillCodes: List<SkillCode> =
        emptyList(),
    val skippedCategories:
    List<QuestionCategory> =
        emptyList(),
    val preferredCategories:
    List<QuestionCategory> =
        emptyList(),
    val explorationRatio: Float =
        DEFAULT_EXPLORATION_RATIO,
    val languageTag: String =
        Locale.getDefault()
            .toLanguageTag()
) {

    init {
        require(
            explorationRatio.isFinite() &&
                    explorationRatio in 0f..1f
        ) {
            "explorationRatio must be between zero and one."
        }

        require(
            languageTag.isNotBlank()
        ) {
            "languageTag cannot be blank."
        }

        require(
            recentQuestionIds.none(
                String::isBlank
            )
        ) {
            "recentQuestionIds cannot contain blank values."
        }
    }

    data class WeakSkill(
        val skillCode: SkillCode,
        val averageScore: Float,
        val evidenceCount: Int,
        val missingPoints: List<String>
    ) {

        init {
            require(
                averageScore.isFinite() &&
                        averageScore in 0f..5f
            ) {
                "averageScore must be between zero and five."
            }

            require(
                evidenceCount >= 0
            ) {
                "evidenceCount cannot be negative."
            }
        }

        fun normalized():
                WeakSkill {
            return copy(
                averageScore =
                    averageScore.coerceIn(
                        0f,
                        5f
                    ),
                evidenceCount =
                    evidenceCount
                        .coerceAtLeast(
                            0
                        ),
                missingPoints =
                    missingPoints
                        .normalizeTextList(
                            maximumItems =
                                MAXIMUM_MISSING_POINT_COUNT,
                            maximumLength =
                                MAXIMUM_MISSING_POINT_LENGTH
                        )
            )
        }

        companion object {
            private const val MAXIMUM_MISSING_POINT_COUNT =
                20

            private const val MAXIMUM_MISSING_POINT_LENGTH =
                500
        }
    }

    fun normalized():
            RecommendationContext {
        return copy(
            targetRole =
                targetRole.normalizeOptional(
                    MAXIMUM_TARGET_ROLE_LENGTH
                ),
            experienceLevel =
                experienceLevel.normalizeOptional(
                    MAXIMUM_EXPERIENCE_LEVEL_LENGTH
                ),
            cvSkills =
                cvSkills
                    .distinct()
                    .take(
                        MAXIMUM_CV_SKILL_COUNT
                    ),
            cvEvidence =
                cvEvidence
                    .asSequence()
                    .map(
                        CvEvidence::normalized
                    )
                    .filter { evidence ->
                        evidence.id.isNotBlank() &&
                                evidence.title.isNotBlank()
                    }
                    .distinctBy { evidence ->
                        evidence.type to
                                evidence.id
                    }
                    .take(
                        MAXIMUM_CV_EVIDENCE_COUNT
                    )
                    .toList(),
            weakSkills =
                weakSkills
                    .asSequence()
                    .map(
                        WeakSkill::normalized
                    )
                    .distinctBy(
                        WeakSkill::skillCode
                    )
                    .take(
                        MAXIMUM_WEAK_SKILL_COUNT
                    )
                    .toList(),
            recentQuestionIds =
                recentQuestionIds
                    .normalizeTextList(
                        maximumItems =
                            MAXIMUM_RECENT_QUESTION_COUNT,
                        maximumLength =
                            MAXIMUM_IDENTIFIER_LENGTH
                    ),
            savedSkillCodes =
                savedSkillCodes
                    .distinct()
                    .take(
                        MAXIMUM_SAVED_SKILL_COUNT
                    ),
            skippedCategories =
                skippedCategories
                    .distinct()
                    .take(
                        MAXIMUM_CATEGORY_COUNT
                    ),
            preferredCategories =
                preferredCategories
                    .distinct()
                    .filterNot { category ->
                        category in
                                skippedCategories
                    }
                    .take(
                        MAXIMUM_CATEGORY_COUNT
                    ),
            explorationRatio =
                explorationRatio
                    .coerceIn(
                        0f,
                        1f
                    ),
            languageTag =
                languageTag
                    .trim()
                    .take(
                        MAXIMUM_LANGUAGE_TAG_LENGTH
                    )
                    .ifBlank {
                        DEFAULT_LANGUAGE_TAG
                    }
        )
    }

    companion object {
        private const val DEFAULT_EXPLORATION_RATIO =
            0.10f

        private const val DEFAULT_LANGUAGE_TAG =
            "en-US"

        private const val MAXIMUM_TARGET_ROLE_LENGTH =
            200

        private const val MAXIMUM_EXPERIENCE_LEVEL_LENGTH =
            100

        private const val MAXIMUM_IDENTIFIER_LENGTH =
            100

        private const val MAXIMUM_LANGUAGE_TAG_LENGTH =
            20

        private const val MAXIMUM_CV_SKILL_COUNT =
            100

        private const val MAXIMUM_CV_EVIDENCE_COUNT =
            30

        private const val MAXIMUM_WEAK_SKILL_COUNT =
            100

        private const val MAXIMUM_RECENT_QUESTION_COUNT =
            100

        private const val MAXIMUM_SAVED_SKILL_COUNT =
            100

        private const val MAXIMUM_CATEGORY_COUNT =
            20
    }
}

private fun String?.normalizeOptional(
    maximumLength: Int
): String? {
    return this
        ?.trim()
        ?.take(
            maximumLength
        )
        ?.takeIf(
            String::isNotBlank
        )
}

private fun List<String>.normalizeTextList(
    maximumItems: Int,
    maximumLength: Int
): List<String> {
    return asSequence()
        .map { value ->
            value
                .trim()
                .take(
                    maximumLength
                )
        }
        .filter(
            String::isNotBlank
        )
        .distinct()
        .take(
            maximumItems
        )
        .toList()
}