package com.julesmeister.sangfroid.data.recommendation

import com.julesmeister.sangfroid.data.local.dao.QuestionDao
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.domain.model.QuestionCategory
import com.julesmeister.sangfroid.domain.model.SkillCode
import com.julesmeister.sangfroid.domain.progress.QuestionProgress
import com.julesmeister.sangfroid.domain.progress.QuestionProgressRepository
import com.julesmeister.sangfroid.domain.recommendation.RecommendationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@Singleton
class QuestionRecommendationRanker @Inject constructor(
    private val questionDao:
    QuestionDao,
    private val questionProgressRepository:
    QuestionProgressRepository,
    private val recommendationContextBuilder:
    RecommendationContextBuilder
) {

    suspend fun rank(
        questions: List<QuestionEntity>,
        now: Long = System.currentTimeMillis()
    ): List<QuestionEntity> {
        return rankWithScores(
            questions =
                questions,
            now =
                now
        )
            .map(
                RankedQuestion::question
            )
    }

    suspend fun rankWithScores(
        questions: List<QuestionEntity>,
        now: Long = System.currentTimeMillis()
    ): List<RankedQuestion> {
        if (questions.isEmpty()) {
            return emptyList()
        }

        val questionIds =
            questions
                .map(
                    QuestionEntity::id
                )

        val context =
            runCatching {
                recommendationContextBuilder
                    .build()
            }
                .getOrElse {
                    RecommendationContext()
                }

        val skillsByQuestionId =
            runCatching {
                questionDao
                    .getQuestionSkills(
                        questionIds =
                            questionIds
                    )
            }
                .getOrDefault(
                    emptyList()
                )
                .groupBy(
                    keySelector = {
                        it.questionId
                    },
                    valueTransform = {
                        it.skillCode
                    }
                )

        val rankedQuestions =
            coroutineScope {
                questions
                    .map { question ->
                        async {
                            val progress =
                                loadQuestionProgress(
                                    questionId =
                                        question.id
                                )

                            val questionSkills =
                                skillsByQuestionId[
                                    question.id
                                ]
                                    .orEmpty()
                                    .toSet()

                            RankedQuestion(
                                question =
                                    question,
                                score =
                                    calculateScore(
                                        question =
                                            question,
                                        questionSkills =
                                            questionSkills,
                                        progress =
                                            progress,
                                        context =
                                            context,
                                        now =
                                            now
                                    )
                            )
                        }
                    }
                    .awaitAll()
            }

        return rankedQuestions
            .sortedWith(
                compareByDescending<RankedQuestion> {
                    it.score.total
                }
                    .thenByDescending {
                        it.score.weakSkillPriority
                    }
                    .thenByDescending {
                        it.score.profileRelevancePriority
                    }
                    .thenByDescending {
                        it.score.decliningPriority
                    }
                    .thenByDescending {
                        it.score.performancePriority
                    }
                    .thenByDescending {
                        it.score.recencyPriority
                    }
                    .thenBy {
                        it.question.createdAt
                    }
                    .thenBy {
                        it.question.id
                    }
            )
    }

    suspend fun explain(
        question: QuestionEntity,
        now: Long = System.currentTimeMillis()
    ): QuestionRecommendationScore {
        val context =
            runCatching {
                recommendationContextBuilder
                    .build()
            }
                .getOrElse {
                    RecommendationContext()
                }

        val questionSkills =
            runCatching {
                questionDao
                    .getSkillCodesForQuestion(
                        questionId =
                            question.id
                    )
            }
                .getOrDefault(
                    emptyList()
                )
                .toSet()

        val progress =
            loadQuestionProgress(
                questionId =
                    question.id
            )

        return calculateScore(
            question =
                question,
            questionSkills =
                questionSkills,
            progress =
                progress,
            context =
                context,
            now =
                now
        )
    }

    private suspend fun loadQuestionProgress(
        questionId: String
    ): QuestionProgress {
        return runCatching {
            questionProgressRepository
                .getQuestionProgress(
                    questionId =
                        questionId
                )
        }
            .getOrElse {
                QuestionProgress.empty(
                    questionId =
                        questionId
                )
            }
    }

    private fun calculateScore(
        question: QuestionEntity,
        questionSkills: Set<SkillCode>,
        progress: QuestionProgress,
        context: RecommendationContext,
        now: Long
    ): QuestionRecommendationScore {
        val profileRelevancePriority =
            calculateProfileRelevancePriority(
                questionSkills =
                    questionSkills,
                context =
                    context
            )

        val weakSkillPriority =
            calculateWeakSkillPriority(
                questionSkills =
                    questionSkills,
                context =
                    context
            )

        val preferredCategoryPriority =
            calculatePreferredCategoryPriority(
                category =
                    question.category,
                context =
                    context
            )

        val savedSkillPriority =
            calculateSavedSkillPriority(
                questionSkills =
                    questionSkills,
                context =
                    context
            )

        val performancePriority =
            calculatePerformancePriority(
                progress =
                    progress
            )

        val masteryPriority =
            calculateMasteryPriority(
                progress =
                    progress
            )

        val recencyPriority =
            calculateRecencyPriority(
                lastPracticedAt =
                    progress.lastPracticedAt,
                now =
                    now
            )

        val unpracticedPriority =
            if (progress.attemptCount == 0) {
                UNPRACTICED_MAXIMUM
            } else {
                0
            }

        val decliningPriority =
            calculateDecliningPriority(
                progress =
                    progress
            )

        val revisionPriority =
            calculateRevisionPriority(
                progress =
                    progress
            )

        val recentQuestionPenalty =
            if (
                question.id in
                context.recentQuestionIds
            ) {
                RECENT_QUESTION_PENALTY
            } else {
                0
            }

        val skippedCategoryPenalty =
            if (
                question.category in
                context.skippedCategories
            ) {
                SKIPPED_CATEGORY_PENALTY
            } else {
                0
            }

        val positiveTotal =
            profileRelevancePriority +
                    weakSkillPriority +
                    preferredCategoryPriority +
                    savedSkillPriority +
                    performancePriority +
                    masteryPriority +
                    recencyPriority +
                    unpracticedPriority +
                    decliningPriority +
                    revisionPriority

        val penaltyTotal =
            recentQuestionPenalty +
                    skippedCategoryPenalty

        val total =
            (
                    positiveTotal -
                            penaltyTotal
                    )
                .coerceIn(
                    minimumValue =
                        MINIMUM_SCORE,
                    maximumValue =
                        MAXIMUM_SCORE
                )

        return QuestionRecommendationScore(
            questionId =
                question.id,
            total =
                total,
            profileRelevancePriority =
                profileRelevancePriority,
            weakSkillPriority =
                weakSkillPriority,
            preferredCategoryPriority =
                preferredCategoryPriority,
            savedSkillPriority =
                savedSkillPriority,
            performancePriority =
                performancePriority,
            masteryPriority =
                masteryPriority,
            recencyPriority =
                recencyPriority,
            unpracticedPriority =
                unpracticedPriority,
            decliningPriority =
                decliningPriority,
            revisionPriority =
                revisionPriority,
            recentQuestionPenalty =
                recentQuestionPenalty,
            skippedCategoryPenalty =
                skippedCategoryPenalty,
            matchingProfileSkills =
                questionSkills
                    .intersect(
                        context.cvSkills.toSet()
                    )
                    .sortedBy(
                        SkillCode::name
                    ),
            matchingWeakSkills =
                questionSkills
                    .intersect(
                        context.weakSkills
                            .map {
                                it.skillCode
                            }
                            .toSet()
                    )
                    .sortedBy(
                        SkillCode::name
                    ),
            attemptCount =
                progress.attemptCount,
            completedEvaluationCount =
                progress.completedEvaluationCount,
            savedRevisionCount =
                progress.savedRevisionCount,
            latestPercentage =
                progress.latestPercentage,
            scoreChangePercentagePoints =
                progress
                    .scoreChangePercentagePoints,
            lastPracticedAt =
                progress.lastPracticedAt
        )
    }

    private fun calculateProfileRelevancePriority(
        questionSkills: Set<SkillCode>,
        context: RecommendationContext
    ): Int {
        val matchingSkills =
            questionSkills.intersect(
                context.cvSkills.toSet()
            )

        if (matchingSkills.isEmpty()) {
            return 0
        }

        return (
                matchingSkills.size *
                        PROFILE_POINTS_PER_SKILL
                )
            .coerceAtMost(
                PROFILE_RELEVANCE_MAXIMUM
            )
    }

    private fun calculateWeakSkillPriority(
        questionSkills: Set<SkillCode>,
        context: RecommendationContext
    ): Int {
        val weakSkillsByCode =
            context.weakSkills
                .associateBy {
                    it.skillCode
                }

        return questionSkills
            .mapNotNull(
                weakSkillsByCode::get
            )
            .sumOf { weakSkill ->
                val normalizedWeakness =
                    (
                            MAXIMUM_RUBRIC_SCORE -
                                    weakSkill.averageScore
                            ) /
                            MAXIMUM_RUBRIC_SCORE

                val evidenceMultiplier =
                    (
                            weakSkill.evidenceCount
                                .coerceAtMost(
                                    MAXIMUM_EVIDENCE_COUNT
                                )
                                .toFloat() /
                                    MAXIMUM_EVIDENCE_COUNT
                                        .toFloat()
                            )
                        .coerceIn(
                            minimumValue =
                                MINIMUM_EVIDENCE_MULTIPLIER,
                            maximumValue =
                                1f
                        )

                (
                        normalizedWeakness *
                                WEAK_SKILL_POINTS_PER_MATCH *
                                evidenceMultiplier
                        )
                    .roundToInt()
                    .coerceAtLeast(
                        MINIMUM_WEAK_SKILL_POINTS
                    )
            }
            .coerceAtMost(
                WEAK_SKILL_MAXIMUM
            )
    }

    private fun calculatePreferredCategoryPriority(
        category: QuestionCategory,
        context: RecommendationContext
    ): Int {
        return if (
            category in
            context.preferredCategories
        ) {
            PREFERRED_CATEGORY_MAXIMUM
        } else {
            0
        }
    }

    private fun calculateSavedSkillPriority(
        questionSkills: Set<SkillCode>,
        context: RecommendationContext
    ): Int {
        val matchingCount =
            questionSkills
                .intersect(
                    context.savedSkillCodes.toSet()
                )
                .size

        return (
                matchingCount *
                        SAVED_SKILL_POINTS_PER_MATCH
                )
            .coerceAtMost(
                SAVED_SKILL_MAXIMUM
            )
    }

    private fun calculatePerformancePriority(
        progress: QuestionProgress
    ): Int {
        if (progress.attemptCount <= 0) {
            return 0
        }

        val latestScore =
            progress.latestNormalizedScore
                ?: return PERFORMANCE_WITHOUT_SCORE

        return (
                (
                        1f -
                                latestScore.coerceIn(
                                    minimumValue =
                                        0f,
                                    maximumValue =
                                        1f
                                )
                        ) *
                        PERFORMANCE_MAXIMUM
                )
            .roundToInt()
            .coerceIn(
                minimumValue =
                    0,
                maximumValue =
                    PERFORMANCE_MAXIMUM
            )
    }

    private fun calculateMasteryPriority(
        progress: QuestionProgress
    ): Int {
        return when {
            progress.attemptCount <= 0 -> {
                MASTERY_NOT_STARTED
            }

            progress.latestNormalizedScore == null -> {
                MASTERY_NO_EVALUATION
            }

            progress.latestNormalizedScore <
                    LOW_MASTERY_THRESHOLD -> {
                MASTERY_LOW
            }

            progress.latestNormalizedScore <
                    DEVELOPING_MASTERY_THRESHOLD -> {
                MASTERY_DEVELOPING
            }

            else -> {
                0
            }
        }
    }

    private fun calculateRecencyPriority(
        lastPracticedAt: Long?,
        now: Long
    ): Int {
        if (lastPracticedAt == null) {
            return 0
        }

        val elapsedDays =
            (
                    now.coerceAtLeast(
                        lastPracticedAt
                    ) -
                            lastPracticedAt
                    )
                .toDouble()
                .div(
                    MILLIS_PER_DAY.toDouble()
                )

        return when {
            elapsedDays >=
                    RECENCY_FULL_PRIORITY_DAYS -> {
                RECENCY_MAXIMUM
            }

            elapsedDays <=
                    RECENCY_MINIMUM_PRIORITY_DAYS -> {
                0
            }

            else -> {
                val normalized =
                    (
                            elapsedDays -
                                    RECENCY_MINIMUM_PRIORITY_DAYS
                            ) /
                            (
                                    RECENCY_FULL_PRIORITY_DAYS -
                                            RECENCY_MINIMUM_PRIORITY_DAYS
                                    )

                (
                        normalized *
                                RECENCY_MAXIMUM
                        )
                    .roundToInt()
            }
        }
    }

    private fun calculateDecliningPriority(
        progress: QuestionProgress
    ): Int {
        val change =
            progress.scoreChange
                ?: return 0

        if (change >= 0f) {
            return 0
        }

        return (
                -change *
                        DECLINING_MULTIPLIER
                )
            .roundToInt()
            .coerceIn(
                minimumValue =
                    0,
                maximumValue =
                    DECLINING_MAXIMUM
            )
    }

    private fun calculateRevisionPriority(
        progress: QuestionProgress
    ): Int {
        return when {
            progress.savedRevisionCount <= 0 -> {
                0
            }

            progress.attemptCount <= 1 -> {
                REVISION_MAXIMUM
            }

            progress.isDeclining -> {
                REVISION_MAXIMUM
            }

            else -> {
                REVISION_MINIMUM
            }
        }
    }

    companion object {
        private const val MINIMUM_SCORE =
            0

        private const val MAXIMUM_SCORE =
            100

        private const val PROFILE_RELEVANCE_MAXIMUM =
            25

        private const val PROFILE_POINTS_PER_SKILL =
            10

        private const val WEAK_SKILL_MAXIMUM =
            25

        private const val WEAK_SKILL_POINTS_PER_MATCH =
            16

        private const val MINIMUM_WEAK_SKILL_POINTS =
            3

        private const val PREFERRED_CATEGORY_MAXIMUM =
            6

        private const val SAVED_SKILL_MAXIMUM =
            6

        private const val SAVED_SKILL_POINTS_PER_MATCH =
            3

        private const val PERFORMANCE_MAXIMUM =
            15

        private const val PERFORMANCE_WITHOUT_SCORE =
            7

        private const val MASTERY_NOT_STARTED =
            8

        private const val MASTERY_NO_EVALUATION =
            6

        private const val MASTERY_LOW =
            8

        private const val MASTERY_DEVELOPING =
            4

        private const val RECENCY_MAXIMUM =
            8

        private const val UNPRACTICED_MAXIMUM =
            7

        private const val DECLINING_MAXIMUM =
            5

        private const val REVISION_MAXIMUM =
            5

        private const val REVISION_MINIMUM =
            2

        private const val RECENT_QUESTION_PENALTY =
            12

        private const val SKIPPED_CATEGORY_PENALTY =
            15

        private const val MAXIMUM_RUBRIC_SCORE =
            5f

        private const val MAXIMUM_EVIDENCE_COUNT =
            8

        private const val MINIMUM_EVIDENCE_MULTIPLIER =
            0.4f

        private const val LOW_MASTERY_THRESHOLD =
            0.55f

        private const val DEVELOPING_MASTERY_THRESHOLD =
            0.75f

        private const val DECLINING_MULTIPLIER =
            30f

        private const val RECENCY_MINIMUM_PRIORITY_DAYS =
            1.0

        private const val RECENCY_FULL_PRIORITY_DAYS =
            14.0

        private const val MILLIS_PER_DAY =
            86_400_000L
    }
}

