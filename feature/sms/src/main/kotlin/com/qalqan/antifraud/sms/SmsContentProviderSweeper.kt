package com.qalqan.antifraud.sms

import java.time.Instant

/**
 * Spec §4.2.2 — recovers broadcasts missed during app standby. Runs on app launch +
 * every 60 s while `:app` is in foreground (wired by `MainActivity` lifecycle in Phase 7).
 *
 * Dedup against the existing `SmsEventRepository` is performed by the shared `AutoSmsCapture`
 * (10-second window on senderHash + receivedAt). We simply feed every inbox row through
 * `capture.accept(...)` and rely on the existing dedup.
 */
class SmsContentProviderSweeper(
    private val reader: SmsContentProviderReader,
    private val capture: AutoSmsCapture,
) {
    suspend fun sweepSince(sinceMs: Long) {
        reader.readSince(sinceMs).forEach { row ->
            capture.accept(
                SmsBroadcast(
                    rawSender = row.rawSender,
                    body = row.body,
                    receivedAt = Instant.ofEpochMilli(row.receivedAtMs),
                    simSlot = row.subscriptionId,
                ),
            )
        }
    }
}
