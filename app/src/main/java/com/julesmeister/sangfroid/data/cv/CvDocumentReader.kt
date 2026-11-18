package com.julesmeister.sangfroid.data.cv

import android.net.Uri

interface CvDocumentReader {

    suspend fun read(
        uri: Uri
    ): CvImportResult
}