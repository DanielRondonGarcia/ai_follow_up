package com.example.data

import android.content.Context
import android.util.Log
import java.util.UUID

/**
 * Provides stable per-install device identifiers stored in SharedPreferences.
 *
 * On first call all three IDs are generated and persisted in a single batched
 * write. Subsequent calls return the in-memory cached values (zero disk I/O).
 */
object DeviceIdStore {
    private const val PREFS_NAME = "device_ids"
    private const val KEY_ANTHROPIC_ANONYMOUS_ID = "anthropic_anonymous_id"
    private const val KEY_ANTHROPIC_DEVICE_ID = "anthropic_device_id"
    private const val KEY_OPENAI_DEVICE_ID = "openai_device_id"

    @Volatile private var cache: Triple<String, String, String>? = null

    fun getAnthropicAnonymousId(context: Context): String = ensureLoaded(context).first
    fun getAnthropicDeviceId(context: Context): String = ensureLoaded(context).second
    fun getOpenAiDeviceId(context: Context): String = ensureLoaded(context).third

    private fun ensureLoaded(context: Context): Triple<String, String, String> {
        cache?.let { return it }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored1 = prefs.getString(KEY_ANTHROPIC_ANONYMOUS_ID, null)
        val stored2 = prefs.getString(KEY_ANTHROPIC_DEVICE_ID, null)
        val stored3 = prefs.getString(KEY_OPENAI_DEVICE_ID, null)
        val v1 = stored1 ?: "claudeai.v1.${UUID.randomUUID()}"
        val v2 = stored2 ?: UUID.randomUUID().toString()
        val v3 = stored3 ?: UUID.randomUUID().toString()
        if (stored1 == null || stored2 == null || stored3 == null) {
            val ok = prefs.edit()
                .putString(KEY_ANTHROPIC_ANONYMOUS_ID, v1)
                .putString(KEY_ANTHROPIC_DEVICE_ID, v2)
                .putString(KEY_OPENAI_DEVICE_ID, v3)
                .commit()
            if (!ok) Log.e("DeviceIdStore", "Failed to persist device IDs")
        }
        return Triple(v1, v2, v3).also { cache = it }
    }
}
