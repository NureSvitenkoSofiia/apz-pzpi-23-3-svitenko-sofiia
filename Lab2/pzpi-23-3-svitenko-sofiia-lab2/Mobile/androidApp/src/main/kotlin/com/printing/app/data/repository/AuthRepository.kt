package com.printing.app.data.repository

import android.util.Log
import com.printing.app.data.local.TokenStore
import com.printing.app.data.remote.AuthApi

private const val TAG = "AuthRepo"

class AuthRepository(
    private val api: AuthApi,
    private val tokenStore: TokenStore
) {
    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val response = api.login(email, password)
        if (response.token.isEmpty()) error("Empty token — check credentials")
        Log.d(TAG, "Login OK: userId=${response.userId} role=${response.role}")
        tokenStore.saveAuth(response.token, response.userId, response.role)
    }

    fun logout() = tokenStore.clear()
    fun isLoggedIn() = tokenStore.isLoggedIn()
    fun getUserId() = tokenStore.getUserId()
}
