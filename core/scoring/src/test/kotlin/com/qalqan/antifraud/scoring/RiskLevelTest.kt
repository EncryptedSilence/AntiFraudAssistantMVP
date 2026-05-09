package com.qalqan.antifraud.scoring

import com.qalqan.antifraud.domain.RiskBand
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class RiskLevelTest {
    @Test fun `boundaries map per spec section 11 dot 5`() {
        RiskLevel.fromScore(0) shouldBe RiskBand.LOW
        RiskLevel.fromScore(30) shouldBe RiskBand.LOW
        RiskLevel.fromScore(31) shouldBe RiskBand.MEDIUM
        RiskLevel.fromScore(60) shouldBe RiskBand.MEDIUM
        RiskLevel.fromScore(61) shouldBe RiskBand.HIGH
        RiskLevel.fromScore(80) shouldBe RiskBand.HIGH
        RiskLevel.fromScore(81) shouldBe RiskBand.CRITICAL
        RiskLevel.fromScore(100) shouldBe RiskBand.CRITICAL
    }

    @Test fun `out of range scores are rejected`() {
        shouldThrow<IllegalArgumentException> { RiskLevel.fromScore(-1) }
        shouldThrow<IllegalArgumentException> { RiskLevel.fromScore(101) }
    }
}
