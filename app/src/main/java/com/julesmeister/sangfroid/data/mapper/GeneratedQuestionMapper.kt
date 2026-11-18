// app/src/main/java/com/julesmeister/sangfroid/data/mapper/GeneratedQuestionMapper.kt

package com.julesmeister.sangfroid.data.mapper

import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.data.local.entity.QuestionSkillCrossRef
import com.julesmeister.sangfroid.data.remote.dto.GeneratedQuestionDto
import com.julesmeister.sangfroid.domain.model.Difficulty
import com.julesmeister.sangfroid.domain.model.QuestionAngle
import com.julesmeister.sangfroid.domain.model.QuestionCategory
import com.julesmeister.sangfroid.domain.model.SkillCode
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeneratedQuestionMapper @Inject constructor() {

    fun map(
        generatedQuestion: GeneratedQuestionDto,
        generatedAt: Long,
        source: String = SOURCE_QWEN
    ): MappedGeneratedQuestion {
        val questionId =
            generatedQuestion.id
                .trim()

        require(
            questionId.isNotBlank()
        ) {
            "Generated question ID cannot be blank."
        }

        val questionText =
            generatedQuestion.question
                .trim()

        require(
            questionText.isNotBlank()
        ) {
            "Generated question text cannot be blank."
        }

        val whyAsked =
            generatedQuestion.whyAsked
                .trim()

        require(
            whyAsked.isNotBlank()
        ) {
            "Generated question whyAsked cannot be blank."
        }

        val normalizedSource =
            source
                .trim()
                .uppercase(
                    Locale.ROOT
                )
                .takeIf(
                    String::isNotBlank
                )
                ?: SOURCE_QWEN

        val evidenceType =
            generatedQuestion
                .evidenceType
                ?.normalizeEnumValue()
                ?.takeIf(
                    String::isNotBlank
                )

        val evidenceId =
            generatedQuestion
                .evidenceId
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )

        require(
            (evidenceType == null) ==
                    (evidenceId == null)
        ) {
            "Generated evidenceType and evidenceId must both be present or both be absent."
        }

        val skillCodes =
            generatedQuestion
                .skillCodes
                .mapNotNull { value ->
                    parseSkillCode(
                        value
                    )
                }
                .distinct()
                .take(
                    MAXIMUM_SKILL_COUNT
                )
                .ifEmpty {
                    listOf(
                        SkillCode.GENERAL
                    )
                }

        val timestamp =
            generatedAt
                .takeIf { value ->
                    value > 0L
                }
                ?: System.currentTimeMillis()

        val question =
            QuestionEntity(
                id =
                    questionId,
                question =
                    questionText,
                category =
                    parseQuestionCategory(
                        generatedQuestion.category
                    ),
                difficulty =
                    parseDifficulty(
                        generatedQuestion.difficulty
                    ),
                whyAsked =
                    whyAsked,
                answerFramework =
                    generatedQuestion
                        .answerFramework
                        ?.trim()
                        ?.takeIf(
                            String::isNotBlank
                        ),
                estimatedMinutes =
                    generatedQuestion
                        .estimatedMinutes
                        .coerceIn(
                            minimumValue = 1,
                            maximumValue = 30
                        ),
                source =
                    normalizedSource,
                generationReason =
                    generatedQuestion
                        .generationReason
                        .trim()
                        .takeIf(
                            String::isNotBlank
                        ),
                evidenceType =
                    evidenceType,
                evidenceId =
                    evidenceId,
                questionAngle =
                    parseQuestionAngle(
                        generatedQuestion.questionAngle
                    ),
                isActive =
                    true,
                createdAt =
                    timestamp,
                updatedAt =
                    timestamp
            )

        val crossReferences =
            skillCodes.map { skillCode ->
                QuestionSkillCrossRef(
                    questionId =
                        questionId,
                    skillCode =
                        skillCode
                )
            }

        return MappedGeneratedQuestion(
            question =
                question,
            skillCrossReferences =
                crossReferences
        )
    }

    private fun parseQuestionCategory(
        value: String
    ): QuestionCategory {
        return when (
            value.normalizeEnumValue()
        ) {
            "HR" ->
                QuestionCategory.HR

            "BEHAVIORAL" ->
                QuestionCategory.BEHAVIORAL

            "TECHNICAL" ->
                QuestionCategory.TECHNICAL

            "SYSTEM_DESIGN" ->
                QuestionCategory.SYSTEM_DESIGN

            "DOMAIN" ->
                QuestionCategory.DOMAIN

            "PROJECT" ->
                QuestionCategory.PROJECT

            else ->
                throw IllegalArgumentException(
                    "Unsupported generated question category: $value"
                )
        }
    }

    private fun parseDifficulty(
        value: String
    ): Difficulty {
        return when (
            value.normalizeEnumValue()
        ) {
            "BEGINNER",
            "FOUNDATION" ->
                Difficulty.FOUNDATION

            "INTERMEDIATE" ->
                Difficulty.INTERMEDIATE

            "ADVANCED" ->
                Difficulty.ADVANCED

            else ->
                throw IllegalArgumentException(
                    "Unsupported generated question difficulty: $value"
                )
        }
    }

    private fun parseQuestionAngle(
        value: String
    ): QuestionAngle {
        return runCatching {
            QuestionAngle.valueOf(
                value.normalizeEnumValue()
            )
        }.getOrDefault(
            QuestionAngle.GENERAL
        )
    }

    private fun parseSkillCode(
        value: String
    ): SkillCode? {
        val normalized =
            value.normalizeEnumValue()

        if (
            normalized ==
            "GENERAL_COMMUNICATION"
        ) {
            return SkillCode.COMMUNICATION
        }

        return runCatching {
            SkillCode.valueOf(
                normalized
            )
        }.getOrNull()
    }

    private fun String.normalizeEnumValue():
            String {
        return trim()
            .uppercase(
                Locale.ROOT
            )
            .replace(
                ENUM_SEPARATOR_REGEX,
                "_"
            )
            .trim(
                '_'
            )
    }

    data class MappedGeneratedQuestion(
        val question: QuestionEntity,
        val skillCrossReferences:
        List<QuestionSkillCrossRef>
    )

    companion object {
        const val SOURCE_QWEN =
            "QWEN"

        const val SOURCE_CUSTOM =
            "CUSTOM"

        private const val MAXIMUM_SKILL_COUNT =
            4

        private val ENUM_SEPARATOR_REGEX =
            Regex(
                "[^A-Z0-9]+"
            )
    }
}