package com.julesmeister.sangfroid.domain.progress

enum class QuestionMasteryLevel {
    NOT_STARTED,
    NEEDS_WORK,
    DEVELOPING,
    STRONG,
    MASTERED;

    val displayName: String
        get() {
            return when (this) {
                NOT_STARTED -> {
                    "Not started"
                }

                NEEDS_WORK -> {
                    "Needs work"
                }

                DEVELOPING -> {
                    "Developing"
                }

                STRONG -> {
                    "Strong"
                }

                MASTERED -> {
                    "Mastered"
                }
            }
        }

    companion object {

        private const val DevelopingThreshold =
            0.50f

        private const val StrongThreshold =
            0.70f

        private const val MasteredThreshold =
            0.85f

        private const val RequiredAttemptsForMastery =
            3

        fun calculate(
            normalizedScore: Float?,
            completedEvaluationCount: Int
        ): QuestionMasteryLevel {
            if (
                normalizedScore == null ||
                completedEvaluationCount <= 0
            ) {
                return NOT_STARTED
            }

            val safeScore =
                normalizedScore.coerceIn(
                    minimumValue = 0f,
                    maximumValue = 1f
                )

            return when {
                safeScore >= MasteredThreshold &&
                        completedEvaluationCount >=
                        RequiredAttemptsForMastery -> {
                    MASTERED
                }

                safeScore >= StrongThreshold -> {
                    STRONG
                }

                safeScore >= DevelopingThreshold -> {
                    DEVELOPING
                }

                else -> {
                    NEEDS_WORK
                }
            }
        }
    }
}