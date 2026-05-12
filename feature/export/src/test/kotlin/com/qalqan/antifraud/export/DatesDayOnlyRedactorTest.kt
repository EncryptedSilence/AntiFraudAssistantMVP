package com.qalqan.antifraud.export

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant

class DatesDayOnlyRedactorTest {
    @Test
    fun `truncates SuspiciousNumber firstSeenAt to midnight UTC`() {
        val before =
            ExportRecord.SuspiciousNumber(
                phoneFull = "+77001234567",
                phoneLast4 = "4567",
                isShortCode = false,
                displayName = "Bank Alfa",
                trustStatus = "suspicious",
                firstSeenAt = Instant.parse("2026-05-01T14:37:22.481Z"),
                riskCounter = 3,
            )
        val after = DatesDayOnlyRedactor.apply(before) as ExportRecord.SuspiciousNumber
        after.firstSeenAt shouldBe Instant.parse("2026-05-01T00:00:00Z")
    }

    @Test
    fun `truncates RiskCampaign startedAt and lastEventAt`() {
        val before =
            ExportRecord.RiskCampaign(
                campaignId = "c-1",
                startedAt = Instant.parse("2026-05-01T14:37:22Z"),
                lastEventAt = Instant.parse("2026-05-02T03:00:11Z"),
                status = "active",
                scenarioType = "x",
                campaignRiskScore = 10,
                campaignRiskLevel = "medium",
                relatedEventCount = 1,
                explanation = "x",
            )
        val after = DatesDayOnlyRedactor.apply(before) as ExportRecord.RiskCampaign
        after.startedAt shouldBe Instant.parse("2026-05-01T00:00:00Z")
        after.lastEventAt shouldBe Instant.parse("2026-05-02T00:00:00Z")
    }

    @Test
    fun `truncates TriggeredPattern triggeredAt`() {
        val before =
            ExportRecord.TriggeredPattern(
                patternId = "p1",
                name = "Pattern",
                scenarioCategory = "bank_security",
                version = "v1",
                triggeredAt = Instant.parse("2026-05-02T11:59:59.999Z"),
                timesTriggered = 2,
            )
        val after = DatesDayOnlyRedactor.apply(before) as ExportRecord.TriggeredPattern
        after.triggeredAt shouldBe Instant.parse("2026-05-02T00:00:00Z")
    }

    @Test
    fun `idempotent — applying twice produces the same result`() {
        val before =
            ExportRecord.SuspiciousNumber(
                phoneFull = null,
                phoneLast4 = "4567",
                isShortCode = false,
                displayName = null,
                trustStatus = "suspicious",
                firstSeenAt = Instant.parse("2026-05-01T14:37:22.481Z"),
                riskCounter = 3,
            )
        val once = DatesDayOnlyRedactor.apply(before)
        val twice = DatesDayOnlyRedactor.apply(once)
        twice shouldBe once
    }
}
