package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.UUID

class AnthropicService {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        
    private val jsonAdapter = moshi.adapter(AnthropicUsageResponse::class.java)

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    @Throws(Exception::class)
    suspend fun fetchUsage(
        orgId: String,
        authToken: String, // can be the sessionKey, e.g. "sk-ant-..."
        cookies: String, // can be full cookies or empty
        userAgent: String
    ): AnthropicUsageResponse = withContext(Dispatchers.IO) {
        
        // Prepare cookies
        val finalCookie = when {
            cookies.trim().isNotEmpty() -> {
                // If the user pasted full cookies, use them. 
                // But also ensure if sessionKey is in authToken but not in cookies, we append/override it.
                if (!cookies.contains("sessionKey") && authToken.trim().isNotEmpty()) {
                    val key = if (authToken.contains("sessionKey=")) authToken.trim() else "sessionKey=${authToken.trim()}"
                    if (cookies.trim().endsWith(";")) "${cookies.trim()} $key" else "${cookies.trim()}; $key"
                } else {
                    cookies.trim()
                }
            }
            authToken.trim().isNotEmpty() -> {
                if (authToken.contains("sessionKey=")) authToken.trim() else "sessionKey=${authToken.trim()}"
            }
            else -> throw IllegalArgumentException("Se requiere sessionKey o cookies para Anthropic")
        }

        val targetUserAgent = if (userAgent.trim().isNotEmpty()) {
            userAgent.trim()
        } else {
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36"
        }

        val activitySessionId = UUID.randomUUID().toString()
        val anonymousId = "claudeai.v1.cb50e252-0327-4bba-b452-fa84e9499371"
        val deviceId = "684618c5-5be1-4dd4-976a-5d9bb876b224"

        val request = Request.Builder()
            .url("https://claude.ai/api/organizations/${orgId.trim()}/usage")
            .header("accept", "*/*")
            .header("accept-language", "es-US,es-419;q=0.9,es;q=0.8")
            .header("anthropic-anonymous-id", anonymousId)
            .header("anthropic-client-platform", "web_claude_ai")
            .header("anthropic-client-sha", "502918bd43fee6de58fb8d08ba37418435388129")
            .header("anthropic-client-version", "1.0.0")
            .header("anthropic-device-id", deviceId)
            .header("content-type", "application/json")
            .header("cookie", finalCookie)
            .header("priority", "u=1, i")
            .header("referer", "https://claude.ai/new")
            .header("sec-ch-ua", "\"Google Chrome\";v=\"149\", \"Chromium\";v=\"149\", \"Not)A;Brand\";v=\"24\"")
            .header("sec-ch-ua-mobile", "?0")
            .header("sec-ch-ua-platform", "\"Linux\"")
            .header("sec-fetch-dest", "empty")
            .header("sec-fetch-mode", "cors")
            .header("sec-fetch-site", "same-origin")
            .header("user-agent", targetUserAgent)
            .header("x-activity-session-id", activitySessionId)
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
