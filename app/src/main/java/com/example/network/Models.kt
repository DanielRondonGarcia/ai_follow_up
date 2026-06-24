package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UsageResponse(
    @Json(name = "user_id") val userId: String,
    @Json(name = "account_id") val accountId: String,
    @Json(name = "email") val email: String,
    @Json(name = "plan_type") val planType: String,
    @Json(name = "rate_limit") val rateLimit: RateLimit?,
    @Json(name = "credits") val credits: Credits?,
    @Json(name = "spend_control") val spendControl: SpendControl?,
    @Json(name = "rate_limit_reached_type") val rateLimitReachedType: RateLimitReachedType?
)

@JsonClass(generateAdapter = true)
data class RateLimit(
    @Json(name = "allowed") val allowed: Boolean?,
    @Json(name = "limit_reached") val limitReached: Boolean?,
    @Json(name = "primary_window") val primaryWindow: LimitWindow?,
    @Json(name = "secondary_window") val secondaryWindow: LimitWindow?
)

@JsonClass(generateAdapter = true)
data class LimitWindow(
    @Json(name = "used_percent") val usedPercent: Double?,
    @Json(name = "limit_window_seconds") val limitWindowSeconds: Long?,
    @Json(name = "reset_after_seconds") val resetAfterSeconds: Long?,
    @Json(name = "reset_at") val resetAt: Long?
)

@JsonClass(generateAdapter = true)
data class Credits(
    @Json(name = "has_credits") val hasCredits: Boolean?,
    @Json(name = "unlimited") val unlimited: Boolean?,
    @Json(name = "overage_limit_reached") val overageLimitReached: Boolean?,
    @Json(name = "balance") val balance: String?,
    @Json(name = "approx_local_messages") val approxLocalMessages: List<Int>?,
    @Json(name = "approx_cloud_messages") val approxCloudMessages: List<Int>?
)

@JsonClass(generateAdapter = true)
data class SpendControl(
    @Json(name = "reached") val reached: Boolean?,
    @Json(name = "individual_limit") val individualLimit: Double?
)

@JsonClass(generateAdapter = true)
data class RateLimitReachedType(
    @Json(name = "type") val type: String?,
    @Json(name = "details") val details: String?
)

@JsonClass(generateAdapter = true)
data class AnthropicUsageResponse(
    @Json(name = "five_hour") val fiveHour: AnthropicLimit?,
    @Json(name = "seven_day") val sevenDay: AnthropicLimit?,
    @Json(name = "spend") val spend: AnthropicSpend?
)

@JsonClass(generateAdapter = true)
data class AnthropicLimit(
    @Json(name = "utilization") val utilization: Double?,
    @Json(name = "resets_at") val resetsAt: String?,
    @Json(name = "limit_dollars") val limitDollars: Double?,
    @Json(name = "used_dollars") val usedDollars: Double?,
    @Json(name = "remaining_dollars") val remainingDollars: Double?
)

@JsonClass(generateAdapter = true)
data class AnthropicSpend(
    @Json(name = "used") val used: AnthropicAmount?,
    @Json(name = "limit") val limit: AnthropicAmount?,
    @Json(name = "percent") val percent: Double?,
    @Json(name = "balance") val balance: AnthropicAmount?
)

@JsonClass(generateAdapter = true)
data class AnthropicAmount(
    @Json(name = "amount_minor") val amountMinor: Long?,
    @Json(name = "currency") val currency: String?,
    @Json(name = "exponent") val exponent: Int?
)
