package com.qalqan.antifraud.correlation

import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PatternId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.domain.ScenarioCategory
import com.qalqan.antifraud.patterns.Correlation
import com.qalqan.antifraud.patterns.EventType
import com.qalqan.antifraud.patterns.Operator
import com.qalqan.antifraud.patterns.PatternCondition
import com.qalqan.antifraud.patterns.ScenarioPattern
import com.qalqan.antifraud.patterns.Warning
import com.qalqan.antifraud.patterns.WarningLevel
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.time.Instant

class CorrelationOrchestratorTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    private val unknownCallEvent =
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

    @Test fun `first event creates session and campaign`() {
        val orch = CorrelationOrchestrator()
        val out =
            orch.absorb(
                event = unknownCallEvent,
                now = t,
                openSessions = emptyList(),
                activeCampaigns = emptyList(),
            )
        out.sessionOutcome shouldNotBe null
        out.campaignOutcome shouldNotBe null
    }

    @Test fun `no patterns produces empty triggered weights`() {
        val orch = CorrelationOrchestrator(patternProvider = { emptyList() })
        val out =
            orch.absorb(
                event = unknownCallEvent,
                now = t,
                openSessions = emptyList(),
                activeCampaigns = emptyList(),
            )
        out.triggeredPatternWeights shouldBe emptyList()
    }

    @Test fun `matching pattern produces non-empty triggered weights`() {
        val unknownCallCondition =
            PatternCondition(
                eventType = EventType.CALL_EVENT,
                field = "isKnownContact",
                operator = Operator.EQUALS,
                value = false,
                weight = 20,
                timeWindowHours = null,
            )
        val triggeringPattern =
            ScenarioPattern(
                patternId = PatternId("test_pattern"),
                name = "Test unknown call pattern",
                description = null,
                category = ScenarioCategory.BANK_FRAUD,
                version = "1.0.0",
                enabled = true,
                userCreated = false,
                source = "system",
                conditions = listOf(unknownCallCondition),
                correlation = Correlation(),
                warning = Warning(WarningLevel.HIGH, "Test warning", "Test message"),
                recommendation = null,
            )
        val orch = CorrelationOrchestrator(patternProvider = { listOf(triggeringPattern) })
        val out =
            orch.absorb(
                event = unknownCallEvent,
                now = t,
                openSessions = emptyList(),
                activeCampaigns = emptyList(),
            )
        out.triggeredPatternWeights.shouldNotBeEmpty()
        out.triggeredPatternWeights[0] shouldBe 20
    }
}
