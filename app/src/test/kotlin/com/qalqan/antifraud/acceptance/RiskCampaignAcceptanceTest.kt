package com.qalqan.antifraud.acceptance

import com.qalqan.antifraud.correlation.CampaignCorrelator
import com.qalqan.antifraud.domain.PhoneHash
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class RiskCampaignAcceptanceTest {
    @Test
    fun `events at day 1, 3, 7 attach to a single active campaign (spec §23 #7)`() {
        val day1 = Instant.parse("2026-05-01T10:00:00Z")
        val day3 = day1.plus(Duration.ofDays(2))
        val day7 = day1.plus(Duration.ofDays(6))

        // Day 1: empty world -> new campaign
        val o1 =
            CampaignCorrelator.findOrOpen(
                actorPhoneHash = PhoneHash("h1"),
                actorSenderHash = null,
                now = day1,
                activeCampaigns = emptyList(),
            )
        val camp = (o1 as CampaignCorrelator.Outcome.Created).campaign

        // Day 3: same actor -> attached
        val o2 =
            CampaignCorrelator.findOrOpen(
                actorPhoneHash = PhoneHash("h1"),
                actorSenderHash = null,
                now = day3,
                activeCampaigns = listOf(camp),
            )
        o2.shouldBeInstanceOf<CampaignCorrelator.Outcome.Attached>()
        (o2 as CampaignCorrelator.Outcome.Attached).campaignId shouldBe camp.campaignId

        // Day 7: same actor -> still attached (within 14d horizon)
        val o3 =
            CampaignCorrelator.findOrOpen(
                actorPhoneHash = PhoneHash("h1"),
                actorSenderHash = null,
                now = day7,
                activeCampaigns = listOf(camp),
            )
        o3.shouldBeInstanceOf<CampaignCorrelator.Outcome.Attached>()
    }
}
