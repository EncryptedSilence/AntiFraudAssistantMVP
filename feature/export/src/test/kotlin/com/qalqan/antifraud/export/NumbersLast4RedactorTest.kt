package com.qalqan.antifraud.export

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant

class NumbersLast4RedactorTest {
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
    fun `clears phoneFull and keeps phoneLast4`() {
        val out = NumbersLast4Redactor.apply(number) as ExportRecord.SuspiciousNumber
        out.phoneFull shouldBe null
        out.phoneLast4 shouldBe "4567"
    }

    @Test
    fun `idempotent — applying twice produces the same result`() {
        val once = NumbersLast4Redactor.apply(number)
        val twice = NumbersLast4Redactor.apply(once)
        twice shouldBe once
    }

    @Test
    fun `non-number records pass through unchanged`() {
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
        NumbersLast4Redactor.apply(campaign) shouldBe campaign
    }
}
