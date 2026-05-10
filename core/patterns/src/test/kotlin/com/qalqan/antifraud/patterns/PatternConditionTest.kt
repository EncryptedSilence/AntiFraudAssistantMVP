package com.qalqan.antifraud.patterns

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PatternConditionTest {
    private fun cond(
        eventType: EventType = EventType.SMS_EVENT,
        field: String = "containsCode",
        operator: Operator = Operator.EQUALS,
        value: Any = true,
        weight: Int = 30,
        timeWindowHours: Int? = null,
    ) = PatternCondition(eventType, field, operator, value, weight, timeWindowHours)

    @Test
    fun `valid condition is accepted`() {
        cond().weight shouldBe 30
    }

    @Test
    fun `weight bounded 0 to 100`() {
        shouldThrow<IllegalArgumentException> { cond(weight = -1) }
        shouldThrow<IllegalArgumentException> { cond(weight = 101) }
    }

    @Test
    fun `field cannot be blank`() {
        shouldThrow<IllegalArgumentException> { cond(field = "") }
    }

    @Test
    fun `timeWindowHours bounded 1 to 336 when present`() {
        shouldThrow<IllegalArgumentException> { cond(timeWindowHours = 0) }
        shouldThrow<IllegalArgumentException> { cond(timeWindowHours = 337) }
        cond(timeWindowHours = 1).timeWindowHours shouldBe 1
        cond(timeWindowHours = 336).timeWindowHours shouldBe 336
    }

    @Test
    fun `condition referencing unsupported event type is rejected`() {
        shouldThrow<IllegalArgumentException> {
            cond(eventType = EventType.CONTACT_EVENT)
        }
    }
}
