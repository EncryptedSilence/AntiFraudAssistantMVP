package com.qalqan.antifraud.domain

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class UserAnswerTest {
    private val now = Instant.parse("2026-05-08T10:00:00Z")

    private fun ans(
        questionCode: QuestionCode = QuestionCode.Q1_CALLER_OFFICIAL_CLAIM,
        answerCode: AnswerCode = AnswerCode.YES,
        userNoteEnc: ByteArray? = null,
        riskScore: Int = 25,
    ) = UserAnswer(
        id = AnswerId("a1"),
        relatedEventId = EventId("e1"),
        relatedSessionId = null,
        relatedCampaignId = null,
        questionCode = questionCode,
        answerCode = answerCode,
        userNoteLocalEnc = userNoteEnc,
        answerRiskScore = riskScore,
        createdAt = now,
    )

    @Test fun `risk score bounded`() {
        shouldThrow<IllegalArgumentException> { ans(riskScore = -1) }
        shouldThrow<IllegalArgumentException> { ans(riskScore = 101) }
    }

    @Test fun `note must respect encrypted byte ceiling`() {
        // 240 chars * 3 bytes UTF-8 + AEAD overhead → keep ceiling at 768
        shouldThrow<IllegalArgumentException> { ans(userNoteEnc = ByteArray(769)) }
    }

    @Test fun `valid answer accepted`() {
        ans().answerCode shouldBe AnswerCode.YES
    }
}
