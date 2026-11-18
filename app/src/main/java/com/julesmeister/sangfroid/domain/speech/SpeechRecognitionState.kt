package com.julesmeister.sangfroid.domain.speech

sealed interface SpeechRecognitionState {

    data object Idle : SpeechRecognitionState

    data object LoadingModel : SpeechRecognitionState

    data class Ready(
        val modelName: String
    ) : SpeechRecognitionState

    data class Listening(
        val partialText: String,
        val elapsedMilliseconds: Long
    ) : SpeechRecognitionState

    data class Processing(
        val partialText: String
    ) : SpeechRecognitionState

    data class Completed(
        val text: String
    ) : SpeechRecognitionState

    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : SpeechRecognitionState
}