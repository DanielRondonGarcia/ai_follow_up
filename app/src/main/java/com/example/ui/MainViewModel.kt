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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
                if (account.provider == "Ollama") {
                    val response = ollamaService.fetchUsage(
                        cookies = account.cookies,
                        userAgent = account.userAgent
                    )
                    
                    val logEntry = mapOllamaResponseToLog(account.id, response)
                    repository.insertLog(logEntry)

                    val updatedAccount = account.copy(
                        email = response.email,
                        planType = response.planType,
                        lastUpdated = System.currentTimeMillis()
                    )
                    repository.updateAccount(updatedAccount)
                } else if (account.provider == "Anthropic") {
                    val response = anthropicService.fetchUsage(
                        orgId = account.userId,
                        authToken = account.authToken,
                        cookies = account.cookies,
                        userAgent = account.userAgent
                    )
                    
                    val logEntry = mapAnthropicResponseToLog(account.id, response)
                    repository.insertLog(logEntry)

                    val updatedAccount = account.copy(
                        lastUpdated = System.currentTimeMillis()
                    )
                    repository.updateAccount(updatedAccount)
                } else {
                    val response = service.fetchUsage(
                        authToken = account.authToken,
                        cookies = account.cookies,
                        userAgent = account.userAgent
                    )
                    
                    // 1. Insert a new usage log
                    val logEntry = mapResponseToLog(account.id, response)
                    repository.insertLog(logEntry)

                    // 2. Update existing account profile (if email or plan type changed)
                    val updatedAccount = account.copy(
                        email = response.email,
                        planType = response.planType,
                        lastUpdated = System.currentTimeMillis()
                    )
                    repository.updateAccount(updatedAccount)
                }
                
                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching usage", e)
                _errorMessage.value = e.localizedMessage ?: "Error de red desconocido"
            } finally {
                _isLoading.value = false
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
                        if (account.provider == "Ollama") {
                            val response = ollamaService.fetchUsage(
                                cookies = account.cookies,
                                userAgent = account.userAgent
                            )
                            val logEntry = mapOllamaResponseToLog(account.id, response)
                            repository.insertLog(logEntry)
                            val updatedAccount = account.copy(
                                email = response.email,
                                planType = response.planType,
                                lastUpdated = System.currentTimeMillis()
                            )
                            repository.updateAccount(updatedAccount)
                        } else if (account.provider == "Anthropic") {
                            val response = anthropicService.fetchUsage(
                                orgId = account.userId,
                                authToken = account.authToken,
                                cookies = account.cookies,
                                userAgent = account.userAgent
                            )
                            val logEntry = mapAnthropicResponseToLog(account.id, response)
                            repository.insertLog(logEntry)
                            val updatedAccount = account.copy(
                                lastUpdated = System.currentTimeMillis()
                            )
                            repository.updateAccount(updatedAccount)
                        } else {
                            val response = service.fetchUsage(
                                authToken = account.authToken,
                                cookies = account.cookies,
                                userAgent = account.userAgent
                            )
                            val logEntry = mapResponseToLog(account.id, response)
                            repository.insertLog(logEntry)
                            val updatedAccount = account.copy(
                                email = response.email,
                                planType = response.planType,
                                lastUpdated = System.currentTimeMillis()
                            )
                            repository.updateAccount(updatedAccount)
                        }
                    } catch (e: Exception) {
                        Log.e("MainViewModel", "Error syncAllAccounts para ${account.email}", e)
                    }
                }
                _errorMessage.value = null
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
                    val response = ollamaService.fetchUsage(
                        cookies = cookies,
                        userAgent = userAgent
                    )

                    repository.insertAccount(
                        Account(
                            provider = "Ollama",
                            email = response.email,
                            userId = response.email,
                            planType = response.planType,
                            authToken = "",
                            cookies = cookies,
                            userAgent = userAgent,
                            isActive = true,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )

                    val savedAccount = repository.getAccountByUserId(response.email)
                    if (savedAccount != null) {
                        repository.setActiveAccount(savedAccount.id)
                        val logEntry = mapOllamaResponseToLog(savedAccount.id, response)
                        repository.insertLog(logEntry)
                    }
                } else if (provider == "Anthropic") {
                    // Try to validate by fetching Anthropic usage
                    val response = anthropicService.fetchUsage(
                        orgId = userId,
                        authToken = authToken,
                        cookies = cookies,
                        userAgent = userAgent
                    )

                    // Deactivate others, save new account
                    repository.insertAccount(
                        Account(
                            provider = "Anthropic",
                            email = "Claude Account",
                            userId = userId,
                            planType = "Pro",
                            authToken = authToken,
                            cookies = cookies,
                            userAgent = userAgent,
                            isActive = true,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )

                    // Switch active in local database
                    val savedAccount = repository.getAccountByUserId(userId)
                    if (savedAccount != null) {
                        repository.setActiveAccount(savedAccount.id)
                        val logEntry = mapAnthropicResponseToLog(savedAccount.id, response)
                        repository.insertLog(logEntry)
                    }
                } else {
                    // OpenAI
                    val response = service.fetchUsage(
                        authToken = authToken,
                        cookies = cookies,
                        userAgent = userAgent
                    )

                    // Deactivate others, save new account
                    repository.insertAccount(
                        Account(
                            provider = "OpenAI",
                            email = response.email,
                            userId = response.userId,
                            planType = response.planType,
                            authToken = authToken,
                            cookies = cookies,
                            userAgent = userAgent,
                            isActive = true,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )

                    // Switch active in local database
                    val savedAccount = repository.getAccountByUserId(response.userId)
                    if (savedAccount != null) {
                        repository.setActiveAccount(savedAccount.id)
                        // Insert the first usage log directly
                        val logEntry = mapResponseToLog(savedAccount.id, response)
                        repository.insertLog(logEntry)
                    }
                }

                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error during web login handling", e)
                // If it fails because we couldn't fetch details, try saving a generic account
                // so the user doesn't lose credentials
                if (provider == "Ollama") {
                    val tempEmail = "ollama_user_${System.currentTimeMillis() % 10000}"
                    val genericAccount = Account(
                        provider = "Ollama",
                        email = tempEmail,
                        userId = tempEmail,
                        planType = "Free",
                        authToken = "",
                        cookies = cookies,
                        userAgent = userAgent,
                        isActive = true,
                        lastUpdated = System.currentTimeMillis()
                    )
                    val newId = repository.insertAccount(genericAccount)
                    repository.setActiveAccount(newId.toInt())
                } else if (provider == "Anthropic") {
                    val tempEmail = "claude_user_${System.currentTimeMillis() % 10000}@anthropic.com"
                    val genericAccount = Account(
                        provider = "Anthropic",
                        email = tempEmail,
                        userId = userId.ifEmpty { "unknown_claude_org" },
                        planType = "Pro",
                        authToken = authToken,
                        cookies = cookies,
                        userAgent = userAgent,
                        isActive = true,
                        lastUpdated = System.currentTimeMillis()
                    )
                    val newId = repository.insertAccount(genericAccount)
                    repository.setActiveAccount(newId.toInt())
                } else {
                    val tempEmail = "chatgpt_user_${System.currentTimeMillis() % 10000}@openai.com"
                    val genericAccount = Account(
                        provider = "OpenAI",
                        email = tempEmail,
                        userId = "unknown_user",
                        planType = "unknown",
                        authToken = authToken,
                        cookies = cookies,
                        userAgent = userAgent,
                        isActive = true,
                        lastUpdated = System.currentTimeMillis()
                    )
                    val newId = repository.insertAccount(genericAccount)
                    repository.setActiveAccount(newId.toInt())
                }
                
                _errorMessage.value = "Se guardó la sesión, pero falló la sincronización inicial: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun handleManualAccountAdd(provider: String, email: String, authToken: String, cookies: String, userAgent: String, userId: String) {
        _showAddManualDialog.value = false
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                if (provider == "Ollama") {
                    val response = ollamaService.fetchUsage(
                        cookies = cookies,
                        userAgent = userAgent
                    )

                    val account = Account(
                        provider = "Ollama",
                        email = email.ifEmpty { response.email },
                        userId = response.email,
                        planType = response.planType,
                        authToken = "",
                        cookies = cookies,
                        userAgent = userAgent,
                        isActive = true,
                        lastUpdated = System.currentTimeMillis()
                    )

                    val newId = repository.insertAccount(account)
                    repository.setActiveAccount(newId.toInt())
                    
                    val logEntry = mapOllamaResponseToLog(newId.toInt(), response)
                    repository.insertLog(logEntry)
                } else if (provider == "Anthropic") {
                    // Try to validate by fetching Anthropic usage
                    val response = anthropicService.fetchUsage(
                        orgId = userId,
                        authToken = authToken,
                        cookies = cookies,
                        userAgent = userAgent
                    )

                    val account = Account(
                        provider = "Anthropic",
                        email = email.ifEmpty { "Claude Account" },
                        userId = userId,
                        planType = "Pro",
                        authToken = authToken,
                        cookies = cookies,
                        userAgent = userAgent,
                        isActive = true,
                        lastUpdated = System.currentTimeMillis()
                    )

                    val newId = repository.insertAccount(account)
                    repository.setActiveAccount(newId.toInt())
                    
                    // Add initial log
                    val logEntry = mapAnthropicResponseToLog(newId.toInt(), response)
                    repository.insertLog(logEntry)
                } else {
                    // OpenAI
                    val response = service.fetchUsage(
                        authToken = authToken,
                        cookies = cookies,
                        userAgent = userAgent
                    )

                    val account = Account(
                        provider = "OpenAI",
                        email = response.email,
                        userId = response.userId,
                        planType = response.planType,
                        authToken = authToken,
                        cookies = cookies,
                        userAgent = userAgent,
                        isActive = true,
                        lastUpdated = System.currentTimeMillis()
                    )

                    val newId = repository.insertAccount(account)
                    repository.setActiveAccount(newId.toInt())
                    
                    val logEntry = mapResponseToLog(newId.toInt(), response)
                    repository.insertLog(logEntry)
                }

                _errorMessage.value = null
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error validating manual account", e)
                
                // Still allow saving it, in case they made a typo but want to try again or if they are offline
                val account = Account(
                    provider = provider,
                    email = email.ifEmpty { 
                        when (provider) {
                            "Anthropic" -> "Claude Account"
                            "Ollama" -> "Ollama Account"
                            else -> "ChatGPT Account"
                        }
                    },
                    userId = userId.ifEmpty { "manual_${System.currentTimeMillis()}" },
                    planType = "N/A",
                    authToken = authToken,
                    cookies = cookies,
                    userAgent = userAgent,
                    isActive = true,
                    lastUpdated = System.currentTimeMillis()
                )
                val newId = repository.insertAccount(account)
                repository.setActiveAccount(newId.toInt())
                
                _errorMessage.value = "Guardado con advertencias (La validación falló): ${e.localizedMessage}"
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
