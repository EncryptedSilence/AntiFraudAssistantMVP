package com.qalqan.antifraud.export

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.Test

class AnonymizationOptionTest {
    @Test
    fun `the three operational variants are singletons (data object)`() {
        val a: AnonymizationOption = AnonymizationOption.NumbersLast4
        val b: AnonymizationOption = AnonymizationOption.NumbersLast4
        (a === b) shouldBe true

        AnonymizationOption.NumbersLast4.shouldBeInstanceOf<AnonymizationOption.NumbersLast4>()
        AnonymizationOption.DomainZoneOnly.shouldBeInstanceOf<AnonymizationOption.DomainZoneOnly>()
        AnonymizationOption.DatesDayOnly.shouldBeInstanceOf<AnonymizationOption.DatesDayOnly>()
    }

    @Test
    fun `jsonValue is the lower_snake form used by ExportProfile_anonymizationLevel`() {
        AnonymizationOption.NumbersLast4.jsonValue shouldBe "numbers_last_4"
        AnonymizationOption.DomainZoneOnly.jsonValue shouldBe "domain_zone_only"
        AnonymizationOption.DatesDayOnly.jsonValue shouldBe "dates_day_only"
    }

    @Test
    fun `OPERATIONAL contains exactly the three demo-mandatory options`() {
        AnonymizationOption.OPERATIONAL.size shouldBe 3
        AnonymizationOption.OPERATIONAL shouldBe
            setOf(
                AnonymizationOption.NumbersLast4,
                AnonymizationOption.DomainZoneOnly,
                AnonymizationOption.DatesDayOnly,
            )
    }
}
