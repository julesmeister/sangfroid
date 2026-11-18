package com.julesmeister.sangfroid.data.speech

import com.k2fsa.sherpa.onnx.EndpointConfig
import com.k2fsa.sherpa.onnx.EndpointRule
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OnlineModelConfig
import com.k2fsa.sherpa.onnx.OnlineRecognizer
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig
import com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class SherpaOnlineStreamingEngineFactory
@Inject
constructor() : SherpaStreamingEngineFactory {

    override suspend fun create(
        model: SpeechModel
    ): Result<SherpaStreamingEngine> {
        return withContext(
            Dispatchers.IO
        ) {
            runCatching {
                require(model.isComplete) {
                    "The Sherpa speech model is incomplete."
                }

                require(
                    model.sampleRate > 0
                ) {
                    "The Sherpa model sample rate must be positive."
                }

                val transducerConfig =
                    OnlineTransducerModelConfig(
                        encoder =
                            model.encoderFile.absolutePath,
                        decoder =
                            model.decoderFile.absolutePath,
                        joiner =
                            model.joinerFile.absolutePath
                    )

                val modelConfig =
                    OnlineModelConfig(
                        transducer =
                            transducerConfig,
                        tokens =
                            model.tokensFile.absolutePath,
                        numThreads =
                            NUMBER_OF_THREADS,
                        debug =
                            ENABLE_SHERPA_DEBUG_LOGS,
                        provider =
                            EXECUTION_PROVIDER,
                        modelType =
                            ""
                    )

                val featureConfig =
                    FeatureConfig(
                        sampleRate =
                            model.sampleRate,
                        featureDim =
                            FEATURE_DIMENSION,
                        dither =
                            FEATURE_DITHER
                    )

                val endpointConfig =
                    EndpointConfig(
                        rule1 =
                            EndpointRule(
                                mustContainNonSilence =
                                    false,
                                minTrailingSilence =
                                    RULE_ONE_TRAILING_SILENCE_SECONDS,
                                minUtteranceLength =
                                    0f
                            ),
                        rule2 =
                            EndpointRule(
                                mustContainNonSilence =
                                    true,
                                minTrailingSilence =
                                    RULE_TWO_TRAILING_SILENCE_SECONDS,
                                minUtteranceLength =
                                    0f
                            ),
                        rule3 =
                            EndpointRule(
                                mustContainNonSilence =
                                    false,
                                minTrailingSilence =
                                    0f,
                                minUtteranceLength =
                                    MAXIMUM_UTTERANCE_SECONDS
                            )
                    )

                val recognizerConfig =
                    OnlineRecognizerConfig(
                        featConfig =
                            featureConfig,
                        modelConfig =
                            modelConfig,
                        endpointConfig =
                            endpointConfig,
                        enableEndpoint =
                            true,
                        decodingMethod =
                            DECODING_METHOD,
                        maxActivePaths =
                            MAXIMUM_ACTIVE_PATHS
                    )

                val recognizer =
                    OnlineRecognizer(
                        assetManager =
                            null,
                        config =
                            recognizerConfig
                    )

                SherpaOnlineStreamingEngine(
                    recognizer =
                        recognizer,
                    sampleRate =
                        model.sampleRate
                )
            }
        }
    }

    companion object {
        private const val EXECUTION_PROVIDER =
            "cpu"

        private const val DECODING_METHOD =
            "greedy_search"

        private const val NUMBER_OF_THREADS =
            2

        private const val MAXIMUM_ACTIVE_PATHS =
            4

        private const val FEATURE_DIMENSION =
            80

        private const val FEATURE_DITHER =
            0f

        private const val ENABLE_SHERPA_DEBUG_LOGS =
            false

        private const val RULE_ONE_TRAILING_SILENCE_SECONDS =
            2.4f

        private const val RULE_TWO_TRAILING_SILENCE_SECONDS =
            1.2f

        private const val MAXIMUM_UTTERANCE_SECONDS =
            30f
    }
}