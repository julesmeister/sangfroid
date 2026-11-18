package com.julesmeister.sangfroid.data.recommendation

import com.julesmeister.sangfroid.data.local.entity.QuestionEntity
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class QuestionSimilarityChecker @Inject constructor() {

    fun review(
        candidates: List<QuestionEntity>,
        existingQuestions: List<QuestionEntity>,
        maximumAcceptedCount: Int
    ): ReviewResult {
        require(maximumAcceptedCount > 0) {
            "maximumAcceptedCount must be positive."
        }

        val accepted =
            mutableListOf<QuestionEntity>()

        val exactDuplicates =
            mutableListOf<RejectedQuestion>()

        val similarDuplicates =
            mutableListOf<RejectedQuestion>()

        val comparisonPool =
            existingQuestions.toMutableList()

        candidates.forEach { candidate ->
            if (accepted.size >= maximumAcceptedCount) {
                return@forEach
            }

            val candidateText =
                NormalizedQuestion.from(candidate.question)

            val exactMatch =
                comparisonPool.firstOrNull { existing ->
                    NormalizedQuestion
                        .from(existing.question)
                        .normalizedText ==
                            candidateText.normalizedText
                }

            if (exactMatch != null) {
                exactDuplicates +=
                    RejectedQuestion(
                        candidateId = candidate.id,
                        candidateText = candidate.question,
                        matchedQuestionId = exactMatch.id,
                        matchedQuestionText = exactMatch.question,
                        similarity = 1f
                    )

                return@forEach
            }

            val closest =
                comparisonPool
                    .asSequence()
                    .map { existing ->
                        val similarity =
                            similarity(
                                first = candidate,
                                second = existing
                            )

                        SimilarityMatch(
                            question = existing,
                            score = similarity
                        )
                    }
                    .maxByOrNull(SimilarityMatch::score)

            if (
                closest != null &&
                closest.score >= SIMILARITY_REJECTION_THRESHOLD
            ) {
                similarDuplicates +=
                    RejectedQuestion(
                        candidateId = candidate.id,
                        candidateText = candidate.question,
                        matchedQuestionId = closest.question.id,
                        matchedQuestionText = closest.question.question,
                        similarity = closest.score
                    )

                return@forEach
            }

            accepted += candidate
            comparisonPool += candidate
        }

        return ReviewResult(
            accepted = accepted,
            exactDuplicates = exactDuplicates,
            similarDuplicates = similarDuplicates
        )
    }

    private fun similarity(
        first: QuestionEntity,
        second: QuestionEntity
    ): Float {
        val firstNormalized =
            NormalizedQuestion.from(first.question)

        val secondNormalized =
            NormalizedQuestion.from(second.question)

        if (
            firstNormalized.normalizedText ==
            secondNormalized.normalizedText
        ) {
            return 1f
        }

        val tokenSimilarity =
            jaccardSimilarity(
                firstNormalized.meaningfulTokens,
                secondNormalized.meaningfulTokens
            )

        val phraseSimilarity =
            diceSimilarity(
                firstNormalized.bigrams,
                secondNormalized.bigrams
            )

        val evidenceBonus =
            when {
                first.evidenceType.isNullOrBlank() ||
                        first.evidenceId.isNullOrBlank() ||
                        second.evidenceType.isNullOrBlank() ||
                        second.evidenceId.isNullOrBlank() -> {
                    0f
                }

                first.evidenceType.equals(
                    second.evidenceType,
                    ignoreCase = true
                ) &&
                        first.evidenceId == second.evidenceId -> {
                    0.12f
                }

                else -> {
                    0f
                }
            }

        val categoryBonus =
            if (first.category == second.category) {
                0.04f
            } else {
                0f
            }

        return (
                tokenSimilarity * 0.58f +
                        phraseSimilarity * 0.30f +
                        evidenceBonus +
                        categoryBonus
                )
            .coerceIn(0f, 1f)
    }

    private fun jaccardSimilarity(
        first: Set<String>,
        second: Set<String>
    ): Float {
        if (first.isEmpty() || second.isEmpty()) {
            return 0f
        }

        val intersection =
            first.intersect(second).size

        val union =
            first.union(second).size

        if (union == 0) {
            return 0f
        }

        return intersection.toFloat() / union.toFloat()
    }

    private fun diceSimilarity(
        first: Set<String>,
        second: Set<String>
    ): Float {
        if (first.isEmpty() || second.isEmpty()) {
            return 0f
        }

        val intersection =
            first.intersect(second).size

        val denominator =
            first.size + second.size

        if (denominator == 0) {
            return 0f
        }

        return 2f * intersection.toFloat() /
                denominator.toFloat()
    }

    data class ReviewResult(
        val accepted: List<QuestionEntity>,
        val exactDuplicates: List<RejectedQuestion>,
        val similarDuplicates: List<RejectedQuestion>
    ) {
        val rejectedCount: Int
            get() =
                exactDuplicates.size +
                        similarDuplicates.size
    }

    data class RejectedQuestion(
        val candidateId: String,
        val candidateText: String,
        val matchedQuestionId: String,
        val matchedQuestionText: String,
        val similarity: Float
    )

    private data class SimilarityMatch(
        val question: QuestionEntity,
        val score: Float
    )

    private data class NormalizedQuestion(
        val normalizedText: String,
        val meaningfulTokens: Set<String>,
        val bigrams: Set<String>
    ) {
        companion object {

            fun from(
                value: String
            ): NormalizedQuestion {
                val normalized =
                    value
                        .lowercase(Locale.ROOT)
                        .replace(
                            NON_ALPHANUMERIC_REGEX,
                            " "
                        )
                        .replace(
                            WHITESPACE_REGEX,
                            " "
                        )
                        .trim()

                val rawTokens =
                    normalized
                        .split(" ")
                        .filter(String::isNotBlank)

                val meaningfulTokens =
                    rawTokens
                        .asSequence()
                        .filterNot { token ->
                            token in STOP_WORDS
                        }
                        .map { token ->
                            stem(token)
                        }
                        .filter { token ->
                            token.length >= 2
                        }
                        .toSet()

                val effectiveTokens =
                    meaningfulTokens
                        .ifEmpty {
                            rawTokens.toSet()
                        }
                        .toList()

                val bigrams =
                    if (effectiveTokens.size < 2) {
                        emptySet()
                    } else {
                        effectiveTokens
                            .windowed(
                                size = 2,
                                step = 1,
                                partialWindows = false
                            )
                            .map { pair ->
                                pair.joinToString(" ")
                            }
                            .toSet()
                    }

                return NormalizedQuestion(
                    normalizedText = normalized,
                    meaningfulTokens = meaningfulTokens,
                    bigrams = bigrams
                )
            }

            private fun stem(
                token: String
            ): String {
                val suffixes =
                    listOf(
                        "ations",
                        "ation",
                        "ments",
                        "ment",
                        "ingly",
                        "edly",
                        "ing",
                        "ies",
                        "ied",
                        "ed",
                        "es",
                        "s"
                    )

                val suffix =
                    suffixes.firstOrNull { candidate ->
                        token.length >
                                candidate.length + 3 &&
                                token.endsWith(candidate)
                    }

                return if (suffix == null) {
                    token
                } else {
                    token.dropLast(suffix.length)
                }
            }

            private val NON_ALPHANUMERIC_REGEX =
                Regex("[^\\p{L}\\p{N}]+")

            private val WHITESPACE_REGEX =
                Regex("\\s+")

            private val STOP_WORDS =
                setOf(
                    "a",
                    "an",
                    "and",
                    "are",
                    "as",
                    "at",
                    "be",
                    "by",
                    "can",
                    "could",
                    "describe",
                    "did",
                    "do",
                    "does",
                    "explain",
                    "for",
                    "from",
                    "how",
                    "i",
                    "in",
                    "is",
                    "it",
                    "of",
                    "on",
                    "or",
                    "tell",
                    "that",
                    "the",
                    "this",
                    "to",
                    "was",
                    "were",
                    "what",
                    "when",
                    "where",
                    "which",
                    "why",
                    "with",
                    "would",
                    "you",
                    "your"
                )
        }
    }

    companion object {
        private const val SIMILARITY_REJECTION_THRESHOLD =
            0.74f
    }
}