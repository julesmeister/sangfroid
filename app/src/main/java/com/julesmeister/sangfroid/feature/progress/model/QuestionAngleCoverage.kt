package com.julesmeister.sangfroid.feature.progress.model

import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.domain.model.QuestionAngle

data class QuestionAngleCoverage(
    val angle: QuestionAngle,
    val questionCount: Int,
    val practisedQuestionCount: Int
)

fun calculateQuestionAngleCoverage(
    allQuestions: List<QuestionEntity>,
    practisedQuestionIds: Set<String>
): List<QuestionAngleCoverage> {
    return QuestionAngle.entries
        .filterNot { angle ->
            angle == QuestionAngle.GENERAL
        }
        .map { angle ->
            val questions =
                allQuestions.filter { question ->
                    question.questionAngle ==
                            angle
                }

            QuestionAngleCoverage(
                angle = angle,
                questionCount =
                    questions.size,
                practisedQuestionCount =
                    questions.count { question ->
                        question.id in
                                practisedQuestionIds
                    }
            )
        }
        .sortedWith(
            compareBy<
                    QuestionAngleCoverage
                    > { coverage ->
                coverage.questionCount
            }
                .thenBy { coverage ->
                    coverage.angle.ordinal
                }
        )
}