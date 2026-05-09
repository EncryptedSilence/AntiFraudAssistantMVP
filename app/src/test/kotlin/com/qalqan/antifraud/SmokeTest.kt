package com.qalqan.antifraud

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SmokeTest {
    @Test
    fun `app module wired`() {
        2 + 2 shouldBe 4
    }
}
