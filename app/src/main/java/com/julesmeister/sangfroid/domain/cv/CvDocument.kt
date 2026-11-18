package com.julesmeister.sangfroid.domain.cv

data class CvDocument(
    val uri: String,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long?
) {
    val isPdf: Boolean
        get() =
            mimeType.equals(
                other =
                    PDF_MIME_TYPE,
                ignoreCase =
                    true
            ) ||
                    displayName.endsWith(
                        suffix =
                            PDF_EXTENSION,
                        ignoreCase =
                            true
                    )

    val formattedSize: String?
        get() =
            sizeBytes
                ?.takeIf {
                    it >= 0L
                }
                ?.let(
                    ::formatBytes
                )

    init {
        require(
            uri.isNotBlank()
        ) {
            "CV document URI cannot be blank."
        }

        require(
            displayName.isNotBlank()
        ) {
            "CV document name cannot be blank."
        }

        require(
            mimeType.isNotBlank()
        ) {
            "CV MIME type cannot be blank."
        }

        require(
            sizeBytes == null ||
                    sizeBytes >= 0L
        ) {
            "CV document size cannot be negative."
        }
    }

    companion object {
        const val PDF_MIME_TYPE =
            "application/pdf"

        const val PDF_EXTENSION =
            ".pdf"

        const val MAX_FILE_SIZE_BYTES =
            10L * 1024L * 1024L

        private fun formatBytes(
            bytes: Long
        ): String {
            return when {
                bytes >= 1024L * 1024L -> {
                    val megabytes =
                        bytes.toDouble() /
                                (1024.0 * 1024.0)

                    String.format(
                        locale =
                            java.util.Locale.US,
                        format =
                            "%.1f MB",
                        megabytes
                    )
                }

                bytes >= 1024L -> {
                    val kilobytes =
                        bytes.toDouble() /
                                1024.0

                    String.format(
                        locale =
                            java.util.Locale.US,
                        format =
                            "%.0f KB",
                        kilobytes
                    )
                }

                else -> {
                    "$bytes B"
                }
            }
        }
    }
}