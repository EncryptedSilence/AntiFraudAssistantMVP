package com.qalqan.antifraud.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SmokeTest {
    @Test
    fun `convention plugin is wired`() {
        2 + 2 shouldBe 4
    }
}
