package com.julesmeister.sangfroid.feature.history.model

import com.julesmeister.sangfroid.domain.history.PracticeAttempt
import com.julesmeister.sangfroid.domain.model.EvaluationStatus
import java.text.DateFormat
import java.util.Date

data class AttemptHistoryItem(
    val attempt: PracticeAttempt,
    val sequenceNumber: Int
) {

    val id: String
        get() =
            attempt.id

    val title: String
        get() =
            "Attempt $sequenceNumber"

    val submittedLabel: String
        get() =
            DateFormat
                .getDateTimeInstance(
                    DateFormat.MEDIUM,
                    DateFormat.SHORT
                )
                .format(
                    Date(
                        attempt.submittedAt
                    )
                )

    val durationLabel: String
        get() {
            val safeDuration =
                attempt.durationSeconds
                    .coerceAtLeast(
                        0
                    )

            val minutes =
                safeDuration / 60

            val seconds =
                safeDuration % 60

            return when {
                minutes > 0 &&
                        seconds > 0 -> {
                    "${minutes}m ${seconds}s"
                }

                minutes > 0 -> {
                    "${minutes}m"
                }

                else -> {
                    "${seconds}s"
                }
            }
        }

    val wordCountLabel: String
        get() =
            buildString {
                append(
                    attempt.wordCount
                        .coerceAtLeast(
                            0
                        )
                )

                append(
                    if (
                        attempt.wordCount ==
                        1
                    ) {
                        " word"
                    } else {
                        " words"
                    }
                )
            }

    val evaluationLabel: String
        get() =
            when {
                attempt.evaluation != null -> {
                    "Evaluated"
                }

                attempt.evaluationStatus ==
                        EvaluationStatus.COMPLETED -> {
                    "Evaluated"
                }

                attempt.evaluationStatus ==
                        EvaluationStatus.FAILED -> {
                    "Evaluation failed"
                }

                else -> {
                    "Awaiting feedback"
                }
            }

    val hasEvaluation: Boolean
        get() =
            attempt.evaluation != null

    val summary: String?
        get() =
            attempt.evaluation
                ?.summary
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )

    val answerPreview: String
        get() =
            attempt.answerText
                .trim()
                .replace(
                    Regex("\\s+"),
                    " "
                )
                .ifBlank {
                    "No answer text was saved."
                }

    val revisionCount: Int
        get() =
            attempt.revisions.size

    val supportingLabel: String
        get() =
            buildString {
                append(
                    submittedLabel
                )

                append(
                    " · "
                )

                append(
                    durationLabel
                )

                append(
                    " · "
                )

                append(
                    wordCountLabel
                )
            }
}