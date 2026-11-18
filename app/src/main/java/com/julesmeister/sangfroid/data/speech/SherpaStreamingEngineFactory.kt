package com.julesmeister.sangfroid.data.speech

interface SherpaStreamingEngine {

    val sampleRate: Int

    suspend fun createStream():
            Result<Unit>

    suspend fun acceptWaveform(
        samples: FloatArray,
        sampleRate: Int
    ): Result<Unit>

    suspend fun decodeAvailable():
            Result<Unit>

    suspend fun getPartialText():
            Result<String>

    suspend fun isEndpoint():
            Result<Boolean>

    suspend fun resetStream():
            Result<Unit>

    suspend fun inputFinished():
            Result<Unit>

    suspend fun getFinalText():
            Result<String>

    suspend fun release()
}

fun interface SherpaStreamingEngineFactory {

    suspend fun create(
        model: SpeechModel
    ): Result<SherpaStreamingEngine>
}