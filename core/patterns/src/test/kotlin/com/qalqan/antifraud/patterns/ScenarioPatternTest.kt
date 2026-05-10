package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.PatternId
import com.qalqan.antifraud.domain.ScenarioCategory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ScenarioPatternTest {
    private val warning = Warning(WarningLevel.HIGH, "title", "message")
    private val condition =
        PatternCondition(
            eventType = EventType.SMS_EVENT,
            field = "containsCode",
            operator = Operator.EQUALS,
            value = true,
            weight = 30,
        )

    private fun pattern(
        patternId: PatternId = PatternId("test_pattern_v1"),
        version: String = "1.0.0",
        conditions: List<PatternCondition> = listOf(condition),
    ) = ScenarioPattern(
        patternId = patternId,
        name = "Test pattern",
        description = null,
        category = ScenarioCategory.BANK_FRAUD,
        version = version,
        enabled = true,
        userCreated = false,
        source = "system",
        conditions = conditions,
        correlation = Correlation(),
        warning = warning,
        recommendation = null,
    )

    @Test
    fun `valid pattern is accepted`() {
        pattern().enabled shouldBe true
    }

    @Test
    fun `name length bounded per Appendix A`() {
        shouldThrow<IllegalArgumentException> {
            pattern().copy(name = "x".repeat(121))
        }
    }

    @Test
    fun `description length bounded per Appendix A when present`() {
        pattern().copy(description = "x".repeat(1000)).description!!.length shouldBe 1000
        shouldThrow<IllegalArgumentException> {
            pattern().copy(description = "x".repeat(1001))
        }
    }

    @Test
    fun `version must match semver`() {
        shouldThrow<IllegalArgumentException> { pattern(version = "1.0") }
        shouldThrow<IllegalArgumentException> { pattern(version = "v1.0.0") }
        shouldThrow<IllegalArgumentException> { pattern(version = "1.0.0-rc1") }
    }

    @Test
    fun `at least one condition required`() {
        shouldThrow<IllegalArgumentException> { pattern(conditions = emptyList()) }
    }

    @Test
    fun `recommendation length bounded per Appendix A when present`() {
        pattern().copy(recommendation = "x".repeat(600)).recommendation!!.length shouldBe 600
        shouldThrow<IllegalArgumentException> {
            pattern().copy(recommendation = "x".repeat(601))
        }
    }
}
