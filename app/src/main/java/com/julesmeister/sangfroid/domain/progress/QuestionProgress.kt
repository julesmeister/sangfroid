package com.julesmeister.sangfroid.domain.progress

import kotlin.math.roundToInt

data class QuestionProgress(
    val questionId: String,
    val attemptCount: Int,
    val completedEvaluationCount: Int,
    val savedRevisionCount: Int,
    val averageNormalizedScore: Float?,
    val latestNormalizedScore: Float?,
    val previousNormalizedScore: Float?,
    val scoreChange: Float?,
    val masteryLevel: QuestionMasteryLevel,
    val rubricProgress: List<RubricProgress>,
    val strongestCriteria: List<RubricProgress>,
    val weakestCriteria: List<RubricProgress>,
    val latestEvaluatedAt: Long?,
    val lastPracticedAt: Long?
) {

    val averagePercentage: Int?
        get() =
            averageNormalizedScore
                ?.toPercentage()

    val latestPercentage: Int?
        get() =
            latestNormalizedScore
                ?.toPercentage()

    val previousPercentage: Int?
        get() =
            previousNormalizedScore
                ?.toPercentage()

    val scoreChangePercentagePoints: Int?
        get() =
            scoreChange
                ?.times(100f)
                ?.roundToInt()

    val hasProgressData: Boolean
        get() =
            completedEvaluationCount > 0 &&
                    latestNormalizedScore != null

    val hasMultipleEvaluations: Boolean
        get() =
            completedEvaluationCount >= 2

    val isImproving: Boolean
        get() =
            scoreChange != null &&
                    scoreChange >
                    MinimumMeaningfulChange

    val isDeclining: Boolean
        get() =
            scoreChange != null &&
                    scoreChange <
                    -MinimumMeaningfulChange

    val progressSummary: String
        get() {
            if (!hasProgressData) {
                return if (attemptCount > 0) {
                    "Waiting for completed feedback"
                } else {
                    "Not practiced yet"
                }
            }

            val score =
                latestPercentage
                    ?: 0

            val change =
                scoreChangePercentagePoints

            return when {
                change == null -> {
                    "$score% from $completedEvaluationCount completed evaluation" +
                            if (
                                completedEvaluationCount == 1
                            ) {
                                ""
                            } else {
                                "s"
                            }
                }

                change > 0 -> {
                    "$score% · up $change points"
                }

                change < 0 -> {
                    "$score% · down ${-change} points"
                }

                else -> {
                    "$score% · unchanged"
                }
            }
        }

    companion object {

        fun empty(
            questionId: String
        ): QuestionProgress {
            return QuestionProgress(
                questionId = questionId,
                attemptCount = 0,
                completedEvaluationCount = 0,
                savedRevisionCount = 0,
                averageNormalizedScore = null,
                latestNormalizedScore = null,
                previousNormalizedScore = null,
                scoreChange = null,
                masteryLevel =
                    QuestionMasteryLevel.NOT_STARTED,
                rubricProgress = emptyList(),
                strongestCriteria = emptyList(),
                weakestCriteria = emptyList(),
                latestEvaluatedAt = null,
                lastPracticedAt = null
            )
        }

        private const val MinimumMeaningfulChange =
            0.005f
    }
}

private fun Float.toPercentage(): Int {
    return coerceIn(
        minimumValue = 0f,
        maximumValue = 1f
    )
        .times(100f)
        .roundToInt()
}