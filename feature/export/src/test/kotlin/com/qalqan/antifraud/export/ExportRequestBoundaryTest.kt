package com.qalqan.antifraud.export

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.Test

class ExportRequestBoundaryTest {
    @Test
    fun `empty categories is rejected at construction time`() {
        shouldThrow<IllegalArgumentException> {
            ExportRequest(emptySet(), ExportFormat.JSON)
        }
    }

    @Test
    fun `two ExportRequests are equal when their content matches regardless of Set order`() {
        val a =
            ExportRequest(
                categories = linkedSetOf(ExportCategory.SUSPICIOUS_NUMBERS, ExportCategory.RISK_CAMPAIGNS),
                format = ExportFormat.JSON,
                anonymization = linkedSetOf(AnonymizationOption.NumbersLast4, AnonymizationOption.DatesDayOnly),
            )
        val b =
            ExportRequest(
                categories = linkedSetOf(ExportCategory.RISK_CAMPAIGNS, ExportCategory.SUSPICIOUS_NUMBERS),
                format = ExportFormat.JSON,
                anonymization = linkedSetOf(AnonymizationOption.DatesDayOnly, AnonymizationOption.NumbersLast4),
            )
        a shouldBe b
        a.hashCode() shouldBe b.hashCode()
    }

    @Test
    fun `single-category single-format empty-anonymization request is the MVP simplest form`() {
        val r = ExportRequest(setOf(ExportCategory.SUSPICIOUS_NUMBERS), ExportFormat.CSV)
        r.categories.size shouldBe 1
        r.anonymization.size shouldBe 0
    }

    @Test
    fun `every (category, format, anonymization-subset) triple is constructible`() {
        // Exhaustive (3 categories × 4 formats × 2^3 options) = 96 combinations.
        // The product compiles and constructs; this pins the type-level coverage.
        val combinations = mutableListOf<ExportRequest>()
        ExportCategory.entries.forEach { c ->
            ExportFormat.entries.forEach { f ->
                listOf(
                    emptySet<AnonymizationOption>(),
                    setOf(AnonymizationOption.NumbersLast4),
                    setOf(AnonymizationOption.DomainZoneOnly),
                    setOf(AnonymizationOption.DatesDayOnly),
                    setOf(AnonymizationOption.NumbersLast4, AnonymizationOption.DomainZoneOnly),
                    setOf(AnonymizationOption.NumbersLast4, AnonymizationOption.DatesDayOnly),
                    setOf(AnonymizationOption.DomainZoneOnly, AnonymizationOption.DatesDayOnly),
                    AnonymizationOption.OPERATIONAL,
                ).forEach { opts ->
                    combinations += ExportRequest(setOf(c), f, opts)
                }
            }
        }
        combinations.size shouldBe 3 * 4 * 8
    }
}
