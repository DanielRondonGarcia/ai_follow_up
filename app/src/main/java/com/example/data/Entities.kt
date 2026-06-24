package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val provider: String = "OpenAI", // "OpenAI" or "Anthropic"
    val email: String,
    val userId: String,
    val planType: String,
    val authToken: String,
    val cookies: String,
    val userAgent: String,
    val isActive: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "usage_logs")
data class UsageLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val accountId: Int, // Refers to Account.id
    val timestamp: Long = System.currentTimeMillis(),
    val planType: String,
    
    // Primary Rate Limit Window (e.g., 5-hour window)
    val primaryUsedPercent: Double,
    val primaryResetAt: Long,
    val primaryWindowSeconds: Long,
    val primaryResetAfterSeconds: Long,
    
    // Secondary Rate Limit Window (e.g., 7-day window)
    val secondaryUsedPercent: Double,
    val secondaryResetAt: Long,
    val secondaryWindowSeconds: Long,
    val secondaryResetAfterSeconds: Long,
    
    // Credits information
    val hasCredits: Boolean,
    val unlimited: Boolean,
    val balance: String,
    val approxLocalUsed: Int,
    val approxLocalLimit: Int,
    val approxCloudUsed: Int,
    val approxCloudLimit: Int
)
