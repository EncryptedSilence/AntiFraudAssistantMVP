package com.qalqan.antifraud.database

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SmokeTest {
    @Test
    fun `Robolectric runtime is wired`() {
        2 + 2 shouldBe 4
    }
}
