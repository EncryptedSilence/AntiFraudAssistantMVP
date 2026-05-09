package com.qalqan.antifraud.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class RiskSessionTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    private fun session(
        endedAt: Instant? = null,
        riskScore: Int = 0,
        riskBand: RiskBand = RiskBand.LOW,
    ) = RiskSession(
        id = SessionId("s1"),
        startedAt = t,
        endedAt = endedAt,
        status = SessionStatus.OPEN,
        relatedCallEventIds = emptyList(),
        relatedSmsEventIds = emptyList(),
        relatedWebEventIds = emptyList(),
        relatedUserAnswerIds = emptyList(),
        sessionRiskScore = riskScore,
        sessionRiskBand = riskBand,
        explanation = null,
    )

    @Test fun `endedAt cannot be before startedAt`() {
        shouldThrow<IllegalArgumentException> {
            session(endedAt = t.minusSeconds(1))
        }
    }

    @Test fun `risk score bounded`() {
        shouldThrow<IllegalArgumentException> { session(riskScore = 101) }
    }

    @Test fun `valid session accepted`() {
        session().status shouldBe SessionStatus.OPEN
    }
}
