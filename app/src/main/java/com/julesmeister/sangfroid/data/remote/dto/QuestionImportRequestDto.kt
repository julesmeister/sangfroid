// app/src/main/java/com/julesmeister/sangfroid/data/remote/dto/QuestionImportRequestDto.kt

package com.julesmeister.sangfroid.data.remote.dto

import org.json.JSONObject

data class QuestionImportRequestDto(
    val requestId: String,
    val content: String,
    val languageTag: String
) {

    init {
        require(requestId.isNotBlank()) {
            "requestId cannot be blank."
        }

        require(
            requestId.length <=
                    MAXIMUM_REQUEST_ID_LENGTH
        ) {
            "requestId cannot exceed 100 characters."
        }

        require(
            content.isNotBlank()
        ) {
            "Import content cannot be blank."
        }

        require(
            content.length <=
                    MAXIMUM_CONTENT_LENGTH
        ) {
            "Import content cannot exceed 50,000 characters."
        }

        require(
            languageTag.isNotBlank()
        ) {
            "languageTag cannot be blank."
        }

        require(
            languageTag.length <=
                    MAXIMUM_LANGUAGE_TAG_LENGTH
        ) {
            "languageTag cannot exceed 20 characters."
        }
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put(
                JSON_REQUEST_ID,
                requestId.trim()
            )

            put(
                JSON_CONTENT,
                content
                    .replace(
                        '\u0000',
                        ' '
                    )
                    .trim()
                    .take(
                        MAXIMUM_CONTENT_LENGTH
                    )
            )

            put(
                JSON_LANGUAGE_TAG,
                languageTag
                    .replace(
                        '\u0000',
                        ' '
                    )
                    .trim()
                    .take(
                        MAXIMUM_LANGUAGE_TAG_LENGTH
                    )
            )
        }
    }

    companion object {
        private const val JSON_REQUEST_ID =
            "requestId"

        private const val JSON_CONTENT =
            "content"

        private const val JSON_LANGUAGE_TAG =
            "languageTag"

        private const val MAXIMUM_REQUEST_ID_LENGTH =
            100

        private const val MAXIMUM_CONTENT_LENGTH =
            50_000

        private const val MAXIMUM_LANGUAGE_TAG_LENGTH =
            20
    }
}