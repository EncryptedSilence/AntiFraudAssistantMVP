package com.qalqan.antifraud.database.sessions

import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.domain.RiskSession
import com.qalqan.antifraud.domain.SessionId
import com.qalqan.antifraud.domain.SessionStatus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class RiskSessionMapperTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    @Test
    fun `round trip preserves event id lists`() {
        val s =
            RiskSession(
                id = SessionId("s1"),
                startedAt = t,
                endedAt = null,
                status = SessionStatus.OPEN,
                relatedCallEventIds = listOf(EventId("c1"), EventId("c2")),
                relatedSmsEventIds = emptyList(),
                relatedWebEventIds = emptyList(),
                relatedUserAnswerIds = emptyList(),
                sessionRiskScore = SCORE,
                sessionRiskBand = RiskBand.MEDIUM,
                explanation = "test",
            )
        s.toEntity().toDomain() shouldBe s
    }

    private companion object {
        const val SCORE: Int = 42
    }
}
