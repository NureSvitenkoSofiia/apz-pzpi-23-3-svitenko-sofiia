package com.printing.app.data.remote

import com.printing.app.data.local.TokenStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.gson.gson

// Emulator → host machine. Real device → PC's LAN IP, e.g. 192.168.1.X
const val BASE_URL = "http://10.0.2.2:8080"

fun buildHttpClient(tokenStore: TokenStore) = HttpClient(Android) {

    install(ContentNegotiation) {
        // GsonBuilder config:
        //  setLenient()      — tolerate minor JSON deviations
        //  serializeNulls()  — include null fields in serialised output
        gson {
            setLenient()
            serializeNulls()
        }
    }

    install(Auth) {
        bearer {
            // Always attach the token without waiting for a 401 challenge
            sendWithoutRequest { true }
            loadTokens {
                val token = tokenStore.getToken() ?: return@loadTokens null
                BearerTokens(token, "")
            }
        }
    }

    defaultRequest {
        url(BASE_URL)
    }

    engine {
        connectTimeout = 15_000
        socketTimeout  = 15_000
    }
}
