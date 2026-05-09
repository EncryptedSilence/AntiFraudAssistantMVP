package com.qalqan.antifraud.scoring

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PatternRiskTest {
    @Test fun `sum of triggered weights, capped at 60`() {
        PatternRisk.compute(listOf(20, 30)) shouldBe 50
        PatternRisk.compute(listOf(30, 30, 30)) shouldBe 60
        PatternRisk.compute(emptyList()) shouldBe 0
    }

    @Test fun `negative weights are clamped to zero`() {
        PatternRisk.compute(listOf(-5, 20)) shouldBe 20
    }
}
