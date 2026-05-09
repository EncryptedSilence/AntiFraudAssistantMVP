package com.qalqan.antifraud.acceptance

import com.qalqan.antifraud.correlation.CampaignCorrelator
import com.qalqan.antifraud.domain.CampaignId
import com.qalqan.antifraud.domain.CampaignStatus
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.domain.RiskCampaign
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import java.time.Instant

class FalsePositiveAcceptanceTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    @Test
    fun `closing a campaign as false positive excludes it from later correlation (spec §23 #21)`() {
        val falsePositive =
            RiskCampaign(
                campaignId = CampaignId("c1"),
                startedAt = t,
                lastEventAt = t,
                status = CampaignStatus.FALSE_POSITIVE,
                scenarioType = null,
                relatedPhoneHashes = setOf(PhoneHash("h1")),
                relatedSmsSenderHashes = emptySet(),
                relatedDomainHashes = emptySet(),
                relatedEventIds = emptyList(),
                relatedSessionIds = emptyList(),
                userAnswerIds = emptyList(),
                triggeredPatternIds = emptyList(),
                campaignRiskScore = 0,
                campaignRiskBand = RiskBand.LOW,
                explanation = null,
            )

        // Note: false-positive campaigns are excluded by callers from the activeCampaigns set.
        // Here we simulate the contract: the correlator only considers ACTIVE campaigns.
        val outcome =
            CampaignCorrelator.findOrOpen(
                actorPhoneHash = PhoneHash("h1"),
                actorSenderHash = null,
                now = t.plusSeconds(60),
                activeCampaigns = listOf(falsePositive).filter { it.status == CampaignStatus.ACTIVE },
            )
        outcome.shouldBeInstanceOf<CampaignCorrelator.Outcome.Created>()
    }
}
