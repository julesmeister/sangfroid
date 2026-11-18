package com.julesmeister.sangfroid.data.speech

import com.k2fsa.sherpa.onnx.OnlineRecognizer
import com.k2fsa.sherpa.onnx.OnlineStream
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class SherpaOnlineStreamingEngine(
    private val recognizer: OnlineRecognizer,
    override val sampleRate: Int,
    private val inferenceDispatcher:
    CoroutineDispatcher =
        Dispatchers.Default
) : SherpaStreamingEngine {

    private val engineMutex =
        Mutex()

    private var stream: OnlineStream? =
        null

    private var isReleased: Boolean =
        false

    override suspend fun createStream():
            Result<Unit> {
        return execute {
            releaseCurrentStream()

            stream =
                recognizer.createStream()

            checkNotNull(stream) {
                "Sherpa could not create an online recognition stream."
            }
        }
    }

    override suspend fun acceptWaveform(
        samples: FloatArray,
        sampleRate: Int
    ): Result<Unit> {
        return execute {
            require(
                sampleRate ==
                        this@SherpaOnlineStreamingEngine
                            .sampleRate
            ) {
                "Expected " +
                        "${this@SherpaOnlineStreamingEngine.sampleRate} Hz " +
                        "audio, received $sampleRate Hz."
            }

            if (samples.isEmpty()) {
                return@execute
            }

            val activeStream =
                requireActiveStream()

            activeStream.acceptWaveform(
                samples = samples,
                sampleRate = sampleRate
            )
        }
    }

    override suspend fun decodeAvailable():
            Result<Unit> {
        return execute {
            val activeStream =
                requireActiveStream()

            while (
                recognizer.isReady(
                    activeStream
                )
            ) {
                recognizer.decode(
                    activeStream
                )
            }
        }
    }

    override suspend fun getPartialText():
            Result<String> {
        return execute {
            val activeStream =
                requireActiveStream()

            recognizer
                .getResult(activeStream)
                .text
                .normalizeTranscript()
        }
    }

    override suspend fun isEndpoint():
            Result<Boolean> {
        return execute {
            val activeStream =
                requireActiveStream()

            recognizer.isEndpoint(
                activeStream
            )
        }
    }

    override suspend fun resetStream():
            Result<Unit> {
        return execute {
            val activeStream =
                requireActiveStream()

            recognizer.reset(
                activeStream
            )
        }
    }

    override suspend fun inputFinished():
            Result<Unit> {
        return execute {
            val activeStream =
                requireActiveStream()

            activeStream.inputFinished()
        }
    }

    override suspend fun getFinalText():
            Result<String> {
        return execute {
            val activeStream =
                requireActiveStream()

            while (
                recognizer.isReady(
                    activeStream
                )
            ) {
                recognizer.decode(
                    activeStream
                )
            }

            recognizer
                .getResult(activeStream)
                .text
                .normalizeTranscript()
        }
    }

    override suspend fun release() {
        withContext(
            inferenceDispatcher
        ) {
            engineMutex.withLock {
                if (isReleased) {
                    return@withLock
                }

                isReleased = true

                releaseCurrentStream()

                recognizer.release()
            }
        }
    }

    private suspend fun <T> execute(
        block: () -> T
    ): Result<T> {
        return withContext(
            inferenceDispatcher
        ) {
            engineMutex.withLock {
                runCatching {
                    check(!isReleased) {
                        "The Sherpa streaming engine has already been released."
                    }

                    block()
                }
            }
        }
    }

    private fun requireActiveStream():
            OnlineStream {
        return checkNotNull(stream) {
            "No active Sherpa recognition stream exists."
        }
    }

    private fun releaseCurrentStream() {
        stream
            ?.let { activeStream ->
                runCatching {
                    activeStream.release()
                }
            }

        stream = null
    }

    private fun String.normalizeTranscript():
            String {
        return trim()
            .replace(
                Regex("\\s+"),
                " "
            )
    }
}