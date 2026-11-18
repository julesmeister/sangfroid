package com.julesmeister.sangfroid.data.remote.api

import com.julesmeister.sangfroid.data.remote.dto.CvAnalysisRequestDto
import com.julesmeister.sangfroid.data.remote.dto.CvAnalysisResponseDto
import com.julesmeister.sangfroid.data.remote.dto.EvaluationRequestDto
import com.julesmeister.sangfroid.data.remote.dto.EvaluationResponseDto
import com.julesmeister.sangfroid.data.remote.dto.QuestionGenerationRequestDto
import com.julesmeister.sangfroid.data.remote.dto.QuestionGenerationResponseDto
import com.julesmeister.sangfroid.data.remote.dto.QuestionImportRequestDto
import com.julesmeister.sangfroid.data.remote.dto.QuestionImportResponseDto

interface SangfroidApi {

    suspend fun evaluateAnswer(
        request: EvaluationRequestDto
    ): Result<EvaluationResponseDto>

    suspend fun generateQuestions(
        request: QuestionGenerationRequestDto
    ): Result<QuestionGenerationResponseDto>

    suspend fun importQuestions(
        request: QuestionImportRequestDto
    ): Result<QuestionImportResponseDto>

    suspend fun analyzeCv(
        request: CvAnalysisRequestDto
    ): Result<CvAnalysisResponseDto>
}