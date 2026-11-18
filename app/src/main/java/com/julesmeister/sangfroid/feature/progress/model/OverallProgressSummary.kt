package com.julesmeister.sangfroid.feature.progress.model

import kotlin.math.roundToInt

data class OverallProgressSummary(
    val totalQuestionCount: Int = 0,
    val practicedQuestionCount: Int = 0,
    val totalAttemptCount: Int = 0,
    val completedEvaluationCount: Int = 0,
    val savedRevisionCount: Int = 0,
    val overallNormalizedScore: Float? = null,
    val averageScoreChange: Float? = null
) {

    val overallPercentage: Int?
        get() =
            overallNormalizedScore
                ?.coerceIn(
                    minimumValue = 0f,
                    maximumValue = 1f
                )
                ?.times(100f)
                ?.roundToInt()

    val averageScoreChangePercentagePoints: Int?
        get() =
            averageScoreChange
                ?.times(100f)
                ?.roundToInt()

    val readinessLabel: String
        get() {
            val percentage =
                overallPercentage
                    ?: return "Start practising"

            return when {
                percentage >= 85 &&
                        completedEvaluationCount >= 3 -> {
                    "Interview ready"
                }

                percentage >= 70 -> {
                    "Strong foundation"
                }

                percentage >= 50 -> {
                    "Developing"
                }

                else -> {
                    "Building confidence"
                }
            }
        }

    val hasPracticeHistory: Boolean
        get() =
            totalAttemptCount > 0

    companion object {

        fun calculate(
            items: List<ProgressQuestionItem>,
            totalQuestionCount: Int
        ): OverallProgressSummary {
            val evaluatedItems =
                items.filter { item ->
                    item.progress
                        .averageNormalizedScore != null &&
                            item.progress
                                .completedEvaluationCount > 0
                }

            val totalEvaluationWeight =
                evaluatedItems.sumOf { item ->
                    item.progress
                        .completedEvaluationCount
                }

            val overallScore =
                if (totalEvaluationWeight > 0) {
                    evaluatedItems
                        .sumOf { item ->
                            val score =
                                item.progress
                                    .averageNormalizedScore
                                    ?: 0f

                            score.toDouble() *
                                    item.progress
                                        .completedEvaluationCount
                        }
                        .div(
                            totalEvaluationWeight
                        )
                        .toFloat()
                        .coerceIn(
                            minimumValue = 0f,
                            maximumValue = 1f
                        )
                } else {
                    null
                }

            val scoreChanges =
                items.mapNotNull { item ->
                    item.progress.scoreChange
                }

            val averageChange =
                scoreChanges
                    .takeIf(
                        List<Float>::isNotEmpty
                    )
                    ?.average()
                    ?.toFloat()

            return OverallProgressSummary(
                totalQuestionCount =
                    totalQuestionCount,
                practicedQuestionCount =
                    items.count { item ->
                        item.progress.attemptCount > 0
                    },
                totalAttemptCount =
                    items.sumOf { item ->
                        item.progress.attemptCount
                    },
                completedEvaluationCount =
                    items.sumOf { item ->
                        item.progress
                            .completedEvaluationCount
                    },
                savedRevisionCount =
                    items.sumOf { item ->
                        item.progress
                            .savedRevisionCount
                    },
                overallNormalizedScore =
                    overallScore,
                averageScoreChange =
                    averageChange
            )
        }
    }
}