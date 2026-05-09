package com.qalqan.antifraud.database.calls

import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.CampaignId
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.SessionId
import java.time.Instant

internal fun CallEvent.toEntity(): CallEventEntity =
    CallEventEntity(
        id = id.value,
        phoneHash = phoneHash.value,
        simSlot = simSlot,
        direction = direction.name,
        startedAtMs = startedAt.toEpochMilli(),
        endedAtMs = endedAt?.toEpochMilli(),
        durationSec = durationSec,
        isKnownContact = isKnownContact,
        isRepeated = isRepeated,
        callRiskScore = callRiskScore,
        linkedSessionId = linkedSessionId?.value,
        linkedCampaignId = linkedCampaignId?.value,
    )

internal fun CallEventEntity.toDomain(): CallEvent =
    CallEvent(
        id = EventId(id),
        phoneHash = PhoneHash(phoneHash),
        simSlot = simSlot,
        direction = CallDirection.valueOf(direction),
        startedAt = Instant.ofEpochMilli(startedAtMs),
        endedAt = endedAtMs?.let(Instant::ofEpochMilli),
        durationSec = durationSec,
        isKnownContact = isKnownContact,
        isRepeated = isRepeated,
        callRiskScore = callRiskScore,
        linkedSessionId = linkedSessionId?.let(::SessionId),
        linkedCampaignId = linkedCampaignId?.let(::CampaignId),
    )
