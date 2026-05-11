package com.qalqan.antifraud.sms

import java.time.Instant

/**
 * Spec §4.2.2 — value class emitted by `SmsParser`. `rawSender` is the originating address
 * exactly as the system delivers it (phone number, short code, or alphanumeric sender ID);
 * normalization / hashing happens later in `SmsEntryDigest` + `SmsEventBuilder`.
 */
data class SmsBroadcast(
    val rawSender: String,
    val body: String,
    val receivedAt: Instant,
    val simSlot: Int?,
)
