package com.qalqan.antifraud.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class RiskEventTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    @Test fun `Call wraps a CallEvent`() {
        val call = sampleCall()
        val ev: RiskEvent = RiskEvent.Call(call)
        ev.eventId shouldBe call.id
        ev.occurredAt shouldBe call.startedAt
    }

    @Test fun `Sms wraps an SmsEvent`() {
        val sms = sampleSms()
        val ev: RiskEvent = RiskEvent.Sms(sms)
        ev.eventId shouldBe sms.id
        ev.occurredAt shouldBe sms.receivedAt
    }

    @Test fun `Web wraps a WebEvent`() {
        val w = sampleWeb()
        val ev: RiskEvent = RiskEvent.Web(w)
        ev.occurredAt shouldBe w.visitedAt
    }

    @Test fun `Answer wraps a UserAnswer`() {
        val a = sampleAnswer()
        val ev: RiskEvent = RiskEvent.Answer(a)
        ev.occurredAt shouldBe a.createdAt
    }

    private fun sampleCall() =
        CallEvent(
            id = EventId("c1"),
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

    private fun sampleSms() =
        SmsEvent(
            id = EventId("s1"),
            senderHash = SenderHash("h"),
            senderDisplayNameLocal = null,
            simSlot = null,
            receivedAt = t,
            smsCategory = SmsCategory.UNKNOWN_SENDER,
            containsCode = false,
            containsLink = false,
            containsFinancialKeyword = false,
            containsSecurityKeyword = false,
            bodyExcerptEnc = byteArrayOf(),
            smsRiskScore = 0,
            linkedSessionId = null,
            linkedCampaignId = null,
        )

    private fun sampleWeb() =
        WebEvent(
            id = EventId("w1"),
            domainHash = DomainHash("h"),
            domainDisplayLocal = "halykbank.kz",
            visitedAt = t,
            isNewDomain = true,
            domainStatus = DomainStatus.NEW,
            webRiskScore = 0,
            linkedSessionId = null,
            linkedCampaignId = null,
        )

    private fun sampleAnswer() =
        UserAnswer(
            id = AnswerId("a1"),
            relatedEventId = EventId("e1"),
            relatedSessionId = null,
            relatedCampaignId = null,
            questionCode = QuestionCode.Q1_CALLER_OFFICIAL_CLAIM,
            answerCode = AnswerCode.YES,
            userNoteLocalEnc = null,
            answerRiskScore = 25,
            createdAt = t,
        )
}
