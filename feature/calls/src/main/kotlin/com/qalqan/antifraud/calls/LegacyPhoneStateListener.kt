@file:Suppress("DEPRECATION")

package com.qalqan.antifraud.calls

import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import java.time.Instant

/**
 * Spec §4.2.1 — Android 8–11 fallback when `TelephonyCallback.CallStateListener` is unavailable.
 * Deprecated on API 31+ but still functional. The `incomingNumber` argument is ignored on
 * purpose: §4.2.1 sources the number from the `CallLog` provider after IDLE, not the callback.
 */
@Suppress("DEPRECATION")
class LegacyPhoneStateListener(
    private val subscriptionId: Int?,
    private val callback: CallStateListenerCallback,
) : PhoneStateListener() {

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onCallStateChanged(state: Int, incomingNumber: String?) {
        val mapped = when (state) {
            TelephonyManager.CALL_STATE_RINGING -> CallTransition.State.RINGING
            TelephonyManager.CALL_STATE_OFFHOOK -> CallTransition.State.OFFHOOK
            TelephonyManager.CALL_STATE_IDLE -> CallTransition.State.IDLE
            else -> return
        }
        callback.onTransition(
            CallTransition(state = mapped, subscriptionId = subscriptionId, occurredAt = Instant.now()),
        )
    }
}
