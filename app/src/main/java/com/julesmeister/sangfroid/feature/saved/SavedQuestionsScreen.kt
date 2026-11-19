package com.julesmeister.sangfroid.feature.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.layout.SangfroidScreen
import com.julesmeister.sangfroid.domain.model.QuestionCategory
import com.julesmeister.sangfroid.feature.feed.filter.CategoryFilterDialog
import com.julesmeister.sangfroid.feature.saved.components.SavedCategoryFilterButton
import com.julesmeister.sangfroid.feature.saved.components.SavedQuestionCard
import com.julesmeister.sangfroid.feature.saved.components.SavedQuestionsEmptyState
import com.julesmeister.sangfroid.feature.saved.components.SavedQuestionsFilteredEmptyState
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

    var categoryFilterVisible by
    rememberSaveable {
        mutableStateOf(
            false
        )
    }

    var selectedCategoryNames by
    rememberSaveable {
        mutableStateOf(
            emptyList<String>()
        )
    }

    val availableCategories:
            Set<QuestionCategory> =
        remember(
            uiState.questions
        ) {
            uiState.questions
                .map { item ->
                    item.question.category
                }
                .toSet()
        }

    val selectedCategories:
            Set<QuestionCategory> =
        remember(
            availableCategories,
            selectedCategoryNames
        ) {
            availableCategories
                .filterTo(
                    linkedSetOf()
                ) { category ->
                    category.name in
                            selectedCategoryNames
                }
        }

    val filteredQuestions =
        remember(
            uiState.questions,
            selectedCategories
        ) {
            if (
                selectedCategories.isEmpty()
            ) {
                uiState.questions
            } else {
                uiState.questions
                    .filter { item ->
                        item.question.category in
                                selectedCategories
                    }
            }
        }

    val filteringEnabled =
        !uiState.isLoading &&
                uiState.questions.isNotEmpty()

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
                    start =
                        SangfroidSpacing
                            .ScreenHorizontal,
                    end =
                        SangfroidSpacing
                            .ScreenHorizontal,
                    top =
                        SangfroidSpacing
                            .ScreenVertical,
                    bottom =
                        SangfroidSpacing.Xs
                )
        )

        if (
            filteringEnabled
        ) {
            SavedCategoryFilterButton(
                activeFilterCount =
                    selectedCategories.size,
                visibleQuestionCount =
                    filteredQuestions.size,
                totalQuestionCount =
                    uiState.questions.size,
                enabled =
                    true,
                onClick = {
                    categoryFilterVisible =
                        true
                },
                modifier =
                    Modifier.padding(
                        horizontal =
                            SangfroidSpacing
                                .ScreenHorizontal
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(
                        SangfroidSpacing.Sm
                    )
            )
        }

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

            filteredQuestions.isEmpty() &&
                    selectedCategories.isNotEmpty() -> {
                SavedQuestionsFilteredEmptyState(
                    selectedCategoryCount =
                        selectedCategories.size,
                    onClearFilters = {
                        selectedCategoryNames =
                            emptyList()
                    },
                    modifier =
                        Modifier.weight(
                            1f
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
                            filteredQuestions,
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

    CategoryFilterDialog(
        visible =
            categoryFilterVisible,
        availableCategories =
            availableCategories,
        selectedCategories =
            selectedCategories,
        onApply = { categories ->
            selectedCategoryNames =
                categories
                    .map { category ->
                        category.name
                    }

            categoryFilterVisible =
                false
        },
        onDismiss = {
            categoryFilterVisible =
                false
        }
    )
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
            CircularProgressIndicator(
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface
            )

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
                    .onSurface,
            textAlign =
                TextAlign.Center
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
                    .onSurfaceVariant,
            textAlign =
                TextAlign.Center
        )
    }
}