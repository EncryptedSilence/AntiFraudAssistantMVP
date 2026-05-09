package com.qalqan.antifraud.acceptance

import com.qalqan.antifraud.correlation.CorrelationOrchestrator
import com.qalqan.antifraud.correlation.SessionCorrelator
import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskEvent
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import java.time.Instant

class RiskSessionAcceptanceTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    @Test
    fun `two related events within 30 minutes share a session (spec §23 #6)`() {
        val first = call("h1", t)
        val second = call("h1", t.plusSeconds(900)) // 15 minutes later

        val orch = CorrelationOrchestrator()
        val firstResult =
            orch.absorb(
                event = RiskEvent.Call(first),
                now = t,
                openSessions = emptyList(),
                activeCampaigns = emptyList(),
            )
        val newSession = (firstResult.sessionOutcome as SessionCorrelator.Outcome.Created).session

        val secondResult =
            orch.absorb(
                event = RiskEvent.Call(second),
                now = t.plusSeconds(900),
                openSessions = listOf(newSession),
                activeCampaigns = emptyList(),
            )
        secondResult.sessionOutcome.shouldBeInstanceOf<SessionCorrelator.Outcome.Attached>()
    }

    private fun call(
        hash: String,
        at: Instant,
    ) = CallEvent(
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
        linkedCampaignId = null,
    )
}
