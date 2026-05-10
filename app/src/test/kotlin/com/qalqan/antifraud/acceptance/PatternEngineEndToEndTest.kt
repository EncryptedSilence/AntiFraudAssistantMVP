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
import com.qalqan.antifraud.patterns.PatternExplainer
import com.qalqan.antifraud.patterns.SeedPatternLoader
import com.qalqan.antifraud.patterns.WarningLevel
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import org.junit.Test
import java.time.Instant

/**
 * End-to-end pattern engine test using the FAST_ATTACK scenario.
 *
 * The Stage-1 FAST_ATTACK demo fixture (§13.1) contains:
 *   - An unknown incoming call (isKnownContact=false, 4 min)
 *   - An OTP SMS 6 minutes later (body containing a 6-digit code → containsCode=true)
 *
 * These two events satisfy the `bank_security_otp_after_call_v1` seed pattern, which
 * requires `CallEvent.isKnownContact == false` AND `SmsEvent.containsCode == true`
 * within a 24-hour window. We construct the events directly (no DB needed) to
 * mirror the fixture data precisely.
 */
class PatternEngineEndToEndTest {
    private val anchor = Instant.parse("2026-05-08T10:00:00Z")

    private val fastAttackEvents: List<RiskEvent> =
        listOf(
            RiskEvent.Call(
                CallEvent(
                    id = EventId("fast-attack-call"),
                    phoneHash = PhoneHash("hash-77001234567"),
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
            ),
            RiskEvent.Sms(
                SmsEvent(
                    id = EventId("fast-attack-sms"),
                    senderHash = SenderHash("hash-BANK24"),
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
            ),
        )

    @Test
    fun `FAST_ATTACK demo events trigger at least one seed pattern`() {
        val patterns = SeedPatternLoader.load()
        val results = BatchPatternMatcher.matchAll(patterns, fastAttackEvents)

        val anyTriggered = results.any { it.matched }
        anyTriggered.shouldBeTrue()
    }

    @Test
    fun `FAST_ATTACK explanation has level at least MEDIUM and at least one reason`() {
        val patterns = SeedPatternLoader.load()
        val results = BatchPatternMatcher.matchAll(patterns, fastAttackEvents)
        val triggered = patterns.zip(results).filter { (_, r) -> r.matched }

        triggered.isNotEmpty().shouldBeTrue()

        val explanation = PatternExplainer.explain(triggered)
        explanation.reasons shouldHaveAtLeastSize 1

        // WarningLevel has no LOW value, so any resolved level is already MEDIUM or above.
        // The enum entries are MEDIUM, HIGH, CRITICAL — all satisfy the §11.5 banding requirement.
        val severityRank =
            when (explanation.level) {
                WarningLevel.MEDIUM -> 1
                WarningLevel.HIGH -> 2
                WarningLevel.CRITICAL -> 3
            }
        (severityRank >= 1).shouldBeTrue()
    }
}
