package com.julesmeister.sangfroid.data.speech

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

class PcmAudioAccumulator @Inject constructor() {

    private var output =
        ByteArrayOutputStream()

    private var storedSampleRate: Int? =
        null

    private var storedSampleCount: Int =
        0

    val sampleCount: Int
        get() = storedSampleCount

    val sampleRate: Int?
        get() = storedSampleRate

    val durationMilliseconds: Long
        get() {
            val rate =
                storedSampleRate
                    ?: return 0L

            if (rate <= 0) {
                return 0L
            }

            return (
                    storedSampleCount.toLong() *
                            1_000L
                    ) / rate.toLong()
        }

    fun reset() {
        output.close()

        output =
            ByteArrayOutputStream()

        storedSampleRate = null
        storedSampleCount = 0
    }

    fun append(
        samples: FloatArray,
        sampleRate: Int
    ) {
        require(sampleRate > 0) {
            "Audio sample rate must be positive."
        }

        if (samples.isEmpty()) {
            return
        }

        val existingRate =
            storedSampleRate

        if (existingRate == null) {
            storedSampleRate =
                sampleRate
        } else {
            require(
                existingRate == sampleRate
            ) {
                "Audio sample rate changed from " +
                        "$existingRate Hz to $sampleRate Hz."
            }
        }

        val byteBuffer =
            ByteBuffer
                .allocate(
                    samples.size *
                            Float.SIZE_BYTES
                )
                .order(
                    ByteOrder.LITTLE_ENDIAN
                )

        samples.forEach { sample ->
            byteBuffer.putFloat(
                sample.coerceIn(
                    minimumValue = -1f,
                    maximumValue = 1f
                )
            )
        }

        output.write(
            byteBuffer.array()
        )

        storedSampleCount +=
            samples.size
    }

    fun snapshot(): RecordedSpeechAudio {
        val rate =
            checkNotNull(storedSampleRate) {
                "No recorded audio is available."
            }

        check(storedSampleCount > 0) {
            "No recorded audio is available."
        }

        val bytes =
            output.toByteArray()

        val floatBuffer =
            ByteBuffer
                .wrap(bytes)
                .order(
                    ByteOrder.LITTLE_ENDIAN
                )
                .asFloatBuffer()

        val samples =
            FloatArray(
                floatBuffer.remaining()
            )

        floatBuffer.get(samples)

        return RecordedSpeechAudio(
            samples = samples,
            sampleRate = rate
        )
    }
}

data class RecordedSpeechAudio(
    val samples: FloatArray,
    val sampleRate: Int
) {
    val durationMilliseconds: Long
        get() {
            if (sampleRate <= 0) {
                return 0L
            }

            return (
                    samples.size.toLong() *
                            1_000L
                    ) / sampleRate.toLong()
        }
}