data class RankedQuestion(
    val question: QuestionEntity,
    val score: QuestionRecommendationScore
)

data class QuestionRecommendationScore(
    val questionId: String,
    val total: Int,
    val profileRelevancePriority: Int,
    val weakSkillPriority: Int,
    val preferredCategoryPriority: Int,
    val savedSkillPriority: Int,
    val performancePriority: Int,
    val masteryPriority: Int,
    val recencyPriority: Int,
    val unpracticedPriority: Int,
    val decliningPriority: Int,
    val revisionPriority: Int,
    val recentQuestionPenalty: Int,
    val skippedCategoryPenalty: Int,
    val matchingProfileSkills: List<SkillCode>,
    val matchingWeakSkills: List<SkillCode>,
    val attemptCount: Int,
    val completedEvaluationCount: Int,
    val savedRevisionCount: Int,
    val latestPercentage: Int?,
    val scoreChangePercentagePoints: Int?,
    val lastPracticedAt: Long?
) {

    val primaryReason:
            QuestionRecommendationReason
        get() =
            when {
                weakSkillPriority > 0 -> {
                    QuestionRecommendationReason
                        .WEAK_SKILL
                }

                profileRelevancePriority > 0 -> {
                    QuestionRecommendationReason
                        .PROFILE_RELEVANCE
                }

                decliningPriority > 0 -> {
                    QuestionRecommendationReason
                        .DECLINING_SCORE
                }

                performancePriority > 0 -> {
                    QuestionRecommendationReason
                        .LOW_SCORE
                }

                revisionPriority > 0 -> {
                    QuestionRecommendationReason
                        .SAVED_REVISION
                }

                recencyPriority > 0 -> {
                    QuestionRecommendationReason
                        .REVISIT
                }

                unpracticedPriority > 0 -> {
                    QuestionRecommendationReason
                        .NEW_AREA
                }

                else -> {
                    QuestionRecommendationReason
                        .GENERAL
                }
            }

    val displayLabel:
            String
        get() =
            primaryReason.displayLabel

    val supportingLabel:
            String
        get() =
            when (primaryReason) {
                QuestionRecommendationReason
                    .WEAK_SKILL -> {
                    val skill =
                        matchingWeakSkills
                            .firstOrNull()
                            ?.displayName()

                    if (skill == null) {
                        "Targets an area that needs improvement"
                    } else {
                        "Build confidence in $skill"
                    }
                }

                QuestionRecommendationReason
                    .PROFILE_RELEVANCE -> {
                    val skill =
                        matchingProfileSkills
                            .firstOrNull()
                            ?.displayName()

                    if (skill == null) {
                        "Relevant to your candidate profile"
                    } else {
                        "Relevant to your $skill experience"
                    }
                }

                QuestionRecommendationReason
                    .DECLINING_SCORE -> {
                    val change =
                        scoreChangePercentagePoints

                    if (
                        change != null &&
                        change < 0
                    ) {
                        "Latest score fell by ${-change} points"
                    } else {
                        "Recent performance needs reinforcement"
                    }
                }

                QuestionRecommendationReason
                    .LOW_SCORE -> {
                    latestPercentage
                        ?.let {
                            "Latest answer scored $it%"
                        }
                        ?: "Previous answers need improvement"
                }

                QuestionRecommendationReason
                    .SAVED_REVISION -> {
                    if (savedRevisionCount == 1) {
                        "A saved revision is ready to practise"
                    } else {
                        "$savedRevisionCount saved revisions are ready"
                    }
                }

                QuestionRecommendationReason
                    .REVISIT -> {
                    "This question has not been practised recently"
                }

                QuestionRecommendationReason
                    .NEW_AREA -> {
                    "You have not attempted this question yet"
                }

                QuestionRecommendationReason
                    .GENERAL -> {
                    "Keep building interview readiness"
                }
            }
}

enum class QuestionRecommendationReason(
    val displayLabel: String
) {
    WEAK_SKILL(
        "Recommended for improvement"
    ),

    PROFILE_RELEVANCE(
        "Matches your profile"
    ),

    DECLINING_SCORE(
        "Needs reinforcement"
    ),

    LOW_SCORE(
        "Improve your previous answer"
    ),

    SAVED_REVISION(
        "Practise a saved revision"
    ),

    REVISIT(
        "Time to revisit"
    ),

    NEW_AREA(
        "New practice area"
    ),

    GENERAL(
        "Recommended practice"
    )
}

private fun SkillCode.displayName():
        String {
    return name
        .lowercase()
        .split("_")
        .joinToString(
            separator =
                " "
        ) { word ->
            word.replaceFirstChar {
                it.uppercase()
            }
        }
}