package com.qalqan.antifraud.export

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test
import java.time.Instant

class ExportRecordTest {
    @Test
    fun `SuspiciousNumber carries the §16_1 ContactProfile fields needed by all four formats`() {
        val r =
            ExportRecord.SuspiciousNumber(
                phoneFull = "+77001234567",
                phoneLast4 = "4567",
                isShortCode = false,
                displayName = "Bank Alfa",
                trustStatus = "suspicious",
                firstSeenAt = Instant.parse("2026-05-01T10:00:00Z"),
                riskCounter = 3,
            )
        r.category shouldBe ExportCategory.SUSPICIOUS_NUMBERS
        r.phoneFull shouldBe "+77001234567"
        r.phoneLast4 shouldBe "4567"
        r.shouldBeInstanceOf<ExportRecord.SuspiciousNumber>()
    }

    @Test
    fun `RiskCampaign carries the §16_7 RiskCampaign fields plus a stable id`() {
        val r =
            ExportRecord.RiskCampaign(
                campaignId = "c-1",
                startedAt = Instant.parse("2026-05-01T10:00:00Z"),
                lastEventAt = Instant.parse("2026-05-02T11:00:00Z"),
                status = "active",
                scenarioType = "bank_security_otp_after_call",
                campaignRiskScore = 87,
                campaignRiskLevel = "critical",
                relatedEventCount = 5,
                explanation = "unknown call then SMS within 24h",
            )
        r.category shouldBe ExportCategory.RISK_CAMPAIGNS
        r.campaignRiskLevel shouldBe "critical"
    }

    @Test
    fun `TriggeredPattern carries the §16_8 ScenarioPattern subset needed for export`() {
        val r =
            ExportRecord.TriggeredPattern(
                patternId = "bank_security_otp_after_call_v1",
                name = "Bank security OTP after call",
                scenarioCategory = "bank_security",
                version = "v1",
                triggeredAt = Instant.parse("2026-05-02T11:00:00Z"),
                timesTriggered = 2,
            )
        r.category shouldBe ExportCategory.TRIGGERED_PATTERNS
        r.scenarioCategory shouldBe "bank_security"
        r.timesTriggered shouldBe 2
    }

    @Test
    fun `phoneFull and domainFull are nullable so the redaction pipeline can clear them`() {
        // After NumbersLast4 anonymization, phoneFull is null and phoneLast4 carries the digits.
        val redacted =
            ExportRecord.SuspiciousNumber(
                phoneFull = null,
                phoneLast4 = "4567",
                isShortCode = false,
                displayName = "Bank Alfa",
                trustStatus = "suspicious",
                firstSeenAt = Instant.parse("2026-05-01T10:00:00Z"),
                riskCounter = 3,
            )
        redacted.phoneFull shouldBe null
        redacted.phoneLast4 shouldBe "4567"
    }
}
