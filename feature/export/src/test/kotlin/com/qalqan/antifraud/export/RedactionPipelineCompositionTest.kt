package com.qalqan.antifraud.export

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant

class RedactionPipelineCompositionTest {
    private val sample: List<ExportRecord> =
        listOf(
            ExportRecord.SuspiciousNumber(
                phoneFull = "+77001234567",
                phoneLast4 = "4567",
                isShortCode = false,
                displayName = "Bank Alfa",
                trustStatus = "suspicious",
                firstSeenAt = Instant.parse("2026-05-01T14:37:22Z"),
                riskCounter = 3,
            ),
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
            ),
            ExportRecord.TriggeredPattern(
                patternId = "bank_security_otp_after_call_v1",
                name = "Bank security OTP after call",
                scenarioCategory = "bank_security",
                version = "v1",
                triggeredAt = Instant.parse("2026-05-02T11:00:00Z"),
                timesTriggered = 2,
            ),
        )

    @Test
    fun `applying NumbersLast4 then DatesDayOnly equals DatesDayOnly then NumbersLast4`() {
        val pipeline = RedactionPipeline.default()
        val a = pipeline.apply(sample, linkedSetOf(AnonymizationOption.NumbersLast4, AnonymizationOption.DatesDayOnly))
        val b = pipeline.apply(sample, linkedSetOf(AnonymizationOption.DatesDayOnly, AnonymizationOption.NumbersLast4))
        a shouldBe b
    }

    @Test
    fun `applying all three options in every order produces the same result`() {
        val n = AnonymizationOption.NumbersLast4
        val d = AnonymizationOption.DomainZoneOnly
        val t = AnonymizationOption.DatesDayOnly
        val pipeline = RedactionPipeline.default()
        val triplets =
            listOf(
                listOf(n, d, t),
                listOf(n, t, d),
                listOf(d, n, t),
                listOf(d, t, n),
                listOf(t, n, d),
                listOf(t, d, n),
            )
        val outputs =
            triplets.map { ordering ->
                pipeline.apply(sample, LinkedHashSet(ordering))
            }
        outputs.toSet().size shouldBe 1
    }

    @Test
    fun `applying NumbersLast4 + DatesDayOnly produces the expected SuspiciousNumber fields`() {
        val pipeline = RedactionPipeline.default()
        val out =
            pipeline.apply(
                sample,
                setOf(AnonymizationOption.NumbersLast4, AnonymizationOption.DatesDayOnly),
            )
        val number = out[0] as ExportRecord.SuspiciousNumber
        number.phoneFull shouldBe null
        number.phoneLast4 shouldBe "4567"
        number.firstSeenAt shouldBe Instant.parse("2026-05-01T00:00:00Z")
    }

    @Test
    fun `empty options set is the identity`() {
        val pipeline = RedactionPipeline.default()
        pipeline.apply(sample, emptySet()) shouldBe sample
    }
}
