package com.julesmeister.sangfroid.data.cv

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CvTextValidator @Inject constructor() {

    fun validate(
        text: String,
        pageCount: Int
    ): CvImportResult {
        val normalizedText =
            normalizeText(
                text
            )

        val characterCount =
            normalizedText.count { character ->
                !character.isWhitespace()
            }

        val words =
            normalizedText
                .split(
                    WHITESPACE_REGEX
                )
                .filter { word ->
                    word.isNotBlank()
                }

        val wordCount =
            words.size

        if (
            normalizedText.isBlank()
        ) {
            return CvImportResult.InsufficientText(
                extractedText = normalizedText,
                pageCount = pageCount,
                characterCount = 0,
                wordCount = 0,
                reason =
                    "No readable text could be extracted from the PDF."
            )
        }

        if (
            characterCount <
            MINIMUM_CHARACTER_COUNT
        ) {
            return CvImportResult.InsufficientText(
                extractedText = normalizedText,
                pageCount = pageCount,
                characterCount = characterCount,
                wordCount = wordCount,
                reason =
                    "The PDF contains too little extractable text."
            )
        }

        if (
            wordCount <
            MINIMUM_WORD_COUNT
        ) {
            return CvImportResult.InsufficientText(
                extractedText = normalizedText,
                pageCount = pageCount,
                characterCount = characterCount,
                wordCount = wordCount,
                reason =
                    "The extracted PDF text does not contain enough words."
            )
        }

        val readableCharacterCount =
            normalizedText.count { character ->
                character.isLetterOrDigit() ||
                        character.isWhitespace() ||
                        character in BASIC_PUNCTUATION
            }

        val readableRatio =
            if (
                normalizedText.isEmpty()
            ) {
                0.0
            } else {
                readableCharacterCount.toDouble() /
                        normalizedText.length.toDouble()
            }

        if (
            readableRatio <
            MINIMUM_READABLE_RATIO
        ) {
            return CvImportResult.InsufficientText(
                extractedText = normalizedText,
                pageCount = pageCount,
                characterCount = characterCount,
                wordCount = wordCount,
                reason =
                    "The extracted PDF text appears corrupted or unreadable."
            )
        }

        return CvImportResult.Success(
            text = normalizedText,
            pageCount = pageCount,
            characterCount = characterCount,
            wordCount = wordCount
        )
    }

    private fun normalizeText(
        value: String
    ): String {
        return value
            .replace(
                "\u0000",
                ""
            )
            .replace(
                HORIZONTAL_WHITESPACE_REGEX,
                " "
            )
            .replace(
                LINE_SPACING_REGEX,
                "\n"
            )
            .replace(
                EXCESSIVE_NEWLINES_REGEX,
                "\n\n"
            )
            .trim()
    }

    companion object {
        private const val MINIMUM_CHARACTER_COUNT =
            200

        private const val MINIMUM_WORD_COUNT =
            30

        private const val MINIMUM_READABLE_RATIO =
            0.70

        private const val BASIC_PUNCTUATION =
            ".,;:!?()[]{}'\"+-/%&@#_*•–—"

        private val WHITESPACE_REGEX =
            Regex(
                "\\s+"
            )

        private val HORIZONTAL_WHITESPACE_REGEX =
            Regex(
                "[\\t\\x0B\\f\\r ]+"
            )

        private val LINE_SPACING_REGEX =
            Regex(
                " *\\n *"
            )

        private val EXCESSIVE_NEWLINES_REGEX =
            Regex(
                "\\n{3,}"
            )
    }
}