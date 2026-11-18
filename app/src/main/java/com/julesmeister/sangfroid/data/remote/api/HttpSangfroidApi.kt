package com.julesmeister.sangfroid.data.remote.api

import android.util.Log
import com.julesmeister.sangfroid.BuildConfig
import com.julesmeister.sangfroid.data.remote.auth.AuthTokenProvider
import com.julesmeister.sangfroid.data.remote.dto.CvAnalysisRequestDto
import com.julesmeister.sangfroid.data.remote.dto.CvAnalysisResponseDto
import com.julesmeister.sangfroid.data.remote.dto.EvaluationRequestDto
import com.julesmeister.sangfroid.data.remote.dto.EvaluationResponseDto
import com.julesmeister.sangfroid.data.remote.dto.QuestionGenerationRequestDto
import com.julesmeister.sangfroid.data.remote.dto.QuestionGenerationResponseDto
import com.julesmeister.sangfroid.data.remote.dto.QuestionImportRequestDto
import com.julesmeister.sangfroid.data.remote.dto.QuestionImportResponseDto
import java.io.IOException
import java.io.InputStream
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

@Singleton
class HttpSangfroidApi @Inject constructor(
    private val authTokenProvider: AuthTokenProvider
) : SangfroidApi {

    override suspend fun evaluateAnswer(
        request: EvaluationRequestDto
    ): Result<EvaluationResponseDto> {
        return executePost(
            path =
                EVALUATION_PATH,
            requestId =
                request.requestId,
            requestBody =
                request.toJson(),
            timeoutPolicy =
                TimeoutPolicy.Standard,
            retryPolicy =
                RetryPolicy.Standard,
            parser =
                EvaluationResponseDto::fromJson
        )
    }

    override suspend fun generateQuestions(
        request: QuestionGenerationRequestDto
    ): Result<QuestionGenerationResponseDto> {
        return executePost(
            path =
                QUESTION_GENERATION_PATH,
            requestId =
                request.requestId,
            requestBody =
                request.toJson(),
            timeoutPolicy =
                TimeoutPolicy.LongRunning,
            retryPolicy =
                RetryPolicy.None,
            parser =
                QuestionGenerationResponseDto::fromJson
        )
    }

    override suspend fun importQuestions(
        request: QuestionImportRequestDto
    ): Result<QuestionImportResponseDto> {
        return executePost(
            path =
                QUESTION_IMPORT_PATH,
            requestId =
                request.requestId,
            requestBody =
                request.toJson(),
            timeoutPolicy =
                TimeoutPolicy.LongRunning,
            retryPolicy =
                RetryPolicy.None,
            parser =
                QuestionImportResponseDto::fromJson
        )
    }

    override suspend fun analyzeCv(
        request: CvAnalysisRequestDto
    ): Result<CvAnalysisResponseDto> {
        Log.d(
            LOG_TAG,
            "CV analysis request started. requestId=${request.requestId}"
        )

        return executePost(
            path =
                CV_ANALYSIS_PATH,
            requestId =
                request.requestId,
            requestBody =
                request.toJson(),
            timeoutPolicy =
                TimeoutPolicy.LongRunning,
            retryPolicy =
                RetryPolicy.None,
            parser =
                CvAnalysisResponseDto::fromJson
        )
    }

    private suspend fun <T> executePost(
        path: String,
        requestId: String,
        requestBody: JSONObject,
        timeoutPolicy: TimeoutPolicy,
        retryPolicy: RetryPolicy,
        parser: (JSONObject) -> T
    ): Result<T> {
        return withContext(
            Dispatchers.IO
        ) {
            try {
                val startedAt =
                    System.currentTimeMillis()

                Log.d(
                    LOG_TAG,
                    "Preparing HTTP POST. " +
                            "path=$path " +
                            "requestId=$requestId " +
                            "connectTimeoutMs=${timeoutPolicy.connectTimeoutMillis} " +
                            "readTimeoutMs=${timeoutPolicy.readTimeoutMillis}"
                )

                val currentToken =
                    authTokenProvider
                        .getAccessToken()

                var response =
                    performWithRetry(
                        path =
                            path,
                        requestId =
                            requestId,
                        requestBody =
                            requestBody,
                        accessToken =
                            currentToken,
                        timeoutPolicy =
                            timeoutPolicy,
                        retryPolicy =
                            retryPolicy
                    )

                if (
                    response.statusCode ==
                    HttpURLConnection.HTTP_UNAUTHORIZED
                ) {
                    val refreshedToken =
                        authTokenProvider
                            .refreshAccessToken()
                            .getOrNull()

                    if (
                        !refreshedToken.isNullOrBlank()
                    ) {
                        response =
                            performWithRetry(
                                path =
                                    path,
                                requestId =
                                    requestId,
                                requestBody =
                                    requestBody,
                                accessToken =
                                    refreshedToken,
                                timeoutPolicy =
                                    timeoutPolicy,
                                retryPolicy =
                                    retryPolicy
                            )
                    }
                }

                Log.d(
                    LOG_TAG,
                    "HTTP response received. " +
                            "path=$path " +
                            "status=${response.statusCode} " +
                            "requestId=$requestId " +
                            "elapsedMs=${System.currentTimeMillis() - startedAt}"
                )

                if (
                    response.statusCode !in
                    HTTP_SUCCESS_RANGE
                ) {
                    return@withContext Result.failure(
                        createApiException(
                            response =
                                response,
                            fallbackRequestId =
                                requestId
                        )
                    )
                }

                if (
                    response.body.isBlank()
                ) {
                    return@withContext Result.failure(
                        SangfroidApiException(
                            statusCode =
                                response.statusCode,
                            message =
                                "The Sangfroid API returned an empty response.",
                            responseBody =
                                response.body
                        )
                    )
                }

                Result.success(
                    parser(
                        JSONObject(
                            response.body
                        )
                    )
                )
            } catch (
                cancellation: CancellationException
            ) {
                throw cancellation
            } catch (
                timeout: SocketTimeoutException
            ) {
                Log.e(
                    LOG_TAG,
                    "Socket timeout. " +
                            "path=$path " +
                            "requestId=$requestId " +
                            "readTimeoutMs=${timeoutPolicy.readTimeoutMillis}",
                    timeout
                )

                Result.failure(
                    SangfroidNetworkException(
                        message =
                            timeoutMessage(
                                path =
                                    path
                            ),
                        cause =
                            timeout
                    )
                )
            } catch (
                connectException: ConnectException
            ) {
                Result.failure(
                    SangfroidNetworkException(
                        message =
                            "Could not connect to the Sangfroid backend at ${configuredBaseUrl()}.",
                        cause =
                            connectException
                    )
                )
            } catch (
                unknownHost: UnknownHostException
            ) {
                Result.failure(
                    SangfroidNetworkException(
                        message =
                            "The Sangfroid backend host could not be resolved.",
                        cause =
                            unknownHost
                    )
                )
            } catch (
                noRoute: NoRouteToHostException
            ) {
                Result.failure(
                    SangfroidNetworkException(
                        message =
                            "There is no network route to the Sangfroid backend.",
                        cause =
                            noRoute
                    )
                )
            } catch (
                jsonException: JSONException
            ) {
                Result.failure(
                    SangfroidResponseException(
                        message =
                            "The Sangfroid service returned an invalid response.",
                        cause =
                            jsonException
                    )
                )
            } catch (
                securityException: SecurityException
            ) {
                Result.failure(
                    SangfroidNetworkException(
                        message =
                            "Android blocked the connection to the Sangfroid backend.",
                        cause =
                            securityException
                    )
                )
            } catch (
                ioException: IOException
            ) {
                Result.failure(
                    SangfroidNetworkException(
                        message =
                            "A network error occurred while contacting Sangfroid: " +
                                    (
                                            ioException.message
                                                ?: ioException::class.java.simpleName
                                            ),
                        cause =
                            ioException
                    )
                )
            } catch (
                throwable: Throwable
            ) {
                Log.e(
                    LOG_TAG,
                    "Unexpected API failure. path=$path requestId=$requestId",
                    throwable
                )

                Result.failure(
                    throwable
                )
            }
        }
    }

    private suspend fun performWithRetry(
        path: String,
        requestId: String,
        requestBody: JSONObject,
        accessToken: String?,
        timeoutPolicy: TimeoutPolicy,
        retryPolicy: RetryPolicy
    ): HttpResponse {
        var attempt =
            1

        while (true) {
            try {
                val response =
                    performPost(
                        path =
                            path,
                        requestId =
                            requestId,
                        requestBody =
                            requestBody,
                        accessToken =
                            accessToken,
                        timeoutPolicy =
                            timeoutPolicy
                    )

                if (
                    attempt >=
                    retryPolicy.maximumAttempts ||
                    !response.statusCode
                        .isRetryableStatus()
                ) {
                    return response
                }

                val retryDelay =
                    retryPolicy.delayFor(
                        failedAttempt =
                            attempt
                    )

                Log.w(
                    LOG_TAG,
                    "Retryable HTTP response. " +
                            "path=$path " +
                            "status=${response.statusCode} " +
                            "requestId=$requestId " +
                            "attempt=$attempt " +
                            "retryInMs=$retryDelay"
                )

                delay(
                    retryDelay
                )

                attempt++
            } catch (
                cancellation: CancellationException
            ) {
                throw cancellation
            } catch (
                timeout: SocketTimeoutException
            ) {
                if (
                    attempt >=
                    retryPolicy.maximumAttempts
                ) {
                    throw timeout
                }

                delay(
                    retryPolicy.delayFor(
                        failedAttempt =
                            attempt
                    )
                )

                attempt++
            } catch (
                ioException: IOException
            ) {
                if (
                    attempt >=
                    retryPolicy.maximumAttempts ||
                    !ioException
                        .isRetryableTransportFailure()
                ) {
                    throw ioException
                }

                delay(
                    retryPolicy.delayFor(
                        failedAttempt =
                            attempt
                    )
                )

                attempt++
            }
        }
    }

    private fun performPost(
        path: String,
        requestId: String,
        requestBody: JSONObject,
        accessToken: String?,
        timeoutPolicy: TimeoutPolicy
    ): HttpResponse {
        val baseUrl =
            configuredBaseUrl()

        require(
            baseUrl.isNotBlank()
        ) {
            "SANGFROID_API_BASE_URL is not configured."
        }

        require(
            baseUrl.startsWith(
                "http://"
            ) ||
                    baseUrl.startsWith(
                        "https://"
                    )
        ) {
            "SANGFROID_API_BASE_URL must use HTTP or HTTPS."
        }

        val endpoint =
            "$baseUrl$path"

        val bodyBytes =
            requestBody
                .toString()
                .toByteArray(
                    Charsets.UTF_8
                )

        val connection =
            URL(
                endpoint
            ).openConnection() as
                    HttpURLConnection

        try {
            connection.requestMethod =
                HTTP_METHOD_POST

            connection.connectTimeout =
                timeoutPolicy
                    .connectTimeoutMillis

            connection.readTimeout =
                timeoutPolicy
                    .readTimeoutMillis

            connection.doInput =
                true

            connection.doOutput =
                true

            connection.useCaches =
                false

            connection.instanceFollowRedirects =
                false

            connection.setFixedLengthStreamingMode(
                bodyBytes.size
            )

            connection.setRequestProperty(
                HEADER_ACCEPT,
                CONTENT_TYPE_JSON
            )

            connection.setRequestProperty(
                HEADER_CONTENT_TYPE,
                CONTENT_TYPE_JSON_UTF8
            )

            connection.setRequestProperty(
                HEADER_REQUEST_ID,
                requestId
            )

            connection.setRequestProperty(
                HEADER_USER_AGENT,
                USER_AGENT_VALUE
            )

            if (
                !accessToken.isNullOrBlank()
            ) {
                connection.setRequestProperty(
                    HEADER_AUTHORIZATION,
                    "Bearer $accessToken"
                )
            }

            connection
                .outputStream
                .use { output ->
                    output.write(
                        bodyBytes
                    )

                    output.flush()
                }

            val statusCode =
                connection.responseCode

            val responseStream =
                if (
                    statusCode in
                    HTTP_SUCCESS_RANGE
                ) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            return HttpResponse(
                statusCode =
                    statusCode,
                body =
                    responseStream.readUtf8(),
                requestId =
                    connection
                        .getHeaderField(
                            HEADER_REQUEST_ID
                        )
                        ?.trim()
                        ?.takeIf(
                            String::isNotBlank
                        )
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun configuredBaseUrl():
            String {
        return BuildConfig
            .SANGFROID_API_BASE_URL
            .trim()
            .trimEnd(
                '/'
            )
    }

    private fun timeoutMessage(
        path: String
    ): String {
        return when (path) {
            QUESTION_GENERATION_PATH ->
                "Fresh-question generation is taking longer than expected. " +
                        "Your existing questions are safe. Try again in a moment."

            QUESTION_IMPORT_PATH ->
                "Question extraction is taking longer than expected. " +
                        "Your existing questions are safe. Try again in a moment."

            CV_ANALYSIS_PATH ->
                "CV analysis is taking longer than expected. Try again in a moment."

            else ->
                "The Sangfroid service took too long to respond."
        }
    }

    private fun createApiException(
        response: HttpResponse,
        fallbackRequestId: String
    ): SangfroidApiException {
        val errorJson =
            runCatching {
                JSONObject(
                    response.body
                )
            }.getOrNull()

        val errorCode =
            errorJson
                ?.optString(
                    JSON_ERROR_CODE
                )
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )

        val serverMessage =
            errorJson
                ?.optString(
                    JSON_ERROR_MESSAGE
                )
                ?.trim()
                ?.takeIf(
                    String::isNotBlank
                )

        val responseRequestId =
            response.requestId
                ?: errorJson
                    ?.optString(
                        JSON_REQUEST_ID
                    )
                    ?.trim()
                    ?.takeIf(
                        String::isNotBlank
                    )
                ?: fallbackRequestId

        val baseMessage =
            serverMessage
                ?: defaultErrorMessage(
                    response.statusCode
                )

        return SangfroidApiException(
            statusCode =
                response.statusCode,
            message =
                buildString {
                    append(
                        baseMessage
                    )

                    if (
                        !errorCode.isNullOrBlank()
                    ) {
                        append(
                            " [$errorCode]"
                        )
                    }

                    if (
                        responseRequestId.isNotBlank()
                    ) {
                        append(
                            " Request: $responseRequestId"
                        )
                    }
                },
            responseBody =
                response.body
        )
    }

    private fun defaultErrorMessage(
        statusCode: Int
    ): String {
        return when (statusCode) {
            HttpURLConnection.HTTP_BAD_REQUEST ->
                "The Sangfroid request was rejected."

            HttpURLConnection.HTTP_UNAUTHORIZED ->
                "Authentication is required."

            HttpURLConnection.HTTP_FORBIDDEN ->
                "The current account cannot perform this action."

            HttpURLConnection.HTTP_NOT_FOUND ->
                "The Sangfroid API endpoint was not found."

            HttpURLConnection.HTTP_CLIENT_TIMEOUT ->
                "The Sangfroid request timed out."

            HTTP_UNPROCESSABLE_ENTITY ->
                "The Sangfroid request contains invalid data."

            HTTP_TOO_MANY_REQUESTS ->
                "Too many AI requests were submitted. Try again shortly."

            HttpURLConnection.HTTP_INTERNAL_ERROR ->
                "The Sangfroid service encountered an internal error."

            HttpURLConnection.HTTP_BAD_GATEWAY ->
                "The Qwen service could not complete the request."

            HttpURLConnection.HTTP_UNAVAILABLE ->
                "The Sangfroid service is temporarily unavailable."

            HttpURLConnection.HTTP_GATEWAY_TIMEOUT ->
                "The Qwen service took too long to respond."

            else ->
                "The Sangfroid API returned HTTP $statusCode."
        }
    }

    private fun Int.isRetryableStatus():
            Boolean {
        return this ==
                HttpURLConnection.HTTP_CLIENT_TIMEOUT ||
                this ==
                HTTP_TOO_MANY_REQUESTS ||
                this ==
                HttpURLConnection.HTTP_INTERNAL_ERROR ||
                this ==
                HttpURLConnection.HTTP_BAD_GATEWAY ||
                this ==
                HttpURLConnection.HTTP_UNAVAILABLE ||
                this ==
                HttpURLConnection.HTTP_GATEWAY_TIMEOUT
    }

    private fun IOException.isRetryableTransportFailure():
            Boolean {
        return this is
                SocketTimeoutException ||
                this is
                        ConnectException ||
                this is
                        NoRouteToHostException ||
                this is
                        UnknownHostException
    }

    private fun InputStream?.readUtf8():
            String {
        if (
            this == null
        ) {
            return ""
        }

        return bufferedReader(
            Charsets.UTF_8
        ).use { reader ->
            reader.readText()
        }
    }

    private data class HttpResponse(
        val statusCode: Int,
        val body: String,
        val requestId: String?
    )

    private sealed class TimeoutPolicy(
        val connectTimeoutMillis: Int,
        val readTimeoutMillis: Int
    ) {

        data object Standard :
            TimeoutPolicy(
                connectTimeoutMillis =
                    20_000,
                readTimeoutMillis =
                    90_000
            )

        data object LongRunning :
            TimeoutPolicy(
                connectTimeoutMillis =
                    20_000,
                readTimeoutMillis =
                    240_000
            )
    }

    private sealed class RetryPolicy(
        val maximumAttempts: Int
    ) {

        abstract fun delayFor(
            failedAttempt: Int
        ): Long

        data object None :
            RetryPolicy(
                maximumAttempts =
                    1
            ) {

            override fun delayFor(
                failedAttempt: Int
            ): Long {
                return 0L
            }
        }

        data object Standard :
            RetryPolicy(
                maximumAttempts =
                    2
            ) {

            override fun delayFor(
                failedAttempt: Int
            ): Long {
                return when (
                    failedAttempt
                ) {
                    1 ->
                        1_000L

                    else ->
                        2_500L
                }
            }
        }
    }

    companion object {
        private const val LOG_TAG =
            "SangfroidApi"

        private const val EVALUATION_PATH =
            "/v1/evaluations"

        private const val QUESTION_GENERATION_PATH =
            "/v1/questions/generate"

        private const val QUESTION_IMPORT_PATH =
            "/v1/questions/import"

        private const val CV_ANALYSIS_PATH =
            "/v1/cv/analyze"

        private const val HTTP_METHOD_POST =
            "POST"

        private const val HEADER_ACCEPT =
            "Accept"

        private const val HEADER_CONTENT_TYPE =
            "Content-Type"

        private const val HEADER_AUTHORIZATION =
            "Authorization"

        private const val HEADER_REQUEST_ID =
            "X-Request-Id"

        private const val HEADER_USER_AGENT =
            "User-Agent"

        private const val CONTENT_TYPE_JSON =
            "application/json"

        private const val CONTENT_TYPE_JSON_UTF8 =
            "application/json; charset=utf-8"

        private const val USER_AGENT_VALUE =
            "Sangfroid-Android/1.0"

        private const val HTTP_UNPROCESSABLE_ENTITY =
            422

        private const val HTTP_TOO_MANY_REQUESTS =
            429

        private const val JSON_ERROR_CODE =
            "code"

        private const val JSON_ERROR_MESSAGE =
            "message"

        private const val JSON_REQUEST_ID =
            "requestId"

        private val HTTP_SUCCESS_RANGE =
            200..299
    }
}

class SangfroidNetworkException(
    override val message: String,
    override val cause: Throwable? = null
) : IOException(
    message,
    cause
)

class SangfroidResponseException(
    override val message: String,
    override val cause: Throwable? = null
) : IllegalStateException(
    message,
    cause
)