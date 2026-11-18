package com.julesmeister.sangfroid.data.cv

sealed interface CvImportResult {

    data class Success(
        val text: String,
        val pageCount: Int,
        val characterCount: Int,
        val wordCount: Int
    ) : CvImportResult

    data class InsufficientText(
        val extractedText: String,
        val pageCount: Int,
        val characterCount: Int,
        val wordCount: Int,
        val reason: String
    ) : CvImportResult

    data class Failure(
        val message: String,
        val cause: Throwable? = null
    ) : CvImportResult
}