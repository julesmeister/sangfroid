package com.julesmeister.sangfroid.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.julesmeister.sangfroid.data.local.entity.AnswerEvaluationEntity
import com.julesmeister.sangfroid.data.local.entity.FollowUpQuestionEntity
import com.julesmeister.sangfroid.data.local.entity.RubricScoreEntity

data class StoredEvaluation(
    @Embedded
    val evaluation: AnswerEvaluationEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "evaluationId"
    )
    val rubricScores: List<RubricScoreEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "evaluationId"
    )
    val followUpQuestions: List<FollowUpQuestionEntity>
)