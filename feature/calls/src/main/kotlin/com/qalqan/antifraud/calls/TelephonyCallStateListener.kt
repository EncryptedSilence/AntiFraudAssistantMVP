package com.qalqan.antifraud.calls

import android.os.Build
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import java.time.Instant

/**
 * Spec §4.2.1 — Android 12+ uses `TelephonyCallback.CallStateListener`. The listener does not
 * receive the phone number on this callback (Android 10+ removed it); call-log read happens
 * after IDLE in `CallLogReader`.
 */
@RequiresApi(Build.VERSION_CODES.S)
class TelephonyCallStateListener(
    private val subscriptionId: Int?,
    private val callback: CallStateListenerCallback,
) : TelephonyCallback(), TelephonyCallback.CallStateListener {

    override fun onCallStateChanged(state: Int) {
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
