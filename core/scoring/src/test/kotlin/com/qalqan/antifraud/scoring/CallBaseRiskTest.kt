package com.qalqan.antifraud.scoring

import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class CallBaseRiskTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    private fun call(
        isKnownContact: Boolean = false,
        isRepeated: Boolean = false,
        durationSec: Long = 60
    ) = CallEvent(
        id = EventId("c"),
        phoneHash = PhoneHash("h"),
        simSlot = null,
        direction = CallDirection.INCOMING,
        startedAt = t,
        endedAt = t.plusSeconds(durationSec),
        durationSec = durationSec,
        isKnownContact = isKnownContact,
        isRepeated = isRepeated,
        callRiskScore = 0,
        linkedSessionId = null,
        linkedCampaignId = null
    )

    @Test fun `unknown contact contributes 20`() {
        CallBaseRisk.compute(call(isKnownContact = false)) shouldBe 20
    }

    @Test fun `known contact contributes 0`() {
        CallBaseRisk.compute(call(isKnownContact = true)) shouldBe 0
    }

    @Test fun `repeated unknown adds 15`() {
        CallBaseRisk.compute(call(isKnownContact = false, isRepeated = true)) shouldBe 35
    }

    @Test fun `over 3 minutes adds 15`() {
        CallBaseRisk.compute(call(isKnownContact = false, durationSec = 181)) shouldBe 35
    }

    @Test fun `exactly 180 seconds does not yet add the duration bonus`() {
        CallBaseRisk.compute(call(isKnownContact = false, durationSec = 180)) shouldBe 20
    }
}
