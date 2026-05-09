package com.qalqan.antifraud.scoring

import com.qalqan.antifraud.domain.AnswerCode
import com.qalqan.antifraud.domain.AnswerId
import com.qalqan.antifraud.domain.CallDirection
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.QuestionCode
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.domain.UserAnswer
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class EventRiskCalculatorTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    @Test fun `for a call sums base, context, answers and caps at 100`() {
        val call =
            CallEvent(
                id = EventId("c"),
                phoneHash = PhoneHash("h"),
                simSlot = null,
                direction = CallDirection.INCOMING,
                startedAt = t,
                endedAt = t.plusSeconds(240),
                durationSec = 240,
                isKnownContact = false,
                isRepeated = true,
                callRiskScore = 0,
                linkedSessionId = null,
                linkedCampaignId = null,
            )
        // base = 20 (unknown) + 15 (repeated) + 15 (>3min) = 50
        // context = 25 (SMS_AFTER_CALL) = 25
        // answers = 50 (Q3 yes)
        // total uncapped = 125, expected = 100
        val answer =
            UserAnswer(
                id = AnswerId("a"),
                relatedEventId = call.id,
                relatedSessionId = null,
                relatedCampaignId = null,
                questionCode = QuestionCode.Q3_ASKED_TO_ACT_NOW,
                answerCode = AnswerCode.YES,
                userNoteLocalEnc = null,
                answerRiskScore = 50,
                createdAt = t,
            )
        val signals = setOf(LinkSignal.SMS_AFTER_CALL)

        val score =
            EventRiskCalculator.computeForCall(
                call = call,
                contextSignals = signals,
                answersForEvent = listOf(answer),
            )
        score shouldBe 100
    }

    @Test fun `low base with no context and no answers returns base alone`() {
        val call =
            CallEvent(
                id = EventId("c"),
                phoneHash = PhoneHash("h"),
                simSlot = null,
                direction = CallDirection.INCOMING,
                startedAt = t,
                endedAt = t.plusSeconds(60),
                durationSec = 60,
                isKnownContact = true,
                isRepeated = false,
                callRiskScore = 0,
                linkedSessionId = null,
                linkedCampaignId = null,
            )
        EventRiskCalculator.computeForCall(call, emptySet(), emptyList()) shouldBe 0
    }

    @Test fun `dispatch on RiskEvent returns the right value`() {
        val call =
            CallEvent(
                id = EventId("c"),
                phoneHash = PhoneHash("h"),
                simSlot = null,
                direction = CallDirection.INCOMING,
                startedAt = t,
                endedAt = t.plusSeconds(60),
                durationSec = 60,
                isKnownContact = false,
                isRepeated = false,
                callRiskScore = 0,
                linkedSessionId = null,
                linkedCampaignId = null,
            )
        EventRiskCalculator.compute(
            event = RiskEvent.Call(call),
            contextSignals = emptySet(),
            answersForEvent = emptyList(),
            lookalikeDomainMatch = false,
        ) shouldBe 20
    }
}
