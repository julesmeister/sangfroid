package com.julesmeister.sangfroid.domain.speech

data class SpeechRecognitionResult(
    val text: String,
    val isFinal: Boolean,
    val segmentIndex: Int,
    val confidence: Float? = null,
    val languageTag: String = "en-US"
)