package com.qalqan.antifraud.scoring

import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SensitivityTest {
    @Test fun `multipliers per Appendix B`() {
        Sensitivity.LOW.eventMultiplier shouldBeExactly 0.7
        Sensitivity.STANDARD.eventMultiplier shouldBeExactly 1.0
        Sensitivity.HIGH.eventMultiplier shouldBeExactly 1.2
        Sensitivity.MAXIMUM.eventMultiplier shouldBeExactly 1.4
    }

    @Test fun `threshold offsets per Appendix B`() {
        Sensitivity.LOW.thresholdOffset shouldBe 10
        Sensitivity.STANDARD.thresholdOffset shouldBe 0
        Sensitivity.HIGH.thresholdOffset shouldBe -10
        Sensitivity.MAXIMUM.thresholdOffset shouldBe -15
    }

    @Test fun `applyTo scales and caps at 100`() {
        Sensitivity.HIGH.applyTo(80) shouldBe 96
        Sensitivity.MAXIMUM.applyTo(80) shouldBe 100
        Sensitivity.LOW.applyTo(50) shouldBe 35
    }
}
