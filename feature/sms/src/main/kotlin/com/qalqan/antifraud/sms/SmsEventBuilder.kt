package com.qalqan.antifraud.sms

import com.qalqan.antifraud.database.crypto.CryptoBox
import com.qalqan.antifraud.database.manual.OtpAndIdGuard
import com.qalqan.antifraud.database.manual.SmsEntryDigest
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.SmsEvent
import java.util.UUID

/**
 * Spec §4.2.2 + §16.3 — composes an `SmsEvent` from an `SmsBroadcast` using the
 * privacy-preserving truncate-then-encrypt path shared with `ManualEntry.SmsSubmitter`.
 * The orchestrator (Stage 1/2) recomputes session / campaign risk on insert, so
 * `smsRiskScore` is left at 0 here.
 */
class SmsEventBuilder(
    private val digest: SmsEntryDigest,
    private val box: CryptoBox,
) {
    fun build(broadcast: SmsBroadcast): SmsEvent {
        val sender = broadcast.rawSender
        val body = broadcast.body
        val excerpt = body.take(SmsEvent.MAX_BODY_EXCERPT_CHARS)
        val excerptEnc = box.encrypt(excerpt.toByteArray(Charsets.UTF_8))
        return SmsEvent(
            id = EventId(UUID.randomUUID().toString()),
            senderHash = digest.hash(sender),
            senderDisplayNameLocal = sender.trim().take(SENDER_DISPLAY_MAX_CHARS),
            simSlot = broadcast.simSlot,
            receivedAt = broadcast.receivedAt,
            smsCategory = SmsCategoryClassifier.classify(sender, body),
            containsCode = OtpAndIdGuard.isLikelySensitive(body),
            containsLink = SmsKeywordDetector.containsLink(body),
            containsFinancialKeyword = SmsKeywordDetector.containsFinancialKeyword(body),
            containsSecurityKeyword = SmsKeywordDetector.containsSecurityKeyword(body),
            bodyExcerptEnc = excerptEnc,
            smsRiskScore = 0,
            linkedSessionId = null,
            linkedCampaignId = null,
        )
    }

    private companion object {
        const val SENDER_DISPLAY_MAX_CHARS = 80
    }
}
