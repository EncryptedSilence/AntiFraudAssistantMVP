package com.qalqan.antifraud.database.calls

import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class CallEventMapperTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    @Test
    fun `round trip preserves data`() {
        val ev =
            CallEvent(
                id = EventId("c1"),
                phoneHash = PhoneHash("h"),
                simSlot = 0,
                direction = CallDirection.INCOMING,
                startedAt = t,
                endedAt = t.plusSeconds(SIXTY),
                durationSec = SIXTY,
                isKnownContact = false,
                isRepeated = true,
                callRiskScore = SCORE,
                linkedSessionId = null,
                linkedCampaignId = null,
            )
        ev.toEntity().toDomain() shouldBe ev
    }

    private companion object {
        const val SIXTY: Long = 60
        const val SCORE: Int = 35
    }
}
