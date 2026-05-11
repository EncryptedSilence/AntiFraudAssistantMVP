package com.qalqan.antifraud.sms

import android.content.Intent
import android.os.Build
import android.provider.Telephony
import java.time.Instant

/**
 * Spec §4.2.2 — reads `(rawSender, body, simSlot, receivedAt)` from an SMS_RECEIVED_ACTION
 * intent. Returns null when the intent is not an SMS broadcast or carries no decodable PDUs.
 *
 * Multi-part SMS: `Telephony.Sms.Intents.getMessagesFromIntent` returns one `SmsMessage` per
 * part; we concatenate the `messageBody` strings in order and take the first message's
 * `originatingAddress` as the sender (all parts of a multi-part SMS share the same sender).
 */
object SmsParser {

    fun extractFromIntent(intent: Intent): SmsBroadcast? {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return null
        val messages = runCatching { Telephony.Sms.Intents.getMessagesFromIntent(intent) }
            .getOrNull()
            ?: return null
        if (messages.isEmpty()) return null

        val sender = messages.firstOrNull()?.originatingAddress ?: return null
        val body = messages.joinToString(separator = "") { it.messageBody ?: "" }
        val receivedAtMs = messages.firstOrNull()?.timestampMillis
            ?: System.currentTimeMillis()
        val simSlot = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            intent.getIntExtra(SUB_ID_EXTRA, -1).takeIf { it >= 0 }
        } else {
            null
        }
        return SmsBroadcast(
            rawSender = sender,
            body = body,
            receivedAt = Instant.ofEpochMilli(receivedAtMs),
            simSlot = simSlot,
        )
    }

    /**
     * Telephony.Sms.Intents.EXTRA_SUBSCRIPTION_ID exists on Android 12+. We reference it by
     * literal string to avoid a compile-time API-level lock on the extra constant.
     */
    private const val SUB_ID_EXTRA = "subscription"
}
