package com.julesmeister.sangfroid.data.recommendation

import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import com.julesmeister.sangfroid.domain.progress.QuestionProgress
import com.julesmeister.sangfroid.domain.progress.QuestionProgressRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest

@Singleton
class ReactiveQuestionRecommendationRanker @Inject constructor(
    private val questionProgressRepository:
    QuestionProgressRepository,
    private val questionRecommendationRanker:
    QuestionRecommendationRanker
) {

    fun observeRankedQuestions(
        questions: List<QuestionEntity>
    ): Flow<List<RankedQuestion>> {
        if (questions.isEmpty()) {
            return flowOf(
                emptyList()
            )
        }

        val progressFlows =
            questions.map { question ->
                questionProgressRepository
                    .observeQuestionProgress(
                        questionId =
                            question.id
                    )
            }

        return combine(
            progressFlows
        ) { progressArray ->
            progressArray.toList()
        }
            .mapLatest {
                /*
                 * QuestionRecommendationRanker reads the latest
                 * profile, CV skills, weak-skill evidence,
                 * question-skill mappings and progress values.
                 *
                 * This Flow is invalidated whenever any observed
                 * question's attempts, evaluations or saved
                 * revisions change.
                 */
                questionRecommendationRanker
                    .rankWithScores(
                        questions =
                            questions
                    )
            }
    }

    fun observeProgress(
        questions: List<QuestionEntity>
    ): Flow<Map<String, QuestionProgress>> {
        if (questions.isEmpty()) {
            return flowOf(
                emptyMap()
            )
        }

        val progressFlows =
            questions.map { question ->
                questionProgressRepository
                    .observeQuestionProgress(
                        questionId =
                            question.id
                    )
            }

        return combine(
            progressFlows
        ) { progressArray ->
            progressArray
                .associateBy(
                    QuestionProgress::questionId
                )
        }
    }
}