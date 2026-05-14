package com.qalqan.antifraud.settings

import com.qalqan.antifraud.domain.RiskBand
import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.Instant

class QuestionFatigueGateTest {
    private val now: Instant = Instant.parse("2026-05-12T10:00:00Z")

    @Test
    fun `§5_5_1 — no prompt below high`() {
        QuestionFatigueGate(allowedKinds = QuestionPromptKind.entries.toSet()).nextPrompt(
            campaignId = "c1",
            currentBand = RiskBand.MEDIUM,
            answeredKinds = emptySet(),
            promptsLast24h = 0,
            dontAskAgain = false,
            now = now,
        ) shouldBe null
    }

    @Test
    fun `§5_5_1 — emit Q1 at high when nothing has been answered`() {
        QuestionFatigueGate(allowedKinds = QuestionPromptKind.entries.toSet()).nextPrompt(
            campaignId = "c1",
            currentBand = RiskBand.HIGH,
            answeredKinds = emptySet(),
            promptsLast24h = 0,
            dontAskAgain = false,
            now = now,
        ) shouldBe QuestionPromptKind.CALLER_IDENTITY
    }

    @Test
    fun `§5_5_2 — skip kinds already answered`() {
        QuestionFatigueGate(allowedKinds = QuestionPromptKind.entries.toSet()).nextPrompt(
            campaignId = "c1",
            currentBand = RiskBand.HIGH,
            answeredKinds = setOf(QuestionPromptKind.CALLER_IDENTITY),
            promptsLast24h = 1,
            dontAskAgain = false,
            now = now,
        ) shouldBe QuestionPromptKind.PRESSURE
    }

    @Test
    fun `§5_5_3 — suppress when promptsLast24h is at the cap (3)`() {
        QuestionFatigueGate(allowedKinds = QuestionPromptKind.entries.toSet()).nextPrompt(
            campaignId = "c1",
            currentBand = RiskBand.CRITICAL,
            answeredKinds = emptySet(),
            promptsLast24h = 3,
            dontAskAgain = false,
            now = now,
        ) shouldBe null
    }

    @Test
    fun `§5_5_3 — suppress when don't-ask-again is set`() {
        QuestionFatigueGate(allowedKinds = QuestionPromptKind.entries.toSet()).nextPrompt(
            campaignId = "c1",
            currentBand = RiskBand.CRITICAL,
            answeredKinds = emptySet(),
            promptsLast24h = 0,
            dontAskAgain = true,
            now = now,
        ) shouldBe null
    }

    @Test
    fun `disallowed kind is skipped — e_g_ post-call questions disabled in §18`() {
        val gate =
            QuestionFatigueGate(
                allowedKinds = setOf(QuestionPromptKind.PRESSURE, QuestionPromptKind.ACTION_REQUEST),
            )
        gate.nextPrompt(
            campaignId = "c1",
            currentBand = RiskBand.HIGH,
            answeredKinds = emptySet(),
            promptsLast24h = 0,
            dontAskAgain = false,
            now = now,
        ) shouldBe QuestionPromptKind.PRESSURE
    }

    @Test
    fun `cap of 3 is per spec §5_5_3 — emits three prompts then null`() {
        val gate = QuestionFatigueGate(allowedKinds = QuestionPromptKind.entries.toSet())
        repeat(3) { i ->
            gate.nextPrompt(
                campaignId = "c1",
                currentBand = RiskBand.HIGH,
                answeredKinds = QuestionPromptKind.entries.take(i).toSet(),
                promptsLast24h = i,
                dontAskAgain = false,
                now = now,
            ) shouldBe QuestionPromptKind.entries[i]
        }
        gate.nextPrompt(
            campaignId = "c1",
            currentBand = RiskBand.HIGH,
            answeredKinds = QuestionPromptKind.entries.toSet(),
            promptsLast24h = 3,
            dontAskAgain = false,
            now = now,
        ) shouldBe null
    }
}
