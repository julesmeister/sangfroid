package com.julesmeister.sangfroid.domain.history

import com.julesmeister.sangfroid.domain.model.AnswerInputMethod
import com.julesmeister.sangfroid.domain.model.EvaluationStatus

data class PracticeAttempt(
    val id: String,
    val questionId: String,
    val answerText: String,
    val inputMethod: AnswerInputMethod,
    val startedAt: Long,
    val submittedAt: Long,
    val durationSeconds: Int,
    val wordCount: Int,
    val evaluationStatus: EvaluationStatus,
    val modelName: String?,
    val modelVersion: String?,
    val evaluation: PracticeAnswerEvaluation?,
    val revisions: List<SavedRevision>
)

data class PracticeAnswerEvaluation(
    val id: String,
    val attemptId: String,
    val summary: String,
    val strengths: String,
    val missingPoints: String,
    val suggestions: String,
    val improvedOutline: String,
    val improvedAnswerExample: String?,
    val modelName: String,
    val modelVersion: String?,
    val processingMode: String,
    val createdAt: Long
)