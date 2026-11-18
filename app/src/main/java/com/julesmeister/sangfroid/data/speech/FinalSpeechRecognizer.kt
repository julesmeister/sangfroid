package com.julesmeister.sangfroid.domain.speech

interface FinalSpeechRecognizer {

    suspend fun prepare():
            Result<Unit>

    suspend fun transcribe(
        samples: FloatArray,
        sampleRate: Int
    ): Result<String>

    suspend fun release()
}