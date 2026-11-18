package com.julesmeister.sangfroid.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.julesmeister.sangfroid.data.local.dao.QuestionDao
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.domain.cv.CvAnalysisRepository
import com.julesmeister.sangfroid.domain.cv.CvProfile
import com.julesmeister.sangfroid.domain.progress.QuestionProgressRepository
import com.julesmeister.sangfroid.domain.progress.RubricProgress
import com.julesmeister.sangfroid.feature.progress.model.CvInsightsCalculator
import com.julesmeister.sangfroid.feature.progress.model.OverallProgressSummary
import com.julesmeister.sangfroid.feature.progress.model.ProgressQuestionItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProgressViewModel @Inject constructor(
    questionDao: QuestionDao,
    private val questionProgressRepository: QuestionProgressRepository,
    private val cvAnalysisRepository: CvAnalysisRepository
) : ViewModel() {

    private val progressDataFlow: Flow<ProgressData> =
        questionDao
            .observeAllActiveQuestions()
            .flatMapLatest { questions ->
                observeProgressData(questions)
            }

    private val cvProfileFlow: Flow<CvProfile?> =
        flow {
            emit(
                cvAnalysisRepository
                    .getSavedProfile()
                    .getOrNull()
            )
        }

    val uiState: StateFlow<ProgressUiState> =
        combine(
            progressDataFlow,
            cvProfileFlow
        ) { progressData, cvProfile ->
            buildUiState(
                progressData = progressData,
                cvProfile = cvProfile
            )
        }
            .catch { throwable ->
                emit(
                    ProgressUiState(
                        isLoading = false,
                        errorMessage =
                            throwable.message
                                ?: "Progress could not be loaded."
                    )
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = ProgressUiState()
            )

    private fun observeProgressData(
        questions: List<QuestionEntity>
    ): Flow<ProgressData> {
        if (questions.isEmpty()) {
            return flowOf(
                ProgressData(
                    totalQuestionCount = 0,
                    questionItems = emptyList()
                )
            )
        }

        val progressItemFlows: List<Flow<ProgressQuestionItem>> =
            questions.map { question ->
                questionProgressRepository
                    .observeQuestionProgress(questionId = question.id)
                    .map { progress ->
                        ProgressQuestionItem(
                            question = question,
                            progress = progress
                        )
                    }
            }

        return combine(progressItemFlows) { progressItems ->
            ProgressData(
                totalQuestionCount = questions.size,
                questionItems =
                    progressItems
                        .toList()
                        .sortedWith(
                            compareByDescending<ProgressQuestionItem> { item ->
                                item.progress.lastPracticedAt ?: Long.MIN_VALUE
                            }
                                .thenByDescending { item ->
                                    item.progress.attemptCount
                                }
                                .thenBy { item ->
                                    item.question.question.lowercase()
                                }
                        )
            )
        }
    }

    private fun buildUiState(
        progressData: ProgressData,
        cvProfile: CvProfile?
    ): ProgressUiState {
        val practisedQuestionItems =
            progressData.questionItems.filter { item ->
                item.progress.attemptCount > 0
            }

        val criterionItems =
            aggregateCriteria(practisedQuestionItems)

        val strongestCriteria =
            criterionItems
                .sortedWith(
                    compareByDescending<CriterionProgressItem> {
                        it.averageNormalizedScore
                    }
                        .thenByDescending { it.evidenceCount }
                        .thenBy { it.criterion.lowercase() }
                )
                .take(CriteriaSummaryLimit)

        val strongestKeys =
            strongestCriteria
                .mapTo(mutableSetOf()) {
                    it.criterion.normalizeCriterionKey()
                }

        val focusCriteria =
            criterionItems
                .filter {
                    it.criterion.normalizeCriterionKey() !in strongestKeys
                }
                .ifEmpty { criterionItems }
                .sortedWith(
                    compareBy<CriterionProgressItem> {
                        it.averageNormalizedScore
                    }
                        .thenByDescending { it.evidenceCount }
                        .thenBy { it.criterion.lowercase() }
                )
                .take(CriteriaSummaryLimit)

        return ProgressUiState(
            isLoading = false,
            summary =
                OverallProgressSummary.calculate(
                    items = practisedQuestionItems,
                    totalQuestionCount = progressData.totalQuestionCount
                ),
            questionItems = practisedQuestionItems,
            strongestCriteria = strongestCriteria,
            focusCriteria = focusCriteria,
            cvInsights =
                CvInsightsCalculator.calculate(
                    items = progressData.questionItems,
                    cvProfile = cvProfile
                ),
            errorMessage = null
        )
    }

    private fun aggregateCriteria(
        questionItems: List<ProgressQuestionItem>
    ): List<CriterionProgressItem> {
        return questionItems
            .flatMap { it.progress.rubricProgress }
            .groupBy { it.criterion.normalizeCriterionKey() }
            .values
            .mapNotNull(::buildCriterionProgressItem)
    }

    private fun buildCriterionProgressItem(
        rubricItems: List<RubricProgress>
    ): CriterionProgressItem? {
        if (rubricItems.isEmpty()) return null

        val criterion =
            rubricItems
                .first()
                .criterion
                .trim()
                .takeIf(String::isNotBlank)
                ?: return null

        val totalEvidence =
            rubricItems.sumOf { it.attemptCount.coerceAtLeast(1) }

        if (totalEvidence <= 0) return null

        val weightedAverage =
            rubricItems
                .sumOf {
                    it.averageNormalizedScore.toDouble() *
                            it.attemptCount.coerceAtLeast(1)
                }
                .div(totalEvidence)
                .toFloat()
                .coerceIn(0f, 1f)

        val latest =
            rubricItems
                .maxByOrNull { it.attemptCount }
                ?.latestNormalizedScore
                ?.coerceIn(0f, 1f)
                ?: weightedAverage

        return CriterionProgressItem(
            criterion = criterion,
            averageNormalizedScore = weightedAverage,
            latestNormalizedScore = latest,
            evidenceCount = totalEvidence
        )
    }

    private fun String.normalizeCriterionKey(): String =
        trim().lowercase()

    private data class ProgressData(
        val totalQuestionCount: Int,
        val questionItems: List<ProgressQuestionItem>
    )

    companion object {
        private const val CriteriaSummaryLimit = 3
    }
}