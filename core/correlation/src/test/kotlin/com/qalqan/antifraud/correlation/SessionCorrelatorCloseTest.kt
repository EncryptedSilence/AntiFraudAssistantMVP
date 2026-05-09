package com.qalqan.antifraud.correlation

import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.domain.RiskSession
import com.qalqan.antifraud.domain.SessionId
import com.qalqan.antifraud.domain.SessionStatus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class SessionCorrelatorCloseTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    private fun open(startedAt: Instant) = RiskSession(
        id = SessionId("s-$startedAt"),
        startedAt = startedAt,
        endedAt = null,
        status = SessionStatus.OPEN,
        relatedCallEventIds = emptyList(),
        relatedSmsEventIds = emptyList(),
        relatedWebEventIds = emptyList(),
        relatedUserAnswerIds = emptyList(),
        sessionRiskScore = 0,
        sessionRiskBand = RiskBand.LOW,
        explanation = null
    )

    @Test fun `session closes when older than the maximum window plus grace`() {
        val ancient = open(t.minus(Duration.ofHours(25)))
        val closed = SessionCorrelator.closeIdle(listOf(ancient), now = t)
        closed.single().status shouldBe SessionStatus.CLOSED_AUTO
    }

    @Test fun `recent session stays open`() {
        val recent = open(t.minus(Duration.ofMinutes(10)))
        val touched = SessionCorrelator.closeIdle(listOf(recent), now = t)
        touched.single().status shouldBe SessionStatus.OPEN
    }
}
