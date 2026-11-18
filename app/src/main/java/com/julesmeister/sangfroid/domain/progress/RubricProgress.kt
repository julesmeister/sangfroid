package com.julesmeister.sangfroid.domain.progress

import kotlin.math.roundToInt

data class RubricProgress(
    val criterion: String,
    val attemptCount: Int,
    val averageNormalizedScore: Float,
    val latestNormalizedScore: Float,
    val previousNormalizedScore: Float?,
    val change: Float?
) {

    val averagePercentage: Int
        get() =
            averageNormalizedScore
                .coerceIn(
                    minimumValue = 0f,
                    maximumValue = 1f
                )
                .times(100f)
                .roundToInt()

    val latestPercentage: Int
        get() =
            latestNormalizedScore
                .coerceIn(
                    minimumValue = 0f,
                    maximumValue = 1f
                )
                .times(100f)
                .roundToInt()

    val previousPercentage: Int?
        get() =
            previousNormalizedScore
                ?.coerceIn(
                    minimumValue = 0f,
                    maximumValue = 1f
                )
                ?.times(100f)
                ?.roundToInt()

    val changePercentagePoints: Int?
        get() =
            change
                ?.times(100f)
                ?.roundToInt()

    val isImproving: Boolean
        get() =
            change != null &&
                    change > MinimumMeaningfulChange

    val isDeclining: Boolean
        get() =
            change != null &&
                    change < -MinimumMeaningfulChange

    companion object {
        private const val MinimumMeaningfulChange =
            0.005f
    }
}