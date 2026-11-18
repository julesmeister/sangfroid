package com.julesmeister.sangfroid.feature.saved.model

import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.domain.progress.QuestionMasteryLevel
import com.julesmeister.sangfroid.domain.progress.QuestionProgress

data class SavedQuestionItem(
    val question: QuestionEntity,
    val progress: QuestionProgress
) {

    val id: String
        get() =
            question.id

    val masteryLevel: QuestionMasteryLevel
        get() =
            progress.masteryLevel

    val masteryLabel: String
        get() =
            masteryLevel.displayName

    val latestPercentage: Int?
        get() =
            progress.latestPercentage

    val categoryLabel: String
        get() =
            question.category
                .name
                .replace(
                    "_",
                    " "
                )

    val progressLabel: String
        get() =
            when {
                progress.attemptCount <= 0 -> {
                    "Not practised yet"
                }

                latestPercentage != null -> {
                    buildString {
                        append(
                            progress.attemptCount
                        )

                        append(
                            if (
                                progress.attemptCount ==
                                1
                            ) {
                                " attempt"
                            } else {
                                " attempts"
                            }
                        )

                        append(
                            " · Latest "
                        )

                        append(
                            latestPercentage
                        )

                        append(
                            "%"
                        )
                    }
                }

                else -> {
                    buildString {
                        append(
                            progress.attemptCount
                        )

                        append(
                            if (
                                progress.attemptCount ==
                                1
                            ) {
                                " attempt"
                            } else {
                                " attempts"
                            }
                        )

                        append(
                            " · Awaiting feedback"
                        )
                    }
                }
            }

    val revisionLabel: String?
        get() {
            val count =
                progress.savedRevisionCount

            if (count <= 0) {
                return null
            }

            return "$count saved " +
                    if (count == 1) {
                        "revision"
                    } else {
                        "revisions"
                    }
        }
}