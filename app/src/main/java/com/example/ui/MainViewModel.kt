package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Account
import com.example.data.AppDatabase
import com.example.data.UsageLog
import com.example.data.UsageRepository
import com.example.network.ChatGPTService
import com.example.network.UsageResponse
import com.example.network.AnthropicService
import com.example.network.AnthropicUsageResponse
import com.example.network.OllamaService
import com.example.network.OllamaUsageResponse
import com.example.network.SyncResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = UsageRepository(database)
    private val service = ChatGPTService()
    private val anthropicService = AnthropicService()
    private val ollamaService = OllamaService()

    // UI state flows
    val allAccounts: StateFlow<List<Account>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAccount: StateFlow<Account?> = repository.activeAccountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allLogs: StateFlow<List<UsageLog>> = repository.allLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Latest [UsageLog] per account id, derived once from [allLogs].
     *
     * Replaces the per-card `allLogs.filter { it.accountId == account.id }.maxByOrNull { it.timestamp }`
     * lookup (O(n) per item, O(n*m) per recomposition) with a single O(n)
     * derivation followed by an O(1) map lookup per card.
     */
    val latestLogByAccount: StateFlow<Map<Int, UsageLog?>> = allLogs
        .map { logs -> latestLogPerAccount(logs) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    val usageLogs: StateFlow<List<UsageLog>> = activeAccount
        .flatMapLatest { account ->
            if (account != null) {
                repository.getLogsForAccount(account.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showLoginWebView = MutableStateFlow(false)
    val showLoginWebView: StateFlow<Boolean> = _showLoginWebView.asStateFlow()

    private val _loginProvider = MutableStateFlow("OpenAI")
    val loginProvider: StateFlow<String> = _loginProvider.asStateFlow()

    private val _showAddManualDialog = MutableStateFlow(false)
    val showAddManualDialog: StateFlow<Boolean> = _showAddManualDialog.asStateFlow()

    private val _expiredAccounts = MutableStateFlow<Set<Int>>(emptySet())
    val expiredAccounts: StateFlow<Set<Int>> = _expiredAccounts.asStateFlow()

    private var pendingReAuthAccountId: Int? = null

    // Automatically sync on startup if an active account is present
    init {
        viewModelScope.launch {
            activeAccount.collectLatest { account ->
                if (account != null && _errorMessage.value == null && usageLogs.value.isEmpty()) {
                    syncActiveAccount()
                }
            }
        }
    }

    fun setShowLoginWebView(show: Boolean) {
        _showLoginWebView.value = show
    }

    fun startLoginWebView(provider: String) {
        _loginProvider.value = provider
        _showLoginWebView.value = true
    }

    /**
     * Re-auth entry point. Opens the WebView for the account's provider and
     * remembers which account id is pending so the success callback upserts
     * into the existing row (by provider+userId) instead of inserting a new one.
     */
    fun startReAuth(accountId: Int, provider: String) {
        pendingReAuthAccountId = accountId
        startLoginWebView(provider)
    }

    fun setShowAddManualDialog(show: Boolean) {
        _showAddManualDialog.value = show
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun syncActiveAccount() {
        val account = activeAccount.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                syncOneAccount(account)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching usage", e)
                _errorMessage.value = e.localizedMessage ?: "Error de red desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Syncs a single account by dispatching to the provider-specific service.
     *
     * Encapsulates the `when (account.provider)` block that was previously
     * duplicated between [syncActiveAccount] and [syncAllAccounts]. Does NOT
     * manage [_isLoading] or [_errorMessage]; the caller owns the loading
     * lifecycle. Throws on network/parse errors so the caller's try/catch can
     * translate them into a user-facing message.
     */
    private suspend fun syncOneAccount(account: Account) {
        when (account.provider) {
            "Ollama" -> {
                val result = ollamaService.fetchUsage(
                    cookies = account.cookies,
                    userAgent = account.userAgent,
                    email = account.userId
                )
                handleSyncResult(account, result) { response ->
                    val logEntry = mapOllamaResponseToLog(account.id, response)
                    repository.insertLog(logEntry)
                    account.copy(
                        email = response.email,
                        planType = response.planType,
                        lastUpdated = System.currentTimeMillis()
                    )
                }
            }
            "Anthropic" -> {
                val result = anthropicService.fetchUsage(
                    orgId = account.userId,
                    authToken = account.authToken,
                    cookies = account.cookies,
                    userAgent = account.userAgent
                )
                handleSyncResult(account, result) { response ->
                    val logEntry = mapAnthropicResponseToLog(account.id, response)
                    repository.insertLog(logEntry)
                    account.copy(
                        lastUpdated = System.currentTimeMillis()
                    )
                }
            }
            else -> {
                val result = service.fetchUsage(
                    authToken = account.authToken,
                    cookies = account.cookies,
                    userAgent = account.userAgent,
                    userId = account.userId
                )
                handleSyncResult(account, result) { response ->
                    val logEntry = mapResponseToLog(account.id, response)
                    repository.insertLog(logEntry)
                    account.copy(
                        email = response.email,
                        planType = response.planType,
                        lastUpdated = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    /**
     * Syncs one account by id, identified from the dashboard list.
     *
     * Distinct from [syncActiveAccount] (syncs the active account) and
     * [syncAllAccounts] (syncs every account). Used by the per-card sync
     * IconButton introduced in PR 2. Returns early if the account id is no
     * longer present. Manages [_isLoading] around the work via try/finally.
     */
    fun syncAccount(accountId: Int) {
        val account = allAccounts.value.find { it.id == accountId } ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                syncOneAccount(account)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error syncing account ${account.email}", e)
                _errorMessage.value = e.localizedMessage ?: "Error de red desconocido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Translates a SyncResult into account update + expired-set management.
     * On Success: updates the account, clears the id from expiredAccounts.
     * On AuthExpired: adds the id to expiredAccounts (no error message).
     * On NetworkError/ParseError: sets a generic error message.
     */
    private suspend fun <T> handleSyncResult(
        account: Account,
        result: SyncResult<T>,
        onSuccess: suspend (T) -> Account,
    ) {
        when (result) {
            is SyncResult.Success -> {
                val updatedAccount = onSuccess(result.data)
                repository.updateAccount(updatedAccount)
                _expiredAccounts.value = _expiredAccounts.value - account.id
                _errorMessage.value = null
            }
            is SyncResult.AuthExpired -> {
                _expiredAccounts.value = _expiredAccounts.value + account.id
                _errorMessage.value = null
            }
            is SyncResult.NetworkError -> {
                _errorMessage.value = result.cause.localizedMessage ?: "Error de red desconocido"
            }
            is SyncResult.ParseError -> {
                _errorMessage.value = result.cause.localizedMessage ?: "Error al analizar la respuesta"
            }
        }
    }

    fun syncAllAccounts(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val accountsList = allAccounts.value
                for (account in accountsList) {
                    try {
                        syncOneAccount(account)
                    } catch (e: Exception) {
                        Log.e("MainViewModel", "Error syncAllAccounts para ${account.email}", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error general en syncAllAccounts", e)
                _errorMessage.value = e.localizedMessage ?: "Error al actualizar agentes"
            } finally {
                _isLoading.value = false
                onComplete?.invoke()
            }
        }
    }

    fun handleWebViewLoginSuccess(provider: String, authToken: String, cookies: String, userAgent: String, userId: String) {
        _showLoginWebView.value = false
        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            try {
                if (provider == "Ollama") {
                    val result = ollamaService.fetchUsage(
                        cookies = cookies,
                        userAgent = userAgent,
                        email = userId
                    )
                    when (result) {
                        is SyncResult.Success -> {
                            val response = result.data
                            val newId = repository.upsertAccount(
                                provider = "Ollama",
                                userId = response.email,
                                email = response.email,
                                authToken = "",
                                cookies = cookies,
                                userAgent = userAgent,
                                planType = response.planType,
                            )
                            repository.setActiveAccount(newId.toInt())
                            val logEntry = mapOllamaResponseToLog(newId.toInt(), response)
                            repository.insertLog(logEntry)
                            clearPendingReAuth(newId.toInt())
                        }
                        is SyncResult.AuthExpired -> {
                            saveExpiredFallback(provider, authToken, cookies, userAgent, userId)
                        }
                        is SyncResult.NetworkError -> {
                            saveExpiredFallback(provider, authToken, cookies, userAgent, userId)
                            _errorMessage.value = "Se guardo la sesion, pero falla la sincronizacion inicial: ${result.cause.localizedMessage}"
                        }
                        is SyncResult.ParseError -> {
                            saveExpiredFallback(provider, authToken, cookies, userAgent, userId)
                            _errorMessage.value = "Se guardo la sesion, pero falla la sincronizacion inicial: ${result.cause.localizedMessage}"
                        }
                    }
                } else if (provider == "Anthropic") {
                    val result = anthropicService.fetchUsage(
                        orgId = userId,
                        authToken = authToken,
                        cookies = cookies,
                        userAgent = userAgent
                    )
                    when (result) {
                        is SyncResult.Success -> {
                            val response = result.data
                            val newId = repository.upsertAccount(
                                provider = "Anthropic",
                                userId = userId,
                                email = "Claude Account",
                                authToken = authToken,
                                cookies = cookies,
                                userAgent = userAgent,
                                planType = "Pro",
                            )
                            repository.setActiveAccount(newId.toInt())
                            val logEntry = mapAnthropicResponseToLog(newId.toInt(), response)
                            repository.insertLog(logEntry)
                            clearPendingReAuth(newId.toInt())
                        }
                        is SyncResult.AuthExpired -> {
                            saveExpiredFallback(provider, authToken, cookies, userAgent, userId)
                        }
                        is SyncResult.NetworkError -> {
                            saveExpiredFallback(provider, authToken, cookies, userAgent, userId)
                            _errorMessage.value = "Se guardo la sesion, pero falla la sincronizacion inicial: ${result.cause.localizedMessage}"
                        }
                        is SyncResult.ParseError -> {
                            saveExpiredFallback(provider, authToken, cookies, userAgent, userId)
                            _errorMessage.value = "Se guardo la sesion, pero falla la sincronizacion inicial: ${result.cause.localizedMessage}"
                        }
                    }
                } else {
                    // OpenAI
                    val result = service.fetchUsage(
                        authToken = authToken,
                        cookies = cookies,
                        userAgent = userAgent,
                        userId = userId
                    )
                    when (result) {
                        is SyncResult.Success -> {
                            val response = result.data
                            val newId = repository.upsertAccount(
                                provider = "OpenAI",
                                userId = response.userId,
                                email = response.email,
                                authToken = authToken,
                                cookies = cookies,
                                userAgent = userAgent,
                                planType = response.planType,
                            )
                            repository.setActiveAccount(newId.toInt())
                            val logEntry = mapResponseToLog(newId.toInt(), response)
                            repository.insertLog(logEntry)
                            clearPendingReAuth(newId.toInt())
                        }
                        is SyncResult.AuthExpired -> {
                            saveExpiredFallback(provider, authToken, cookies, userAgent, userId)
                        }
                        is SyncResult.NetworkError -> {
                            saveExpiredFallback(provider, authToken, cookies, userAgent, userId)
                            _errorMessage.value = "Se guardo la sesion, pero falla la sincronizacion inicial: ${result.cause.localizedMessage}"
                        }
                        is SyncResult.ParseError -> {
                            saveExpiredFallback(provider, authToken, cookies, userAgent, userId)
                            _errorMessage.value = "Se guardo la sesion, pero falla la sincronizacion inicial: ${result.cause.localizedMessage}"
                        }
                    }
                }

                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error during web login handling", e)
                saveExpiredFallback(provider, authToken, cookies, userAgent, userId)
                _errorMessage.value = "Se guardo la sesion, pero falla la sincronizacion inicial: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Fallback when the initial sync fails but the credentials should still be
     * saved so the user doesn't lose them. Uses upsert so a re-auth updates the
     * existing row instead of inserting a duplicate.
     */
    private suspend fun saveExpiredFallback(
        provider: String,
        authToken: String,
        cookies: String,
        userAgent: String,
        userId: String,
    ) {
        val fallbackEmail = when (provider) {
            "Ollama" -> userId.ifEmpty { "ollama_user_${System.currentTimeMillis() % 10000}" }
            "Anthropic" -> "Claude Account"
            else -> "chatgpt_user_${System.currentTimeMillis() % 10000}@openai.com"
        }
        val fallbackUserId = when (provider) {
            "Ollama" -> userId.ifEmpty { "ollama_user_${System.currentTimeMillis() % 10000}" }
            "Anthropic" -> userId.ifEmpty { "unknown_claude_org" }
            else -> userId.ifEmpty { "unknown_user" }
        }
        val fallbackPlan = when (provider) {
            "Anthropic" -> "Pro"
            "Ollama" -> "Free"
            else -> "unknown"
        }
        val newId = repository.upsertAccount(
            provider = provider,
            userId = fallbackUserId,
            email = fallbackEmail,
            authToken = authToken,
            cookies = cookies,
            userAgent = userAgent,
            planType = fallbackPlan,
        )
        repository.setActiveAccount(newId.toInt())
        clearPendingReAuth(newId.toInt())
    }

    /**
     * Clears the pending re-auth slot and removes the account id from the
     * expired set after a successful upsert.
     */
    private fun clearPendingReAuth(accountId: Int) {
        if (pendingReAuthAccountId == accountId) {
            pendingReAuthAccountId = null
        }
        _expiredAccounts.value = _expiredAccounts.value - accountId
    }

    fun handleManualAccountAdd(provider: String, email: String, authToken: String, cookies: String, userAgent: String, userId: String) {
        _showAddManualDialog.value = false
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                if (provider == "Ollama") {
                    val result = ollamaService.fetchUsage(
                        cookies = cookies,
                        userAgent = userAgent,
                        email = userId
                    )
                    when (result) {
                        is SyncResult.Success -> {
                            val response = result.data
                            val newId = repository.upsertAccount(
                                provider = "Ollama",
                                userId = response.email,
                                email = email.ifEmpty { response.email },
                                authToken = "",
                                cookies = cookies,
                                userAgent = userAgent,
                                planType = response.planType,
                            )
                            repository.setActiveAccount(newId.toInt())
                            val logEntry = mapOllamaResponseToLog(newId.toInt(), response)
                            repository.insertLog(logEntry)
                        }
                        else -> {
                            throw IOException("No se pudo validar la cuenta de Ollama")
                        }
                    }
                } else if (provider == "Anthropic") {
                    val result = anthropicService.fetchUsage(
                        orgId = userId,
                        authToken = authToken,
                        cookies = cookies,
                        userAgent = userAgent
                    )
                    when (result) {
                        is SyncResult.Success -> {
                            val response = result.data
                            val newId = repository.upsertAccount(
                                provider = "Anthropic",
                                userId = userId,
                                email = email.ifEmpty { "Claude Account" },
                                authToken = authToken,
                                cookies = cookies,
                                userAgent = userAgent,
                                planType = "Pro",
                            )
                            repository.setActiveAccount(newId.toInt())
                            val logEntry = mapAnthropicResponseToLog(newId.toInt(), response)
                            repository.insertLog(logEntry)
                        }
                        else -> {
                            throw IOException("No se pudo validar la cuenta de Anthropic")
                        }
                    }
                } else {
                    // OpenAI
                    val result = service.fetchUsage(
                        authToken = authToken,
                        cookies = cookies,
                        userAgent = userAgent,
                        userId = userId
                    )
                    when (result) {
                        is SyncResult.Success -> {
                            val response = result.data
                            val newId = repository.upsertAccount(
                                provider = "OpenAI",
                                userId = response.userId,
                                email = response.email,
                                authToken = authToken,
                                cookies = cookies,
                                userAgent = userAgent,
                                planType = response.planType,
                            )
                            repository.setActiveAccount(newId.toInt())
                            val logEntry = mapResponseToLog(newId.toInt(), response)
                            repository.insertLog(logEntry)
                        }
                        else -> {
                            throw IOException("No se pudo validar la cuenta de OpenAI")
                        }
                    }
                }

                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error validating manual account", e)

                // Still allow saving it, in case they made a typo but want to try again or if they are offline
                val fallbackEmail = email.ifEmpty {
                    when (provider) {
                        "Anthropic" -> "Claude Account"
                        "Ollama" -> "Ollama Account"
                        else -> "ChatGPT Account"
                    }
                }
                val fallbackUserId = userId.ifEmpty { "manual_${System.currentTimeMillis()}" }
                val newId = repository.upsertAccount(
                    provider = provider,
                    userId = fallbackUserId,
                    email = fallbackEmail,
                    authToken = authToken,
                    cookies = cookies,
                    userAgent = userAgent,
                    planType = "N/A",
                )
                repository.setActiveAccount(newId.toInt())

                _errorMessage.value = "Guardado con advertencias (La validacion falla): ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun switchAccount(accountId: Int) {
        viewModelScope.launch {
            repository.setActiveAccount(accountId)
            _errorMessage.value = null
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
            // Clear all web cookies so a new account of the same provider
            // does not auto-login to the deleted account's session.
            android.webkit.CookieManager.getInstance().removeAllCookies { }
            // If the deleted account was active, pick the first other account (if any) as active
            val currentAccounts = allAccounts.value
            val remaining = currentAccounts.filter { it.id != account.id }
            if (account.isActive && remaining.isNotEmpty()) {
                repository.setActiveAccount(remaining.first().id)
            }
        }
    }

    fun deleteLogs() {
        val account = activeAccount.value ?: return
        viewModelScope.launch {
            database.usageLogDao().deleteLogsForAccount(account.id)
        }
    }

    private fun mapResponseToLog(accountId: Int, response: UsageResponse): UsageLog {
        val rate = response.rateLimit
        val primary = rate?.primaryWindow
        val secondary = rate?.secondaryWindow
        val credits = response.credits

        return UsageLog(
            accountId = accountId,
            planType = response.planType,
            primaryUsedPercent = primary?.usedPercent ?: 0.0,
            primaryResetAt = primary?.resetAt ?: 0L,
            primaryWindowSeconds = primary?.limitWindowSeconds ?: 0L,
            primaryResetAfterSeconds = primary?.resetAfterSeconds ?: 0L,
            secondaryUsedPercent = secondary?.usedPercent ?: 0.0,
            secondaryResetAt = secondary?.resetAt ?: 0L,
            secondaryWindowSeconds = secondary?.limitWindowSeconds ?: 0L,
            secondaryResetAfterSeconds = secondary?.resetAfterSeconds ?: 0L,
            hasCredits = credits?.hasCredits ?: false,
            unlimited = credits?.unlimited ?: false,
            balance = credits?.balance ?: "0",
            approxLocalUsed = credits?.approxLocalMessages?.getOrNull(0) ?: 0,
            approxLocalLimit = credits?.approxLocalMessages?.getOrNull(1) ?: 0,
            approxCloudUsed = credits?.approxCloudMessages?.getOrNull(0) ?: 0,
            approxCloudLimit = credits?.approxCloudMessages?.getOrNull(1) ?: 0
        )
    }

    private fun mapAnthropicResponseToLog(accountId: Int, response: AnthropicUsageResponse): UsageLog {
        val fiveHour = response.fiveHour
        val sevenDay = response.sevenDay
        val spend = response.spend

        val nowSec = System.currentTimeMillis() / 1000
        val primaryResetAt = parseIsoTimestampToEpoch(fiveHour?.resetsAt)
        val primaryResetAfterSeconds = if (primaryResetAt > nowSec) primaryResetAt - nowSec else 0L

        val secondaryResetAt = parseIsoTimestampToEpoch(sevenDay?.resetsAt)
        val secondaryResetAfterSeconds = if (secondaryResetAt > nowSec) secondaryResetAt - nowSec else 0L

        val spentAmount: Double = if (spend?.used != null) {
            val amountMinor = spend.used.amountMinor ?: 0L
            val exponent = spend.used.exponent ?: 2
            amountMinor.toDouble() / Math.pow(10.0, exponent.toDouble())
        } else {
            0.0
        }

        val limitAmount: Double? = if (spend?.limit != null) {
            val amountMinor = spend.limit.amountMinor ?: 0L
            val exponent = spend.limit.exponent ?: 2
            amountMinor.toDouble() / Math.pow(10.0, exponent.toDouble())
        } else {
            null
        }

        val balanceStr = if (limitAmount != null) {
            String.format(java.util.Locale.US, "%.2f / %.2f", spentAmount, limitAmount)
        } else {
            String.format(java.util.Locale.US, "%.2f", spentAmount)
        }

        return UsageLog(
            accountId = accountId,
            planType = "Pro",
            primaryUsedPercent = (fiveHour?.utilization ?: 0.0),
            primaryResetAt = primaryResetAt,
            primaryWindowSeconds = 5 * 3600L,
            primaryResetAfterSeconds = primaryResetAfterSeconds,
            secondaryUsedPercent = (sevenDay?.utilization ?: 0.0),
            secondaryResetAt = secondaryResetAt,
            secondaryWindowSeconds = 7 * 24 * 3600L,
            secondaryResetAfterSeconds = secondaryResetAfterSeconds,
            hasCredits = spend != null,
            unlimited = false,
            balance = balanceStr,
            approxLocalUsed = 0,
            approxLocalLimit = 0,
            approxCloudUsed = 0,
            approxCloudLimit = 0
        )
    }

    private fun parseIsoTimestampToEpoch(iso: String?): Long {
        if (iso == null) return 0L
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                java.time.OffsetDateTime.parse(iso).toInstant().toEpochMilli() / 1000
            } else {
                val cleanIso = iso.substring(0, 19)
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                val date = sdf.parse(cleanIso)
                (date?.time ?: 0L) / 1000
            }
        } catch (e: Exception) {
            0L
        }
    }

    private fun mapOllamaResponseToLog(accountId: Int, response: OllamaUsageResponse): UsageLog {
        val nowSec = System.currentTimeMillis() / 1000
        val primaryResetAfterSeconds = parseResetsInToSeconds(response.sessionReset)
        val primaryResetAt = nowSec + primaryResetAfterSeconds

        val secondaryResetAfterSeconds = parseResetsInToSeconds(response.weeklyReset)
        val secondaryResetAt = nowSec + secondaryResetAfterSeconds

        return UsageLog(
            accountId = accountId,
            planType = response.planType,
            primaryUsedPercent = response.sessionPercent,
            primaryResetAt = primaryResetAt,
            primaryWindowSeconds = 3600L, // 1 hour typical session window
            primaryResetAfterSeconds = primaryResetAfterSeconds,
            secondaryUsedPercent = response.weeklyPercent,
            secondaryResetAt = secondaryResetAt,
            secondaryWindowSeconds = 7 * 24 * 3600L, // 7 days weekly window
            secondaryResetAfterSeconds = secondaryResetAfterSeconds,
            hasCredits = response.extraBalance != "$0" && response.extraBalance.isNotEmpty(),
            unlimited = false,
            balance = response.extraBalance,
            approxLocalUsed = 0,
            approxLocalLimit = 0,
            approxCloudUsed = 0,
            approxCloudLimit = 0
        )
    }

    private fun parseResetsInToSeconds(resetsIn: String): Long {
        val cleaned = resetsIn.lowercase().replace("resets in", "").trim()
        val numberPart = """\d+""".toRegex().find(cleaned)?.value?.toLongOrNull() ?: return 0L
        return when {
            cleaned.contains("second") -> numberPart
            cleaned.contains("minute") -> numberPart * 60L
            cleaned.contains("hour") -> numberPart * 3600L
            cleaned.contains("day") -> numberPart * 24 * 3600L
            else -> 0L
        }
    }
}

/**
 * Derives the latest [UsageLog] per account id from a flat list of logs.
 *
 * Pure function: single O(n) pass (groupBy) then one [maxByOrNull] per group.
 * Extracted from [MainViewModel.latestLogByAccount] so the derivation is
 * unit-testable without instantiating the AndroidViewModel (which needs an
 * Application + Room database). A null entry is produced for an account id
 * only if the group is empty, which cannot happen via groupBy; in practice
 * every value is non-null, but the type stays nullable to match the original
 * inline expression.
 */
fun latestLogPerAccount(logs: List<UsageLog>): Map<Int, UsageLog?> =
    logs.groupBy { it.accountId }.mapValues { it.value.maxByOrNull { log -> log.timestamp } }
