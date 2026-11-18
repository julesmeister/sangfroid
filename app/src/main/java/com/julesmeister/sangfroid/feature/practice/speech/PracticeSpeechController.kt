package com.julesmeister.sangfroid.feature.practice.speech

import com.julesmeister.sangfroid.data.speech.AudioRecorder
import com.julesmeister.sangfroid.data.speech.AudioRecorderState
import com.julesmeister.sangfroid.data.speech.PcmAudioAccumulator
import com.julesmeister.sangfroid.domain.speech.FinalSpeechRecognizer
import com.julesmeister.sangfroid.domain.speech.SpeechRecognitionResult
import com.julesmeister.sangfroid.domain.speech.SpeechRecognitionState
import com.julesmeister.sangfroid.domain.speech.SpeechRecognizer
import java.io.Closeable
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PracticeSpeechController @Inject constructor(
    private val audioRecorder: AudioRecorder,
    private val speechRecognizer: SpeechRecognizer,
    private val finalSpeechRecognizer:
    FinalSpeechRecognizer,
    private val audioAccumulator:
    PcmAudioAccumulator
) : Closeable {

    companion object {
        private const val TIMER_UPDATE_MILLIS =
            100L

        private const val MINIMUM_WHISPER_AUDIO_MILLIS =
            500L
    }

    private val mutableUiState =
        MutableStateFlow(
            SpeechUiState()
        )

    val uiState: StateFlow<SpeechUiState> =
        mutableUiState.asStateFlow()

    private var controllerScope:
            CoroutineScope? = null

    private var audioFramesJob:
            Job? = null

    private var audioRecorderStateJob:
            Job? = null

    private var recognizerStateJob:
            Job? = null

    private var recognitionResultsJob:
            Job? = null

    private var timerJob:
            Job? = null

    private var prepareJob:
            Job? = null

    private var recordingOperationJob:
            Job? = null

    private var whisperPrepareJob:
            Job? = null

    private var shutdownJob:
            Job? = null

    private var isWhisperReady =
        false

    private var isAcceptingAudio =
        false

    private var isStoppingRecording =
        false

    private var isClosed =
        false

    fun bind(
        scope: CoroutineScope
    ) {
        check(!isClosed) {
            "PracticeSpeechController has already been closed."
        }

        if (controllerScope === scope) {
            return
        }

        unbindObservers()

        controllerScope =
            scope

        observeAudioFrames(
            scope = scope
        )

        observeAudioRecorderState(
            scope = scope
        )

        observeRecognizerState(
            scope = scope
        )

        observeRecognitionResults(
            scope = scope
        )
    }

    fun prepare() {
        val scope =
            requireScope()

        if (
            prepareJob?.isActive == true ||
            mutableUiState.value.isPreparing
        ) {
            return
        }

        prepareJob =
            scope.launch {
                mutableUiState.value =
                    mutableUiState.value.copy(
                        isPreparing = true,
                        isReady = false,
                        preparationStage =
                            SpeechPreparationStage
                                .STREAMING_MODEL,
                        errorMessage = null
                    )

                try {
                    speechRecognizer
                        .prepare()
                        .getOrThrow()

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isPreparing = false,
                            isReady = true,
                            preparationStage =
                                SpeechPreparationStage.NONE,
                            errorMessage = null
                        )

                    prepareWhisperInBackground(
                        scope = scope
                    )
                } catch (
                    cancellation:
                    CancellationException
                ) {
                    throw cancellation
                } catch (
                    throwable: Throwable
                ) {
                    publishError(
                        throwable.toUserMessage(
                            fallback =
                                "Unable to prepare offline speech recognition."
                        )
                    )
                }
            }
    }

    fun startRecording() {
        val scope =
            requireScope()

        if (
            recordingOperationJob?.isActive ==
            true
        ) {
            return
        }

        if (!mutableUiState.value.canStart) {
            return
        }

        recordingOperationJob =
            scope.launch {
                isStoppingRecording =
                    false

                isAcceptingAudio =
                    false

                audioAccumulator.reset()

                mutableUiState.value =
                    mutableUiState.value.copy(
                        isRecording = false,
                        isProcessing = false,
                        processingStage =
                            SpeechProcessingStage.NONE,
                        elapsedMilliseconds = 0L,
                        amplitude = 0f,
                        partialTranscript = "",
                        finalTranscript = null,
                        usedEnhancedTranscript = false,
                        errorMessage = null
                    )

                try {
                    speechRecognizer
                        .startRecognition()
                        .getOrThrow()

                    audioRecorder
                        .start()
                        .getOrThrow()

                    isAcceptingAudio =
                        true

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isRecording = true,
                            isProcessing = false,
                            processingStage =
                                SpeechProcessingStage.NONE,
                            errorMessage = null
                        )

                    startTimer()
                } catch (
                    cancellation:
                    CancellationException
                ) {
                    cleanupFailedRecordingStart()
                    throw cancellation
                } catch (
                    throwable: Throwable
                ) {
                    cleanupFailedRecordingStart()

                    publishError(
                        throwable.toUserMessage(
                            fallback =
                                "Unable to start voice recording."
                        )
                    )
                }
            }
    }

    fun stopRecording(
        onTranscriptReady: (String) -> Unit
    ) {
        val scope =
            requireScope()

        if (
            recordingOperationJob?.isActive ==
            true
        ) {
            return
        }

        if (
            !mutableUiState.value.isRecording ||
            isStoppingRecording
        ) {
            return
        }

        isStoppingRecording =
            true

        recordingOperationJob =
            scope.launch {
                stopTimer()

                mutableUiState.value =
                    mutableUiState.value.copy(
                        isRecording = false,
                        isProcessing = true,
                        processingStage =
                            SpeechProcessingStage
                                .FINALIZING_STREAMING,
                        amplitude = 0f,
                        errorMessage = null
                    )

                try {
                    /*
                     * AudioRecord.stop() also waits for the capture
                     * coroutine to finish. Audio frames that were
                     * already emitted remain valid and can still be
                     * accepted while isAcceptingAudio is true.
                     */
                    audioRecorder
                        .stop()
                        .getOrThrow()

                    /*
                     * Allow the collector to consume frames already
                     * waiting in SharedFlow's small buffer before
                     * taking the PCM snapshot.
                     */
                    delay(
                        TIMER_UPDATE_MILLIS
                    )

                    isAcceptingAudio =
                        false

                    val streamingResult =
                        speechRecognizer
                            .stopRecognition()

                    val streamingTranscript =
                        streamingResult
                            .getOrNull()
                            ?.text
                            ?.normalizeTranscript()
                            .orEmpty()

                    val recordedAudio =
                        runCatching {
                            audioAccumulator.snapshot()
                        }
                            .getOrNull()

                    val hasRecordedAudio =
                        recordedAudio != null &&
                                recordedAudio.samples
                                    .isNotEmpty()

                    if (
                        streamingResult.isFailure &&
                        !hasRecordedAudio
                    ) {
                        throw streamingResult
                            .exceptionOrNull()
                            ?: IllegalStateException(
                                "Unable to finish the live transcript."
                            )
                    }

                    val shouldRunWhisper =
                        recordedAudio != null &&
                                recordedAudio
                                    .durationMilliseconds >=
                                MINIMUM_WHISPER_AUDIO_MILLIS

                    if (!shouldRunWhisper) {
                        completeTranscript(
                            transcript =
                                streamingTranscript,
                            usedEnhancedTranscript =
                                false,
                            onTranscriptReady =
                                onTranscriptReady
                        )

                        return@launch
                    }

                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isRecording = false,
                            isProcessing = true,
                            processingStage =
                                SpeechProcessingStage
                                    .ENHANCING_WITH_WHISPER,
                            partialTranscript =
                                streamingTranscript,
                            finalTranscript =
                                streamingTranscript
                                    .takeIf(
                                        String::isNotBlank
                                    ),
                            amplitude = 0f,
                            errorMessage = null
                        )

                    val whisperTranscript =
                        transcribeWithWhisper(
                            samples =
                                recordedAudio.samples,
                            sampleRate =
                                recordedAudio.sampleRate
                        )

                    val selectedTranscript =
                        whisperTranscript
                            .takeIf(
                                String::isNotBlank
                            )
                            ?: streamingTranscript

                    completeTranscript(
                        transcript =
                            selectedTranscript,
                        usedEnhancedTranscript =
                            whisperTranscript
                                .isNotBlank(),
                        onTranscriptReady =
                            onTranscriptReady
                    )
                } catch (
                    cancellation:
                    CancellationException
                ) {
                    throw cancellation
                } catch (
                    throwable: Throwable
                ) {
                    isAcceptingAudio =
                        false

                    runCatching {
                        speechRecognizer
                            .cancelRecognition()
                    }

                    publishError(
                        throwable.toUserMessage(
                            fallback =
                                "Unable to finish the transcript."
                        )
                    )
                } finally {
                    isAcceptingAudio =
                        false

                    isStoppingRecording =
                        false

                    audioAccumulator.reset()
                }
            }
    }

    fun cancelRecording() {
        val scope =
            requireScope()

        if (isStoppingRecording) {
            return
        }

        if (
            !mutableUiState.value.isRecording &&
            !mutableUiState.value.isProcessing
        ) {
            return
        }

        recordingOperationJob?.cancel()

        isStoppingRecording =
            false

        isAcceptingAudio =
            false

        recordingOperationJob =
            scope.launch {
                stopTimer()

                runCatching {
                    audioRecorder.cancel()
                }

                runCatching {
                    speechRecognizer
                        .cancelRecognition()
                }

                audioAccumulator.reset()

                mutableUiState.value =
                    mutableUiState.value.copy(
                        isRecording = false,
                        isProcessing = false,
                        processingStage =
                            SpeechProcessingStage.NONE,
                        elapsedMilliseconds = 0L,
                        amplitude = 0f,
                        partialTranscript = "",
                        finalTranscript = null,
                        usedEnhancedTranscript = false,
                        errorMessage = null
                    )
            }
    }

    fun clearTranscript() {
        mutableUiState.value =
            mutableUiState.value.copy(
                partialTranscript = "",
                finalTranscript = null,
                usedEnhancedTranscript = false
            )
    }

    fun dismissError() {
        mutableUiState.value =
            mutableUiState.value.copy(
                errorMessage = null
            )
    }

    fun unbind() {
        stopTimer()

        prepareJob?.cancel()
        prepareJob = null

        recordingOperationJob?.cancel()
        recordingOperationJob = null

        whisperPrepareJob?.cancel()
        whisperPrepareJob = null

        isAcceptingAudio =
            false

        isStoppingRecording =
            false

        unbindObservers()

        controllerScope =
            null
    }

    override fun close() {
        if (isClosed) {
            return
        }

        isClosed =
            true

        val scope =
            controllerScope

        stopTimer()

        prepareJob?.cancel()
        prepareJob = null

        recordingOperationJob?.cancel()
        recordingOperationJob = null

        whisperPrepareJob?.cancel()
        whisperPrepareJob = null

        isAcceptingAudio =
            false

        isStoppingRecording =
            false

        if (scope != null) {
            shutdownJob =
                scope.launch {
                    runCatching {
                        audioRecorder.release()
                    }

                    runCatching {
                        speechRecognizer.release()
                    }

                    runCatching {
                        finalSpeechRecognizer.release()
                    }

                    audioAccumulator.reset()
                }
        }

        unbindObservers()

        controllerScope =
            null
    }

    private fun observeAudioFrames(
        scope: CoroutineScope
    ) {
        audioFramesJob =
            scope.launch {
                audioRecorder
                    .audioFrames
                    .collect { frame ->
                        if (!isAcceptingAudio) {
                            return@collect
                        }

                        audioAccumulator.append(
                            samples =
                                frame.samples,
                            sampleRate =
                                frame.sampleRate
                        )

                        if (
                            mutableUiState.value
                                .isRecording
                        ) {
                            mutableUiState.value =
                                mutableUiState.value.copy(
                                    amplitude =
                                        frame.amplitude
                                )
                        }

                        speechRecognizer
                            .acceptAudio(
                                samples =
                                    frame.samples,
                                sampleRate =
                                    frame.sampleRate
                            )
                            .onFailure { throwable ->
                                if (
                                    !isStoppingRecording
                                ) {
                                    handleCaptureFailure(
                                        throwable =
                                            throwable
                                    )
                                }
                            }
                    }
            }
    }

    private fun observeAudioRecorderState(
        scope: CoroutineScope
    ) {
        audioRecorderStateJob =
            scope.launch {
                audioRecorder
                    .state
                    .collect { state ->
                        when (state) {
                            AudioRecorderState.Idle -> {
                                mutableUiState.value =
                                    mutableUiState.value.copy(
                                        amplitude = 0f
                                    )
                            }

                            is AudioRecorderState
                            .Recording -> {
                                if (
                                    mutableUiState.value
                                        .isRecording
                                ) {
                                    mutableUiState.value =
                                        mutableUiState.value.copy(
                                            amplitude =
                                                state.amplitude
                                        )
                                }
                            }

                            AudioRecorderState.Stopped -> {
                                mutableUiState.value =
                                    mutableUiState.value.copy(
                                        amplitude = 0f
                                    )
                            }

                            is AudioRecorderState.Error -> {
                                if (
                                    !isStoppingRecording
                                ) {
                                    publishError(
                                        state.message
                                    )
                                }
                            }
                        }
                    }
            }
    }

    private fun observeRecognizerState(
        scope: CoroutineScope
    ) {
        recognizerStateJob =
            scope.launch {
                speechRecognizer
                    .state
                    .collect { state ->
                        when (state) {
                            SpeechRecognitionState.Idle -> {
                                if (
                                    !mutableUiState.value
                                        .isProcessing &&
                                    !mutableUiState.value
                                        .isRecording
                                ) {
                                    mutableUiState.value =
                                        mutableUiState.value.copy(
                                            isPreparing =
                                                false,
                                            isReady =
                                                false,
                                            preparationStage =
                                                SpeechPreparationStage
                                                    .NONE
                                        )
                                }
                            }

                            SpeechRecognitionState
                                .LoadingModel -> {
                                mutableUiState.value =
                                    mutableUiState.value.copy(
                                        isPreparing =
                                            true,
                                        isReady =
                                            false,
                                        preparationStage =
                                            SpeechPreparationStage
                                                .STREAMING_MODEL,
                                        errorMessage =
                                            null
                                    )
                            }

                            is SpeechRecognitionState
                            .Ready -> {
                                if (
                                    !mutableUiState.value
                                        .isProcessing
                                ) {
                                    mutableUiState.value =
                                        mutableUiState.value.copy(
                                            isPreparing =
                                                false,
                                            isReady =
                                                true,
                                            isRecording =
                                                false,
                                            preparationStage =
                                                SpeechPreparationStage
                                                    .NONE,
                                            modelName =
                                                state.modelName,
                                            errorMessage =
                                                null
                                        )
                                }
                            }

                            is SpeechRecognitionState
                            .Listening -> {
                                if (
                                    !isStoppingRecording
                                ) {
                                    mutableUiState.value =
                                        mutableUiState.value.copy(
                                            isPreparing =
                                                false,
                                            isReady =
                                                true,
                                            isRecording =
                                                true,
                                            isProcessing =
                                                false,
                                            preparationStage =
                                                SpeechPreparationStage
                                                    .NONE,
                                            processingStage =
                                                SpeechProcessingStage
                                                    .NONE,
                                            partialTranscript =
                                                state.partialText
                                                    .normalizeTranscript(),
                                            elapsedMilliseconds =
                                                state
                                                    .elapsedMilliseconds,
                                            errorMessage =
                                                null
                                        )
                                }
                            }

                            is SpeechRecognitionState
                            .Processing -> {
                                mutableUiState.value =
                                    mutableUiState.value.copy(
                                        isRecording =
                                            false,
                                        isProcessing =
                                            true,
                                        processingStage =
                                            SpeechProcessingStage
                                                .FINALIZING_STREAMING,
                                        partialTranscript =
                                            state.partialText
                                                .normalizeTranscript(),
                                        amplitude =
                                            0f
                                    )
                            }

                            is SpeechRecognitionState
                            .Completed -> {
                                val normalizedText =
                                    state.text
                                        .normalizeTranscript()

                                /*
                                 * This is still only the streaming
                                 * recognizer result. Keep processing
                                 * active because Whisper may replace it.
                                 */
                                mutableUiState.value =
                                    mutableUiState.value.copy(
                                        isRecording =
                                            false,
                                        isReady =
                                            true,
                                        partialTranscript =
                                            normalizedText,
                                        finalTranscript =
                                            normalizedText
                                                .takeIf(
                                                    String::isNotBlank
                                                ),
                                        amplitude =
                                            0f
                                    )
                            }

                            is SpeechRecognitionState
                            .Error -> {
                                if (
                                    !isStoppingRecording
                                ) {
                                    publishError(
                                        state.message
                                    )
                                }
                            }
                        }
                    }
            }
    }

    private fun observeRecognitionResults(
        scope: CoroutineScope
    ) {
        recognitionResultsJob =
            scope.launch {
                speechRecognizer
                    .results
                    .collect { result ->
                        applyStreamingResult(
                            result =
                                result
                        )
                    }
            }
    }

    private fun applyStreamingResult(
        result: SpeechRecognitionResult
    ) {
        val normalizedText =
            result.text
                .normalizeTranscript()

        if (
            result.isFinal &&
            mutableUiState.value
                .isProcessing
        ) {
            mutableUiState.value =
                mutableUiState.value.copy(
                    partialTranscript =
                        normalizedText,
                    finalTranscript =
                        normalizedText.takeIf(
                            String::isNotBlank
                        )
                )

            return
        }

        if (!result.isFinal) {
            mutableUiState.value =
                mutableUiState.value.copy(
                    partialTranscript =
                        normalizedText
                )
        }
    }

    private fun prepareWhisperInBackground(
        scope: CoroutineScope
    ) {
        if (
            isWhisperReady ||
            whisperPrepareJob?.isActive == true
        ) {
            return
        }

        whisperPrepareJob =
            scope.launch {
                mutableUiState.value =
                    mutableUiState.value.copy(
                        isFinalRecognizerPreparing =
                            true
                    )

                try {
                    finalSpeechRecognizer
                        .prepare()
                        .onSuccess {
                            isWhisperReady =
                                true
                        }
                        .onFailure {
                            isWhisperReady =
                                false
                        }
                } catch (
                    cancellation:
                    CancellationException
                ) {
                    throw cancellation
                } catch (
                    throwable: Throwable
                ) {
                    isWhisperReady =
                        false
                } finally {
                    mutableUiState.value =
                        mutableUiState.value.copy(
                            isFinalRecognizerPreparing =
                                false
                        )
                }
            }
    }

    private suspend fun transcribeWithWhisper(
        samples: FloatArray,
        sampleRate: Int
    ): String {
        if (!isWhisperReady) {
            val prepareResult =
                finalSpeechRecognizer
                    .prepare()

            isWhisperReady =
                prepareResult.isSuccess

            if (prepareResult.isFailure) {
                return ""
            }
        }

        return finalSpeechRecognizer
            .transcribe(
                samples =
                    samples,
                sampleRate =
                    sampleRate
            )
            .getOrNull()
            ?.normalizeTranscript()
            .orEmpty()
    }

    private fun completeTranscript(
        transcript: String,
        usedEnhancedTranscript: Boolean,
        onTranscriptReady: (String) -> Unit
    ) {
        val normalizedTranscript =
            transcript
                .normalizeTranscript()

        mutableUiState.value =
            mutableUiState.value.copy(
                isRecording = false,
                isProcessing = false,
                isReady = true,
                processingStage =
                    SpeechProcessingStage.NONE,
                partialTranscript = "",
                finalTranscript =
                    normalizedTranscript.takeIf(
                        String::isNotBlank
                    ),
                amplitude = 0f,
                usedEnhancedTranscript =
                    usedEnhancedTranscript,
                errorMessage =
                    if (
                        normalizedTranscript
                            .isBlank()
                    ) {
                        "No speech was detected. Try speaking closer to the microphone."
                    } else {
                        null
                    }
            )

        if (
            normalizedTranscript
                .isNotBlank()
        ) {
            onTranscriptReady(
                normalizedTranscript
            )
        }
    }

    private suspend fun cleanupFailedRecordingStart() {
        isAcceptingAudio =
            false

        isStoppingRecording =
            false

        stopTimer()

        runCatching {
            audioRecorder.cancel()
        }

        runCatching {
            speechRecognizer
                .cancelRecognition()
        }

        audioAccumulator.reset()
    }

    private fun handleCaptureFailure(
        throwable: Throwable
    ) {
        val scope =
            controllerScope
                ?: return

        if (
            recordingOperationJob
                ?.isActive == true
        ) {
            return
        }

        recordingOperationJob =
            scope.launch {
                isAcceptingAudio =
                    false

                stopTimer()

                runCatching {
                    audioRecorder.cancel()
                }

                runCatching {
                    speechRecognizer
                        .cancelRecognition()
                }

                audioAccumulator.reset()

                publishError(
                    throwable.toUserMessage(
                        fallback =
                            "Unable to process microphone audio."
                    )
                )
            }
    }

    private fun startTimer() {
        stopTimer()

        val scope =
            requireScope()

        val startedAt =
            System.currentTimeMillis()

        timerJob =
            scope.launch {
                while (true) {
                    mutableUiState.value =
                        mutableUiState.value.copy(
                            elapsedMilliseconds =
                                (
                                        System
                                            .currentTimeMillis() -
                                                startedAt
                                        )
                                    .coerceAtLeast(
                                        0L
                                    )
                        )

                    delay(
                        TIMER_UPDATE_MILLIS
                    )
                }
            }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob =
            null
    }

    private fun publishError(
        message: String
    ) {
        isAcceptingAudio =
            false

        stopTimer()

        mutableUiState.value =
            mutableUiState.value.copy(
                isPreparing = false,
                isRecording = false,
                isProcessing = false,
                preparationStage =
                    SpeechPreparationStage.NONE,
                processingStage =
                    SpeechProcessingStage.NONE,
                amplitude = 0f,
                errorMessage = message
            )
    }

    private fun unbindObservers() {
        audioFramesJob?.cancel()
        audioFramesJob =
            null

        audioRecorderStateJob?.cancel()
        audioRecorderStateJob =
            null

        recognizerStateJob?.cancel()
        recognizerStateJob =
            null

        recognitionResultsJob?.cancel()
        recognitionResultsJob =
            null
    }

    private fun requireScope():
            CoroutineScope {
        return checkNotNull(
            controllerScope
        ) {
            "PracticeSpeechController must be bound before use."
        }
    }

    private fun Throwable.toUserMessage(
        fallback: String
    ): String {
        message
            ?.trim()
            ?.takeIf(
                String::isNotBlank
            )
            ?.let {
                return it
            }

        var currentCause =
            cause

        while (currentCause != null) {
            currentCause
                .message
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )
                ?.let {
                    return it
                }

            currentCause =
                currentCause.cause
        }

        return fallback
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