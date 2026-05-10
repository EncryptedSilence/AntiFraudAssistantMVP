package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PatternId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class MatchResultTest {
    @Test
    fun `valid match accepted`() {
        val r = MatchResult(
            patternId = PatternId("p"),
            matched = true,
            triggeredWeight = 50,
            triggeringEventIds = listOf(EventId("e1"), EventId("e2"))
        )
        r.matched shouldBe true
    }

    @Test
    fun `weight bounded 0 to PatternRisk cap`() {
        shouldThrow<IllegalArgumentException> {
            MatchResult(PatternId("p"), true, 61, emptyList())
        }
    }

    @Test
    fun `non-match has weight zero`() {
        val r = MatchResult(PatternId("p"), false, 0, emptyList())
        r.matched shouldBe false
    }
}
