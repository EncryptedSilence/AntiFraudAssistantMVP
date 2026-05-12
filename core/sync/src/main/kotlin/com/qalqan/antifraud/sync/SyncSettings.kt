package com.qalqan.antifraud.sync

import android.content.Context
import android.content.SharedPreferences
import java.time.Instant

/**
 * Spec §7.4 — the user disable switch. Default-OFF means a fresh install never makes
 * an outbound request; this is the §23 #4 zero-egress boundary held by construction.
 *
 * Backed by [SharedPreferences]; no encryption needed because the values are not
 * sensitive (a boolean toggle, a timestamp, a one-word result string).
 */
class SyncSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_ENABLED, value).apply()
        }

    var lastSyncAt: Instant?
        get() {
            val millis = prefs.getLong(KEY_LAST_SYNC_AT, -1L)
            return if (millis < 0) null else Instant.ofEpochMilli(millis)
        }
        set(value) {
            prefs.edit().putLong(KEY_LAST_SYNC_AT, value?.toEpochMilli() ?: -1L).apply()
        }

    var lastSyncResult: String?
        get() = prefs.getString(KEY_LAST_SYNC_RESULT, null)
        set(value) {
            prefs.edit().putString(KEY_LAST_SYNC_RESULT, value).apply()
        }

    companion object {
        const val PREFS_NAME = "antifraud.sync"
        const val KEY_ENABLED = "sync.enabled"
        const val KEY_LAST_SYNC_AT = "sync.lastSyncAt"
        const val KEY_LAST_SYNC_RESULT = "sync.lastSyncResult"
    }
}
