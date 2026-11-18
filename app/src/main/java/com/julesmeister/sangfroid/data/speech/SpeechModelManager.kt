package com.julesmeister.sangfroid.data.speech

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class SpeechModelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val DEFAULT_MODEL_ID =
            "sherpa-onnx-streaming-zipformer-en"

        const val DEFAULT_MODEL_DISPLAY_NAME =
            "English Streaming Zipformer"

        const val DEFAULT_LANGUAGE_TAG = "en-US"
        const val DEFAULT_SAMPLE_RATE = 16_000

        private const val MODEL_ASSET_ROOT =
            "speech_models/$DEFAULT_MODEL_ID"

        private const val MODEL_DIRECTORY_NAME =
            "speech_models"

        private const val ENCODER_FILE = "encoder.onnx"
        private const val DECODER_FILE = "decoder.onnx"
        private const val JOINER_FILE = "joiner.onnx"
        private const val TOKENS_FILE = "tokens.txt"
    }

    suspend fun prepareDefaultModel(): Result<SpeechModel> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val destination = File(
                    context.filesDir,
                    "$MODEL_DIRECTORY_NAME/$DEFAULT_MODEL_ID"
                )

                destination.mkdirs()

                copyAssetIfRequired(
                    assetPath = "$MODEL_ASSET_ROOT/$ENCODER_FILE",
                    destination = File(destination, ENCODER_FILE)
                )

                copyAssetIfRequired(
                    assetPath = "$MODEL_ASSET_ROOT/$DECODER_FILE",
                    destination = File(destination, DECODER_FILE)
                )

                copyAssetIfRequired(
                    assetPath = "$MODEL_ASSET_ROOT/$JOINER_FILE",
                    destination = File(destination, JOINER_FILE)
                )

                copyAssetIfRequired(
                    assetPath = "$MODEL_ASSET_ROOT/$TOKENS_FILE",
                    destination = File(destination, TOKENS_FILE)
                )

                val model = SpeechModel(
                    id = DEFAULT_MODEL_ID,
                    displayName = DEFAULT_MODEL_DISPLAY_NAME,
                    languageTag = DEFAULT_LANGUAGE_TAG,
                    sampleRate = DEFAULT_SAMPLE_RATE,
                    directory = destination,
                    encoderFile = File(destination, ENCODER_FILE),
                    decoderFile = File(destination, DECODER_FILE),
                    joinerFile = File(destination, JOINER_FILE),
                    tokensFile = File(destination, TOKENS_FILE)
                )

                require(model.isComplete) {
                    "The offline speech model is incomplete."
                }

                model
            }
        }
    }

    suspend fun getDefaultModelOrNull(): SpeechModel? {
        return withContext(Dispatchers.IO) {
            val directory = File(
                context.filesDir,
                "$MODEL_DIRECTORY_NAME/$DEFAULT_MODEL_ID"
            )

            val model = SpeechModel(
                id = DEFAULT_MODEL_ID,
                displayName = DEFAULT_MODEL_DISPLAY_NAME,
                languageTag = DEFAULT_LANGUAGE_TAG,
                sampleRate = DEFAULT_SAMPLE_RATE,
                directory = directory,
                encoderFile = File(directory, ENCODER_FILE),
                decoderFile = File(directory, DECODER_FILE),
                joinerFile = File(directory, JOINER_FILE),
                tokensFile = File(directory, TOKENS_FILE)
            )

            model.takeIf {
                it.isComplete
            }
        }
    }

    suspend fun deleteDefaultModel(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val directory = File(
                    context.filesDir,
                    "$MODEL_DIRECTORY_NAME/$DEFAULT_MODEL_ID"
                )

                if (directory.exists()) {
                    check(directory.deleteRecursively()) {
                        "Unable to delete the speech model."
                    }
                }
            }
        }
    }

    private fun copyAssetIfRequired(
        assetPath: String,
        destination: File
    ) {
        if (
            destination.exists() &&
            destination.length() > 0L
        ) {
            return
        }

        destination.parentFile?.mkdirs()

        context.assets.open(assetPath).use { input ->
            FileOutputStream(destination).use { output ->
                input.copyTo(output)
            }
        }
    }
}

data class SpeechModel(
    val id: String,
    val displayName: String,
    val languageTag: String,
    val sampleRate: Int,
    val directory: File,
    val encoderFile: File,
    val decoderFile: File,
    val joinerFile: File,
    val tokensFile: File
) {
    val isComplete: Boolean
        get() = encoderFile.isValidModelFile() &&
                decoderFile.isValidModelFile() &&
                joinerFile.isValidModelFile() &&
                tokensFile.isValidModelFile()

    private fun File.isValidModelFile(): Boolean {
        return exists() &&
                isFile &&
                length() > 0L
    }
}