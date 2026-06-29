package com.example.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.UUID

open class AnthropicService(
    private val anonymousId: String,
    private val deviceId: String
) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        
    private val jsonAdapter = moshi.adapter(AnthropicUsageResponse::class.java)

    protected open val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    protected open fun usageEndpoint(orgId: String): String =
        "https://claude.ai/api/organizations/${orgId.trim()}/usage"

    suspend fun fetchUsage(
        orgId: String,
        authToken: String, // can be the sessionKey, e.g. "sk-ant-..."
        cookies: String, // can be full cookies or empty
        userAgent: String
    ): SyncResult<AnthropicUsageResponse> = withContext(Dispatchers.IO) {

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
            else -> return@withContext SyncResult.NetworkError(
                IllegalArgumentException("Se requiere sessionKey o cookies para Anthropic")
            )
        }

        val targetUserAgent = if (userAgent.trim().isNotEmpty()) {
            userAgent.trim()
        } else {
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36"
        }

        val activitySessionId = UUID.randomUUID().toString()

        val request = Request.Builder()
            .url(usageEndpoint(orgId))
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

        try {
            client.newCall(request).execute().use { response ->
                if (response.code == 401 || response.code == 403) {
                    return@withContext SyncResult.AuthExpired("Anthropic", orgId)
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
                val parsed = jsonAdapter.fromJson(bodyString)
                if (parsed == null) {
                    return@withContext SyncResult.ParseError(
                        IOException("Error al analizar el JSON de respuesta")
                    )
                }
                return@withContext SyncResult.Success(parsed)
            }
        } catch (e: IOException) {
            return@withContext SyncResult.NetworkError(e)
        } catch (e: Exception) {
            return@withContext SyncResult.ParseError(e)
        }
    }
}
