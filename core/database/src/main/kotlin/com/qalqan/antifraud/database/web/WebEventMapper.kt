package com.qalqan.antifraud.database.web

import com.qalqan.antifraud.domain.CampaignId
import com.qalqan.antifraud.domain.DomainHash
import com.qalqan.antifraud.domain.DomainStatus
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.SessionId
import com.qalqan.antifraud.domain.WebEvent
import java.time.Instant

internal fun WebEvent.toEntity(): WebEventEntity =
    WebEventEntity(
        id = id.value,
        domainHash = domainHash.value,
        domainDisplayLocal = domainDisplayLocal,
        visitedAtMs = visitedAt.toEpochMilli(),
        isNewDomain = isNewDomain,
        domainStatus = domainStatus.name,
        webRiskScore = webRiskScore,
        linkedSessionId = linkedSessionId?.value,
        linkedCampaignId = linkedCampaignId?.value,
    )

internal fun WebEventEntity.toDomain(): WebEvent =
    WebEvent(
        id = EventId(id),
        domainHash = DomainHash(domainHash),
        domainDisplayLocal = domainDisplayLocal,
        visitedAt = Instant.ofEpochMilli(visitedAtMs),
        isNewDomain = isNewDomain,
        domainStatus = DomainStatus.valueOf(domainStatus),
        webRiskScore = webRiskScore,
        linkedSessionId = linkedSessionId?.let(::SessionId),
        linkedCampaignId = linkedCampaignId?.let(::CampaignId),
    )
