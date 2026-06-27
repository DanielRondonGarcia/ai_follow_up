package com.example.network

/**
 * Typed result for sync operations. Distinguishes auth-expiry from network
 * and parse failures so the ViewModel can set a per-account expired state
 * instead of a generic error string.
 *
 * - [Success]: the usage data was fetched and parsed.
 * - [AuthExpired]: HTTP 401/403 (ChatGPT/Anthropic) or login-page HTML (Ollama).
 * - [NetworkError]: connectivity failure, timeout, 5xx, generic IOException.
 * - [ParseError]: 200 OK but unparseable body.
 *
 * The [AuthExpired] variant carries the provider tag and the account userId so
 * the ViewModel can reconcile the expired id without parsing an expired body.
 */
sealed class SyncResult<out T> {
    data class Success<T>(val data: T) : SyncResult<T>()
    data class AuthExpired(val provider: String, val userId: String) : SyncResult<Nothing>()
    data class NetworkError(val cause: Throwable) : SyncResult<Nothing>()
    data class ParseError(val cause: Throwable) : SyncResult<Nothing>()
}