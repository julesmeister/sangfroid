package com.julesmeister.sangfroid.feature.practice.speech

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnswerSpeechMerger @Inject constructor() {

    fun merge(
        existingAnswer: String,
        recognizedSpeech: String
    ): String {
        val existing =
            existingAnswer
                .normalizeWhitespace()

        val speech =
            recognizedSpeech
                .normalizeWhitespace()

        if (speech.isBlank()) {
            return existing
        }

        if (existing.isBlank()) {
            return speech
                .capitalizeFirstLetter()
        }

        if (
            existing.equals(
                other = speech,
                ignoreCase = true
            )
        ) {
            return existing
        }

        if (
            speech.startsWith(
                prefix = existing,
                ignoreCase = true
            )
        ) {
            return speech
        }

        if (
            existing.endsWith(
                suffix = speech,
                ignoreCase = true
            )
        ) {
            return existing
        }

        val overlapLength =
            findLargestWordOverlap(
                existingAnswer = existing,
                recognizedSpeech = speech
            )

        if (overlapLength > 0) {
            val speechWords =
                speech.split(
                    WHITESPACE_REGEX
                )

            val remainingSpeech =
                speechWords
                    .drop(
                        overlapLength
                    )
                    .joinToString(
                        separator = " "
                    )
                    .trim()

            if (remainingSpeech.isBlank()) {
                return existing
            }

            return joinAnswerAndSpeech(
                existingAnswer = existing,
                recognizedSpeech =
                    remainingSpeech
            )
        }

        return joinAnswerAndSpeech(
            existingAnswer = existing,
            recognizedSpeech = speech
        )
    }

    fun buildLiveAnswer(
        existingAnswer: String,
        partialTranscript: String
    ): String {
        val existing =
            existingAnswer
                .normalizeWhitespace()

        val partial =
            partialTranscript
                .normalizeWhitespace()

        if (partial.isBlank()) {
            return existing
        }

        if (existing.isBlank()) {
            return partial
                .capitalizeFirstLetter()
        }

        if (
            partial.startsWith(
                prefix = existing,
                ignoreCase = true
            )
        ) {
            return partial
        }

        if (
            existing.endsWith(
                suffix = partial,
                ignoreCase = true
            )
        ) {
            return existing
        }

        return joinAnswerAndSpeech(
            existingAnswer = existing,
            recognizedSpeech = partial
        )
    }

    private fun joinAnswerAndSpeech(
        existingAnswer: String,
        recognizedSpeech: String
    ): String {
        val existing =
            existingAnswer.trimEnd()

        val speech =
            recognizedSpeech.trimStart()

        if (existing.isBlank()) {
            return speech
                .capitalizeFirstLetter()
        }

        if (speech.isBlank()) {
            return existing
        }

        val preparedSpeech =
            when {
                existing.endsWithSentencePunctuation() -> {
                    speech.capitalizeFirstLetter()
                }

                speech.startsWithPunctuation() -> {
                    speech
                }

                else -> {
                    speech.lowercaseFirstLetterUnlessProtected()
                }
            }

        return when {
            preparedSpeech.startsWithPunctuation() -> {
                existing +
                        preparedSpeech
            }

            else -> {
                "$existing $preparedSpeech"
            }
        }
            .normalizeWhitespaceAroundPunctuation()
    }

    private fun findLargestWordOverlap(
        existingAnswer: String,
        recognizedSpeech: String
    ): Int {
        val existingWords =
            existingAnswer
                .split(
                    WHITESPACE_REGEX
                )
                .filter(
                    String::isNotBlank
                )

        val speechWords =
            recognizedSpeech
                .split(
                    WHITESPACE_REGEX
                )
                .filter(
                    String::isNotBlank
                )

        val maximumOverlap =
            minOf(
                existingWords.size,
                speechWords.size,
                MAXIMUM_OVERLAP_WORDS
            )

        for (
        overlapSize in
        maximumOverlap downTo MINIMUM_OVERLAP_WORDS
        ) {
            val existingSuffix =
                existingWords
                    .takeLast(
                        overlapSize
                    )
                    .joinToString(
                        separator = " "
                    )
                    .normalizeForComparison()

            val speechPrefix =
                speechWords
                    .take(
                        overlapSize
                    )
                    .joinToString(
                        separator = " "
                    )
                    .normalizeForComparison()

            if (
                existingSuffix ==
                speechPrefix
            ) {
                return overlapSize
            }
        }

        return 0
    }

    private fun String.normalizeWhitespace():
            String {
        return trim()
            .replace(
                WHITESPACE_REGEX,
                " "
            )
            .normalizeWhitespaceAroundPunctuation()
    }

    private fun String.normalizeWhitespaceAroundPunctuation():
            String {
        return replace(
            SPACE_BEFORE_PUNCTUATION_REGEX,
            "$1"
        )
            .replace(
                MULTIPLE_SPACES_REGEX,
                " "
            )
            .trim()
    }

    private fun String.normalizeForComparison():
            String {
        return lowercase()
            .replace(
                COMPARISON_PUNCTUATION_REGEX,
                ""
            )
            .replace(
                WHITESPACE_REGEX,
                " "
            )
            .trim()
    }

    private fun String.endsWithSentencePunctuation():
            Boolean {
        val lastCharacter =
            lastOrNull()
                ?: return false

        return lastCharacter in
                SENTENCE_ENDINGS
    }

    private fun String.startsWithPunctuation():
            Boolean {
        val firstCharacter =
            firstOrNull()
                ?: return false

        return firstCharacter in
                ATTACHED_PUNCTUATION
    }

    private fun String.capitalizeFirstLetter():
            String {
        val firstLetterIndex =
            indexOfFirst {
                it.isLetter()
            }

        if (firstLetterIndex < 0) {
            return this
        }

        return replaceRange(
            range =
                firstLetterIndex..
                        firstLetterIndex,
            replacement =
                this[firstLetterIndex]
                    .uppercase()
        )
    }

    private fun String.lowercaseFirstLetterUnlessProtected():
            String {
        val firstWord =
            substringBefore(
                delimiter = " "
            )
                .trim(
                    *ATTACHED_PUNCTUATION
                        .toCharArray()
                )

        if (
            firstWord in
            PROTECTED_INITIAL_WORDS
        ) {
            return this
        }

        val firstLetterIndex =
            indexOfFirst {
                it.isLetter()
            }

        if (firstLetterIndex < 0) {
            return this
        }

        val character =
            this[firstLetterIndex]

        if (!character.isUpperCase()) {
            return this
        }

        return replaceRange(
            range =
                firstLetterIndex..
                        firstLetterIndex,
            replacement =
                character.lowercase()
        )
    }

    companion object {

        private const val MINIMUM_OVERLAP_WORDS =
            2

        private const val MAXIMUM_OVERLAP_WORDS =
            16

        private val WHITESPACE_REGEX =
            Regex("\\s+")

        private val MULTIPLE_SPACES_REGEX =
            Regex(" {2,}")

        private val SPACE_BEFORE_PUNCTUATION_REGEX =
            Regex("\\s+([,.!?;:])")

        private val COMPARISON_PUNCTUATION_REGEX =
            Regex("[^\\p{L}\\p{N}\\s]")

        private const val SENTENCE_ENDINGS =
            ".!?"

        private const val ATTACHED_PUNCTUATION =
            ".,!?;:"

        private val PROTECTED_INITIAL_WORDS =
            setOf(
                "I",
                "API",
                "APIs",
                "AWS",
                "CI",
                "CD",
                "CI/CD",
                "CSS",
                "DB",
                "EC2",
                "ECR",
                "ECS",
                "HTML",
                "HTTP",
                "HTTPS",
                "IAM",
                "IDE",
                "JSON",
                "JWT",
                "NoSQL",
                "OAuth",
                "REST",
                "S3",
                "SDK",
                "SNS",
                "SQL",
                "SQS",
                "UI",
                "UX",
                "XML"
            )
    }
}