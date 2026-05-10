package com.qalqan.antifraud.patterns

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CorrelationTest {
    @Test
    fun `defaults match Appendix A spec`() {
        val c = Correlation()
        c.maxCampaignAgeDays shouldBe 14
        c.linkStrength shouldBe 0.0
    }

    @Test
    fun `valid correlation is accepted`() {
        val c = Correlation(maxCampaignAgeDays = 7, linkStrength = 0.9)
        c.maxCampaignAgeDays shouldBe 7
        c.linkStrength shouldBe 0.9
    }

    @Test
    fun `maxCampaignAgeDays is bounded 1 to 14`() {
        shouldThrow<IllegalArgumentException> { Correlation(maxCampaignAgeDays = 0) }
        shouldThrow<IllegalArgumentException> { Correlation(maxCampaignAgeDays = 15) }
    }

    @Test
    fun `linkStrength is bounded 0 to 1`() {
        shouldThrow<IllegalArgumentException> { Correlation(linkStrength = -0.1) }
        shouldThrow<IllegalArgumentException> { Correlation(linkStrength = 1.1) }
    }
}
