package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.PatternId

/**
 * Result of evaluating one `ScenarioPattern` against a campaign's events.
 *
 * `triggeredWeight` is bounded by `PATTERN_RISK_CAP` (60) per spec §11.1 / §11.4 —
 * the same cap `PatternRisk` enforces in `:core:scoring` (Stage 1 T33). The matcher
 * applies the cap up front so callers can sum unconstrained.
 *
 * `triggeringEventIds` carries the events that satisfied at least one condition,
 * so `PatternExplainer` (Phase 6) can reference them in §14 reasons.
 */
data class MatchResult(
    val patternId: PatternId,
    val matched: Boolean,
    val triggeredWeight: Int,
    val triggeringEventIds: List<EventId>,
) {
    init {
        require(triggeredWeight in 0..PATTERN_RISK_CAP) {
            "triggeredWeight must be in 0..$PATTERN_RISK_CAP"
        }
    }

    companion object {
        const val PATTERN_RISK_CAP: Int = 60
    }
}
