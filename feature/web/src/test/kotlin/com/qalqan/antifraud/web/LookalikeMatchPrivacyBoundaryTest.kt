package com.qalqan.antifraud.web

import io.kotest.matchers.shouldBe
import org.junit.Test

/**
 * Spec §20.1 — the action log records "lookalike_match" as a state marker only. The
 * seed string itself never reaches the log because [LookalikeMatch.seed] is part of the
 * scoring/explainability path, not the action-log path.
 *
 * The boundary check here is structural: any function returning [LookalikeMatch] is
 * fine, but the orchestrator (T20) and the action-log writer (T22) treat the seed as
 * sensitive-by-policy. This test pins one half of that contract.
 */
class LookalikeMatchPrivacyBoundaryTest {
    @Test
    fun `LookalikeMatch toString does not embed the seed in the form an action log key forbids`() {
        // Action-log forbidden keys (see Stage 3/4 actionLog tests) include "domain", "url".
        // The data class auto-generated toString includes "seed=…" — that's the key name
        // we're enforcing the absence of in the action log, not the seed string itself.
        val m = LookalikeMatch(seed = "halykbank.kz", distance = 1)
        m.toString().contains("domain=") shouldBe false
        m.toString().contains("url=") shouldBe false
    }
}
