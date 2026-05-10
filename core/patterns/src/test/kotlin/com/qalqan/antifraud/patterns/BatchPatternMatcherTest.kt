package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PatternId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.domain.ScenarioCategory
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class BatchPatternMatcherTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")
    private val warning = Warning(WarningLevel.HIGH, "title", "message")

    private fun pattern(
        id: String,
        condField: String,
        condValue: Any,
        weight: Int,
    ) = ScenarioPattern(
        patternId = PatternId(id),
        name = id, description = null, category = ScenarioCategory.BANK_FRAUD,
        version = "1.0.0", enabled = true, userCreated = false, source = "system",
        conditions =
            listOf(
                PatternCondition(EventType.CALL_EVENT, condField, Operator.EQUALS, condValue, weight = weight),
            ),
        correlation = Correlation(), warning = warning, recommendation = null,
    )

    private fun unknownCall(id: String) =
        RiskEvent.Call(
            CallEvent(
                id = EventId(id),
                phoneHash = PhoneHash("h"),
                simSlot = null,
                direction = CallDirection.INCOMING,
                startedAt = t,
                endedAt = t,
                durationSec = 0,
                isKnownContact = false,
                isRepeated = false,
                callRiskScore = 0,
                linkedSessionId = null,
                linkedCampaignId = null,
            ),
        )

    @Test
    fun `matchAll returns triggered weights for triggering patterns`() {
        val patterns =
            listOf(
                pattern("p_match", "isKnownContact", false, weight = 30),
                pattern("p_nomatch", "isKnownContact", true, weight = 50),
                pattern("p_match2", "direction", "INCOMING", weight = 20),
            )
        val results = BatchPatternMatcher.matchAll(patterns, listOf(unknownCall("c1")))
        results.size shouldBe 3
        results.first { it.patternId.value == "p_match" }.triggeredWeight shouldBe 30
        results.first { it.patternId.value == "p_nomatch" }.matched shouldBe false
        results.first { it.patternId.value == "p_match2" }.triggeredWeight shouldBe 20
    }

    @Test
    fun `triggeredWeights returns only matched-pattern weights`() {
        val patterns =
            listOf(
                pattern("p_match", "isKnownContact", false, weight = 30),
                pattern("p_nomatch", "isKnownContact", true, weight = 50),
                pattern("p_match2", "direction", "INCOMING", weight = 20),
            )
        val weights = BatchPatternMatcher.triggeredWeights(patterns, listOf(unknownCall("c1")))
        weights shouldBe listOf(30, 20)
    }
}
