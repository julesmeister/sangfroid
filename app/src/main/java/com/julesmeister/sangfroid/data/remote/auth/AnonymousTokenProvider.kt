package com.julesmeister.sangfroid.data.remote.auth

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnonymousTokenProvider @Inject constructor() :
    AuthTokenProvider {

    override suspend fun getAccessToken(): String? {
        return null
    }

    override suspend fun refreshAccessToken(): Result<String?> {
        return Result.success(null)
    }

    override suspend fun clearSession() = Unit
}