package com.qalqan.antifraud.correlation

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Duration

class SessionWindowSelectorTest {
    @Test fun `same actor signals pick the 15 minute window`() {
        SessionWindowSelector.windowFor(
            setOf(LinkClue.SAME_PHONE_HASH)
        ) shouldBe Duration.ofMinutes(15)
        SessionWindowSelector.windowFor(
            setOf(LinkClue.SAME_DOMAIN_HASH)
        ) shouldBe Duration.ofMinutes(15)
    }

    @Test fun `user-confirmed link picks the 60 minute window`() {
        SessionWindowSelector.windowFor(
            setOf(LinkClue.USER_CONFIRMED_LINK)
        ) shouldBe Duration.ofMinutes(60)
    }

    @Test fun `thematic-only link picks the 24 hour window`() {
        SessionWindowSelector.windowFor(
            setOf(LinkClue.SAME_SCENARIO_CATEGORY)
        ) shouldBe Duration.ofHours(24)
    }

    @Test fun `multiple clues pick the strongest (smallest) window`() {
        SessionWindowSelector.windowFor(
            setOf(LinkClue.SAME_PHONE_HASH, LinkClue.SAME_SCENARIO_CATEGORY)
        ) shouldBe Duration.ofMinutes(15)
    }

    @Test fun `no clues pick the default 30 minute window`() {
        SessionWindowSelector.windowFor(emptySet()) shouldBe Duration.ofMinutes(30)
    }
}
