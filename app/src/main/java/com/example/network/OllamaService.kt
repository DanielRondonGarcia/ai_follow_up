package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class OllamaService {
    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    @Throws(Exception::class)
    suspend fun fetchUsage(
        cookies: String,
        userAgent: String
    ): OllamaUsageResponse = withContext(Dispatchers.IO) {
        val targetUserAgent = if (userAgent.trim().isNotEmpty()) {
            userAgent.trim()
        } else {
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36"
        }

        val request = Request.Builder()
            .url("https://ollama.com/settings")
            .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .header("accept-language", "en-US,en;q=0.5")
            .header("cookie", cookies.trim())
            .header("user-agent", targetUserAgent)
            .build()

        client.newCall(request).execute().use { response ->
            val bodyString = response.body?.string() ?: throw IOException("Cuerpo de respuesta vacío")
            if (!response.isSuccessful) {
                throw IOException("Código de error HTTP: ${response.code}")
            }
            return@withContext parseOllamaHtml(bodyString)
        }
    }

    fun parseOllamaHtml(html: String): OllamaUsageResponse {
        var email = "Ollama User"
        // Try to extract username or email from profile link if available
        val profileMatch = """href="/settings/profile"[^>]*>([^<]+)""".toRegex(RegexOption.IGNORE_CASE).find(html)
        if (profileMatch != null) {
            val name = profileMatch.groupValues[1].trim()
            if (name.isNotEmpty() && name != "Profile" && name != "Perfil") {
                email = name
            }
        }

        // 1. Session usage percent (e.g. 22% used)
        var sessionPercent = 0.0
        val ariaSessionMatch = """aria-label="Session usage\s+([\d.]+)\s*%""".toRegex(RegexOption.IGNORE_CASE).find(html)
        if (ariaSessionMatch != null) {
            sessionPercent = ariaSessionMatch.groupValues[1].toDoubleOrNull() ?: 0.0
        } else {
            val sessionMatch = """Session usage.*?([\d.]+)\s*%""".toRegex(RegexOption.DOT_MATCHES_ALL).find(html)
            if (sessionMatch != null) {
                sessionPercent = sessionMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            }
        }

        // 2. Weekly usage percent (e.g. 8.9% used)
        var weeklyPercent = 0.0
        val ariaWeeklyMatch = """aria-label="Weekly usage\s+([\d.]+)\s*%""".toRegex(RegexOption.IGNORE_CASE).find(html)
        if (ariaWeeklyMatch != null) {
            weeklyPercent = ariaWeeklyMatch.groupValues[1].toDoubleOrNull() ?: 0.0
        } else {
            val weeklyMatch = """Weekly usage.*?([\d.]+)\s*%""".toRegex(RegexOption.DOT_MATCHES_ALL).find(html)
            if (weeklyMatch != null) {
                weeklyPercent = weeklyMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            }
        }

        // 3. Resets in X (e.g. "Resets in 1 hour", "Resets in 4 days")
        val resetsMatches = """Resets in\s+([^<"]+)""".toRegex(RegexOption.IGNORE_CASE).findAll(html).map { it.groupValues[1].trim() }.toList()
        val sessionReset = if (resetsMatches.isNotEmpty()) "Resets in ${resetsMatches[0]}" else "Resets in 1 hour"
        val weeklyReset = if (resetsMatches.size > 1) "Resets in ${resetsMatches[1]}" else "Resets in 7 days"

        // 4. Extra balance
        var extraBalance = "$0"
        val balanceMatch = """Extra balance.*?\$([\d.]+)""".toRegex(RegexOption.DOT_MATCHES_ALL).find(html)
        if (balanceMatch != null) {
            extraBalance = "$${balanceMatch.groupValues[1]}"
        }

        // 5. Plan type (e.g. "pro")
        var planType = "Free"
        val planMatch = """Cloud usage</span>\s*<span[^>]*class="[^"]*capitalize[^"]*"[^>]*>\s*([^<]+)""".toRegex(RegexOption.IGNORE_CASE).find(html)
        if (planMatch != null) {
            planType = planMatch.groupValues[1].trim()
        } else if (html.contains("capitalize")) {
            if (html.lowercase().contains("pro")) planType = "Pro"
        }

        return OllamaUsageResponse(
            email = email,
            planType = planType,
            sessionPercent = sessionPercent,
            sessionReset = sessionReset,
            weeklyPercent = weeklyPercent,
            weeklyReset = weeklyReset,
            extraBalance = extraBalance
        )
    }
}

data class OllamaUsageResponse(
    val email: String,
    val planType: String,
    val sessionPercent: Double,
    val sessionReset: String,
    val weeklyPercent: Double,
    val weeklyReset: String,
    val extraBalance: String
)
