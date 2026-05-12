package com.qalqan.antifraud.export

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant

class RedactionPipelineTest {
    private val sampleNumber: ExportRecord.SuspiciousNumber =
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
    fun `empty options set returns records unchanged`() {
        val pipeline = RedactionPipeline.default()
        val out = pipeline.apply(listOf(sampleNumber), emptySet())
        out shouldBe listOf(sampleNumber)
    }

    @Test
    fun `pipeline preserves list size`() {
        val records = listOf(sampleNumber, sampleNumber.copy(phoneLast4 = "9999"))
        val pipeline = RedactionPipeline.default()
        val out = pipeline.apply(records, AnonymizationOption.OPERATIONAL)
        out.size shouldBe 2
    }
}
