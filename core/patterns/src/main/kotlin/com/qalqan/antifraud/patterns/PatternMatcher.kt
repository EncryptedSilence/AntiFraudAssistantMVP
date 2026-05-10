package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.EventId
import com.qalqan.antifraud.domain.RiskEvent
import kotlin.math.min

/**
 * Spec §6 + §11.1 / §11.4 — match a single pattern against a campaign's events.
 *
 * Semantics:
 * - All conditions must find ≥1 matching event (AND-of-conditions).
 * - Disabled patterns never trigger (acceptance §23 #14).
 * - Conditions with `timeWindowHours` only consider events within that window of
 *   the most recent event in `events`.
 * - Triggered weight is the sum of triggered condition weights, capped at
 *   `MatchResult.PATTERN_RISK_CAP` (60) per `PatternRisk` in `:core:scoring`.
 */
@Suppress("ReturnCount")
object PatternMatcher {
    fun match(
        pattern: ScenarioPattern,
        events: List<RiskEvent>,
    ): MatchResult {
        if (!pattern.enabled) {
            return noMatch(pattern)
        }
        if (events.isEmpty()) {
            return noMatch(pattern)
        }
        val triggeringEventIds = mutableListOf<EventId>()
        var weightSum = 0
        for (cond in pattern.conditions) {
            val candidates = applyTimeWindow(events, cond)
            val firstMatch =
                candidates.firstOrNull { ConditionEvaluator.evaluate(cond, it) }
                    ?: return noMatch(pattern)
            triggeringEventIds += firstMatch.eventId
            weightSum += cond.weight
        }
        return MatchResult(
            patternId = pattern.patternId,
            matched = true,
            triggeredWeight = min(weightSum, MatchResult.PATTERN_RISK_CAP),
            triggeringEventIds = triggeringEventIds,
        )
    }

    private fun noMatch(pattern: ScenarioPattern): MatchResult =
        MatchResult(
            patternId = pattern.patternId,
            matched = false,
            triggeredWeight = 0,
            triggeringEventIds = emptyList(),
        )

    private fun applyTimeWindow(
        events: List<RiskEvent>,
        cond: PatternCondition,
    ): List<RiskEvent> {
        val window = cond.timeWindowHours ?: return events
        val newest = events.maxByOrNull { it.occurredAt } ?: return emptyList()
        val cutoff = newest.occurredAt.minusSeconds(window.toLong() * SECONDS_PER_HOUR)
        return events.filter { !it.occurredAt.isBefore(cutoff) }
    }

    private const val SECONDS_PER_HOUR: Long = 3600
}
