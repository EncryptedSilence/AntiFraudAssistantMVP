package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.RiskEvent

/**
 * Spec §11.4 + Stage 1 T34 — runs the catalog against a campaign's events and
 * surfaces the triggered weights list `CampaignRiskScorer` already accepts.
 */
object BatchPatternMatcher {
    fun matchAll(
        patterns: List<ScenarioPattern>,
        events: List<RiskEvent>,
    ): List<MatchResult> = patterns.map { PatternMatcher.match(it, events) }

    fun triggeredWeights(
        patterns: List<ScenarioPattern>,
        events: List<RiskEvent>,
    ): List<Int> = matchAll(patterns, events).filter { it.matched }.map { it.triggeredWeight }
}
