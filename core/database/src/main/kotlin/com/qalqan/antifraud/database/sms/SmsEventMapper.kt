package com.qalqan.antifraud.database.sms

import com.qalqan.antifraud.domain.CampaignId
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.SenderHash
import com.qalqan.antifraud.domain.SessionId
import com.qalqan.antifraud.domain.SmsCategory
import com.qalqan.antifraud.domain.SmsEvent
import java.time.Instant

internal fun SmsEvent.toEntity(): SmsEventEntity =
    SmsEventEntity(
        id = id.value,
        senderHash = senderHash.value,
        senderDisplayNameLocal = senderDisplayNameLocal,
        simSlot = simSlot,
        receivedAtMs = receivedAt.toEpochMilli(),
        smsCategory = smsCategory.name,
        containsCode = containsCode,
        containsLink = containsLink,
        containsFinancialKeyword = containsFinancialKeyword,
        containsSecurityKeyword = containsSecurityKeyword,
        bodyExcerptEnc = bodyExcerptEnc,
        smsRiskScore = smsRiskScore,
        linkedSessionId = linkedSessionId?.value,
        linkedCampaignId = linkedCampaignId?.value,
    )

internal fun SmsEventEntity.toDomain(): SmsEvent =
    SmsEvent(
        id = EventId(id),
        senderHash = SenderHash(senderHash),
        senderDisplayNameLocal = senderDisplayNameLocal,
        simSlot = simSlot,
        receivedAt = Instant.ofEpochMilli(receivedAtMs),
        smsCategory = SmsCategory.valueOf(smsCategory),
        containsCode = containsCode,
        containsLink = containsLink,
        containsFinancialKeyword = containsFinancialKeyword,
        containsSecurityKeyword = containsSecurityKeyword,
        bodyExcerptEnc = bodyExcerptEnc,
        smsRiskScore = smsRiskScore,
        linkedSessionId = linkedSessionId?.let(::SessionId),
        linkedCampaignId = linkedCampaignId?.let(::CampaignId),
    )
