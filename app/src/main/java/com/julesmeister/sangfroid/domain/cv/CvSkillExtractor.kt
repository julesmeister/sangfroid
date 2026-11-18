package com.julesmeister.sangfroid.domain.cv

interface CvSkillExtractor {

    suspend fun extractSkills(
        document: CvDocument,
        textExtraction: CvTextExtraction
    ): Result<CvExtractionResult>
}