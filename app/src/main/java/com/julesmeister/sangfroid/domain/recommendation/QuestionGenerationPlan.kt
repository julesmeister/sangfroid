package com.julesmeister.sangfroid.domain.recommendation

import com.julesmeister.sangfroid.domain.model.QuestionAngle

data class QuestionGenerationPlan(
    val evidence: CvEvidence,
    val requestedAngles: List<QuestionAngle>,
    val coveredAngles: List<QuestionAngle>,
    val previousQuestionCount: Int
)