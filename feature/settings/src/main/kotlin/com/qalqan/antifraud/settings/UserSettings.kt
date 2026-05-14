package com.qalqan.antifraud.settings

import android.content.Context
import com.qalqan.antifraud.scoring.Sensitivity

/**
 * Spec §18 — persistent user-settings storage.
 *
 * Backed by SharedPreferences named `antifraud_user_prefs`. All reads are synchronous and
 * cheap (in-memory after the first access); writes use `apply()` (async to disk) so caller
 * threads never block. Mirrors the [com.qalqan.antifraud.sync.SyncSettings] pattern from
 * Stage 6.
 *
 * Concrete properties land in subsequent tasks; T03 only wires sensitivity.
 */
class UserSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var sensitivity: Sensitivity
        get() {
            val raw = prefs.getString(KEY_SENSITIVITY, null) ?: return Sensitivity.STANDARD
            return Sensitivity.entries.firstOrNull { it.name == raw } ?: Sensitivity.STANDARD
        }
        set(value) {
            prefs.edit().putString(KEY_SENSITIVITY, value.name).apply()
        }

    companion object {
        const val PREFS_NAME: String = "antifraud_user_prefs"
        private const val KEY_SENSITIVITY: String = "sensitivity"
    }
}
