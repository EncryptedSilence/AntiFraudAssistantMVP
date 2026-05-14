package com.qalqan.antifraud.alerts

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.Test

class AlertContentTest {
    @Test
    fun `title is the §17_0_1 fixed copy`() {
        val content = AlertContent(reasons = listOf("a", "b", "c"))
        content.title shouldBe "Possible fraud — pause"
    }

    @Test
    fun `at least three reasons required per §23 #17`() {
        val ex =
            shouldThrow<IllegalArgumentException> {
                AlertContent(reasons = listOf("only one"))
            }
        ex.message?.contains("at least 3") shouldBe true
    }

    @Test
    fun `reasons containing forbidden patterns are rejected`() {
        shouldThrow<IllegalArgumentException> {
            // looks like a +7-prefixed phone number — must be redacted upstream
            AlertContent(reasons = listOf("a", "b", "+77001234567"))
        }
        shouldThrow<IllegalArgumentException> {
            // looks like a 4-6 digit OTP
            AlertContent(reasons = listOf("a", "b", "your code 8421"))
        }
        shouldThrow<IllegalArgumentException> {
            // looks like a domain
            AlertContent(reasons = listOf("a", "b", "visit kaspi-bonus.kz"))
        }
    }

    @Test
    fun `dismissLabel and pauseLabel are §17_0_1 fixed copy`() {
        val content = AlertContent(reasons = listOf("a", "b", "c"))
        content.pauseLabel shouldBe "Pause and verify"
        content.dismissLabel shouldBe "Dismiss"
    }
}
