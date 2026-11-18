package com.julesmeister.sangfroid.feature.practice.speech

import java.util.Locale

data class SpeechUiState(
    val isPreparing: Boolean = false,
    val isReady: Boolean = false,
    val isRecording: Boolean = false,
    val isProcessing: Boolean = false,
    val isFinalRecognizerPreparing:
    Boolean = false,
    val preparationStage:
    SpeechPreparationStage =
        SpeechPreparationStage.NONE,
    val processingStage:
    SpeechProcessingStage =
        SpeechProcessingStage.NONE,
    val elapsedMilliseconds: Long = 0L,
    val amplitude: Float = 0f,
    val partialTranscript: String = "",
    val finalTranscript: String? = null,
    val modelName: String? = null,
    val usedEnhancedTranscript:
    Boolean = false,
    val errorMessage: String? = null
) {

    val elapsedLabel: String
        get() {
            val totalSeconds =
                elapsedMilliseconds /
                        1_000L

            val minutes =
                totalSeconds /
                        60L

            val seconds =
                totalSeconds %
                        60L

            return String.format(
                Locale.US,
                "%02d:%02d",
                minutes,
                seconds
            )
        }

    val normalizedAmplitude: Float
        get() =
            amplitude.coerceIn(
                minimumValue = 0f,
                maximumValue = 1f
            )

    val displayedTranscript: String
        get() =
            partialTranscript
                .trim()
                .takeIf(
                    String::isNotBlank
                )
                ?: finalTranscript
                    .orEmpty()
                    .trim()

    val hasTranscript: Boolean
        get() =
            displayedTranscript
                .isNotBlank()

    val canStart: Boolean
        get() =
            isReady &&
                    !isRecording &&
                    !isPreparing &&
                    !isProcessing

    val canStop: Boolean
        get() =
            isRecording &&
                    !isProcessing

    val canCancel: Boolean
        get() =
            isRecording ||
                    isProcessing

    val statusLabel: String
        get() =
            when {
                errorMessage != null -> {
                    errorMessage
                }

                isPreparing &&
                        preparationStage ==
                        SpeechPreparationStage
                            .STREAMING_MODEL -> {
                    "Preparing offline speech model"
                }

                isPreparing &&
                        preparationStage ==
                        SpeechPreparationStage
                            .FINAL_MODEL -> {
                    "Preparing offline transcript enhancement"
                }

                isProcessing &&
                        processingStage ==
                        SpeechProcessingStage
                            .FINALIZING_STREAMING -> {
                    "Finalizing live transcript"
                }

                isProcessing &&
                        processingStage ==
                        SpeechProcessingStage
                            .ENHANCING_WITH_WHISPER -> {
                    "Improving transcript offline"
                }

                isRecording &&
                        partialTranscript
                            .isBlank() -> {
                    "Listening..."
                }

                isRecording -> {
                    "Transcribing..."
                }

                isReady &&
                        isFinalRecognizerPreparing -> {
                    "Ready to record · offline enhancement loading"
                }

                isReady &&
                        usedEnhancedTranscript -> {
                    "Enhanced offline transcript ready"
                }

                isReady -> {
                    "Ready to record"
                }

                else -> {
                    "Voice input unavailable"
                }
            }

    val transcriptSourceLabel: String?
        get() =
            when {
                finalTranscript
                    .isNullOrBlank() -> {
                    null
                }

                usedEnhancedTranscript -> {
                    "Whisper offline transcript"
                }

                else -> {
                    "Live offline transcript"
                }
            }
}

enum class SpeechPreparationStage {
    NONE,
    STREAMING_MODEL,
    FINAL_MODEL
}

enum class SpeechProcessingStage {
    NONE,
    FINALIZING_STREAMING,
    ENHANCING_WITH_WHISPER
}