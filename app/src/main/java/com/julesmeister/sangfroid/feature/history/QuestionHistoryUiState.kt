package com.julesmeister.sangfroid.feature.history

import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.domain.progress.QuestionProgress
import com.julesmeister.sangfroid.feature.history.model.AttemptHistoryItem
import com.julesmeister.sangfroid.feature.history.model.RevisionHistoryItem

data class QuestionHistoryUiState(
    val isLoading: Boolean = true,
    val question: QuestionEntity? = null,
    val progress: QuestionProgress? = null,
    val attempts:
    List<AttemptHistoryItem> =
        emptyList(),
    val revisions:
    List<RevisionHistoryItem> =
        emptyList(),
    val deletingAttemptIds:
    Set<String> =
        emptySet(),
    val deletingRevisionIds:
    Set<String> =
        emptySet(),
    val errorMessage: String? = null
) {

    val questionId: String?
        get() =
            question?.id

    val questionText: String
        get() =
            question
                ?.question
                ?.trim()
                .orEmpty()

    val attemptCount: Int
        get() =
            progress
                ?.attemptCount
                ?: attempts.size

    val evaluatedAttemptCount: Int
        get() =
            progress
                ?.completedEvaluationCount
                ?: attempts.count {
                    it.hasEvaluation
                }

    val revisionCount: Int
        get() =
            revisions.size

    val latestPercentage: Int?
        get() =
            progress?.latestPercentage

    val averagePercentage: Int?
        get() =
            progress?.averagePercentage

    val scoreChangePercentagePoints: Int?
        get() =
            progress
                ?.scoreChangePercentagePoints

    val masteryLabel: String
        get() =
            progress
                ?.masteryLevel
                ?.displayName
                ?: "Not started"

    val hasAttempts: Boolean
        get() =
            attempts.isNotEmpty()

    val hasRevisions: Boolean
        get() =
            revisions.isNotEmpty()

    val isEmpty: Boolean
        get() =
            !isLoading &&
                    !hasAttempts &&
                    !hasRevisions

    val canPracticeAgain: Boolean
        get() =
            !isLoading &&
                    question != null

    fun isDeletingAttempt(
        attemptId: String
    ): Boolean {
        return attemptId in
                deletingAttemptIds
    }

    fun isDeletingRevision(
        revisionId: String
    ): Boolean {
        return revisionId in
                deletingRevisionIds
    }
}