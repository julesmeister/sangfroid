package com.julesmeister.sangfroid.domain.cv

import com.julesmeister.sangfroid.domain.model.SkillCode

data class CvExtractionResult(
    val document: CvDocument,
    val extractedText: String,
    val pageCount: Int,
    val skills: List<CvSkillMatch>,
    val warnings: List<CvExtractionWarning> =
        emptyList()
) {
    val skillCodes: Set<SkillCode>
        get() =
            skills
                .map {
                    it.skillCode
                }
                .toSet()

    val hasUsableText: Boolean
        get() =
            extractedText
                .isNotBlank()

    val hasSkills: Boolean
        get() =
            skills
                .isNotEmpty()

    init {
        require(
            pageCount > 0
        ) {
            "CV page count must be greater than zero."
        }
    }
}

data class CvTextExtraction(
    val text: String,
    val pageCount: Int,
    val warnings: List<CvExtractionWarning> =
        emptyList()
) {
    init {
        require(
            pageCount > 0
        ) {
            "PDF page count must be greater than zero."
        }
    }
}

data class CvSkillMatch(
    val skillCode: SkillCode,
    val confidence: Float,
    val matchedAliases: List<String>,
    val evidence: List<String>
) {
    init {
        require(
            confidence in 0f..1f
        ) {
            "Skill confidence must be between zero and one."
        }
    }
}

enum class CvExtractionWarning {
    FILE_SIZE_UNKNOWN,
    TEXT_WAS_NORMALIZED,
    LIMITED_PAGE_COUNT,
    VERY_LITTLE_TEXT,
    POSSIBLY_SCANNED_DOCUMENT
}

sealed interface CvImportFailure {

    val userMessage: String

    data object UnsupportedDocument :
        CvImportFailure {

        override val userMessage =
            "Choose a PDF version of your CV."
    }

    data object FileTooLarge :
        CvImportFailure {

        override val userMessage =
            "The CV must be smaller than 10 MB."
    }

    data object UnableToOpen :
        CvImportFailure {

        override val userMessage =
            "Sangfroid could not open this PDF."
    }

    data object PasswordProtected :
        CvImportFailure {

        override val userMessage =
            "Password-protected PDFs are not supported."
    }

    data object NoReadableText :
        CvImportFailure {

        override val userMessage =
            "No readable text was found. This may be a scanned or image-only PDF."
    }

    data class ExtractionFailed(
        val technicalMessage: String?
    ) : CvImportFailure {

        override val userMessage =
            "Sangfroid could not read the CV text."
    }
}

class CvImportException(
    val failure: CvImportFailure,
    cause: Throwable? = null
) : Exception(
    failure.userMessage,
    cause
)