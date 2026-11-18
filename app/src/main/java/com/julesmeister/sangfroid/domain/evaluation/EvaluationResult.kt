package com.julesmeister.sangfroid.domain.evaluation

data class EvaluationResult(
    val id: String,
    val attemptId: String,
    val summary: String,
    val strengths: List<String>,
    val missingPoints: List<String>,
    val suggestions: List<String>,
    val improvedOutline: List<String>,
    val improvedAnswerExample: String?,
    val followUpQuestions: List<FollowUpQuestion>,
    val rubricScores: List<EvaluationRubricScore>,
    val modelName: String,
    val modelVersion: String,
    val processingMode: EvaluationProcessingMode,
    val createdAt: Long
)

data class FollowUpQuestion(
    val question: String,
    val reason: String?
)

data class EvaluationRubricScore(
    val criterion: String,
    val score: Int,
    val maximumScore: Int,
    val explanation: String
) {
    init {
        require(maximumScore > 0) {
            "Maximum score must be positive."
        }

        require(score in 0..maximumScore) {
            "Score must be between zero and maximumScore."
        }
    }

    val normalizedProgress: Float
        get() = score.toFloat() /
                maximumScore.toFloat()
}

enum class EvaluationProcessingMode {
    LOCAL_QWEN,
    CLOUD_QWEN,
    RULE_BASED_FALLBACK
}