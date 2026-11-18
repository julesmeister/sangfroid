package com.julesmeister.sangfroid.domain.cv

interface CvTextExtractor {

    suspend fun extractText(
        document: CvDocument
    ): Result<CvTextExtraction>
}