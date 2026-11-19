package com.julesmeister.sangfroid.feature.practice

import com.julesmeister.sangfroid.data.local.entity.PracticeAttemptEntity
import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.domain.model.AnswerInputMethod
import com.julesmeister.sangfroid.domain.repository.IdealAnswerRepository

data class PracticeUiState(
    val isLoading: Boolean = true,
    val question: QuestionEntity? = null,
    val answerText: String = "",
    val suggestedScript: String? = null,
    val inputMethod: AnswerInputMethod =
        AnswerInputMethod.TYPED,
    val startedAt: Long =
        System.currentTimeMillis(),
    val attempts:
    List<PracticeAttemptEntity> =
        emptyList(),
    val selectedAttemptId: String? = null,
    val isDraftSaved: Boolean = true,
    val isSubmitting: Boolean = false,
    val submittedAttemptId: String? = null,
    val pendingNavigationAttemptId: String? = null,

    val idealAnswer:
    IdealAnswerRepository.IdealAnswer? =
        null,

    val isGeneratingIdealAnswer: Boolean =
        false,

    val idealAnswerErrorMessage: String? =
        null,

    val errorMessage: String? = null
) {

    val selectedAttempt:
            PracticeAttemptEntity?
        get() =
            selectedAttemptId
                ?.let { id ->
                    attempts
                        .firstOrNull { attempt ->
                            attempt.id ==
                                    id
                        }
                }

    val isViewingHistoricalAttempt:
            Boolean
        get() =
            selectedAttempt !=
                    null

    val hasSuggestedScript:
            Boolean
        get() =
            !suggestedScript
                .isNullOrBlank()

    val hasIdealAnswer:
            Boolean
        get() =
            !idealAnswer
                ?.content
                .isNullOrBlank()

    val idealAnswerRevision:
            Int
        get() =
            idealAnswer
                ?.revisionNumber
                ?: 0

    val canGenerateIdealAnswer:
            Boolean
        get() =
            question != null &&
                    !isLoading &&
                    !isViewingHistoricalAttempt &&
                    !isSubmitting &&
                    !isGeneratingIdealAnswer

    val canRefreshIdealAnswer:
            Boolean
        get() =
            hasIdealAnswer &&
                    canGenerateIdealAnswer

    val wordCount:
            Int
        get() {
            val normalizedAnswer =
                answerText.trim()

            if (
                normalizedAnswer.isBlank()
            ) {
                return 0
            }

            return normalizedAnswer
                .split(
                    Regex("\\s+")
                )
                .count(
                    String::isNotBlank
                )
        }

    val canSubmit:
            Boolean
        get() =
            question != null &&
                    !isViewingHistoricalAttempt &&
                    answerText.isNotBlank() &&
                    !isSubmitting
}