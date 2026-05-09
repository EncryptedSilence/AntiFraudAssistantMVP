package com.qalqan.antifraud.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class RiskCampaignTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    private fun campaign(
        lastEventAt: Instant = t,
        riskScore: Int = 0,
        scenarioType: ScenarioCategory? = null,
    ) = RiskCampaign(
        campaignId = CampaignId("c1"),
        startedAt = t,
        lastEventAt = lastEventAt,
        status = CampaignStatus.ACTIVE,
        scenarioType = scenarioType,
        relatedPhoneHashes = emptySet(),
        relatedSmsSenderHashes = emptySet(),
        relatedDomainHashes = emptySet(),
        relatedEventIds = emptyList(),
        relatedSessionIds = emptyList(),
        userAnswerIds = emptyList(),
        triggeredPatternIds = emptyList(),
        campaignRiskScore = riskScore,
        campaignRiskBand = RiskBand.LOW,
        explanation = null,
    )

    @Test fun `lastEventAt cannot precede startedAt`() {
        shouldThrow<IllegalArgumentException> {
            campaign(lastEventAt = t.minusSeconds(1))
        }
    }

    @Test fun `risk score bounded`() {
        shouldThrow<IllegalArgumentException> { campaign(riskScore = 101) }
    }

    @Test fun `valid campaign accepted`() {
        campaign().status shouldBe CampaignStatus.ACTIVE
    }
}
