package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.AnswerCode
import com.qalqan.antifraud.domain.AnswerId
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.QuestionCode
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.domain.UserAnswer
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class UserAnswerFieldsTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")
    private val ans =
        RiskEvent.Answer(
            UserAnswer(
                id = AnswerId("a1"),
                relatedEventId = EventId("e"),
                relatedSessionId = null,
                relatedCampaignId = null,
                questionCode = QuestionCode.Q3_ASKED_TO_ACT_NOW,
                answerCode = AnswerCode.YES,
                userNoteLocalEnc = null,
                answerRiskScore = 50,
                createdAt = t,
            ),
        )

    @Test fun `questionCode as String`() {
        UserAnswerFields.lookup(ans, "questionCode") shouldBe "Q3_ASKED_TO_ACT_NOW"
    }

    @Test fun `answerCode as String`() {
        UserAnswerFields.lookup(ans, "answerCode") shouldBe "YES"
    }

    @Test fun `unknown field returns null`() {
        UserAnswerFields.lookup(ans, "ghost") shouldBe null
    }
}
