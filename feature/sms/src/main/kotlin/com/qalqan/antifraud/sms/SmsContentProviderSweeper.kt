package com.qalqan.antifraud.sms

import com.qalqan.antifraud.calls.SimEnumerator
import java.time.Instant

/**
 * Spec §4.2.2 — recovers broadcasts missed during app standby. Runs on app launch +
 * every 60 s while `:app` is in foreground (wired by `MainActivity` lifecycle in Phase 7).
 *
 * Dedup against the existing `SmsEventRepository` is performed by the shared `AutoSmsCapture`
 * (10-second window on senderHash + receivedAt). We simply feed every inbox row through
 * `capture.accept(...)` and rely on the existing dedup.
 *
 * Multi-SIM: when a `SimEnumerator` is supplied, the inbox's raw `SUBSCRIPTION_ID` is mapped
 * to `simSlotIndex` via `slotsBySubscriptionId()`. When the enumerator can't resolve the id
 * (pre-Android-12 or single-SIM device), we fall back to the raw subscription id — matching
 * Stage 3's call-side behavior.
 */
class SmsContentProviderSweeper(
    private val reader: SmsContentProviderReader,
    private val capture: AutoSmsCapture,
    private val sims: SimEnumerator? = null,
) {
    suspend fun sweepSince(sinceMs: Long) {
        val slotsById = sims?.slotsBySubscriptionId().orEmpty()
        reader.readSince(sinceMs).forEach { row ->
            val mappedSlot = row.subscriptionId?.let(slotsById::get) ?: row.subscriptionId
            capture.accept(
                SmsBroadcast(
                    rawSender = row.rawSender,
                    body = row.body,
                    receivedAt = Instant.ofEpochMilli(row.receivedAtMs),
                    simSlot = mappedSlot,
                ),
            )
        }
    }
}
