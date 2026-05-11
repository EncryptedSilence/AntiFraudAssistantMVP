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
 * Multi-SIM listener registration (one per `SubscriptionId`) is added in Phase 6;
 * for Phase 3 the router registers a single listener with `subscriptionId = null`.
 */
class CallStateRouter(
    private val context: Context,
    private val callback: CallStateListenerCallback,
) {
    enum class Path { MODERN, LEGACY }

    private val telephonyManager: TelephonyManager? =
        context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager

    private val executor = Executors.newSingleThreadExecutor()

    private var modernCallback: TelephonyCallback? = null

    private var legacyListener: PhoneStateListener? = null

    fun path(): Path = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) Path.MODERN else Path.LEGACY

    fun register() {
        val tm = telephonyManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val cb = TelephonyCallStateListener(subscriptionId = null, callback = callback)
            tm.registerTelephonyCallback(executor, cb)
            modernCallback = cb
        } else {
            val ll = LegacyPhoneStateListener(subscriptionId = null, callback = callback)
            tm.listen(ll, PhoneStateListener.LISTEN_CALL_STATE)
            legacyListener = ll
        }
    }

    fun unregister() {
        val tm = telephonyManager ?: return
        modernCallback?.let { tm.unregisterTelephonyCallback(it) }
        modernCallback = null
        legacyListener?.let { tm.listen(it, PhoneStateListener.LISTEN_NONE) }
        legacyListener = null
    }
}
