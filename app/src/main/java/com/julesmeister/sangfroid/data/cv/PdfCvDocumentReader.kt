package com.julesmeister.sangfroid.data.cv

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class PdfCvDocumentReader @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val textValidator: CvTextValidator
) : CvDocumentReader {

    override suspend fun read(
        uri: Uri
    ): CvImportResult {
        return withContext(
            Dispatchers.IO
        ) {
            readPdf(
                uri = uri
            )
        }
    }

    private fun readPdf(
        uri: Uri
    ): CvImportResult {
        return try {
            PDFBoxResourceLoader.init(
                context.applicationContext
            )

            val inputStream =
                context
                    .contentResolver
                    .openInputStream(
                        uri
                    )
                    ?: return CvImportResult.Failure(
                        message =
                            "Sangfroid could not open the selected PDF."
                    )

            inputStream.use { stream ->
                PDDocument
                    .load(
                        stream
                    )
                    .use { document ->

                        val pageCount =
                            document.numberOfPages

                        if (
                            pageCount <= 0
                        ) {
                            return CvImportResult.InsufficientText(
                                extractedText = "",
                                pageCount = 0,
                                characterCount = 0,
                                wordCount = 0,
                                reason =
                                    "The selected PDF contains no pages."
                            )
                        }

                        val textStripper =
                            PDFTextStripper().apply {
                                sortByPosition =
                                    true
                            }

                        val extractedText =
                            textStripper.getText(
                                document
                            )

                        return textValidator.validate(
                            text = extractedText,
                            pageCount = pageCount
                        )
                    }
            }
        } catch (
            securityException: SecurityException
        ) {
            CvImportResult.Failure(
                message =
                    "Sangfroid does not have permission to read the selected PDF.",
                cause =
                    securityException
            )
        } catch (
            exception: Exception
        ) {
            CvImportResult.Failure(
                message =
                    "Sangfroid could not extract text from the selected PDF.",
                cause =
                    exception
            )
        }
    }
}