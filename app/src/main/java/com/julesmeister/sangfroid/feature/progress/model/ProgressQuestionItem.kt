package com.julesmeister.sangfroid.feature.progress.model

import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.domain.progress.QuestionMasteryLevel
import com.julesmeister.sangfroid.domain.progress.QuestionProgress

data class ProgressQuestionItem(
    val question: QuestionEntity,
    val progress: QuestionProgress
) {

    val latestPercentage: Int?
        get() =
            progress.latestPercentage

    val masteryLevel: QuestionMasteryLevel
        get() =
            progress.masteryLevel

    val masteryLabel: String
        get() =
            masteryLevel.displayName

    val supportingLabel: String
        get() {
            val score =
                progress.latestPercentage

            return when {
                score != null -> {
                    buildString {
                        append("$score%")
                        append(" · ")
                        append(progress.attemptCount)
                        append(
                            if (progress.attemptCount == 1) {
                                " attempt"
                            } else {
                                " attempts"
                            }
                        )
                    }
                }

                progress.attemptCount > 0 -> {
                    buildString {
                        append(progress.attemptCount)
                        append(
                            if (progress.attemptCount == 1) {
                                " attempt"
                            } else {
                                " attempts"
                            }
                        )
                        append(" · waiting for feedback")
                    }
                }

                else -> {
                    "Not practised yet"
                }
            }
        }
}