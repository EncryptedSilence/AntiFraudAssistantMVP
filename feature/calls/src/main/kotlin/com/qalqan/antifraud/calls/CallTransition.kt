package com.qalqan.antifraud.calls

import java.time.Instant

/**
 * Spec §4.2.1 — the listener emits one of three call-state transitions per phone state change.
 * The listener never reads the phone number from the system callback (Android 10+ no longer
 * delivers it there); the number arrives via `CallLogReader` after IDLE.
 */
data class CallTransition(
    val state: State,
    val subscriptionId: Int?,
    val occurredAt: Instant,
) {
    enum class State(val id: String) {
        RINGING("RINGING"),
        OFFHOOK("OFFHOOK"),
        IDLE("IDLE"),
    }
}

fun interface CallStateListenerCallback {
    fun onTransition(transition: CallTransition)
}
