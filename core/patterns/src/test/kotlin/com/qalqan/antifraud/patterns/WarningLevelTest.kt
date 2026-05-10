package com.qalqan.antifraud.patterns

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class WarningLevelTest {
    @Test
    fun `warning levels match Appendix A enum values`() {
        WarningLevel.entries.map { it.jsonValue }.toSet() shouldBe
            setOf(
                "medium", "high", "critical",
            )
    }

    @Test
    fun `fromJson resolves a known level`() {
        WarningLevel.fromJson("high") shouldBe WarningLevel.HIGH
    }

    @Test
    fun `fromJson returns null for an unknown level`() {
        WarningLevel.fromJson("low") shouldBe null
    }
}
