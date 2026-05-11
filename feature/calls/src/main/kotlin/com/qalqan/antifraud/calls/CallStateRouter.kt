@file:Suppress("DEPRECATION")

package com.qalqan.antifraud.calls

import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import java.util.concurrent.Executors

/**
 * Spec §4.2.1 — selects between the Android-12+ `TelephonyCallback` API and the
 * pre-12 `PhoneStateListener` API at runtime. Owns the registration lifecycle.
 *
 * On Android 12+ registers one listener per subscription ID so dual-SIM devices
 * report independent transitions. Pre-12 always registers a single listener with
 * `subscriptionId = null` (multi-SIM deferred per the Stage-3 plan).
 */
class CallStateRouter(
    private val context: Context,
    private val callback: CallStateListenerCallback,
) {
    enum class Path { MODERN, LEGACY }

    private val telephonyManager: TelephonyManager? =
        context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    private val executor = Executors.newSingleThreadExecutor()

    private val modernCallbacks = mutableListOf<TelephonyCallback>()

    private val legacyListeners = mutableListOf<PhoneStateListener>()

    fun path(): Path =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Path.MODERN else Path.LEGACY

    /**
     * Register one listener per subscription ID. Pass an empty list (or null entry) to
     * register a single fallback listener with `subscriptionId = null` — used on devices
     * where the SubscriptionManager returns no entries (single SIM, pre-Android-12).
     */
    fun register(subscriptionIds: List<Int> = emptyList()) {
        val tm = telephonyManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val ids: List<Int?> = if (subscriptionIds.isEmpty()) listOf(null) else subscriptionIds
            ids.forEach { id ->
                val cb = TelephonyCallStateListener(subscriptionId = id, callback = callback)
                tm.registerTelephonyCallback(executor, cb)
                modernCallbacks.add(cb)
            }
        } else {
            val ll = LegacyPhoneStateListener(subscriptionId = null, callback = callback)
            tm.listen(ll, PhoneStateListener.LISTEN_CALL_STATE)
            legacyListeners.add(ll)
        }
    }

    fun unregister() {
        val tm = telephonyManager ?: return
        modernCallbacks.forEach { tm.unregisterTelephonyCallback(it) }
        modernCallbacks.clear()
        legacyListeners.forEach { tm.listen(it, PhoneStateListener.LISTEN_NONE) }
        legacyListeners.clear()
    }
}
