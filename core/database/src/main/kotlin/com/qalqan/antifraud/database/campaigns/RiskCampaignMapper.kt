package com.qalqan.antifraud.database.campaigns

import com.qalqan.antifraud.domain.AnswerId
import com.qalqan.antifraud.domain.CampaignId
import com.qalqan.antifraud.domain.CampaignStatus
import com.qalqan.antifraud.domain.DomainHash
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PatternId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.domain.RiskCampaign
import com.qalqan.antifraud.domain.ScenarioCategory
import com.qalqan.antifraud.domain.SenderHash
import com.qalqan.antifraud.domain.SessionId
import java.time.Instant

internal fun RiskCampaign.toEntity(): RiskCampaignEntity =
    RiskCampaignEntity(
        campaignId = campaignId.value,
        startedAtMs = startedAt.toEpochMilli(),
        lastEventAtMs = lastEventAt.toEpochMilli(),
        status = status.name,
        scenarioType = scenarioType?.name,
        relatedPhoneHashes = relatedPhoneHashes.map { it.value },
        relatedSmsSenderHashes = relatedSmsSenderHashes.map { it.value },
        relatedDomainHashes = relatedDomainHashes.map { it.value },
        relatedEventIds = relatedEventIds.map { it.value },
        relatedSessionIds = relatedSessionIds.map { it.value },
        userAnswerIds = userAnswerIds.map { it.value },
        triggeredPatternIds = triggeredPatternIds.map { it.value },
        campaignRiskScore = campaignRiskScore,
        campaignRiskBand = campaignRiskBand.name,
        explanation = explanation,
    )

internal fun RiskCampaignEntity.toDomain(): RiskCampaign =
    RiskCampaign(
        campaignId = CampaignId(campaignId),
        startedAt = Instant.ofEpochMilli(startedAtMs),
        lastEventAt = Instant.ofEpochMilli(lastEventAtMs),
        status = CampaignStatus.valueOf(status),
        scenarioType = scenarioType?.let { ScenarioCategory.valueOf(it) },
        relatedPhoneHashes = relatedPhoneHashes.map(::PhoneHash).toSet(),
        relatedSmsSenderHashes = relatedSmsSenderHashes.map(::SenderHash).toSet(),
        relatedDomainHashes = relatedDomainHashes.map(::DomainHash).toSet(),
        relatedEventIds = relatedEventIds.map(::EventId),
        relatedSessionIds = relatedSessionIds.map(::SessionId),
        userAnswerIds = userAnswerIds.map(::AnswerId),
        triggeredPatternIds = triggeredPatternIds.map(::PatternId),
        campaignRiskScore = campaignRiskScore,
        campaignRiskBand = RiskBand.valueOf(campaignRiskBand),
        explanation = explanation,
    )
