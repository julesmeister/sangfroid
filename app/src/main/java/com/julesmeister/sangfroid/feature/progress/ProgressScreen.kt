package com.julesmeister.sangfroid.feature.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.layout.SangfroidScreen
import com.julesmeister.sangfroid.feature.progress.components.CvInsightsCard
import com.julesmeister.sangfroid.feature.progress.components.ProgressHeader
import com.julesmeister.sangfroid.feature.progress.components.ProgressTrendCard
import com.julesmeister.sangfroid.feature.progress.components.QuestionProgressList
import com.julesmeister.sangfroid.feature.progress.components.ReadinessOverviewCard
import com.julesmeister.sangfroid.feature.progress.components.SkillProgressSection

@Composable
fun ProgressScreen(
    onNavigateBack: () -> Unit,
    onPracticeQuestion: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProgressViewModel =
        hiltViewModel()
) {
    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()

    ProgressContent(
        uiState =
            uiState,
        onNavigateBack =
            onNavigateBack,
        onPracticeQuestion =
            onPracticeQuestion,
        modifier =
            modifier
    )
}

@Composable
private fun ProgressContent(
    uiState: ProgressUiState,
    onNavigateBack: () -> Unit,
    onPracticeQuestion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SangfroidScreen(
        modifier =
            modifier,
        applyHorizontalPadding =
            false,
        applyVerticalPadding =
            false
    ) {
        ProgressHeader(
            onNavigateBack =
                onNavigateBack,
            modifier =
                Modifier.padding(
                    horizontal =
                        SangfroidSpacing
                            .ScreenHorizontal,
                    vertical =
                        SangfroidSpacing
                            .ScreenVertical
                )
        )

        when {
            uiState.isLoading -> {
                ProgressMessage(
                    title =
                        "Calculating progress",
                    text =
                        "Reviewing your practice history and completed feedback.",
                    showProgress =
                        true,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )
            }

            uiState.errorMessage != null -> {
                ProgressMessage(
                    title =
                        "Progress is unavailable",
                    text =
                        uiState.errorMessage,
                    showProgress =
                        false,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )
            }

            uiState.isEmpty -> {
                ProgressMessage(
                    title =
                        "No practice history yet",
                    text =
                        "Complete an interview question to start building your progress profile.",
                    showProgress =
                        false,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(
                            start = SangfroidSpacing.ScreenHorizontal,
                            end = SangfroidSpacing.ScreenHorizontal,
                            bottom = SangfroidSpacing.Section
                        ),
                    verticalArrangement = Arrangement.spacedBy(SangfroidSpacing.Lg)
                ) {
                    if (uiState.summary.hasPracticeHistory) {
                        item(key = "readiness") {
                            ReadinessOverviewCard(summary = uiState.summary)
                        }

                        item(key = "trend") {
                            ProgressTrendCard(summary = uiState.summary)
                        }
                    }

                    uiState.cvInsights?.let { insights ->
                        item(key = "cv-insights") {
                            CvInsightsCard(
                                insights = insights,
                                onPracticeQuestion = onPracticeQuestion
                            )
                        }
                    }

                    if (uiState.strongestCriteria.isNotEmpty()) {
                        item(key = "strongest") {
                            SkillProgressSection(
                                title = "Strongest areas",
                                supportingText = "Criteria with your highest average feedback scores.",
                                items = uiState.strongestCriteria,
                                emphasized = true
                            )
                        }
                    }

                    if (uiState.focusCriteria.isNotEmpty()) {
                        item(key = "focus") {
                            SkillProgressSection(
                                title = "Focus next",
                                supportingText = "The criteria that currently have the most room to improve.",
                                items = uiState.focusCriteria,
                                emphasized = false
                            )
                        }
                    }

                    item(key = "questions") {
                        QuestionProgressList(
                            items = uiState.questionItems,
                            onQuestionClick = onPracticeQuestion
                        )
                    }

                    item(key = "bottom-space") {
                        Spacer(modifier = Modifier.height(SangfroidSpacing.Section))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressMessage(
    title: String,
    text: String,
    showProgress: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(
                    horizontal =
                        SangfroidSpacing
                            .ScreenHorizontal
                ),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        if (showProgress) {
            CircularProgressIndicator()

            Spacer(
                modifier =
                    Modifier.height(
                        SangfroidSpacing.Md
                    )
            )
        }

        Text(
            text =
                title,
            style =
                MaterialTheme
                    .typography
                    .titleLarge,
            fontWeight =
                FontWeight.Bold,
            color =
                MaterialTheme
                    .colorScheme
                    .onBackground
        )

        Spacer(
            modifier =
                Modifier.height(
                    SangfroidSpacing.Xs
                )
        )

        Text(
            text =
                text,
            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}