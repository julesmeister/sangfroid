package com.julesmeister.sangfroid.data.repository

import com.julesmeister.sangfroid.data.local.dao.QuestionDao
import com.julesmeister.sangfroid.data.mapper.GeneratedQuestionMapper
import com.julesmeister.sangfroid.data.recommendation.CvEvidenceSelector
import com.julesmeister.sangfroid.data.recommendation.FreshQuestionPlanner
import com.julesmeister.sangfroid.data.recommendation.QuestionSimilarityChecker
import com.julesmeister.sangfroid.data.remote.api.SangfroidApi
import com.julesmeister.sangfroid.data.remote.dto.CvEvidenceDto
import com.julesmeister.sangfroid.data.remote.dto.QuestionGenerationPlanDto
import com.julesmeister.sangfroid.data.remote.dto.QuestionGenerationRequestDto
import com.julesmeister.sangfroid.data.remote.dto.WeakSkillDto
import com.julesmeister.sangfroid.domain.recommendation.CvEvidence
import com.julesmeister.sangfroid.domain.recommendation.QuestionGenerationPlan
import com.julesmeister.sangfroid.domain.recommendation.RecommendationContext
import com.julesmeister.sangfroid.domain.repository.QuestionGenerationRepository
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

@Singleton
class OfflineFirstQuestionGenerationRepository
@Inject constructor(
    private val sangfroidApi: SangfroidApi,
    private val questionDao: QuestionDao,
    private val generatedQuestionMapper: GeneratedQuestionMapper,
    private val cvEvidenceSelector: CvEvidenceSelector,
    private val freshQuestionPlanner: FreshQuestionPlanner,
    private val questionSimilarityChecker: QuestionSimilarityChecker
) : QuestionGenerationRepository {

    override suspend fun generateAndStore(
        context: RecommendationContext,
        requestedCount: Int
    ): Result<QuestionGenerationRepository.GenerationResult> {
        require(
            requestedCount in
                    MINIMUM_REQUESTED_COUNT..
                    MAXIMUM_REQUESTED_COUNT
        ) {
            "requestedCount must be between one and ten."
        }

        return try {
            val normalizedContext =
                context.normalized()

            val existingQuestions =
                questionDao.getAllQuestions()

            val evidenceUsage =
                questionDao.getCvEvidenceUsage()

            val angleUsage =
                questionDao.getCvEvidenceAngleUsage()

            val selectedCvEvidence =
                cvEvidenceSelector.select(
                    evidence =
                        normalizedContext.cvEvidence,
                    usage =
                        evidenceUsage,
                    requestedQuestionCount =
                        requestedCount
                )

            val generationPlans =
                freshQuestionPlanner.createPlans(
                    evidence =
                        selectedCvEvidence,
                    evidenceUsage =
                        evidenceUsage,
                    angleUsage =
                        angleUsage,
                    requestedQuestionCount =
                        requestedCount
                )

            val requestId =
                UUID.randomUUID().toString()

            val request =
                createRequest(
                    requestId =
                        requestId,
                    requestedCount =
                        requestedCount,
                    context =
                        normalizedContext,
                    selectedCvEvidence =
                        selectedCvEvidence,
                    generationPlans =
                        generationPlans,
                    existingQuestions =
                        existingQuestions
                            .sortedByDescending { question ->
                                question.createdAt
                            }
                            .map { question ->
                                question.question
                            }
                            .distinctBy { question ->
                                normalizeQuestionText(
                                    question
                                )
                            }
                            .take(
                                MAXIMUM_PROMPT_EXISTING_QUESTIONS
                            )
                )

            val response =
                sangfroidApi
                    .generateQuestions(
                        request
                    )
                    .getOrThrow()

            require(
                response.requestId.isBlank() ||
                        response.requestId ==
                        requestId
            ) {
                "The generated-question response does not match the request."
            }

            require(
                response.questions.isNotEmpty()
            ) {
                "The server returned no generated questions."
            }

            require(
                response.questions.size <=
                        MAXIMUM_SERVER_CANDIDATE_COUNT
            ) {
                "The server returned too many generated questions."
            }

            validateEvidenceReferences(
                selectedCvEvidence =
                    selectedCvEvidence,
                responseQuestions =
                    response.questions.map { question ->
                        GeneratedEvidenceReference(
                            evidenceType =
                                question.evidenceType,
                            evidenceId =
                                question.evidenceId
                        )
                    }
            )

            val mappedCandidates =
                response.questions.mapNotNull { generatedQuestion ->
                    runCatching {
                        generatedQuestionMapper.map(
                            generatedQuestion =
                                generatedQuestion,
                            generatedAt =
                                response.generatedAt
                        )
                    }.getOrNull()
                }

            val review =
                questionSimilarityChecker.review(
                    candidates =
                        mappedCandidates.map { mapped ->
                            mapped.question
                        },
                    existingQuestions =
                        existingQuestions,
                    maximumAcceptedCount =
                        requestedCount
                )

            val acceptedIds =
                review.accepted
                    .mapTo(
                        mutableSetOf()
                    ) { question ->
                        question.id
                    }

            val acceptedMappedQuestions =
                mappedCandidates.filter { mapped ->
                    mapped.question.id in
                            acceptedIds
                }

            val acceptedQuestions =
                acceptedMappedQuestions.map { mapped ->
                    mapped.question
                }

            val acceptedSkillReferences =
                acceptedMappedQuestions
                    .flatMap { mapped ->
                        mapped.skillCrossReferences
                    }
                    .distinctBy { reference ->
                        reference.questionId to
                                reference.skillCode
                    }

            questionDao.insertQuestionCatalogue(
                questions =
                    acceptedQuestions,
                skills =
                    acceptedSkillReferences
            )

            Result.success(
                QuestionGenerationRepository
                    .GenerationResult(
                        requestId =
                            requestId,
                        requestedCount =
                            requestedCount,
                        candidateCount =
                            mappedCandidates.size,
                        storedQuestionCount =
                            acceptedQuestions.size,
                        storedQuestionIds =
                            acceptedQuestions.map { question ->
                                question.id
                            },
                        exactDuplicateCount =
                            review.exactDuplicates.size,
                        similarDuplicateCount =
                            review.similarDuplicates.size,
                        modelName =
                            response.modelName,
                        modelVersion =
                            response.modelVersion,
                        generatedAt =
                            response.generatedAt
                    )
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

    private fun createRequest(
        requestId: String,
        requestedCount: Int,
        context: RecommendationContext,
        selectedCvEvidence: List<CvEvidence>,
        generationPlans: List<QuestionGenerationPlan>,
        existingQuestions: List<String>
    ): QuestionGenerationRequestDto {
        return QuestionGenerationRequestDto(
            requestId =
                requestId,
            requestedCount =
                requestedCount,
            targetRole =
                context.targetRole,
            experienceLevel =
                context.experienceLevel,
            cvSkills =
                context.cvSkills.map { skill ->
                    skill.name
                },
            cvEvidence =
                selectedCvEvidence.map { evidence ->
                    evidence.toDto()
                },
            weakSkills =
                context.weakSkills.map { weakSkill ->
                    WeakSkillDto(
                        skillCode =
                            weakSkill.skillCode.name,
                        averageScore =
                            weakSkill.averageScore,
                        evidenceCount =
                            weakSkill.evidenceCount,
                        missingPoints =
                            weakSkill.missingPoints
                    )
                },
            generationPlans =
                generationPlans.map { plan ->
                    QuestionGenerationPlanDto(
                        evidenceType =
                            plan.evidence.type.name,
                        evidenceId =
                            plan.evidence.id,
                        requestedAngles =
                            plan.requestedAngles.map { angle ->
                                angle.name
                            },
                        coveredAngles =
                            plan.coveredAngles.map { angle ->
                                angle.name
                            },
                        previousQuestionCount =
                            plan.previousQuestionCount
                    )
                },
            existingQuestions =
                existingQuestions,
            recentQuestionIds =
                context.recentQuestionIds,
            savedSkillCodes =
                context.savedSkillCodes.map { skill ->
                    skill.name
                },
            skippedCategories =
                context.skippedCategories.map { category ->
                    category.name
                },
            preferredCategories =
                context.preferredCategories.map { category ->
                    category.name
                },
            explorationRatio =
                context.explorationRatio,
            languageTag =
                context.languageTag
        )
    }

    private fun CvEvidence.toDto():
            CvEvidenceDto {
        return CvEvidenceDto(
            id =
                id,
            type =
                type,
            title =
                title,
            context =
                context,
            candidateRole =
                candidateRole,
            actions =
                actions,
            methods =
                methods,
            tools =
                tools,
            outcomes =
                outcomes,
            metrics =
                metrics,
            relatedSkills =
                relatedSkills,
            evidence =
                evidence
        )
    }

    private fun validateEvidenceReferences(
        selectedCvEvidence: List<CvEvidence>,
        responseQuestions: List<GeneratedEvidenceReference>
    ) {
        val allowedReferences =
            selectedCvEvidence
                .map { evidence ->
                    EvidenceKey(
                        type =
                            evidence.type.name,
                        id =
                            evidence.id
                    )
                }
                .toSet()

        responseQuestions.forEach { reference ->
            val evidenceType =
                reference.evidenceType
                    ?.normalizeEnumValue()
                    ?.takeIf(
                        String::isNotBlank
                    )

            val evidenceId =
                reference.evidenceId
                    ?.trim()
                    ?.takeIf(
                        String::isNotBlank
                    )

            require(
                (evidenceType == null) ==
                        (evidenceId == null)
            ) {
                "Generated evidence type and ID must both be present or both be absent."
            }

            if (
                evidenceType != null &&
                evidenceId != null
            ) {
                require(
                    EvidenceKey(
                        type =
                            evidenceType,
                        id =
                            evidenceId
                    ) in allowedReferences
                ) {
                    "A generated question referenced CV evidence that was not supplied in the request."
                }
            }
        }
    }

    private fun normalizeQuestionText(
        value: String
    ): String {
        return value
            .lowercase(
                Locale.ROOT
            )
            .replace(
                NON_ALPHANUMERIC_REGEX,
                " "
            )
            .replace(
                WHITESPACE_REGEX,
                " "
            )
            .trim()
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

    private data class GeneratedEvidenceReference(
        val evidenceType: String?,
        val evidenceId: String?
    )

    private data class EvidenceKey(
        val type: String,
        val id: String
    )

    companion object {
        private const val MINIMUM_REQUESTED_COUNT =
            1

        private const val MAXIMUM_REQUESTED_COUNT =
            10

        private const val MAXIMUM_SERVER_CANDIDATE_COUNT =
            20

        private const val MAXIMUM_PROMPT_EXISTING_QUESTIONS =
            250

        private val ENUM_SEPARATOR_REGEX =
            Regex(
                "[^A-Z0-9]+"
            )

        private val NON_ALPHANUMERIC_REGEX =
            Regex(
                "[^\\p{L}\\p{N}]+"
            )

        private val WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )
    }
}