package com.qalqan.antifraud.web

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.Test

class LookalikeMatchTest {
    @Test
    fun `holds canonical seed plus distance`() {
        val m = LookalikeMatch(seed = "halykbank.kz", distance = 2)
        m.seed shouldBe "halykbank.kz"
        m.distance shouldBe 2
    }

    @Test
    fun `distance must be in 1__2`() {
        // 0 would be a self-match (not a lookalike), 3+ is over threshold.
        shouldThrow<IllegalArgumentException> {
            LookalikeMatch(seed = "kaspi.kz", distance = 0)
        }
        shouldThrow<IllegalArgumentException> {
            LookalikeMatch(seed = "kaspi.kz", distance = 3)
        }
    }
}
