package com.julesmeister.sangfroid.data.speech

import com.julesmeister.sangfroid.domain.speech.FinalSpeechRecognizer
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
class SherpaWhisperFinalSpeechRecognizer
@Inject
constructor(
    private val modelManager:
    WhisperModelManager
) : FinalSpeechRecognizer {

    private val inferenceDispatcher:
            CoroutineDispatcher =
        Dispatchers.Default

    private val recognizerMutex =
        Mutex()

    private var recognizer:
            OfflineRecognizer? =
        null

    private var model:
            WhisperSpeechModel? =
        null

    override suspend fun prepare():
            Result<Unit> {
        return withContext(
            Dispatchers.IO
        ) {
            recognizerMutex.withLock {
                runCatching {
                    if (
                        recognizer != null &&
                        model != null
                    ) {
                        return@runCatching
                    }

                    val preparedModel =
                        modelManager
                            .prepareModel()
                            .getOrThrow()

                    val whisperConfig =
                        OfflineWhisperModelConfig(
                            encoder =
                                preparedModel
                                    .encoderFile
                                    .absolutePath,
                            decoder =
                                preparedModel
                                    .decoderFile
                                    .absolutePath,
                            language =
                                "en",
                            task =
                                "transcribe",
                            tailPaddings =
                                TAIL_PADDINGS,
                            enableTokenTimestamps =
                                false,
                            enableSegmentTimestamps =
                                false
                        )

                    val offlineModelConfig =
                        OfflineModelConfig(
                            whisper =
                                whisperConfig,
                            numThreads =
                                NUMBER_OF_THREADS,
                            debug =
                                ENABLE_DEBUG_LOGS,
                            provider =
                                EXECUTION_PROVIDER,
                            modelType =
                                "whisper",
                            tokens =
                                preparedModel
                                    .tokensFile
                                    .absolutePath
                        )

                    val recognizerConfig =
                        OfflineRecognizerConfig(
                            featConfig =
                                FeatureConfig(
                                    sampleRate =
                                        preparedModel
                                            .sampleRate,
                                    featureDim =
                                        FEATURE_DIMENSION,
                                    dither =
                                        0f
                                ),
                            modelConfig =
                                offlineModelConfig,
                            decodingMethod =
                                "greedy_search",
                            maxActivePaths =
                                4
                        )

                    recognizer?.release()

                    recognizer =
                        OfflineRecognizer(
                            assetManager =
                                null,
                            config =
                                recognizerConfig
                        )

                    model =
                        preparedModel
                }
            }
        }
    }

    override suspend fun transcribe(
        samples: FloatArray,
        sampleRate: Int
    ): Result<String> {
        return withContext(
            inferenceDispatcher
        ) {
            recognizerMutex.withLock {
                runCatching {
                    require(
                        samples.isNotEmpty()
                    ) {
                        "No recorded speech is available."
                    }

                    val activeModel =
                        requireNotNull(model) {
                            "Whisper has not been prepared."
                        }

                    require(
                        sampleRate ==
                                activeModel.sampleRate
                    ) {
                        "Whisper expected " +
                                "${activeModel.sampleRate} Hz audio, " +
                                "received $sampleRate Hz."
                    }

                    val activeRecognizer =
                        requireNotNull(
                            recognizer
                        ) {
                            "Whisper has not been prepared."
                        }

                    val stream =
                        activeRecognizer
                            .createStream()

                    try {
                        stream.acceptWaveform(
                            samples =
                                samples,
                            sampleRate =
                                sampleRate
                        )

                        activeRecognizer.decode(
                            stream
                        )

                        activeRecognizer
                            .getResult(stream)
                            .text
                            .normalizeTranscript()
                    } finally {
                        stream.release()
                    }
                }
            }
        }
    }

    override suspend fun release() {
        withContext(
            Dispatchers.IO
        ) {
            recognizerMutex.withLock {
                recognizer?.release()

                recognizer = null
                model = null
            }
        }
    }

    private fun String.normalizeTranscript():
            String {
        return trim()
            .replace(
                Regex("\\s+"),
                " "
            )
    }

    companion object {
        private const val EXECUTION_PROVIDER =
            "cpu"

        private const val NUMBER_OF_THREADS =
            2

        private const val FEATURE_DIMENSION =
            80

        private const val TAIL_PADDINGS =
            1_000

        private const val ENABLE_DEBUG_LOGS =
            false
    }
}