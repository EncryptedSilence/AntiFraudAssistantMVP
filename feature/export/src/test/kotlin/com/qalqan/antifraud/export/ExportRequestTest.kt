package com.qalqan.antifraud.export

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.Test

class ExportRequestTest {
    @Test
    fun `valid request is constructible with a single category`() {
        val r =
            ExportRequest(
                categories = setOf(ExportCategory.SUSPICIOUS_NUMBERS),
                format = ExportFormat.JSON,
                anonymization = emptySet(),
            )
        r.categories shouldBe setOf(ExportCategory.SUSPICIOUS_NUMBERS)
        r.format shouldBe ExportFormat.JSON
        r.anonymization shouldBe emptySet()
    }

    @Test
    fun `valid request is constructible with all three categories and all three options`() {
        val r =
            ExportRequest(
                categories = ExportCategory.entries.toSet(),
                format = ExportFormat.TXT,
                anonymization = AnonymizationOption.OPERATIONAL,
            )
        r.categories.size shouldBe 3
        r.anonymization.size shouldBe 3
    }

    @Test
    fun `empty categories set is rejected`() {
        shouldThrow<IllegalArgumentException> {
            ExportRequest(
                categories = emptySet(),
                format = ExportFormat.TXT,
                anonymization = emptySet(),
            )
        }
    }

    @Test
    fun `anonymization defaults to empty when omitted`() {
        val r =
            ExportRequest(
                categories = setOf(ExportCategory.RISK_CAMPAIGNS),
                format = ExportFormat.CSV,
            )
        r.anonymization shouldBe emptySet()
    }
}
