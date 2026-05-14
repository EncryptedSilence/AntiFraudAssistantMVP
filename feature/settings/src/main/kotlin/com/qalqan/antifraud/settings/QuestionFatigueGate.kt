package com.qalqan.antifraud.settings

import com.qalqan.antifraud.domain.RiskBand
import java.time.Instant

/**
 * Spec §5.5 — decides whether to surface the next question for a campaign.
 *
 * - §5.5.1: prompts only fire at [RiskBand.HIGH] or [RiskBand.CRITICAL].
 * - §5.5.3: at most [MAX_PROMPTS_PER_CAMPAIGN_24H] prompts per campaign in a 24 h window.
 * - §5.5.3: kinds already answered for this campaign are not re-asked.
 * - §5.5.3: "Don't ask again for this campaign" suppresses all further prompts.
 *
 * [allowedKinds] is the §18 user-settings filter — if `postCallQuestionsEnabled = false`,
 * `CALLER_IDENTITY` is excluded; the same for SMS / site.
 *
 * The `now` parameter is unused today but reserved for time-window expansions
 * (post-MVP §5.5.3 24 h sliding window).
 */
class QuestionFatigueGate(
    private val allowedKinds: Set<QuestionPromptKind>,
) {
    @Suppress("LongParameterList")
    fun nextPrompt(
        @Suppress("UNUSED_PARAMETER") campaignId: String,
        currentBand: RiskBand,
        answeredKinds: Set<QuestionPromptKind>,
        promptsLast24h: Int,
        dontAskAgain: Boolean,
        @Suppress("UNUSED_PARAMETER") now: Instant,
    ): QuestionPromptKind? {
        if (dontAskAgain) return null
        if (currentBand != RiskBand.HIGH && currentBand != RiskBand.CRITICAL) return null
        if (promptsLast24h >= MAX_PROMPTS_PER_CAMPAIGN_24H) return null
        return QuestionPromptKind.entries.firstOrNull { it in allowedKinds && it !in answeredKinds }
    }

    companion object {
        const val MAX_PROMPTS_PER_CAMPAIGN_24H: Int = 3
    }
}
