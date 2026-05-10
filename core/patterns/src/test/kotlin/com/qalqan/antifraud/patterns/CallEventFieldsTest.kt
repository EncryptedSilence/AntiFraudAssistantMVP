package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskEvent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class CallEventFieldsTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")
    private val call = RiskEvent.Call(
        CallEvent(
            id = EventId("c1"),
            phoneHash = PhoneHash("h"),
            simSlot = 0,
            direction = CallDirection.INCOMING,
            startedAt = t,
            endedAt = t.plusSeconds(120),
            durationSec = 120,
            isKnownContact = false,
            isRepeated = true,
            callRiskScore = 25,
            linkedSessionId = null,
            linkedCampaignId = null
        )
    )

    @Test
    fun `looks up isKnownContact`() {
        CallEventFields.lookup(call, "isKnownContact") shouldBe false
    }

    @Test
    fun `looks up isRepeated`() {
        CallEventFields.lookup(call, "isRepeated") shouldBe true
    }

    @Test
    fun `looks up direction as String`() {
        CallEventFields.lookup(call, "direction") shouldBe "INCOMING"
    }

    @Test
    fun `looks up durationSec as Long`() {
        CallEventFields.lookup(call, "durationSec") shouldBe 120L
    }

    @Test
    fun `looks up simSlot as Int`() {
        CallEventFields.lookup(call, "simSlot") shouldBe 0
    }

    @Test
    fun `unknown field returns null`() {
        CallEventFields.lookup(call, "ghost") shouldBe null
    }

    @Test
    fun `non-call event returns null`() {
        val webEvent = RiskEvent.Web(
            com.qalqan.antifraud.domain.WebEvent(
                id = EventId("w1"),
                domainHash = com.qalqan.antifraud.domain.DomainHash("d"),
                domainDisplayLocal = "halykbank.kz",
                visitedAt = t,
                isNewDomain = false,
                domainStatus = com.qalqan.antifraud.domain.DomainStatus.KNOWN,
                webRiskScore = 0,
                linkedSessionId = null,
                linkedCampaignId = null
            )
        )
        CallEventFields.lookup(webEvent, "isKnownContact") shouldBe null
    }
}
