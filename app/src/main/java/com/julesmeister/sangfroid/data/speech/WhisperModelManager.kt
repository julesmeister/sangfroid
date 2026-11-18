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
class WhisperModelManager @Inject constructor(
    @ApplicationContext
    private val context: Context
) {

    suspend fun prepareModel():
            Result<WhisperSpeechModel> {
        return withContext(
            Dispatchers.IO
        ) {
            runCatching {
                val destination =
                    File(
                        context.filesDir,
                        "$MODEL_DIRECTORY_NAME/" +
                                MODEL_ID
                    )

                check(
                    destination.exists() ||
                            destination.mkdirs()
                ) {
                    "Unable to create the Whisper model directory."
                }

                val encoderFile =
                    File(
                        destination,
                        ENCODER_FILE
                    )

                val decoderFile =
                    File(
                        destination,
                        DECODER_FILE
                    )

                val tokensFile =
                    File(
                        destination,
                        TOKENS_FILE
                    )

                copyAssetIfRequired(
                    assetPath =
                        "$MODEL_ASSET_ROOT/" +
                                ENCODER_FILE,
                    destination =
                        encoderFile
                )

                copyAssetIfRequired(
                    assetPath =
                        "$MODEL_ASSET_ROOT/" +
                                DECODER_FILE,
                    destination =
                        decoderFile
                )

                copyAssetIfRequired(
                    assetPath =
                        "$MODEL_ASSET_ROOT/" +
                                TOKENS_FILE,
                    destination =
                        tokensFile
                )

                WhisperSpeechModel(
                    id = MODEL_ID,
                    displayName =
                        MODEL_DISPLAY_NAME,
                    languageTag =
                        LANGUAGE_TAG,
                    sampleRate =
                        SAMPLE_RATE,
                    encoderFile =
                        encoderFile,
                    decoderFile =
                        decoderFile,
                    tokensFile =
                        tokensFile
                )
                    .also { model ->
                        check(model.isComplete) {
                            "The offline Whisper model is incomplete."
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
            destination.isFile &&
            destination.length() > 0L
        ) {
            return
        }

        destination.parentFile?.mkdirs()

        val temporaryFile =
            File(
                destination.parentFile,
                "${destination.name}.copying"
            )

        runCatching {
            if (temporaryFile.exists()) {
                temporaryFile.delete()
            }

            context.assets
                .open(assetPath)
                .use { input ->
                    FileOutputStream(
                        temporaryFile
                    ).use { output ->
                        input.copyTo(
                            output
                        )

                        output.fd.sync()
                    }
                }

            check(
                temporaryFile.length() > 0L
            ) {
                "Copied Whisper model asset is empty: $assetPath"
            }

            if (destination.exists()) {
                destination.delete()
            }

            check(
                temporaryFile.renameTo(
                    destination
                )
            ) {
                "Unable to finish copying Whisper model asset: $assetPath"
            }
        }
            .onFailure {
                temporaryFile.delete()
            }
            .getOrThrow()
    }

    companion object {
        const val SAMPLE_RATE =
            16_000

        private const val MODEL_ID =
            "sherpa-onnx-whisper-base.en"

        private const val MODEL_DISPLAY_NAME =
            "Whisper Base English INT8"

        private const val LANGUAGE_TAG =
            "en-US"

        private const val MODEL_DIRECTORY_NAME =
            "speech_models"

        private const val MODEL_ASSET_ROOT =
            "speech_models/$MODEL_ID"

        private const val ENCODER_FILE =
            "encoder.onnx"

        private const val DECODER_FILE =
            "decoder.onnx"

        private const val TOKENS_FILE =
            "tokens.txt"
    }
}

data class WhisperSpeechModel(
    val id: String,
    val displayName: String,
    val languageTag: String,
    val sampleRate: Int,
    val encoderFile: File,
    val decoderFile: File,
    val tokensFile: File
) {
    val isComplete: Boolean
        get() =
            encoderFile.isValidModelFile() &&
                    decoderFile.isValidModelFile() &&
                    tokensFile.isValidModelFile()

    private fun File.isValidModelFile():
            Boolean {
        return exists() &&
                isFile &&
                length() > 0L
    }
}