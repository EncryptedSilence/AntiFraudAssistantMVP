package com.qalqan.antifraud.database.answers

import com.qalqan.antifraud.domain.AnswerCode
import com.qalqan.antifraud.domain.AnswerId
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.QuestionCode
import com.qalqan.antifraud.domain.UserAnswer
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class UserAnswerMapperTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    @Test
    fun `round trip preserves data when note is null`() {
        val a =
            UserAnswer(
                id = AnswerId("a1"),
                relatedEventId = EventId("e1"),
                relatedSessionId = null,
                relatedCampaignId = null,
                questionCode = QuestionCode.Q1_CALLER_OFFICIAL_CLAIM,
                answerCode = AnswerCode.YES,
                userNoteLocalEnc = null,
                answerRiskScore = SCORE_HALF,
                createdAt = t,
            )
        a.toEntity().toDomain() shouldBe a
    }

    @Test
    fun `round trip preserves a 240-byte encrypted note`() {
        val note = ByteArray(NOTE_SIZE) { (it and 0xFF).toByte() }
        val a =
            UserAnswer(
                id = AnswerId("a2"),
                relatedEventId = EventId("e2"),
                relatedSessionId = null,
                relatedCampaignId = null,
                questionCode = QuestionCode.Q3_ASKED_TO_ACT_NOW,
                answerCode = AnswerCode.NO,
                userNoteLocalEnc = note,
                answerRiskScore = 0,
                createdAt = t,
            )
        a.toEntity().toDomain() shouldBe a
    }

    private companion object {
        const val SCORE_HALF: Int = 50
        const val NOTE_SIZE: Int = 240
    }
}
