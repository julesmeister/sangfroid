package com.julesmeister.sangfroid.data.speech

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.sqrt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class AudioRecorder @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    companion object {
        const val SAMPLE_RATE =
            16_000

        private const val CHANNEL_CONFIGURATION =
            AudioFormat.CHANNEL_IN_MONO

        private const val AUDIO_ENCODING =
            AudioFormat.ENCODING_PCM_16BIT

        private const val BUFFER_MULTIPLIER =
            2

        private const val FRAME_DURATION_MILLIS =
            100

        private const val PCM_16_NORMALIZER =
            32_768f
    }

    private val recorderDispatcher:
            CoroutineDispatcher =
        Dispatchers.IO

    private val recorderScope =
        CoroutineScope(
            SupervisorJob() +
                    recorderDispatcher
        )

    private val mutableState =
        MutableStateFlow<AudioRecorderState>(
            AudioRecorderState.Idle
        )

    val state: StateFlow<AudioRecorderState> =
        mutableState.asStateFlow()

    private val mutableAudioFrames =
        MutableSharedFlow<AudioFrame>(
            extraBufferCapacity = 16
        )

    val audioFrames: SharedFlow<AudioFrame> =
        mutableAudioFrames.asSharedFlow()

    private var audioRecord:
            AudioRecord? = null

    private var recordingJob:
            Job? = null

    fun hasRecordPermission():
            Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun start():
            Result<Unit> {
        return withContext(
            recorderDispatcher
        ) {
            if (!hasRecordPermission()) {
                val exception =
                    SecurityException(
                        "Microphone permission has not been granted."
                    )

                mutableState.value =
                    AudioRecorderState.Error(
                        message =
                            exception.message
                                ?: "Microphone permission has not been granted.",
                        cause =
                            exception
                    )

                return@withContext Result.failure(
                    exception
                )
            }

            startWithPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startWithPermission():
            Result<Unit> {
        return runCatching {
            check(hasRecordPermission()) {
                "Microphone permission has not been granted."
            }

            check(
                recordingJob?.isActive != true
            ) {
                "Audio recording is already active."
            }

            releaseRecorder()

            val minimumBufferSize =
                AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    CHANNEL_CONFIGURATION,
                    AUDIO_ENCODING
                )

            check(
                minimumBufferSize >
                        AudioRecord.ERROR
            ) {
                "This device does not support 16 kHz mono PCM recording."
            }

            val frameSampleCount =
                (
                        SAMPLE_RATE *
                                FRAME_DURATION_MILLIS
                        ) / 1_000

            val frameByteCount =
                frameSampleCount *
                        Short.SIZE_BYTES

            val internalBufferSize =
                max(
                    minimumBufferSize *
                            BUFFER_MULTIPLIER,
                    frameByteCount * 2
                )

            val recorder =
                try {
                    AudioRecord(
                        MediaRecorder.AudioSource
                            .VOICE_RECOGNITION,
                        SAMPLE_RATE,
                        CHANNEL_CONFIGURATION,
                        AUDIO_ENCODING,
                        internalBufferSize
                    )
                } catch (
                    securityException:
                    SecurityException
                ) {
                    throw SecurityException(
                        "Microphone access was denied.",
                        securityException
                    )
                }

            if (
                recorder.state !=
                AudioRecord.STATE_INITIALIZED
            ) {
                recorder.release()

                error(
                    "Unable to initialize the microphone."
                )
            }

            try {
                recorder.startRecording()
            } catch (
                securityException:
                SecurityException
            ) {
                recorder.release()

                throw SecurityException(
                    "Microphone access was denied.",
                    securityException
                )
            } catch (
                throwable: Throwable
            ) {
                recorder.release()
                throw throwable
            }

            check(
                recorder.recordingState ==
                        AudioRecord
                            .RECORDSTATE_RECORDING
            ) {
                runCatching {
                    recorder.stop()
                }

                recorder.release()

                "The microphone did not enter the recording state."
            }

            audioRecord =
                recorder

            val startedAt =
                System.currentTimeMillis()

            mutableState.value =
                AudioRecorderState.Recording(
                    startedAt =
                        startedAt,
                    amplitude =
                        0f
                )

            recordingJob =
                recorderScope.launch {
                    captureLoop(
                        recorder =
                            recorder,
                        frameSampleCount =
                            frameSampleCount,
                        startedAt =
                            startedAt
                    )
                }
        }
            .onFailure { throwable ->
                releaseRecorder()

                mutableState.value =
                    AudioRecorderState.Error(
                        message =
                            throwable.message
                                ?: "Unable to start recording.",
                        cause =
                            throwable
                    )
            }
    }

    suspend fun stop():
            Result<Unit> {
        return withContext(
            recorderDispatcher
        ) {
            runCatching {
                val recorder =
                    audioRecord

                val job =
                    recordingJob

                recordingJob =
                    null

                if (
                    recorder != null &&
                    recorder.recordingState ==
                    AudioRecord
                        .RECORDSTATE_RECORDING
                ) {
                    runCatching {
                        recorder.stop()
                    }
                }

                job?.cancelAndJoin()

                releaseRecorder()

                mutableState.value =
                    AudioRecorderState.Stopped
            }
                .onFailure { throwable ->
                    recordingJob =
                        null

                    releaseRecorder()

                    mutableState.value =
                        AudioRecorderState.Error(
                            message =
                                throwable.message
                                    ?: "Unable to stop recording.",
                            cause =
                                throwable
                        )
                }
        }
    }

    suspend fun cancel() {
        withContext(
            recorderDispatcher
        ) {
            val recorder =
                audioRecord

            val job =
                recordingJob

            recordingJob =
                null

            if (
                recorder != null &&
                recorder.recordingState ==
                AudioRecord
                    .RECORDSTATE_RECORDING
            ) {
                runCatching {
                    recorder.stop()
                }
            }

            job?.cancelAndJoin()

            releaseRecorder()

            mutableState.value =
                AudioRecorderState.Idle
        }
    }

    suspend fun release() {
        cancel()
    }

    private suspend fun captureLoop(
        recorder: AudioRecord,
        frameSampleCount: Int,
        startedAt: Long
    ) {
        val buffer =
            ShortArray(
                frameSampleCount
            )

        try {
            while (
                currentCoroutineContext()
                    .isActive &&
                recorder.recordingState ==
                AudioRecord
                    .RECORDSTATE_RECORDING
            ) {
                currentCoroutineContext()
                    .ensureActive()

                val readCount =
                    recorder.read(
                        buffer,
                        0,
                        buffer.size,
                        AudioRecord.READ_BLOCKING
                    )

                when {
                    readCount > 0 -> {
                        val samples =
                            FloatArray(
                                readCount
                            )

                        var sumOfSquares =
                            0.0

                        for (
                        index in
                        0 until readCount
                        ) {
                            val normalizedSample =
                                (
                                        buffer[index]
                                            .toFloat() /
                                                PCM_16_NORMALIZER
                                        )
                                    .coerceIn(
                                        minimumValue =
                                            -1f,
                                        maximumValue =
                                            1f
                                    )

                            samples[index] =
                                normalizedSample

                            sumOfSquares +=
                                normalizedSample
                                    .toDouble() *
                                        normalizedSample
                                            .toDouble()
                        }

                        val rootMeanSquare =
                            sqrt(
                                sumOfSquares /
                                        readCount
                                            .toDouble()
                            )
                                .toFloat()
                                .coerceIn(
                                    minimumValue =
                                        0f,
                                    maximumValue =
                                        1f
                                )

                        mutableState.value =
                            AudioRecorderState
                                .Recording(
                                    startedAt =
                                        startedAt,
                                    amplitude =
                                        rootMeanSquare
                                )

                        mutableAudioFrames.emit(
                            AudioFrame(
                                samples =
                                    samples,
                                sampleRate =
                                    SAMPLE_RATE,
                                amplitude =
                                    rootMeanSquare,
                                capturedAt =
                                    System
                                        .currentTimeMillis()
                            )
                        )
                    }

                    readCount ==
                            AudioRecord
                                .ERROR_DEAD_OBJECT -> {
                        error(
                            "The microphone became unavailable."
                        )
                    }

                    readCount ==
                            AudioRecord
                                .ERROR_INVALID_OPERATION -> {
                        error(
                            "The microphone is not in a valid recording state."
                        )
                    }

                    readCount ==
                            AudioRecord
                                .ERROR_BAD_VALUE -> {
                        error(
                            "The microphone rejected the audio buffer."
                        )
                    }

                    readCount ==
                            AudioRecord.ERROR -> {
                        error(
                            "The microphone returned an unknown recording error."
                        )
                    }
                }
            }
        } catch (
            cancellation:
            CancellationException
        ) {
            throw cancellation
        } catch (
            throwable: Throwable
        ) {
            mutableState.value =
                AudioRecorderState.Error(
                    message =
                        throwable.message
                            ?: "Audio capture failed.",
                    cause =
                        throwable
                )
        }
    }

    private fun releaseRecorder() {
        val recorder =
            audioRecord

        audioRecord =
            null

        if (recorder != null) {
            runCatching {
                if (
                    recorder.recordingState ==
                    AudioRecord
                        .RECORDSTATE_RECORDING
                ) {
                    recorder.stop()
                }
            }

            runCatching {
                recorder.release()
            }
        }
    }
}

data class AudioFrame(
    val samples: FloatArray,
    val sampleRate: Int,
    val amplitude: Float,
    val capturedAt: Long
)

sealed interface AudioRecorderState {

    data object Idle :
        AudioRecorderState

    data class Recording(
        val startedAt: Long,
        val amplitude: Float
    ) : AudioRecorderState

    data object Stopped :
        AudioRecorderState

    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : AudioRecorderState
}