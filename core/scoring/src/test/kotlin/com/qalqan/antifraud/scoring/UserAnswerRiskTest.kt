package com.qalqan.antifraud.scoring

import com.qalqan.antifraud.domain.AnswerCode
import com.qalqan.antifraud.domain.AnswerId
import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.QuestionCode
import com.qalqan.antifraud.domain.UserAnswer
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class UserAnswerRiskTest {
    private val t = Instant.parse("2026-05-08T10:00:00Z")

    private fun ans(q: QuestionCode, a: AnswerCode) = UserAnswer(
        id = AnswerId("a"),
        relatedEventId = EventId("e"),
        relatedSessionId = null,
        relatedCampaignId = null,
        questionCode = q,
        answerCode = a,
        userNoteLocalEnc = null,
        answerRiskScore = 0,
        createdAt = t
    )

    @Test fun `Q1 yes (claimed bank or authority) is 25`() {
        UserAnswerRisk.compute(listOf(ans(QuestionCode.Q1_CALLER_OFFICIAL_CLAIM, AnswerCode.YES))) shouldBe 25
    }

    @Test fun `Q2 yes (pressure or don't consult) is 40`() {
        UserAnswerRisk.compute(listOf(ans(QuestionCode.Q2_PRESSURE_OR_DONT_CONSULT, AnswerCode.YES))) shouldBe 40
    }

    @Test fun `Q3 yes (asked to act now) is 50`() {
        UserAnswerRisk.compute(listOf(ans(QuestionCode.Q3_ASKED_TO_ACT_NOW, AnswerCode.YES))) shouldBe 50
    }

    @Test fun `not sure halves the contribution`() {
        UserAnswerRisk.compute(listOf(ans(QuestionCode.Q1_CALLER_OFFICIAL_CLAIM, AnswerCode.NOT_SURE))) shouldBe 12
        UserAnswerRisk.compute(listOf(ans(QuestionCode.Q3_ASKED_TO_ACT_NOW, AnswerCode.NOT_SURE))) shouldBe 25
    }

    @Test fun `no contributes 0`() {
        UserAnswerRisk.compute(listOf(ans(QuestionCode.Q1_CALLER_OFFICIAL_CLAIM, AnswerCode.NO))) shouldBe 0
    }

    @Test fun `not answered contributes 0`() {
        UserAnswerRisk.compute(listOf(ans(QuestionCode.Q3_ASKED_TO_ACT_NOW, AnswerCode.NOT_ANSWERED))) shouldBe 0
    }

    @Test fun `multiple answers take the MAX, not the sum, capped at 50`() {
        val all = QuestionCode.entries.map { ans(it, AnswerCode.YES) }
        UserAnswerRisk.compute(all) shouldBe 50
    }
}
