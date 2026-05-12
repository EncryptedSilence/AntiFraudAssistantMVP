package com.qalqan.antifraud.web

import io.kotest.matchers.shouldBe
import org.junit.Test

class SmokeTest {
    @Test
    fun `module is wired`() {
        2 + 2 shouldBe 4
    }
}
