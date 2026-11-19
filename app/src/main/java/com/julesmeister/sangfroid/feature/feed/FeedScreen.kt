package com.julesmeister.sangfroid.feature.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidDimensions
import com.julesmeister.sangfroid.core.designsystem.dimensions.SangfroidSpacing
import com.julesmeister.sangfroid.core.designsystem.layout.SangfroidScreen
import com.julesmeister.sangfroid.domain.model.QuestionCategory
import com.julesmeister.sangfroid.feature.feed.components.FeedEmptyState
import com.julesmeister.sangfroid.feature.feed.components.FeedHeader
import com.julesmeister.sangfroid.feature.feed.components.FeedSplitActionButton
import com.julesmeister.sangfroid.feature.feed.components.QuestionCardStack
import com.julesmeister.sangfroid.feature.feed.components.SwipeActionBar
import com.julesmeister.sangfroid.feature.feed.custom.QuestionImportDialog
import com.julesmeister.sangfroid.feature.feed.filter.CategoryFilterDialog
import com.julesmeister.sangfroid.feature.feed.model.SwipeAction
import com.julesmeister.sangfroid.feature.feed.model.SwipeHighlight
import com.julesmeister.sangfroid.feature.feed.presentation.rememberSwipeCardState
import kotlinx.coroutines.launch

private const val VisibleStackSize =
    3

@Composable
fun FeedScreen(
    modifier: Modifier = Modifier,
    onPracticeQuestion:
        (String) -> Unit = {},
    onOpenSaved:
        () -> Unit = {},
    onOpenProgress:
        () -> Unit = {},
    onOpenProfile:
        () -> Unit = {},
    viewModel: FeedViewModel =
        hiltViewModel()
) {
    val uiState by
    viewModel
        .uiState
        .collectAsStateWithLifecycle()

    FeedContent(
        modifier =
            modifier,
        uiState =
            uiState,
        onOpenSaved =
            onOpenSaved,
        onOpenProgress =
            onOpenProgress,
        onOpenProfile =
            onOpenProfile,
        onSwipeResolved = {
                action,
                questionId ->

            viewModel.resolveSwipe(
                action =
                    action,
                questionId =
                    questionId,
                onPracticeRequested =
                    onPracticeQuestion
            )
        },
        onGenerateFresh =
            viewModel::generateFreshQuestions,
        onRestoreSkipped =
            viewModel::restoreSkippedQuestions,
        onRestartFeed =
            viewModel::restartFeed,
        onOpenQuestionImport =
            viewModel::openQuestionImport,
        onQuestionImportContentChanged =
            viewModel::updateQuestionImportContent,
        onExtractImportedQuestions =
            viewModel::extractImportedQuestions,
        onConfirmQuestionImport =
            viewModel::confirmQuestionImport,
        onEditQuestionImport =
            viewModel::editQuestionImportContent,
        onDismissQuestionImport =
            viewModel::closeQuestionImport
    )
}

