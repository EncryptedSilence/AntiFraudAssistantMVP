package com.qalqan.antifraud.sms

import com.qalqan.antifraud.database.repository.SmsEventRepository
import com.qalqan.antifraud.domain.SmsEvent

/**
 * Spec §4.2.2 — entry point invoked by `SmsBroadcastReceiver` and by the content-provider
 * sweep. Builds an `SmsEvent`, persists it, and fires an optional `onCaptured` callback
 * for downstream wiring (Stage 9 alert pipeline).
 *
 * Deduplication: §4.2.2 calls out the broadcast-and-sweep overlap. We deduplicate against
 * the existing repository within a 10-second window: if an `SmsEvent` with the same
 * `senderHash` and a `receivedAt` within ±10 s already exists, the new arrival is dropped.
 */
class AutoSmsCapture(
    private val builder: SmsEventBuilder,
    private val sms: SmsEventRepository,
    private val onCaptured: suspend (SmsEvent) -> Unit = {},
) {
    suspend fun accept(broadcast: SmsBroadcast) {
        val event = builder.build(broadcast)
        if (isDuplicate(event)) return
        sms.save(event)
        onCaptured(event)
    }

    private suspend fun isDuplicate(candidate: SmsEvent): Boolean {
        val cutoff = candidate.receivedAt.minusSeconds(DEDUP_WINDOW_SEC)
        return sms.listSince(cutoff).any {
            it.senderHash == candidate.senderHash &&
                Math.abs(it.receivedAt.toEpochMilli() - candidate.receivedAt.toEpochMilli()) <=
                DEDUP_WINDOW_SEC * 1000L
        }
    }

    private companion object {
        const val DEDUP_WINDOW_SEC = 10L
    }
}
