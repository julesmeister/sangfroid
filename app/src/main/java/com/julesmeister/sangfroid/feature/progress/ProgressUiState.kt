package com.julesmeister.sangfroid.feature.progress

import com.julesmeister.sangfroid.feature.progress.model.CvInsights
import com.julesmeister.sangfroid.feature.progress.model.OverallProgressSummary
import com.julesmeister.sangfroid.feature.progress.model.ProgressQuestionItem
import kotlin.math.roundToInt

data class ProgressUiState(
    val isLoading: Boolean = true,
    val summary: OverallProgressSummary = OverallProgressSummary(),
    val questionItems: List<ProgressQuestionItem> = emptyList(),
    val strongestCriteria: List<CriterionProgressItem> = emptyList(),
    val focusCriteria: List<CriterionProgressItem> = emptyList(),
    val cvInsights: CvInsights? = null,
    val errorMessage: String? = null
) {

    val isEmpty: Boolean
        get() =
            !isLoading &&
                    !summary.hasPracticeHistory &&
                    cvInsights == null
}

data class CriterionProgressItem(
    val criterion: String,
    val averageNormalizedScore: Float,
    val latestNormalizedScore: Float,
    val evidenceCount: Int
) {

    val averagePercentage: Int
        get() =
            averageNormalizedScore
                .coerceIn(0f, 1f)
                .times(100f)
                .roundToInt()

    val latestPercentage: Int
        get() =
            latestNormalizedScore
                .coerceIn(0f, 1f)
                .times(100f)
                .roundToInt()
}