package com.printing.app.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

// ASP.NET Core serialises PascalCase → camelCase by default (JsonNamingPolicy.CamelCase)
// Gson matches by field name → all Kotlin fields must be camelCase to match

data class LoginRequestDto(
    val email: String = "",
    val password: String = ""
)

data class LoginResponseDto(
    val token: String = "",
    val userId: Int = -1,
    val email: String = "",
    val role: String = "",
    val expiresAt: String = ""
)

class AuthApi(private val client: HttpClient) {
    // POST /api/auth/login → LoginResponse (no BaseResponse wrapper)
    suspend fun login(email: String, password: String): LoginResponseDto =
        client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(email, password))
        }.body()
}
