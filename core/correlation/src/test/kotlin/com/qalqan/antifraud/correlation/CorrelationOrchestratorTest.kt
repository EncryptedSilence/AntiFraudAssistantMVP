package com.qalqan.antifraud.correlation

import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskEvent
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.time.Instant

class CorrelationOrchestratorTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    @Test fun `first event creates session and campaign`() {
        val orch = CorrelationOrchestrator()
        val ev =
            RiskEvent.Call(
                CallEvent(
                    id = EventId("c1"),
                    phoneHash = PhoneHash("h1"),
                    simSlot = null,
                    direction = CallDirection.INCOMING,
                    startedAt = t,
                    endedAt = t.plusSeconds(30),
                    durationSec = 30,
                    isKnownContact = false,
                    isRepeated = false,
                    callRiskScore = 0,
                    linkedSessionId = null,
                    linkedCampaignId = null,
                ),
            )
        val out =
            orch.absorb(
                event = ev,
                now = t,
                openSessions = emptyList(),
                activeCampaigns = emptyList(),
            )
        out.sessionOutcome shouldNotBe null
        out.campaignOutcome shouldNotBe null
    }
}
