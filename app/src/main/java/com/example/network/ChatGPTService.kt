package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class ChatGPTService {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        
    private val jsonAdapter = moshi.adapter(UsageResponse::class.java)

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    @Throws(Exception::class)
    suspend fun fetchUsage(
        authToken: String,
        cookies: String,
        userAgent: String
    ): UsageResponse = withContext(Dispatchers.IO) {
        val bearerToken = if (authToken.trim().startsWith("Bearer ")) {
            authToken.trim()
        } else {
            "Bearer ${authToken.trim()}"
        }
        
        val targetUserAgent = if (userAgent.trim().isNotEmpty()) {
            userAgent.trim()
        } else {
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36"
        }

        val request = Request.Builder()
            .url("https://chatgpt.com/backend-api/wham/usage")
            .header("accept", "*/*")
            .header("accept-language", "es-US,es-419;q=0.9,es;q=0.8")
            .header("authorization", bearerToken)
            .header("cookie", cookies.trim())
            .header("oai-client-build-number", "7748709")
            .header("oai-client-version", "prod-0ec16b465a393744359db32717d5183e41cdd4ee")
            .header("oai-device-id", "28fafdbd-ece8-4599-8289-762873703360")
            .header("priority", "u=1, i")
            .header("referer", "https://chatgpt.com/codex/cloud/settings/analytics")
            .header("sec-ch-ua", "\"Google Chrome\";v=\"149\", \"Chromium\";v=\"149\", \"Not)A;Brand\";v=\"24\"")
            .header("sec-ch-ua-mobile", "?0")
            .header("sec-ch-ua-platform", "\"Linux\"")
            .header("sec-fetch-dest", "empty")
            .header("sec-fetch-mode", "cors")
            .header("sec-fetch-site", "same-origin")
            .header("user-agent", targetUserAgent)
            .header("x-openai-target-path", "/backend-api/wham/usage")
            .header("x-openai-target-route", "/backend-api/wham/usage")
            .build()

        client.newCall(request).execute().use { response ->
            val bodyString = response.body?.string() ?: throw IOException("Cuerpo de respuesta vacío")
            if (!response.isSuccessful) {
                throw IOException("Código de error HTTP: ${response.code}. Servidor respondió: $bodyString")
            }
            return@withContext jsonAdapter.fromJson(bodyString) ?: throw IOException("Error al analizar el JSON de respuesta")
        }
    }
}
