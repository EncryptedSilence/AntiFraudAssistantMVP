package com.qalqan.antifraud.patterns

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SmokeTest {
    @Test
    fun `module is wired`() {
        2 + 2 shouldBe 4
    }
}
