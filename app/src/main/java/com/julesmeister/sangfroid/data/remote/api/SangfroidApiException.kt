package com.julesmeister.sangfroid.data.remote.api

class SangfroidApiException(
    val statusCode: Int,
    override val message: String,
    val responseBody: String? = null,
    override val cause: Throwable? = null
) : IllegalStateException(
    message,
    cause
)