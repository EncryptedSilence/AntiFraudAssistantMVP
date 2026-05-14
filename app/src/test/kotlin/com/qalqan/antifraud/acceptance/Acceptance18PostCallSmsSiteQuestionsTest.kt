package com.qalqan.antifraud.acceptance

import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.settings.QuestionFatigueGate
import com.qalqan.antifraud.settings.QuestionPromptKind
import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant

/**
 * Spec §23 #18 — same question not asked twice in one campaign.
 */
class Acceptance18PostCallSmsSiteQuestionsTest {
    @Test
    fun `§23 #18 — once a kind is answered, it is not re-emitted`() {
        val gate = QuestionFatigueGate(allowedKinds = QuestionPromptKind.entries.toSet())
        gate.nextPrompt(
            campaignId = "c1",
            currentBand = RiskBand.HIGH,
            answeredKinds = setOf(QuestionPromptKind.CALLER_IDENTITY),
            promptsLast24h = 1,
            dontAskAgain = false,
            now = Instant.now(),
        ) shouldBe QuestionPromptKind.PRESSURE
    }
}
