package com.printing.app.data.local

import android.content.Context

class TokenStore(context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun saveAuth(token: String, userId: Int, role: String) {
        prefs.edit()
            .putString("token", token)
            .putInt("userId", userId)
            .putString("role", role)
            .apply()
    }

    fun getToken(): String? = prefs.getString("token", null)
    fun getUserId(): Int = prefs.getInt("userId", -1)
    fun getRole(): String? = prefs.getString("role", null)
    fun isLoggedIn(): Boolean = getToken() != null

    fun clear() {
        prefs.edit().clear().apply()
    }
}
