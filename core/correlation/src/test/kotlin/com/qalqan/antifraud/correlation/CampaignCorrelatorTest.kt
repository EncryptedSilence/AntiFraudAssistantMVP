package com.qalqan.antifraud.correlation

import com.qalqan.antifraud.domain.CampaignId
import com.qalqan.antifraud.domain.CampaignStatus
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.domain.RiskCampaign
import com.qalqan.antifraud.domain.SenderHash
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class CampaignCorrelatorTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    private fun activeCampaign(
        startedDaysAgo: Long = 1,
        phoneHash: PhoneHash = PhoneHash("h1"),
    ) = RiskCampaign(
        campaignId = CampaignId("camp"),
        startedAt = t.minus(Duration.ofDays(startedDaysAgo)),
        lastEventAt = t.minus(Duration.ofDays(startedDaysAgo)),
        status = CampaignStatus.ACTIVE,
        scenarioType = null,
        relatedPhoneHashes = setOf(phoneHash),
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

    @Test fun `same actor inside 14 day horizon attaches`() {
        val camp = activeCampaign(startedDaysAgo = 5, phoneHash = PhoneHash("h1"))
        val outcome =
            CampaignCorrelator.findOrOpen(
                actorPhoneHash = PhoneHash("h1"),
                actorSenderHash = null,
                now = t,
                activeCampaigns = listOf(camp),
            )
        outcome.shouldBeInstanceOf<CampaignCorrelator.Outcome.Attached>()
        (outcome as CampaignCorrelator.Outcome.Attached).campaignId shouldBe camp.campaignId
    }

    @Test fun `same actor older than 14 days opens a new campaign`() {
        val camp = activeCampaign(startedDaysAgo = 20, phoneHash = PhoneHash("h1"))
        val outcome =
            CampaignCorrelator.findOrOpen(
                actorPhoneHash = PhoneHash("h1"),
                actorSenderHash = null,
                now = t,
                activeCampaigns = listOf(camp),
            )
        outcome.shouldBeInstanceOf<CampaignCorrelator.Outcome.Created>()
    }

    @Test fun `different actor opens new campaign`() {
        val camp = activeCampaign(phoneHash = PhoneHash("h1"))
        val outcome =
            CampaignCorrelator.findOrOpen(
                actorPhoneHash = PhoneHash("h2"),
                actorSenderHash = SenderHash("s1"),
                now = t,
                activeCampaigns = listOf(camp),
            )
        outcome.shouldBeInstanceOf<CampaignCorrelator.Outcome.Created>()
    }
}
