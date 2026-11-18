package com.julesmeister.sangfroid.data.cv

import android.content.Context
import android.net.Uri
import com.julesmeister.sangfroid.domain.cv.CvDocument
import com.julesmeister.sangfroid.domain.cv.CvExtractionWarning
import com.julesmeister.sangfroid.domain.cv.CvImportException
import com.julesmeister.sangfroid.domain.cv.CvImportFailure
import com.julesmeister.sangfroid.domain.cv.CvTextExtraction
import com.julesmeister.sangfroid.domain.cv.CvTextExtractor
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class PdfCvTextExtractor @Inject constructor(
    @ApplicationContext
    private val context: Context
) : CvTextExtractor {

    override suspend fun extractText(
        document: CvDocument
    ): Result<CvTextExtraction> {
        return withContext(
            Dispatchers.IO
        ) {
            try {
                validateDocument(
                    document
                )

                PDFBoxResourceLoader.init(
                    context.applicationContext
                )

                val uri =
                    Uri.parse(
                        document.uri
                    )

                val inputStream =
                    context
                        .contentResolver
                        .openInputStream(
                            uri
                        )
                        ?: throw CvImportException(
                            CvImportFailure.UnableToOpen
                        )

                val extraction =
                    inputStream.use { stream ->
                        PDDocument.load(
                            stream
                        ).use { pdf ->
                            if (pdf.isEncrypted) {
                                throw CvImportException(
                                    CvImportFailure.PasswordProtected
                                )
                            }

                            val pageCount =
                                pdf.numberOfPages

                            if (pageCount <= 0) {
                                throw CvImportException(
                                    CvImportFailure.NoReadableText
                                )
                            }

                            val pagesToRead =
                                pageCount.coerceAtMost(
                                    MAX_PAGES
                                )

                            val rawText =
                                PDFTextStripper()
                                    .apply {
                                        sortByPosition =
                                            true

                                        startPage =
                                            1

                                        endPage =
                                            pagesToRead
                                    }
                                    .getText(
                                        pdf
                                    )

                            val normalizedText =
                                normalizeText(
                                    rawText
                                )

                            if (
                                normalizedText.length <
                                MINIMUM_USEFUL_CHARACTER_COUNT
                            ) {
                                throw CvImportException(
                                    CvImportFailure.NoReadableText
                                )
                            }

                            val warnings =
                                buildList {
                                    if (
                                        document.sizeBytes ==
                                        null
                                    ) {
                                        add(
                                            CvExtractionWarning
                                                .FILE_SIZE_UNKNOWN
                                        )
                                    }

                                    if (
                                        rawText.trim() !=
                                        normalizedText
                                    ) {
                                        add(
                                            CvExtractionWarning
                                                .TEXT_WAS_NORMALIZED
                                        )
                                    }

                                    if (
                                        pageCount >
                                        MAX_PAGES
                                    ) {
                                        add(
                                            CvExtractionWarning
                                                .LIMITED_PAGE_COUNT
                                        )
                                    }

                                    if (
                                        normalizedText.length <
                                        LOW_TEXT_CHARACTER_COUNT
                                    ) {
                                        add(
                                            CvExtractionWarning
                                                .VERY_LITTLE_TEXT
                                        )

                                        add(
                                            CvExtractionWarning
                                                .POSSIBLY_SCANNED_DOCUMENT
                                        )
                                    }
                                }
                                    .distinct()

                            CvTextExtraction(
                                text =
                                    normalizedText,
                                pageCount =
                                    pageCount,
                                warnings =
                                    warnings
                            )
                        }
                    }

                Result.success(
                    extraction
                )
            } catch (
                cancellation: CancellationException
            ) {
                throw cancellation
            } catch (
                exception: CvImportException
            ) {
                Result.failure(
                    exception
                )
            } catch (
                exception: SecurityException
            ) {
                Result.failure(
                    CvImportException(
                        failure =
                            CvImportFailure.UnableToOpen,
                        cause =
                            exception
                    )
                )
            } catch (
                exception: IOException
            ) {
                Result.failure(
                    CvImportException(
                        failure =
                            CvImportFailure.ExtractionFailed(
                                technicalMessage =
                                    exception.message
                            ),
                        cause =
                            exception
                    )
                )
            } catch (
                throwable: Throwable
            ) {
                Result.failure(
                    CvImportException(
                        failure =
                            CvImportFailure.ExtractionFailed(
                                technicalMessage =
                                    throwable.message
                            ),
                        cause =
                            throwable
                    )
                )
            }
        }
    }

    private fun validateDocument(
        document: CvDocument
    ) {
        if (!document.isPdf) {
            throw CvImportException(
                CvImportFailure.UnsupportedDocument
            )
        }

        if (
            document.sizeBytes != null &&
            document.sizeBytes >
            CvDocument.MAX_FILE_SIZE_BYTES
        ) {
            throw CvImportException(
                CvImportFailure.FileTooLarge
            )
        }
    }

    private fun normalizeText(
        value: String
    ): String {
        return value
            .replace(
                "\u0000",
                ""
            )
            .replace(
                Regex(
                    "[\\t\\x0B\\f\\r ]+"
                ),
                " "
            )
            .replace(
                Regex(
                    " *\\n *"
                ),
                "\n"
            )
            .replace(
                Regex(
                    "\\n{3,}"
                ),
                "\n\n"
            )
            .trim()
    }

    companion object {

        private const val MAX_PAGES =
            20

        private const val MINIMUM_USEFUL_CHARACTER_COUNT =
            80

        private const val LOW_TEXT_CHARACTER_COUNT =
            300
    }
}