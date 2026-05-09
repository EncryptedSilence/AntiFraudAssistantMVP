package com.qalqan.antifraud.correlation

import com.qalqan.antifraud.domain.CampaignId
import com.qalqan.antifraud.domain.CampaignStatus
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.domain.RiskCampaign
import com.qalqan.antifraud.domain.SenderHash
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Spec §3.3 — 14-day active correlation horizon. After that the campaign no longer attracts
 * events; it is moved to archive (retention §15.2) but does not appear in `activeCampaigns`.
 */
object CampaignCorrelator {
    private val HORIZON: Duration = Duration.ofDays(RiskCampaign.ACTIVE_HORIZON_DAYS)

    sealed interface Outcome {
        data class Attached(val campaignId: CampaignId) : Outcome

        data class Created(val campaign: RiskCampaign) : Outcome
    }

    @Suppress("MaxLineLength")
    fun findOrOpen(
        actorPhoneHash: PhoneHash?,
        actorSenderHash: SenderHash?,
        now: Instant,
        activeCampaigns: List<RiskCampaign>,
    ): Outcome {
        val match =
            activeCampaigns.firstOrNull { camp ->
                val withinHorizon = Duration.between(camp.startedAt, now) <= HORIZON
                val sameActor =
                    (actorPhoneHash != null && actorPhoneHash in camp.relatedPhoneHashes) ||
                        (actorSenderHash != null && actorSenderHash in camp.relatedSmsSenderHashes)
                withinHorizon && sameActor
            }
        return if (match != null) Outcome.Attached(match.campaignId) else Outcome.Created(empty(actorPhoneHash, actorSenderHash, now))
    }

    private fun empty(
        actorPhoneHash: PhoneHash?,
        actorSenderHash: SenderHash?,
        now: Instant,
    ): RiskCampaign =
        RiskCampaign(
            campaignId = CampaignId(UUID.randomUUID().toString()),
            startedAt = now,
            lastEventAt = now,
            status = CampaignStatus.ACTIVE,
            scenarioType = null,
            relatedPhoneHashes = listOfNotNull(actorPhoneHash).toSet(),
            relatedSmsSenderHashes = listOfNotNull(actorSenderHash).toSet(),
            relatedDomainHashes = emptySet(),
            relatedEventIds = emptyList(),
            relatedSessionIds = emptyList(),
            userAnswerIds = emptyList(),
            triggeredPatternIds = emptyList(),
            campaignRiskScore = 0,
            campaignRiskBand = RiskBand.LOW,
            explanation = null,
        )

    fun archiveExpired(
        campaigns: List<RiskCampaign>,
        now: Instant,
    ): List<RiskCampaign> =
        campaigns.map { c ->
            if (c.status == CampaignStatus.ACTIVE &&
                Duration.between(c.startedAt, now) > HORIZON
            ) {
                c.copy(status = CampaignStatus.ARCHIVED)
            } else {
                c
            }
        }
}
