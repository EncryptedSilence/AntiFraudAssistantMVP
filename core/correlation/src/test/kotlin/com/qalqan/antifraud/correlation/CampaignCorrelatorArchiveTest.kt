package com.qalqan.antifraud.correlation

import com.qalqan.antifraud.domain.CampaignId
import com.qalqan.antifraud.domain.CampaignStatus
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.domain.RiskCampaign
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class CampaignCorrelatorArchiveTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    private fun camp(
        daysAgo: Long,
        status: CampaignStatus = CampaignStatus.ACTIVE,
    ) = RiskCampaign(
        campaignId = CampaignId("c-$daysAgo"),
        startedAt = t.minus(Duration.ofDays(daysAgo)),
        lastEventAt = t.minus(Duration.ofDays(daysAgo)),
        status = status,
        scenarioType = null,
        relatedPhoneHashes = emptySet(),
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

    @Test fun `campaigns older than 14 days are archived`() {
        val c = camp(daysAgo = 15)
        CampaignCorrelator.archiveExpired(listOf(c), now = t)
            .single().status shouldBe CampaignStatus.ARCHIVED
    }

    @Test fun `campaigns within horizon stay active`() {
        val c = camp(daysAgo = 13)
        CampaignCorrelator.archiveExpired(listOf(c), now = t)
            .single().status shouldBe CampaignStatus.ACTIVE
    }

    @Test fun `false positives are not re-archived`() {
        val c = camp(daysAgo = 30, status = CampaignStatus.FALSE_POSITIVE)
        CampaignCorrelator.archiveExpired(listOf(c), now = t)
            .single().status shouldBe CampaignStatus.FALSE_POSITIVE
    }
}
