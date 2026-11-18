// app/src/main/java/com/julesmeister/sangfroid/navigation/SangfroidNavHost.kt

package com.julesmeister.sangfroid.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.julesmeister.sangfroid.domain.history.SavedRevision
import com.julesmeister.sangfroid.feature.feed.FeedScreen
import com.julesmeister.sangfroid.feature.feedback.FeedbackScreen
import com.julesmeister.sangfroid.feature.history.QuestionHistoryScreen
import com.julesmeister.sangfroid.feature.practice.PracticeScreen
import com.julesmeister.sangfroid.feature.profile.ProfileScreen
import com.julesmeister.sangfroid.feature.progress.ProgressScreen
import com.julesmeister.sangfroid.feature.saved.SavedQuestionsScreen

@Composable
fun SangfroidNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = SangfroidDestination.Feed,
        modifier = modifier
    ) {
        composable(
            route = SangfroidDestination.Feed
        ) {
            FeedScreen(
                onPracticeQuestion = { questionId ->
                    navController.navigate(
                        SangfroidDestination.practice(
                            questionId
                        )
                    ) {
                        launchSingleTop = true
                    }
                },
                onOpenSaved = {
                    navController.navigate(
                        SangfroidDestination.Saved
                    ) {
                        launchSingleTop = true
                    }
                },
                onOpenProgress = {
                    navController.navigate(
                        SangfroidDestination.Progress
                    ) {
                        launchSingleTop = true
                    }
                },
                onOpenProfile = {
                    navController.navigate(
                        SangfroidDestination.Profile
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = SangfroidDestination.Saved
        ) {
            SavedQuestionsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenHistory = { questionId ->
                    navController.navigate(
                        SangfroidDestination.questionHistory(
                            questionId
                        )
                    ) {
                        launchSingleTop = true
                    }
                },
                onPracticeQuestion = { questionId ->
                    navController.navigate(
                        SangfroidDestination.practice(
                            questionId
                        )
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = SangfroidDestination.Profile
        ) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = SangfroidDestination.Progress
        ) {
            ProgressScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPracticeQuestion = { questionId ->
                    navController.navigate(
                        SangfroidDestination.questionHistory(
                            questionId
                        )
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = SangfroidDestination.QuestionHistory,
            arguments = listOf(
                navArgument(
                    SangfroidDestination
                        .QuestionHistoryArgument
                ) {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) {
            QuestionHistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPracticeAgain = { questionId ->
                    navController.navigate(
                        SangfroidDestination.practice(
                            questionId
                        )
                    ) {
                        launchSingleTop = true
                    }
                },
                onOpenFeedback = { attemptId ->
                    navController.navigate(
                        SangfroidDestination.feedback(
                            attemptId
                        )
                    ) {
                        launchSingleTop = true
                    }
                },
                onUseRevision = {
                        questionId,
                        revision ->

                    openPracticeWithRevision(
                        navController = navController,
                        questionId = questionId,
                        revision = revision
                    )
                }
            )
        }

        composable(
            route = SangfroidDestination.Practice,
            arguments = listOf(
                navArgument(
                    SangfroidDestination
                        .PracticeArgument
                ) {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { practiceEntry ->
            val revisionRequest by
            practiceEntry
                .savedStateHandle
                .getStateFlow<String?>(
                    key =
                        SangfroidDestination
                            .PracticeRevisionRequestKey,
                    initialValue =
                        null
                )
                .collectAsStateWithLifecycle()

            PracticeScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onOpenFeedback = { attemptId ->
                    navController.navigate(
                        SangfroidDestination.feedback(
                            attemptId
                        )
                    ) {
                        launchSingleTop = true
                    }
                },
                revisionRequest =
                    revisionRequest,
                onRevisionRequestConsumed = {
                    practiceEntry.savedStateHandle[
                        SangfroidDestination
                            .PracticeRevisionRequestKey
                    ] = null
                }
            )
        }

        composable(
            route = SangfroidDestination.Feedback,
            arguments = listOf(
                navArgument(
                    SangfroidDestination
                        .FeedbackArgument
                ) {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) {
            FeedbackScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onTryAgain = { questionId ->
                    openPractice(
                        navController = navController,
                        questionId = questionId
                    )
                },
                onUseRevision = {
                        questionId,
                        revision ->

                    openPracticeWithRevision(
                        navController = navController,
                        questionId = questionId,
                        revision = revision
                    )
                }
            )
        }
    }
}

private fun openPractice(
    navController: NavHostController,
    questionId: String
) {
    val normalizedQuestionId =
        questionId.trim()

    if (normalizedQuestionId.isBlank()) {
        return
    }

    val practiceRoute =
        SangfroidDestination.practice(
            normalizedQuestionId
        )

    val existingPracticeEntry =
        findPracticeEntry(
            navController = navController,
            questionId = normalizedQuestionId
        )

    if (existingPracticeEntry != null) {
        existingPracticeEntry.savedStateHandle[
            SangfroidDestination
                .PracticeSelectedAttemptIdKey
        ] = null

        navController.popBackStack(
            route = practiceRoute,
            inclusive = false
        )

        return
    }

    navController.navigate(
        practiceRoute
    ) {
        launchSingleTop = true
    }
}

private fun openPracticeWithRevision(
    navController: NavHostController,
    questionId: String,
    revision: SavedRevision
) {
    val normalizedQuestionId =
        questionId.trim()

    val normalizedScript =
        revision.content
            .trim()
            .takeIf(
                String::isNotBlank
            )
            ?: return

    if (normalizedQuestionId.isBlank()) {
        return
    }

    val practiceRoute =
        SangfroidDestination.practice(
            normalizedQuestionId
        )

    val existingPracticeEntry =
        findPracticeEntry(
            navController = navController,
            questionId = normalizedQuestionId
        )

    if (existingPracticeEntry != null) {
        existingPracticeEntry.savedStateHandle[
            SangfroidDestination
                .PracticeSelectedAttemptIdKey
        ] = null

        existingPracticeEntry.savedStateHandle[
            SangfroidDestination
                .PracticeRevisionRequestKey
        ] = normalizedScript

        navController.popBackStack(
            route = practiceRoute,
            inclusive = false
        )

        return
    }

    navController.navigate(
        practiceRoute
    ) {
        launchSingleTop = true
    }

    navController.currentBackStackEntry
        ?.takeIf { entry ->
            entry.isPracticeEntryFor(
                normalizedQuestionId
            )
        }
        ?.savedStateHandle
        ?.apply {
            this[
                SangfroidDestination
                    .PracticeSelectedAttemptIdKey
            ] = null

            this[
                SangfroidDestination
                    .PracticeRevisionRequestKey
            ] = normalizedScript
        }
}

private fun findPracticeEntry(
    navController: NavHostController,
    questionId: String
): NavBackStackEntry? {
    val previousEntry =
        navController.previousBackStackEntry

    if (
        previousEntry.isPracticeEntryFor(
            questionId
        )
    ) {
        return previousEntry
    }

    return runCatching {
        navController.getBackStackEntry(
            SangfroidDestination.practice(
                questionId
            )
        )
    }.getOrNull()
}

private fun NavBackStackEntry?.isPracticeEntryFor(
    questionId: String
): Boolean {
    if (this == null) {
        return false
    }

    return arguments
        ?.getString(
            SangfroidDestination.PracticeArgument
        ) == questionId
}