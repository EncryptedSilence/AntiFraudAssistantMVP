package com.qalqan.antifraud.acceptance

import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.settings.QuestionFatigueGate
import com.qalqan.antifraud.settings.QuestionPromptKind
import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant

/**
 * Spec §23 #42 — an attack scenario reaching only `medium` risk produces zero question
 * prompts.
 */
class Acceptance42NoQuestionBelowHighTest {
    @Test
    fun `§23 #42 — QuestionFatigueGate returns null for LOW and MEDIUM`() {
        val gate = QuestionFatigueGate(allowedKinds = QuestionPromptKind.entries.toSet())
        listOf(RiskBand.LOW, RiskBand.MEDIUM).forEach { band ->
            gate.nextPrompt(
                campaignId = "c1",
                currentBand = band,
                answeredKinds = emptySet(),
                promptsLast24h = 0,
                dontAskAgain = false,
                now = Instant.parse("2026-05-12T10:00:00Z"),
            ) shouldBe null
        }
    }
}
