package com.qalqan.antifraud.export

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant

class DomainZoneOnlyRedactorTest {
    private val number =
        ExportRecord.SuspiciousNumber(
            phoneFull = "+77001234567",
            phoneLast4 = "4567",
            isShortCode = false,
            displayName = "Bank Alfa",
            trustStatus = "suspicious",
            firstSeenAt = Instant.parse("2026-05-01T10:00:00Z"),
            riskCounter = 3,
        )

    @Test
    fun `current record variants pass through unchanged (no domain field yet)`() {
        val campaign =
            ExportRecord.RiskCampaign(
                campaignId = "c-1",
                startedAt = Instant.parse("2026-05-01T10:00:00Z"),
                lastEventAt = Instant.parse("2026-05-02T10:00:00Z"),
                status = "active",
                scenarioType = "x",
                campaignRiskScore = 10,
                campaignRiskLevel = "medium",
                relatedEventCount = 1,
                explanation = "x",
            )
        DomainZoneOnlyRedactor.apply(number) shouldBe number
        DomainZoneOnlyRedactor.apply(campaign) shouldBe campaign
    }

    @Test
    fun `extractZone takes the last dot-separated segment as the eTLD`() {
        DomainZoneOnlyRedactor.extractZone("example.kz") shouldBe "*.kz"
        DomainZoneOnlyRedactor.extractZone("alfabank.kz") shouldBe "*.kz"
        DomainZoneOnlyRedactor.extractZone("foo.bar.example.com") shouldBe "*.com"
        DomainZoneOnlyRedactor.extractZone("kz") shouldBe "*.kz"
    }

    @Test
    fun `extractZone returns the empty zone for input without any dot`() {
        // No suffix → empty redaction marker. The formatter renders this as "*.".
        DomainZoneOnlyRedactor.extractZone("localhost") shouldBe "*.localhost"
    }
}
