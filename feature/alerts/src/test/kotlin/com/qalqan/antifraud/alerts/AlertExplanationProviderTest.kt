package com.qalqan.antifraud.alerts

import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskBand
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.string.shouldNotContain
import org.junit.Test
import java.time.Instant

class AlertExplanationProviderTest {
    private val provider = AlertExplanationProvider()

    @Test
    fun `produces at least 3 reasons for a HIGH-risk unknown-caller event`() {
        val event = unknownCaller(durationSec = 65L)
        val reasons =
            provider.reasonsFor(
                event = event,
                band = RiskBand.HIGH,
                triggeredPatternLabels = listOf("unknown bank pattern"),
            )
        reasons shouldHaveAtLeastSize 3
    }

    @Test
    fun `reasons never include a phone number, domain, or OTP`() {
        val event = unknownCaller(durationSec = 65L)
        val reasons =
            provider.reasonsFor(
                event = event,
                band = RiskBand.CRITICAL,
                triggeredPatternLabels = listOf("kaspi-bonus.kz lookalike"),
            )
        reasons.forEach { line ->
            line shouldNotContain "+7"
            line shouldNotContain ".kz"
        }
    }

    private fun unknownCaller(durationSec: Long) =
        CallEvent(
            id = EventId("e1"),
            phoneHash = PhoneHash("h1"),
            simSlot = 0,
            direction = CallDirection.INCOMING,
            startedAt = Instant.parse("2026-05-14T12:00:00Z"),
            endedAt = Instant.parse("2026-05-14T12:01:05Z"),
            durationSec = durationSec,
            isKnownContact = false,
            isRepeated = false,
            callRiskScore = 70,
            linkedSessionId = null,
            linkedCampaignId = null,
        )
}
