package com.qalqan.antifraud.calls

import android.os.Build
import android.telephony.TelephonyManager
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.S]) // Android 12
class TelephonyCallStateListenerTest {
    @Test
    fun `forwards CALL_STATE_RINGING as RINGING`() {
        val seen = AtomicReference<CallTransition?>()
        val listener = TelephonyCallStateListener(subscriptionId = 1) { seen.set(it) }
        listener.onCallStateChanged(TelephonyManager.CALL_STATE_RINGING)
        seen.get()?.state shouldBe CallTransition.State.RINGING
        seen.get()?.subscriptionId shouldBe 1
    }

    @Test
    fun `forwards CALL_STATE_OFFHOOK as OFFHOOK`() {
        val seen = AtomicReference<CallTransition?>()
        val listener = TelephonyCallStateListener(subscriptionId = 0) { seen.set(it) }
        listener.onCallStateChanged(TelephonyManager.CALL_STATE_OFFHOOK)
        seen.get()?.state shouldBe CallTransition.State.OFFHOOK
    }

    @Test
    fun `forwards CALL_STATE_IDLE as IDLE`() {
        val seen = AtomicReference<CallTransition?>()
        val listener = TelephonyCallStateListener(subscriptionId = null) { seen.set(it) }
        listener.onCallStateChanged(TelephonyManager.CALL_STATE_IDLE)
        seen.get()?.state shouldBe CallTransition.State.IDLE
        seen.get()?.subscriptionId shouldBe null
    }

    @Test
    fun `transition occurredAt is between before-and-after instants`() {
        val seen = AtomicReference<CallTransition?>()
        val listener = TelephonyCallStateListener(subscriptionId = null) { seen.set(it) }
        val before = Instant.now()
        listener.onCallStateChanged(TelephonyManager.CALL_STATE_IDLE)
        val after = Instant.now()
        val at = seen.get()?.occurredAt!!
        (!at.isBefore(before)) shouldBe true
        (!at.isAfter(after)) shouldBe true
    }
}
