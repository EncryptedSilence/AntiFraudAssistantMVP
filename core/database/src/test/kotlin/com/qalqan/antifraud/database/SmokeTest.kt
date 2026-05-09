package com.qalqan.antifraud.database

import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SmokeTest {
    @Test
    fun `Robolectric runtime is wired`() {
        RuntimeEnvironment.getApiLevel() shouldBe 34
    }
}
