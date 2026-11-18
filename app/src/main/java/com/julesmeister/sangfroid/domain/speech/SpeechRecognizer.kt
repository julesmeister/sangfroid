package com.julesmeister.sangfroid.domain.speech

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SpeechRecognizer {

    val state: StateFlow<SpeechRecognitionState>

    val results: Flow<SpeechRecognitionResult>

    suspend fun prepare(): Result<Unit>

    suspend fun startRecognition(): Result<Unit>

    suspend fun acceptAudio(
        samples: FloatArray,
        sampleRate: Int
    ): Result<Unit>

    suspend fun stopRecognition():
            Result<SpeechRecognitionResult>

    suspend fun cancelRecognition()

    suspend fun release()
}