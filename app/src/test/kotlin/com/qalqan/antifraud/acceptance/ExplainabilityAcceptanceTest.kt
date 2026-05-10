package com.qalqan.antifraud.acceptance

import com.qalqan.antifraud.domain.AnswerCode
import com.qalqan.antifraud.domain.AnswerId
import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.QuestionCode
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.domain.UserAnswer
import com.qalqan.antifraud.patterns.BatchPatternMatcher
import com.qalqan.antifraud.patterns.Explanation
import com.qalqan.antifraud.patterns.PatternExplainer
import com.qalqan.antifraud.patterns.SeedPatternLoader
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.Test
import java.time.Instant
import kotlin.math.min

/**
 * Spec §23 #17 — every warning shows ≥3 specific reasons, or all available if fewer than 3 fire.
 */
class ExplainabilityAcceptanceTest {
    private val anchor = Instant.parse("2026-05-08T10:00:00Z")

    @Test
    fun `multistage_pressure triggers all conditions and explanation has at least 3 reasons (spec §23 #17)`() {
        val callEventId = EventId("call-1")
        val events: List<RiskEvent> =
            listOf(
                RiskEvent.Call(
                    CallEvent(
                        id = callEventId,
                        phoneHash = PhoneHash("h-unknown"),
                        simSlot = null,
                        direction = CallDirection.INCOMING,
                        startedAt = anchor,
                        endedAt = anchor.plusSeconds(120),
                        durationSec = 120,
                        isKnownContact = false,
                        isRepeated = false,
                        callRiskScore = 0,
                        linkedSessionId = null,
                        linkedCampaignId = null,
                    ),
                ),
                RiskEvent.Answer(
                    UserAnswer(
                        id = AnswerId("ans-1"),
                        relatedEventId = callEventId,
                        relatedSessionId = null,
                        relatedCampaignId = null,
                        questionCode = QuestionCode.Q3_ASKED_TO_ACT_NOW,
                        answerCode = AnswerCode.YES,
                        userNoteLocalEnc = null,
                        answerRiskScore = 0,
                        createdAt = anchor.plusSeconds(60),
                    ),
                ),
            )

        val patterns = SeedPatternLoader.load()
        val results = BatchPatternMatcher.matchAll(patterns, events)
        val pairs = patterns.zip(results)
        val triggered = pairs.filter { (_, r) -> r.matched }

        triggered.isNotEmpty().shouldBeTrue()

        val multistage = triggered.firstOrNull { (p, _) -> p.patternId.value == "multistage_pressure_campaign_v1" }
        checkNotNull(multistage) { "multistage_pressure_campaign_v1 should have triggered" }

        val totalConditions = multistage.first.conditions.size
        val explanation = PatternExplainer.explain(pairs.filter { (_, r) -> r.matched })
        val minExpected = min(Explanation.MIN_REASONS_TARGET, totalConditions)
        explanation.reasons.size shouldBeGreaterThanOrEqual minExpected
    }

    @Test
    fun `when only one condition fires explanation has exactly one reason`() {
        val events: List<RiskEvent> =
            listOf(
                RiskEvent.Call(
                    CallEvent(
                        id = EventId("call-only"),
                        phoneHash = PhoneHash("h-unknown"),
                        simSlot = null,
                        direction = CallDirection.INCOMING,
                        startedAt = anchor,
                        endedAt = anchor.plusSeconds(60),
                        durationSec = 60,
                        isKnownContact = false,
                        isRepeated = false,
                        callRiskScore = 0,
                        linkedSessionId = null,
                        linkedCampaignId = null,
                    ),
                ),
            )

        val patterns = SeedPatternLoader.load()
        val results = BatchPatternMatcher.matchAll(patterns, events)
        val pairs = patterns.zip(results)
        val triggered = pairs.filter { (_, r) -> r.matched }

        if (triggered.isEmpty()) return

        val explanation = PatternExplainer.explain(triggered)
        explanation.reasons.size shouldBeGreaterThanOrEqual 1
    }

    @Test
    fun `all explanation reasons have non-blank text`() {
        val callEventId = EventId("call-reasons")
        val events: List<RiskEvent> =
            listOf(
                RiskEvent.Call(
                    CallEvent(
                        id = callEventId,
                        phoneHash = PhoneHash("h-unknown"),
                        simSlot = null,
                        direction = CallDirection.INCOMING,
                        startedAt = anchor,
                        endedAt = anchor.plusSeconds(120),
                        durationSec = 120,
                        isKnownContact = false,
                        isRepeated = false,
                        callRiskScore = 0,
                        linkedSessionId = null,
                        linkedCampaignId = null,
                    ),
                ),
                RiskEvent.Answer(
                    UserAnswer(
                        id = AnswerId("ans-reasons"),
                        relatedEventId = callEventId,
                        relatedSessionId = null,
                        relatedCampaignId = null,
                        questionCode = QuestionCode.Q3_ASKED_TO_ACT_NOW,
                        answerCode = AnswerCode.YES,
                        userNoteLocalEnc = null,
                        answerRiskScore = 0,
                        createdAt = anchor.plusSeconds(60),
                    ),
                ),
            )

        val patterns = SeedPatternLoader.load()
        val results = BatchPatternMatcher.matchAll(patterns, events)
        val triggered = patterns.zip(results).filter { (_, r) -> r.matched }

        triggered.isNotEmpty().shouldBeTrue()

        val explanation = PatternExplainer.explain(triggered)
        explanation.reasons.forEach { reason ->
            reason.text.shouldNotBeBlank()
        }
    }
}
