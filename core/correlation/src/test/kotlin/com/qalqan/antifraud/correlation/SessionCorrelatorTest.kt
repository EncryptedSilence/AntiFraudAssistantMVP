package com.qalqan.antifraud.correlation

import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.domain.RiskSession
import com.qalqan.antifraud.domain.SessionId
import com.qalqan.antifraud.domain.SessionStatus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import java.time.Instant

class SessionCorrelatorTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    @Test fun `no open session creates a new session`() {
        val event = call("h1", t)
        val result = SessionCorrelator.findOrOpen(
            event = RiskEvent.Call(event),
            openSessions = emptyList(),
            now = t
        )
        result.shouldBeInstanceOf<SessionCorrelator.Outcome.Created>()
    }

    @Test fun `event within session window for same actor attaches to existing session`() {
        val first = call("h1", t)
        val openSession = sessionFor(first)
        val second = call("h1", t.plusSeconds(60))

        val result = SessionCorrelator.findOrOpen(
            event = RiskEvent.Call(second),
            openSessions = listOf(openSession),
            now = t.plusSeconds(60)
        )
        result.shouldBeInstanceOf<SessionCorrelator.Outcome.Attached>()
        (result as SessionCorrelator.Outcome.Attached).sessionId shouldBe openSession.id
    }

    @Test fun `event outside the window for the same actor opens a new session`() {
        val first = call("h1", t)
        val openSession = sessionFor(first)
        val second = call("h1", t.plusSeconds(60 * 60))
        val result = SessionCorrelator.findOrOpen(
            event = RiskEvent.Call(second),
            openSessions = listOf(openSession),
            now = t.plusSeconds(60 * 60)
        )
        result.shouldBeInstanceOf<SessionCorrelator.Outcome.Created>()
    }

    private fun call(hash: String, at: Instant) = CallEvent(
        id = EventId("c-$hash-$at"),
        phoneHash = PhoneHash(hash),
        simSlot = null,
        direction = CallDirection.INCOMING,
        startedAt = at,
        endedAt = at.plusSeconds(60),
        durationSec = 60,
        isKnownContact = false,
        isRepeated = false,
        callRiskScore = 0,
        linkedSessionId = null,
        linkedCampaignId = null
    )

    private fun sessionFor(call: CallEvent) = RiskSession(
        id = SessionId("s-${call.id.value}"),
        startedAt = call.startedAt,
        endedAt = null,
        status = SessionStatus.OPEN,
        relatedCallEventIds = listOf(call.id),
        relatedSmsEventIds = emptyList(),
        relatedWebEventIds = emptyList(),
        relatedUserAnswerIds = emptyList(),
        sessionRiskScore = 0,
        sessionRiskBand = RiskBand.LOW,
        explanation = null
    )
}