@Composable
private fun FeedContent(
    uiState: FeedUiState,
    onOpenSaved: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenProfile: () -> Unit,
    onSwipeResolved:
        (SwipeAction, String) -> Unit,
    onGenerateFresh: () -> Unit,
    onRestoreSkipped: () -> Unit,
    onRestartFeed: () -> Unit,
    onOpenQuestionImport: () -> Unit,
    onQuestionImportContentChanged:
        (String) -> Unit,
    onExtractImportedQuestions: () -> Unit,
    onConfirmQuestionImport: () -> Unit,
    onEditQuestionImport: () -> Unit,
    onDismissQuestionImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val swipeState =
        rememberSwipeCardState()

    val scope =
        rememberCoroutineScope()

    val density =
        LocalDensity.current

    val containerSize =
        LocalWindowInfo
            .current
            .containerSize

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
                .map { question ->
                    question.category
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

    val activeFilterCount =
        selectedCategories.size

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
                    .filter { question ->
                        question.category in
                                selectedCategories
                    }
            }
        }

    val horizontalThresholdPx =
        with(density) {
            SangfroidDimensions
                .SwipeHorizontalThreshold
                .toPx()
        }

    val verticalThresholdPx =
        with(density) {
            SangfroidDimensions
                .SwipeVerticalThreshold
                .toPx()
        }

    val highlight:
                () -> SwipeHighlight =
        remember(
            swipeState,
            horizontalThresholdPx,
            verticalThresholdPx
        ) {
            {
                SwipeHighlight.from(
                    offset =
                        swipeState.offset.value,
                    horizontalThresholdPx =
                        horizontalThresholdPx,
                    verticalThresholdPx =
                        verticalThresholdPx
                )
            }
        }

    val resolveAction:
                (SwipeAction, String) -> Unit = {
            action,
            questionId ->

        onSwipeResolved(
            action,
            questionId
        )

        if (
            action ==
            SwipeAction.PRACTICE
        ) {
            scope.launch {
                swipeState.reset()
            }
        }
    }

    val restoreSkipped:
                () -> Unit = {
        scope.launch {
            swipeState.snapBack()
        }

        onRestoreSkipped()
    }

    val restartFeed:
                () -> Unit = {
        scope.launch {
            swipeState.snapBack()
        }

        onRestartFeed()
    }

    val clearCategoryFilter:
                () -> Unit = {
        selectedCategoryNames =
            emptyList()

        scope.launch {
            swipeState.snapBack()
        }
    }

    val interactionEnabled =
        !uiState.isProcessingAction &&
                !uiState.isGeneratingFreshQuestions &&
                !uiState.isImportingQuestions

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {
        SangfroidScreen(
            modifier =
                modifier
        ) {
            FeedHeader(
                skippedCount =
                    uiState.skippedCount,
                canRestoreSkipped =
                    uiState.canRestoreSkipped,
                onRestoreSkipped =
                    restoreSkipped,
                onOpenSaved =
                    onOpenSaved,
                onOpenProgress =
                    onOpenProgress,
                onOpenProfile =
                    onOpenProfile
            )

            Spacer(
                modifier =
                    Modifier.height(
                        SangfroidSpacing.Xs
                    )
            )

            FeedSplitActionButton(
                activeFilterCount =
                    activeFilterCount,
                enabled =
                    interactionEnabled,
                onPasteQuestions =
                    onOpenQuestionImport,
                onOpenCategoryFilter = {
                    categoryFilterVisible =
                        true
                },
                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                modifier =
                    Modifier.height(
                        SangfroidSpacing.Sm
                    )
            )

            when {
                uiState.isLoading -> {
                    FeedMessageArea {
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

                        Text(
                            text =
                                "Preparing your questions",
                            style =
                                MaterialTheme
                                    .typography
                                    .bodyMedium,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurface
                        )
                    }
                }

                uiState.errorMessage != null -> {
                    val errorMessage =
                        uiState.errorMessage

                    FeedMessageArea {
                        Text(
                            text =
                                "Something went wrong",
                            style =
                                MaterialTheme
                                    .typography
                                    .titleLarge,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .error
                        )

                        Spacer(
                            modifier =
                                Modifier.height(
                                    SangfroidSpacing.Xs
                                )
                        )

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
                                    .onSurface,
                            textAlign =
                                TextAlign.Center
                        )
                    }
                }

                uiState.questions.isEmpty() -> {
                    FeedMessageArea {
                        FeedEmptyState(
                            skippedCount =
                                uiState.skippedCount,
                            enabled =
                                interactionEnabled,
                            generationState =
                                uiState.freshGeneration,
                            onGenerateFresh =
                                onGenerateFresh,
                            onRestoreSkipped =
                                restoreSkipped,
                            onRestartFeed =
                                restartFeed
                        )
                    }
                }

                filteredQuestions.isEmpty() &&
                        selectedCategories.isNotEmpty() -> {
                    FeedMessageArea {
                        FilteredFeedEmptyState(
                            selectedCount =
                                selectedCategories.size,
                            onClearFilters =
                                clearCategoryFilter
                        )
                    }
                }

                else -> {
                    QuestionCardStack(
                        questions =
                            filteredQuestions.take(
                                VisibleStackSize
                            ),
                        totalQuestions =
                            filteredQuestions.size,
                        recommendationScores =
                            uiState
                                .recommendationScores,
                        swipeState =
                            swipeState,
                        enabled =
                            interactionEnabled,
                        onSwipeResolved = {
                                action,
                                question ->

                            resolveAction(
                                action,
                                question.id
                            )
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(
                                    1f
                                )
                    )

                    Spacer(
                        modifier =
                            Modifier.height(
                                SangfroidSpacing.Sm
                            )
                    )

                    SwipeActionBar(
                        enabled =
                            interactionEnabled,
                        highlight =
                            highlight,
                        onAction = { action ->
                            val currentQuestion =
                                filteredQuestions
                                    .firstOrNull()
                                    ?: return@SwipeActionBar

                            scope.launch {
                                swipeState.dismiss(
                                    action =
                                        action,
                                    containerWidthPx =
                                        containerSize
                                            .width
                                            .toFloat(),
                                    containerHeightPx =
                                        containerSize
                                            .height
                                            .toFloat()
                                )

                                resolveAction(
                                    action,
                                    currentQuestion.id
                                )
                            }
                        }
                    )
                }
            }
        }

        QuestionImportDialog(
            state =
                uiState.questionImport,
            onContentChanged =
                onQuestionImportContentChanged,
            onExtractQuestions =
                onExtractImportedQuestions,
            onImportQuestions =
                onConfirmQuestionImport,
            onEditContent =
                onEditQuestionImport,
            onDismiss =
                onDismissQuestionImport
        )

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

                scope.launch {
                    swipeState.snapBack()
                }
            },
            onDismiss = {
                categoryFilterVisible =
                    false
            }
        )
    }
}

@Composable
private fun FilteredFeedEmptyState(
    selectedCount: Int,
    onClearFilters: () -> Unit
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 24.dp
                ),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.Center
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        color =
                            MaterialTheme
                                .colorScheme
                                .surfaceVariant
                                .copy(
                                    alpha = 0.46f
                                ),
                        shape =
                            RoundedCornerShape(
                                22.dp
                            )
                    )
                    .padding(
                        horizontal = 22.dp,
                        vertical = 20.dp
                    )
        ) {
            Column(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                Text(
                    text =
                        "No matching questions",
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.ExtraBold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface,
                    textAlign =
                        TextAlign.Center
                )

                Text(
                    text =
                        if (
                            selectedCount == 1
                        ) {
                            "No remaining questions match the selected category."
                        } else {
                            "No remaining questions match the " +
                                    "$selectedCount selected categories."
                        },
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

                Spacer(
                    modifier =
                        Modifier.height(
                            2.dp
                        )
                )

                Text(
                    text =
                        "Clear filters",
                    modifier =
                        Modifier
                            .clickable(
                                role =
                                    Role.Button,
                                onClick =
                                    onClearFilters
                            )
                            .padding(
                                horizontal = 14.dp,
                                vertical = 8.dp
                            ),
                    style =
                        MaterialTheme
                            .typography
                            .labelLarge,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            }
        }
    }
}

@Composable
private fun FeedMessageArea(
    content: @Composable () -> Unit
) {
    Box(
        modifier =
            Modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}