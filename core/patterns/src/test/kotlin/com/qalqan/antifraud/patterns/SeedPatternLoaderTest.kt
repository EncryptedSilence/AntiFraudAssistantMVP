package com.qalqan.antifraud.patterns

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SeedPatternLoaderTest {
    @Test
    fun `loads all 5 seed patterns`() {
        val seeds = SeedPatternLoader.load()
        seeds shouldHaveSize 5
    }

    @Test
    fun `every seed pattern has at least one condition`() {
        SeedPatternLoader.load().forEach {
            (it.conditions.size > 0) shouldBe true
        }
    }

    @Test
    fun `every seed pattern is enabled by default`() {
        SeedPatternLoader.load().forEach {
            it.enabled shouldBe true
        }
    }

    @Test
    fun `every seed pattern is system-sourced (not userCreated)`() {
        SeedPatternLoader.load().forEach {
            it.userCreated shouldBe false
            it.source shouldBe "system"
        }
    }
}
