package com.qalqan.antifraud.scoring

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SessionRiskCalculatorTest {
    @Test fun `weighted sum per spec 11 dot 3`() {
        // call max=80, sms max=60, web max=40, answer=50
        // expected = round(80*0.35 + 60*0.30 + 40*0.20 + 50*0.15) = round(28 + 18 + 8 + 7.5) = 62
        SessionRiskCalculator.compute(
            callMax = 80,
            smsMax = 60,
            webMax = 40,
            answerMax = 50,
        ) shouldBe 62
    }

    @Test fun `zeros yield zero`() {
        SessionRiskCalculator.compute(0, 0, 0, 0) shouldBe 0
    }

    @Test fun `caps at 100`() {
        SessionRiskCalculator.compute(100, 100, 100, 100) shouldBe 100
    }

    @Test fun `inputs out of range are rejected`() {
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            SessionRiskCalculator.compute(-1, 0, 0, 0)
        }
        org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            SessionRiskCalculator.compute(0, 0, 0, 101)
        }
    }
}
