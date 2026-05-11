package com.qalqan.antifraud.calls

import android.os.Build
import android.telephony.TelephonyManager
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicReference

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R]) // Android 11
class LegacyPhoneStateListenerTest {

    @Test
    fun `legacy listener maps CALL_STATE_RINGING`() {
        val seen = AtomicReference<CallTransition?>()
        val listener = LegacyPhoneStateListener(subscriptionId = null) { seen.set(it) }
        listener.onCallStateChanged(TelephonyManager.CALL_STATE_RINGING, "")
        seen.get()?.state shouldBe CallTransition.State.RINGING
    }

    @Test
    fun `legacy listener maps CALL_STATE_IDLE`() {
        val seen = AtomicReference<CallTransition?>()
        val listener = LegacyPhoneStateListener(subscriptionId = null) { seen.set(it) }
        listener.onCallStateChanged(TelephonyManager.CALL_STATE_IDLE, "")
        seen.get()?.state shouldBe CallTransition.State.IDLE
    }

    @Test
    fun `legacy listener ignores any incoming-number argument`() {
        // Spec §4.2.1: even on legacy the number is sourced from CallLog, not the callback,
        // so the listener must drop the `incomingNumber` parameter on the floor.
        val seen = AtomicReference<CallTransition?>()
        val listener = LegacyPhoneStateListener(subscriptionId = null) { seen.set(it) }
        listener.onCallStateChanged(TelephonyManager.CALL_STATE_RINGING, "+71234567890")
        // The transition carries no number field — it can't leak the argument.
        seen.get()?.state shouldBe CallTransition.State.RINGING
    }
}
