package com.qalqan.antifraud.database.sms

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sms_event",
    indices = [Index("receivedAtMs"), Index("senderHash")],
)
internal data class SmsEventEntity(
    @PrimaryKey val id: String,
    val senderHash: String,
    val senderDisplayNameLocal: String?,
    val simSlot: Int?,
    val receivedAtMs: Long,
    val smsCategory: String,
    val containsCode: Boolean,
    val containsLink: Boolean,
    val containsFinancialKeyword: Boolean,
    val containsSecurityKeyword: Boolean,
    val bodyExcerptEnc: ByteArray,
    val smsRiskScore: Int,
    val linkedSessionId: String?,
    val linkedCampaignId: String?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SmsEventEntity) return false
        return id == other.id &&
            senderHash == other.senderHash &&
            senderDisplayNameLocal == other.senderDisplayNameLocal &&
            simSlot == other.simSlot &&
            receivedAtMs == other.receivedAtMs &&
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
        r = 31 * r + receivedAtMs.hashCode()
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
}
