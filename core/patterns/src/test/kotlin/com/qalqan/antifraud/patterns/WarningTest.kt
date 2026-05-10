package com.qalqan.antifraud.patterns

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class WarningTest {
    @Test
    fun `valid warning is accepted`() {
        val w = Warning(level = WarningLevel.HIGH, title = "Possible fraud", message = "Do not share codes")
        w.level shouldBe WarningLevel.HIGH
    }

    @Test
    fun `title length is bounded per Appendix A`() {
        shouldThrow<IllegalArgumentException> {
            Warning(WarningLevel.HIGH, "x".repeat(81), "ok")
        }
    }

    @Test
    fun `message length is bounded per Appendix A`() {
        shouldThrow<IllegalArgumentException> {
            Warning(WarningLevel.HIGH, "ok", "x".repeat(601))
        }
    }

    @Test
    fun `title cannot be blank`() {
        shouldThrow<IllegalArgumentException> {
            Warning(WarningLevel.HIGH, "", "ok")
        }
    }

    @Test
    fun `message cannot be blank`() {
        shouldThrow<IllegalArgumentException> {
            Warning(WarningLevel.HIGH, "ok", "")
        }
    }
}
