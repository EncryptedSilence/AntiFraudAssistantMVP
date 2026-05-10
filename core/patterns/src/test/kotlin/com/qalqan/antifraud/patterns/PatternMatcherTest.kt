package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PatternId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.domain.ScenarioCategory
import com.qalqan.antifraud.domain.SenderHash
import com.qalqan.antifraud.domain.SmsCategory
import com.qalqan.antifraud.domain.SmsEvent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class PatternMatcherTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")
    private val warning = Warning(WarningLevel.HIGH, "title", "message")

    private fun pattern(conditions: List<PatternCondition>) =
        ScenarioPattern(
            patternId = PatternId("p"),
            name = "p",
            description = null,
            category = ScenarioCategory.BANK_FRAUD,
            version = "1.0.0",
            enabled = true,
            userCreated = false,
            source = "system",
            conditions = conditions,
            correlation = Correlation(),
            warning = warning,
            recommendation = null,
        )

    private fun unknownCall(
        id: String,
        at: Instant = t,
    ): RiskEvent.Call =
        RiskEvent.Call(
            CallEvent(
                id = EventId(id),
                phoneHash = PhoneHash("h"),
                simSlot = null,
                direction = CallDirection.INCOMING,
                startedAt = at,
                endedAt = at,
                durationSec = 0,
                isKnownContact = false,
                isRepeated = false,
                callRiskScore = 0,
                linkedSessionId = null,
                linkedCampaignId = null,
            ),
        )

    private fun smsWithCode(
        id: String,
        at: Instant = t,
    ): RiskEvent.Sms =
        RiskEvent.Sms(
            SmsEvent(
                id = EventId(id),
                senderHash = SenderHash("h"),
                senderDisplayNameLocal = null,
                simSlot = null,
                receivedAt = at,
                smsCategory = SmsCategory.OTP,
                containsCode = true,
                containsLink = false,
                containsFinancialKeyword = false,
                containsSecurityKeyword = false,
                bodyExcerptEnc = byteArrayOf(),
                smsRiskScore = 0,
                linkedSessionId = null,
                linkedCampaignId = null,
            ),
        )

    @Test
    fun `pattern with all conditions satisfied triggers`() {
        val p =
            pattern(
                listOf(
                    PatternCondition(EventType.CALL_EVENT, "isKnownContact", Operator.EQUALS, false, weight = 20),
                    PatternCondition(EventType.SMS_EVENT, "containsCode", Operator.EQUALS, true, weight = 30),
                ),
            )
        val events = listOf(unknownCall("c1"), smsWithCode("s1"))
        val result = PatternMatcher.match(p, events)
        result.matched shouldBe true
        result.triggeredWeight shouldBe 50
        result.triggeringEventIds shouldBe listOf(EventId("c1"), EventId("s1"))
    }

    @Test
    fun `pattern with one condition unsatisfied does not trigger`() {
        val p =
            pattern(
                listOf(
                    PatternCondition(EventType.CALL_EVENT, "isKnownContact", Operator.EQUALS, false, weight = 20),
                    PatternCondition(EventType.SMS_EVENT, "containsCode", Operator.EQUALS, true, weight = 30),
                ),
            )
        val events = listOf(unknownCall("c1")) // no SMS
        val result = PatternMatcher.match(p, events)
        result.matched shouldBe false
        result.triggeredWeight shouldBe 0
        result.triggeringEventIds shouldBe emptyList()
    }

    @Test
    fun `disabled pattern never triggers`() {
        val p =
            pattern(
                listOf(
                    PatternCondition(EventType.CALL_EVENT, "isKnownContact", Operator.EQUALS, false, weight = 20),
                ),
            ).copy(enabled = false)
        val result = PatternMatcher.match(p, listOf(unknownCall("c1")))
        result.matched shouldBe false
    }
}
