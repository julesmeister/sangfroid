package com.julesmeister.sangfroid.data.speech

import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OnlineModelConfig
import com.k2fsa.sherpa.onnx.OnlineRecognizer
import com.k2fsa.sherpa.onnx.OnlineRecognizerConfig
import com.k2fsa.sherpa.onnx.OnlineStream
import com.k2fsa.sherpa.onnx.OnlineTransducerModelConfig

@Suppress("unused")
private object SherpaApiCheck {

    private val featureConfigType =
        FeatureConfig::class

    private val modelConfigType =
        OnlineModelConfig::class

    private val recognizerConfigType =
        OnlineRecognizerConfig::class

    private val transducerConfigType =
        OnlineTransducerModelConfig::class

    private val recognizerType =
        OnlineRecognizer::class

    private val streamType =
        OnlineStream::class
}