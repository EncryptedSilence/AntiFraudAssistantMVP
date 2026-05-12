package com.qalqan.antifraud.export

import io.kotest.matchers.shouldBe
import org.junit.Test

class ExportCategoryTest {
    @Test
    fun `enum has exactly three variants for Stage 7`() {
        ExportCategory.entries.size shouldBe 3
    }

    @Test
    fun `variant names match the spec §8_2 closed enum`() {
        ExportCategory.SUSPICIOUS_NUMBERS.name shouldBe "SUSPICIOUS_NUMBERS"
        ExportCategory.RISK_CAMPAIGNS.name shouldBe "RISK_CAMPAIGNS"
        ExportCategory.TRIGGERED_PATTERNS.name shouldBe "TRIGGERED_PATTERNS"
    }

    @Test
    fun `jsonValue is the lower_snake form for the JSON formatter contract`() {
        ExportCategory.SUSPICIOUS_NUMBERS.jsonValue shouldBe "suspicious_numbers"
        ExportCategory.RISK_CAMPAIGNS.jsonValue shouldBe "risk_campaigns"
        ExportCategory.TRIGGERED_PATTERNS.jsonValue shouldBe "triggered_patterns"
    }
}
