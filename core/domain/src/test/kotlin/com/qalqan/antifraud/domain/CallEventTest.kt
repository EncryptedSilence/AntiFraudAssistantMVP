package com.qalqan.antifraud.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class CallEventTest {
    private val started = Instant.parse("2026-05-08T10:00:00Z")
    private val ended = started.plusSeconds(180)

    private fun call(
        durationSec: Long = 180,
        endedAt: Instant? = ended,
        riskScore: Int = 0
    ): CallEvent = CallEvent(
        id = EventId("e1"),
        phoneHash = PhoneHash("h"),
        simSlot = null,
        direction = CallDirection.INCOMING,
        startedAt = started,
        endedAt = endedAt,
        durationSec = durationSec,
        isKnownContact = false,
        isRepeated = false,
        callRiskScore = riskScore,
        linkedSessionId = null,
        linkedCampaignId = null
    )

    @Test fun `durationSec cannot be negative`() {
        shouldThrow<IllegalArgumentException> { call(durationSec = -1) }
    }

    @Test fun `endedAt cannot be before startedAt`() {
        shouldThrow<IllegalArgumentException> {
            call(endedAt = started.minusSeconds(1))
        }
    }

    @Test fun `risk score is bounded to 0 dot dot 100`() {
        shouldThrow<IllegalArgumentException> { call(riskScore = -1) }
        shouldThrow<IllegalArgumentException> { call(riskScore = 101) }
    }

    @Test fun `valid call is accepted`() {
        call(durationSec = 0, endedAt = started, riskScore = 0).durationSec shouldBe 0
    }
}
