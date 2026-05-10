package com.qalqan.antifraud.acceptance

import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.domain.SenderHash
import com.qalqan.antifraud.domain.SmsCategory
import com.qalqan.antifraud.domain.SmsEvent
import com.qalqan.antifraud.patterns.BatchPatternMatcher
import com.qalqan.antifraud.patterns.SeedPatternLoader
import com.qalqan.antifraud.patterns.WarningLevel
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant

/**
 * Spec §23 #12 — a system pattern matches an example event sequence and produces
 * the expected warning level.
 */
class PatternMatchAcceptanceTest {
    private val anchor = Instant.parse("2026-05-08T10:00:00Z")

    @Test
    fun `bank_security pattern matches unknown call followed by OTP SMS within 24h (spec §23 #12)`() {
        val callEvent =
            RiskEvent.Call(
                CallEvent(
                    id = EventId("call-1"),
                    phoneHash = PhoneHash("h-unknown"),
                    simSlot = null,
                    direction = CallDirection.INCOMING,
                    startedAt = anchor,
                    endedAt = anchor.plusSeconds(240),
                    durationSec = 240,
                    isKnownContact = false,
                    isRepeated = false,
                    callRiskScore = 0,
                    linkedSessionId = null,
                    linkedCampaignId = null,
                ),
            )
        val smsEvent =
            RiskEvent.Sms(
                SmsEvent(
                    id = EventId("sms-1"),
                    senderHash = SenderHash("sender-hash"),
                    senderDisplayNameLocal = "BANK24",
                    simSlot = null,
                    receivedAt = anchor.plusSeconds(360),
                    smsCategory = SmsCategory.OTP,
                    containsCode = true,
                    containsLink = false,
                    containsFinancialKeyword = false,
                    containsSecurityKeyword = false,
                    bodyExcerptEnc = ByteArray(0),
                    smsRiskScore = 0,
                    linkedSessionId = null,
                    linkedCampaignId = null,
                ),
            )
        val events: List<RiskEvent> = listOf(callEvent, smsEvent)

        val results = BatchPatternMatcher.matchAll(SeedPatternLoader.load(), events)

        val bankSecurity = results.first { it.patternId.value == "bank_security_otp_after_call_v1" }
        bankSecurity.matched.shouldBeTrue()

        val patterns = SeedPatternLoader.load()
        val bankSecurityPattern = patterns.first { it.patternId.value == "bank_security_otp_after_call_v1" }
        bankSecurityPattern.warning.level shouldBe WarningLevel.HIGH
    }
}
