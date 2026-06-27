package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

open class ChatGPTService {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        
    private val jsonAdapter = moshi.adapter(UsageResponse::class.java)

    protected open val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    protected open fun usageEndpoint(): String = "https://chatgpt.com/backend-api/wham/usage"

    suspend fun fetchUsage(
        authToken: String,
        cookies: String,
        userAgent: String,
        userId: String
    ): SyncResult<UsageResponse> = withContext(Dispatchers.IO) {
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
            .url(usageEndpoint())
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

        try {
            client.newCall(request).execute().use { response ->
                if (response.code == 401 || response.code == 403) {
                    return@withContext SyncResult.AuthExpired("OpenAI", userId)
                }
                if (!response.isSuccessful) {
                    return@withContext SyncResult.NetworkError(
                        IOException("Codigo de error HTTP: ${response.code}")
                    )
                }
                val bodyString = response.body?.string()
                    ?: return@withContext SyncResult.ParseError(
                        IOException("Cuerpo de respuesta vacio")
                    )
                val parsed = try {
                    jsonAdapter.fromJson(bodyString)
                } catch (e: Exception) {
                    return@withContext SyncResult.ParseError(e)
                }
                if (parsed == null) {
                    return@withContext SyncResult.ParseError(
                        IOException("Error al analizar el JSON de respuesta")
                    )
                }
                return@withContext SyncResult.Success(parsed)
            }
        } catch (e: IOException) {
            return@withContext SyncResult.NetworkError(e)
        }
    }
}
