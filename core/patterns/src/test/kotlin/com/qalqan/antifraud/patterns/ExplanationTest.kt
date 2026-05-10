package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.PatternId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ExplanationTest {
    @Test
    fun `valid explanation accepted`() {
        val e = Explanation(
            level = WarningLevel.HIGH,
            reasons = listOf(
                Reason(PatternId("p1"), "An unknown call happened."),
                Reason(PatternId("p1"), "An SMS with a code arrived within 24h.")
            )
        )
        e.reasons.size shouldBe 2
    }

    @Test
    fun `reasons cannot be empty`() {
        shouldThrow<IllegalArgumentException> {
            Explanation(level = WarningLevel.MEDIUM, reasons = emptyList())
        }
    }

    @Test
    fun `Reason text cannot be blank`() {
        shouldThrow<IllegalArgumentException> { Reason(PatternId("p"), "") }
    }
}
