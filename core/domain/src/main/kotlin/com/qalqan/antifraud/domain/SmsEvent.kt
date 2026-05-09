package com.qalqan.antifraud.domain

import java.time.Instant

/**
 * Spec §16.3 + §5.3 — body excerpt is encrypted at rest, capped at 200 chars (≤512 bytes after AEAD).
 */
data class SmsEvent(
    val id: EventId,
    val senderHash: SenderHash,
    val senderDisplayNameLocal: String?,
    val simSlot: Int?,
    val receivedAt: Instant,
    val smsCategory: SmsCategory,
    val containsCode: Boolean,
    val containsLink: Boolean,
    val containsFinancialKeyword: Boolean,
    val containsSecurityKeyword: Boolean,
    val bodyExcerptEnc: ByteArray,
    val smsRiskScore: Int,
    val linkedSessionId: SessionId?,
    val linkedCampaignId: CampaignId?,
) {
    init {
        require(bodyExcerptEnc.size <= MAX_BODY_EXCERPT_BYTES) {
            "bodyExcerptEnc must be at most $MAX_BODY_EXCERPT_BYTES bytes"
        }
        require(smsRiskScore in 0..100) { "smsRiskScore must be in 0..100" }
        simSlot?.let { require(it >= 0) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SmsEvent) return false
        return id == other.id &&
            senderHash == other.senderHash &&
            senderDisplayNameLocal == other.senderDisplayNameLocal &&
            simSlot == other.simSlot &&
            receivedAt == other.receivedAt &&
            smsCategory == other.smsCategory &&
            containsCode == other.containsCode &&
            containsLink == other.containsLink &&
            containsFinancialKeyword == other.containsFinancialKeyword &&
            containsSecurityKeyword == other.containsSecurityKeyword &&
            bodyExcerptEnc.contentEquals(other.bodyExcerptEnc) &&
            smsRiskScore == other.smsRiskScore &&
            linkedSessionId == other.linkedSessionId &&
            linkedCampaignId == other.linkedCampaignId
    }

    override fun hashCode(): Int {
        var r = id.hashCode()
        r = 31 * r + senderHash.hashCode()
        r = 31 * r + (senderDisplayNameLocal?.hashCode() ?: 0)
        r = 31 * r + (simSlot ?: 0)
        r = 31 * r + receivedAt.hashCode()
        r = 31 * r + smsCategory.hashCode()
        r = 31 * r + containsCode.hashCode()
        r = 31 * r + containsLink.hashCode()
        r = 31 * r + containsFinancialKeyword.hashCode()
        r = 31 * r + containsSecurityKeyword.hashCode()
        r = 31 * r + bodyExcerptEnc.contentHashCode()
        r = 31 * r + smsRiskScore
        r = 31 * r + (linkedSessionId?.hashCode() ?: 0)
        r = 31 * r + (linkedCampaignId?.hashCode() ?: 0)
        return r
    }

    companion object {
        const val MAX_BODY_EXCERPT_BYTES: Int = 512
        const val MAX_BODY_EXCERPT_CHARS: Int = 200
    }
}
