package com.julesmeister.sangfroid.data.remote.auth

interface AuthTokenProvider {

    suspend fun getAccessToken(): String?

    suspend fun refreshAccessToken(): Result<String?>

    suspend fun clearSession()
}