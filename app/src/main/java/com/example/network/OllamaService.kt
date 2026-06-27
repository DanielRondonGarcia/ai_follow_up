package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

open class OllamaService {
    protected open val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    protected open fun settingsEndpoint(): String = "https://ollama.com/settings"

    suspend fun fetchUsage(
        cookies: String,
        userAgent: String,
        email: String
    ): SyncResult<OllamaUsageResponse> = withContext(Dispatchers.IO) {
        val targetUserAgent = if (userAgent.trim().isNotEmpty()) {
            userAgent.trim()
        } else {
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/149.0.0.0 Safari/537.36"
        }

        val request = Request.Builder()
            .url(settingsEndpoint())
            .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .header("accept-language", "en-US,en;q=0.5")
            .header("cookie", cookies.trim())
            .header("user-agent", targetUserAgent)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext SyncResult.NetworkError(
                        IOException("Codigo de error HTTP: ${response.code}")
                    )
                }
                val bodyString = response.body?.string()
                    ?: return@withContext SyncResult.ParseError(
                        IOException("Cuerpo de respuesta vacio")
                    )
                if (isOllamaSessionExpired(bodyString)) {
                    return@withContext SyncResult.AuthExpired("Ollama", email)
                }
                val parsed = try {
                    parseOllamaHtml(bodyString)
                } catch (e: Exception) {
                    return@withContext SyncResult.ParseError(e)
                }
                return@withContext SyncResult.Success(parsed)
            }
        } catch (e: IOException) {
            return@withContext SyncResult.NetworkError(e)
        } catch (e: Exception) {
            return@withContext SyncResult.ParseError(e)
        }
    }

    /**
     * Pure predicate: classifies an Ollama /settings HTML body as a login page
     * (session expired) vs a valid settings dashboard. No network needed.
     *
     * A page is considered expired when it does NOT contain the two aria-labels
     * the parser keys on ("Session usage" and "Weekly usage"), OR it contains a
     * sign-in marker (/signin path or a password input). Empty or garbage input
     * returns false so the caller falls through to ParseError, not AuthExpired.
     */
    fun isOllamaSessionExpired(html: String): Boolean {
        if (html.isBlank()) return false
        val hasSession = html.contains("aria-label=\"Session usage", ignoreCase = true)
        val hasWeekly = html.contains("aria-label=\"Weekly usage", ignoreCase = true)
        val hasSignin = html.contains("/signin") ||
            html.contains("name=\"password\"") ||
            html.contains("type=\"password\"")
        return (!hasSession && !hasWeekly) || hasSignin
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
