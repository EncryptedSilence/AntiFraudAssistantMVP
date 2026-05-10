package com.qalqan.antifraud.patterns

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class OperatorTest {
    @Test
    fun `operators match Appendix A enum values`() {
        Operator.entries.map { it.jsonValue }.toSet() shouldBe setOf(
            "equals", "in", "greaterThan", "lessThan", "contains", "matches"
        )
    }

    @Test
    fun `fromJson resolves a known operator`() {
        Operator.fromJson("equals") shouldBe Operator.EQUALS
        Operator.fromJson("matches") shouldBe Operator.MATCHES
    }

    @Test
    fun `fromJson returns null for an unknown operator`() {
        Operator.fromJson("regex") shouldBe null
    }
}
