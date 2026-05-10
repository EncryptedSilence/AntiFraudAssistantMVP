package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.DomainHash
import com.qalqan.antifraud.domain.DomainStatus
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.domain.WebEvent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class ConditionEvaluatorTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    private fun call(isKnownContact: Boolean = false, direction: CallDirection = CallDirection.INCOMING) =
        RiskEvent.Call(
            CallEvent(
                id = EventId("c"),
                phoneHash = PhoneHash("h"),
                simSlot = null,
                direction = direction,
                startedAt = t,
                endedAt = t,
                durationSec = 0,
                isKnownContact = isKnownContact,
                isRepeated = false,
                callRiskScore = 0,
                linkedSessionId = null,
                linkedCampaignId = null
            )
        )

    @Test
    fun `equals on a Boolean field matches`() {
        val cond = PatternCondition(
            eventType = EventType.CALL_EVENT, field = "isKnownContact",
            operator = Operator.EQUALS, value = false, weight = 10
        )
        ConditionEvaluator.evaluate(cond, call(isKnownContact = false)) shouldBe true
        ConditionEvaluator.evaluate(cond, call(isKnownContact = true)) shouldBe false
    }

    @Test
    fun `in operator matches a String value against a list`() {
        val cond = PatternCondition(
            eventType = EventType.CALL_EVENT, field = "direction",
            operator = Operator.IN, value = listOf("INCOMING", "MISSED"), weight = 10
        )
        ConditionEvaluator.evaluate(cond, call(direction = CallDirection.INCOMING)) shouldBe true
        ConditionEvaluator.evaluate(cond, call(direction = CallDirection.OUTGOING)) shouldBe false
    }

    @Test
    fun `unknown field returns false`() {
        val cond = PatternCondition(
            eventType = EventType.CALL_EVENT, field = "ghost",
            operator = Operator.EQUALS, value = "anything", weight = 10
        )
        ConditionEvaluator.evaluate(cond, call()) shouldBe false
    }

    @Test
    fun `event type mismatch returns false`() {
        val cond = PatternCondition(
            eventType = EventType.SMS_EVENT, field = "containsCode",
            operator = Operator.EQUALS, value = true, weight = 10
        )
        ConditionEvaluator.evaluate(cond, call()) shouldBe false
    }

    @Test
    fun `greaterThan on Long field`() {
        val cond = PatternCondition(
            eventType = EventType.CALL_EVENT, field = "durationSec",
            operator = Operator.GREATER_THAN, value = 60, weight = 10
        )
        val long = call().copy(event = call().event.copy(durationSec = 120, endedAt = t.plusSeconds(120)))
        ConditionEvaluator.evaluate(cond, long) shouldBe true
        val short = call().copy(event = call().event.copy(durationSec = 30, endedAt = t.plusSeconds(30)))
        ConditionEvaluator.evaluate(cond, short) shouldBe false
    }

    @Test
    fun `lessThan on Int field`() {
        val cond = PatternCondition(
            eventType = EventType.WEB_EVENT, field = "webRiskScore",
            operator = Operator.LESS_THAN, value = 50, weight = 10
        )
        val low = RiskEvent.Web(
            WebEvent(
                id = EventId("w"),
                domainHash = DomainHash("d"),
                domainDisplayLocal = "x.kz",
                visitedAt = t,
                isNewDomain = false,
                domainStatus = DomainStatus.KNOWN,
                webRiskScore = 25,
                linkedSessionId = null,
                linkedCampaignId = null
            )
        )
        ConditionEvaluator.evaluate(cond, low) shouldBe true
    }

    @Test
    fun `contains on a String field`() {
        val cond = PatternCondition(
            eventType = EventType.WEB_EVENT, field = "domainDisplayLocal",
            operator = Operator.CONTAINS, value = "halyk", weight = 10
        )
        val w = RiskEvent.Web(
            WebEvent(
                id = EventId("w"),
                domainHash = DomainHash("d"),
                domainDisplayLocal = "halykbank.kz",
                visitedAt = t,
                isNewDomain = false,
                domainStatus = DomainStatus.KNOWN,
                webRiskScore = 0,
                linkedSessionId = null,
                linkedCampaignId = null
            )
        )
        ConditionEvaluator.evaluate(cond, w) shouldBe true
    }

    @Test
    fun `matches on a regex value`() {
        val cond = PatternCondition(
            eventType = EventType.WEB_EVENT, field = "domainDisplayLocal",
            operator = Operator.MATCHES, value = """^halyk\w+\.kz$""", weight = 10
        )
        val w = RiskEvent.Web(
            WebEvent(
                id = EventId("w"),
                domainHash = DomainHash("d"),
                domainDisplayLocal = "halykbank.kz",
                visitedAt = t,
                isNewDomain = false,
                domainStatus = DomainStatus.KNOWN,
                webRiskScore = 0,
                linkedSessionId = null,
                linkedCampaignId = null
            )
        )
        ConditionEvaluator.evaluate(cond, w) shouldBe true
    }
}
