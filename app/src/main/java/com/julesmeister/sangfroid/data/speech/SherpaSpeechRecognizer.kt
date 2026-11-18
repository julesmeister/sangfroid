package com.julesmeister.sangfroid.data.speech

import com.julesmeister.sangfroid.domain.speech.SpeechRecognitionResult
import com.julesmeister.sangfroid.domain.speech.SpeechRecognitionState
import com.julesmeister.sangfroid.domain.speech.SpeechRecognizer
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

@Singleton
class SherpaSpeechRecognizer @Inject constructor(
    private val modelManager: SpeechModelManager,
    private val engineFactory: SherpaStreamingEngineFactory,
    private val transcriptFormatter: TranscriptFormatter
) : SpeechRecognizer {

    private val mutableState =
        MutableStateFlow<SpeechRecognitionState>(
            SpeechRecognitionState.Idle
        )

    override val state =
        mutableState.asStateFlow()

    private val mutableResults =
        MutableSharedFlow<SpeechRecognitionResult>(
            extraBufferCapacity = 16
        )

    override val results:
            Flow<SpeechRecognitionResult> =
        mutableResults.asSharedFlow()

    private var model:
            SpeechModel? = null

    private var engine:
            SherpaStreamingEngine? = null

    private var isRecognizing =
        false

    private var startedAt =
        0L

    private var segmentIndex =
        0

    private val committedSegments =
        mutableListOf<String>()

    private var currentPartial =
        ""

    override suspend fun prepare():
            Result<Unit> {
        return withContext(
            Dispatchers.IO
        ) {
            mutableState.value =
                SpeechRecognitionState
                    .LoadingModel

            runCatching {
                isRecognizing =
                    false

                runCatching {
                    engine?.release()
                }

                engine =
                    null

                model =
                    null

                resetRecognitionSession()

                val preparedModel =
                    modelManager
                        .prepareDefaultModel()
                        .getOrThrow()

                val preparedEngine =
                    engineFactory
                        .create(
                            preparedModel
                        )
                        .getOrThrow()

                model =
                    preparedModel

                engine =
                    preparedEngine

                mutableState.value =
                    SpeechRecognitionState
                        .Ready(
                            modelName =
                                preparedModel
                                    .displayName
                        )
            }
                .onFailure { throwable ->
                    isRecognizing =
                        false

                    runCatching {
                        engine?.release()
                    }

                    engine =
                        null

                    model =
                        null

                    resetRecognitionSession()

                    mutableState.value =
                        SpeechRecognitionState
                            .Error(
                                message =
                                    throwable.message
                                        ?: "Unable to load the offline speech model.",
                                cause =
                                    throwable
                            )
                }
        }
    }

    override suspend fun startRecognition():
            Result<Unit> {
        return withContext(
            Dispatchers.Default
        ) {
            runCatching {
                val activeEngine =
                    requireNotNull(
                        engine
                    ) {
                        "The speech model has not been prepared."
                    }

                val activeModel =
                    requireNotNull(
                        model
                    ) {
                        "The speech model has not been prepared."
                    }

                check(
                    !isRecognizing
                ) {
                    "Speech recognition is already active."
                }

                check(
                    activeEngine.sampleRate ==
                            activeModel.sampleRate
                ) {
                    "The speech engine sample rate does not match the loaded model."
                }

                resetRecognitionSession()

                activeEngine
                    .createStream()
                    .getOrThrow()

                startedAt =
                    System.currentTimeMillis()

                isRecognizing =
                    true

                mutableState.value =
                    SpeechRecognitionState
                        .Listening(
                            partialText =
                                "",
                            elapsedMilliseconds =
                                0L
                        )
            }
                .onFailure { throwable ->
                    isRecognizing =
                        false

                    mutableState.value =
                        SpeechRecognitionState
                            .Error(
                                message =
                                    throwable.message
                                        ?: "Unable to start speech recognition.",
                                cause =
                                    throwable
                            )
                }
        }
    }

    override suspend fun acceptAudio(
        samples: FloatArray,
        sampleRate: Int
    ): Result<Unit> {
        return withContext(
            Dispatchers.Default
        ) {
            runCatching {
                check(
                    isRecognizing
                ) {
                    "Speech recognition is not active."
                }

                if (samples.isEmpty()) {
                    return@runCatching
                }

                val activeEngine =
                    requireNotNull(
                        engine
                    ) {
                        "The speech engine is unavailable."
                    }

                require(
                    sampleRate ==
                            activeEngine.sampleRate
                ) {
                    "Expected ${activeEngine.sampleRate} Hz audio, received $sampleRate Hz."
                }

                activeEngine
                    .acceptWaveform(
                        samples =
                            samples,
                        sampleRate =
                            sampleRate
                    )
                    .getOrThrow()

                activeEngine
                    .decodeAvailable()
                    .getOrThrow()

                val partial =
                    activeEngine
                        .getPartialText()
                        .getOrThrow()
                        .let(
                            transcriptFormatter::format
                        )

                if (
                    partial !=
                    currentPartial
                ) {
                    currentPartial =
                        partial

                    mutableResults.emit(
                        SpeechRecognitionResult(
                            text =
                                buildTranscript(
                                    segments =
                                        committedSegments,
                                    partial =
                                        currentPartial
                                ),
                            isFinal =
                                false,
                            segmentIndex =
                                segmentIndex,
                            languageTag =
                                model
                                    ?.languageTag
                                    ?: DEFAULT_LANGUAGE_TAG
                        )
                    )
                }

                val endpointDetected =
                    activeEngine
                        .isEndpoint()
                        .getOrThrow()

                if (endpointDetected) {
                    commitCurrentSegment(
                        activeEngine =
                            activeEngine
                    )
                }

                mutableState.value =
                    SpeechRecognitionState
                        .Listening(
                            partialText =
                                buildTranscript(
                                    segments =
                                        committedSegments,
                                    partial =
                                        currentPartial
                                ),
                            elapsedMilliseconds =
                                elapsedSinceStart()
                        )
            }
                .onFailure { throwable ->
                    isRecognizing =
                        false

                    mutableState.value =
                        SpeechRecognitionState
                            .Error(
                                message =
                                    throwable.message
                                        ?: "Speech recognition failed.",
                                cause =
                                    throwable
                            )
                }
        }
    }

    override suspend fun stopRecognition():
            Result<SpeechRecognitionResult> {
        return withContext(
            Dispatchers.Default
        ) {
            runCatching {
                check(
                    isRecognizing
                ) {
                    "Speech recognition is not active."
                }

                val activeEngine =
                    requireNotNull(
                        engine
                    ) {
                        "The speech engine is unavailable."
                    }

                mutableState.value =
                    SpeechRecognitionState
                        .Processing(
                            partialText =
                                buildTranscript(
                                    segments =
                                        committedSegments,
                                    partial =
                                        currentPartial
                                )
                        )

                activeEngine
                    .inputFinished()
                    .getOrThrow()

                activeEngine
                    .decodeAvailable()
                    .getOrThrow()

                val finalSegment =
                    activeEngine
                        .getFinalText()
                        .getOrThrow()
                        .let(
                            transcriptFormatter::format
                        )

                val segmentToCommit =
                    finalSegment.ifBlank {
                        currentPartial
                    }

                appendCommittedSegment(
                    segment =
                        segmentToCommit
                )

                currentPartial =
                    ""

                isRecognizing =
                    false

                val finalText =
                    buildTranscript(
                        segments =
                            committedSegments,
                        partial =
                            ""
                    )

                val result =
                    SpeechRecognitionResult(
                        text =
                            finalText,
                        isFinal =
                            true,
                        segmentIndex =
                            segmentIndex,
                        languageTag =
                            model
                                ?.languageTag
                                ?: DEFAULT_LANGUAGE_TAG
                    )

                mutableResults.emit(
                    result
                )

                mutableState.value =
                    SpeechRecognitionState
                        .Completed(
                            text =
                                finalText
                        )

                result
            }
                .onFailure { throwable ->
                    isRecognizing =
                        false

                    mutableState.value =
                        SpeechRecognitionState
                            .Error(
                                message =
                                    throwable.message
                                        ?: "Unable to finish speech recognition.",
                                cause =
                                    throwable
                            )
                }
        }
    }

    override suspend fun cancelRecognition() {
        withContext(
            Dispatchers.Default
        ) {
            isRecognizing =
                false

            runCatching {
                engine?.resetStream()
                    ?.getOrThrow()
            }

            resetRecognitionSession()

            mutableState.value =
                if (
                    engine != null &&
                    model != null
                ) {
                    SpeechRecognitionState
                        .Ready(
                            modelName =
                                requireNotNull(
                                    model
                                ).displayName
                        )
                } else {
                    SpeechRecognitionState.Idle
                }
        }
    }

    override suspend fun release() {
        withContext(
            Dispatchers.IO
        ) {
            isRecognizing =
                false

            runCatching {
                engine?.release()
            }

            engine =
                null

            model =
                null

            resetRecognitionSession()

            mutableState.value =
                SpeechRecognitionState.Idle
        }
    }

    private suspend fun commitCurrentSegment(
        activeEngine:
        SherpaStreamingEngine
    ) {
        activeEngine
            .decodeAvailable()
            .getOrThrow()

        val finalSegment =
            activeEngine
                .getFinalText()
                .getOrThrow()
                .let(
                    transcriptFormatter::format
                )

        val segmentToCommit =
            finalSegment.ifBlank {
                currentPartial
            }

        val committed =
            appendCommittedSegment(
                segment =
                    segmentToCommit
            )

        currentPartial =
            ""

        if (committed) {
            mutableResults.emit(
                SpeechRecognitionResult(
                    text =
                        buildTranscript(
                            segments =
                                committedSegments,
                            partial =
                                ""
                        ),
                    isFinal =
                        false,
                    segmentIndex =
                        segmentIndex,
                    languageTag =
                        model
                            ?.languageTag
                            ?: DEFAULT_LANGUAGE_TAG
                )
            )

            segmentIndex +=
                1
        }

        activeEngine
            .resetStream()
            .getOrThrow()
    }

    private fun appendCommittedSegment(
        segment: String
    ): Boolean {
        val formattedSegment =
            transcriptFormatter
                .format(
                    segment
                )

        if (formattedSegment.isBlank()) {
            return false
        }

        if (
            committedSegments
                .lastOrNull()
                ?.equals(
                    other =
                        formattedSegment,
                    ignoreCase =
                        false
                ) == true
        ) {
            return false
        }

        committedSegments +=
            formattedSegment

        return true
    }

    private fun buildTranscript(
        segments: List<String>,
        partial: String
    ): String {
        val combined =
            buildList {
                segments
                    .map(
                        transcriptFormatter::format
                    )
                    .filter(
                        String::isNotBlank
                    )
                    .forEach(
                        ::add
                    )

                transcriptFormatter
                    .format(
                        partial
                    )
                    .takeIf(
                        String::isNotBlank
                    )
                    ?.let(
                        ::add
                    )
            }
                .joinToString(
                    separator =
                        " "
                )

        return transcriptFormatter
            .format(
                combined
            )
    }

    private fun resetRecognitionSession() {
        committedSegments.clear()

        currentPartial =
            ""

        segmentIndex =
            0

        startedAt =
            0L
    }

    private fun elapsedSinceStart():
            Long {
        if (startedAt <= 0L) {
            return 0L
        }

        return (
                System.currentTimeMillis() -
                        startedAt
                )
            .coerceAtLeast(
                0L
            )
    }

    companion object {
        private const val DEFAULT_LANGUAGE_TAG =
            "en-US"
    }
}