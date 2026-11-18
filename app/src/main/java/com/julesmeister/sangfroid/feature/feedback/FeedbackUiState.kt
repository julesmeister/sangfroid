package com.julesmeister.sangfroid.feature.feedback

import com.julesmeister.sangfroid.data.local.entity.PracticeAttemptEntity
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.domain.evaluation.EvaluationResult
import com.julesmeister.sangfroid.domain.history.SavedRevision
import com.julesmeister.sangfroid.domain.model.EvaluationStatus
import com.julesmeister.sangfroid.domain.progress.QuestionMasteryLevel
import com.julesmeister.sangfroid.domain.progress.QuestionProgress

data class FeedbackUiState(
    val attempt:
    PracticeAttemptEntity? = null,
    val question:
    QuestionEntity? = null,
    val evaluation:
    EvaluationResult? = null,
    val questionProgress:
    QuestionProgress? = null,
    val revisions:
    List<SavedRevision> = emptyList(),
    val isLoading:
    Boolean = true,
    val isEvaluating:
    Boolean = false,
    val isSavingRevision:
    Boolean = false,
    val revisionSaved:
    Boolean = false,
    val isRevisionHistoryVisible:
    Boolean = false,
    val selectedRevision:
    SavedRevision? = null,
    val revisionPendingDeletion:
    SavedRevision? = null,
    val isDeletingRevision:
    Boolean = false,
    val errorMessage:
    String? = null,
    val revisionErrorMessage:
    String? = null
) {

    val evaluationStatus:
            EvaluationStatus?
        get() =
            attempt?.evaluationStatus

    val isQueued:
            Boolean
        get() =
            evaluationStatus ==
                    EvaluationStatus.QUEUED

    val improvedAnswerExample:
            String?
        get() =
            evaluation
                ?.improvedAnswerExample
                ?.normalizeRevisionContent()
                ?.takeIf(
                    String::isNotBlank
                )

    val isCurrentRevisionSaved:
            Boolean
        get() {
            val example =
                improvedAnswerExample
                    ?: return false

            return revisions.any { revision ->
                revision.content
                    .normalizeRevisionContent() ==
                        example
            }
        }

    val canSaveRevision:
            Boolean
        get() =
            attempt != null &&
                    question != null &&
                    improvedAnswerExample != null &&
                    !isLoading &&
                    !isEvaluating &&
                    !isSavingRevision &&
                    !isDeletingRevision &&
                    !isCurrentRevisionSaved

    val canRetry:
            Boolean
        get() =
            attempt != null &&
                    question != null &&
                    !isLoading &&
                    !isEvaluating &&
                    !isDeletingRevision

    val revisionCount:
            Int
        get() =
            revisions.size

    val masteryLevel:
            QuestionMasteryLevel
        get() =
            questionProgress
                ?.masteryLevel
                ?: QuestionMasteryLevel
                    .NOT_STARTED

    val masteryLabel:
            String
        get() =
            masteryLevel.displayName

    val latestScorePercentage:
            Int?
        get() =
            questionProgress
                ?.latestPercentage

    val scoreChangePercentagePoints:
            Int?
        get() =
            questionProgress
                ?.scoreChangePercentagePoints

    val completedEvaluationCount:
            Int
        get() =
            questionProgress
                ?.completedEvaluationCount
                ?: 0

    val strongestCriterion:
            String?
        get() =
            questionProgress
                ?.strongestCriteria
                ?.firstOrNull()
                ?.criterion

    val weakestCriterion:
            String?
        get() =
            questionProgress
                ?.weakestCriteria
                ?.firstOrNull()
                ?.criterion
}

private fun String.normalizeRevisionContent():
        String {
    return trim()
        .replace(
            Regex("\\s+"),
            " "
        )
}