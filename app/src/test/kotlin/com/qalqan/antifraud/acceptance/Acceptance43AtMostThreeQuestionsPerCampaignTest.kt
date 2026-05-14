package com.qalqan.antifraud.acceptance

import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.settings.QuestionFatigueGate
import com.qalqan.antifraud.settings.QuestionPromptKind
import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant

/**
 * Spec §23 #43 — at most 3 prompts per campaign in 24 h.
 */
class Acceptance43AtMostThreeQuestionsPerCampaignTest {
    @Test
    fun `§23 #43 — prompts cap at 3 regardless of unanswered kinds`() {
        val gate = QuestionFatigueGate(allowedKinds = QuestionPromptKind.entries.toSet())
        gate.nextPrompt(
            campaignId = "c1",
            currentBand = RiskBand.CRITICAL,
            answeredKinds = emptySet(),
            promptsLast24h = 3,
            dontAskAgain = false,
            now = Instant.now(),
        ) shouldBe null
    }
}
