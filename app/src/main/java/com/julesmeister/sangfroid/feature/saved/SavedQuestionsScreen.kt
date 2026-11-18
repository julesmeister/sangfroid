package com.julesmeister.sangfroid.feature.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.julesmeister.sangfroid.feature.saved.components.SavedQuestionCard
import com.julesmeister.sangfroid.feature.saved.components.SavedQuestionsEmptyState
import com.julesmeister.sangfroid.feature.saved.components.SavedQuestionsHeader

@Composable
fun SavedQuestionsScreen(
    onNavigateBack: () -> Unit,
    onOpenHistory: (String) -> Unit,
    onPracticeQuestion: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavedQuestionsViewModel =
        hiltViewModel()
) {
    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()

    SangfroidScreen(
        modifier =
            modifier,
        applyHorizontalPadding =
            false,
        applyVerticalPadding =
            false
    ) {
        SavedQuestionsHeader(
            savedCount =
                uiState.savedCount,
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
                SavedMessage(
                    title =
                        "Loading saved questions",
                    text =
                        "Preparing your interview practice library.",
                    showProgress =
                        true,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )
            }

            uiState.errorMessage != null &&
                    uiState.questions.isEmpty() -> {
                SavedMessage(
                    title =
                        "Saved questions are unavailable",
                    text =
                        uiState.errorMessage!!,
                    showProgress =
                        false,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )
            }

            uiState.isEmpty -> {
                SavedQuestionsEmptyState(
                    modifier =
                        Modifier
                            .weight(
                                1f
                            )
                            .padding(
                                horizontal =
                                    SangfroidSpacing
                                        .ScreenHorizontal
                            )
                )
            }

            else -> {
                LazyColumn(
                    modifier =
                        Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(
                            start =
                                SangfroidSpacing
                                    .ScreenHorizontal,
                            end =
                                SangfroidSpacing
                                    .ScreenHorizontal,
                            bottom =
                                SangfroidSpacing
                                    .Section
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(
                            SangfroidSpacing.Md
                        )
                ) {
                    uiState.errorMessage
                        ?.let { errorMessage ->
                            item(
                                key =
                                    "saved-error"
                            ) {
                                Text(
                                    text =
                                        errorMessage,
                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodyMedium,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .error
                                )
                            }
                        }

                    items(
                        items =
                            uiState.questions,
                        key = { item ->
                            item.id
                        }
                    ) { item ->
                        SavedQuestionCard(
                            item =
                                item,
                            isRemoving =
                                uiState.isRemoving(
                                    item.id
                                ),
                            onOpenHistory =
                                onOpenHistory,
                            onPractice =
                                onPracticeQuestion,
                            onRemove =
                                viewModel::removeQuestion
                        )
                    }

                    item(
                        key =
                            "saved-bottom-space"
                    ) {
                        Spacer(
                            modifier =
                                Modifier.height(
                                    SangfroidSpacing.Section
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedMessage(
    title: String,
    text: String,
    showProgress: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier =
            modifier.fillMaxSize(),
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
                FontWeight.Bold
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