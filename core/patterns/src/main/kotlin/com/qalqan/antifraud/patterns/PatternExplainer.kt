package com.qalqan.antifraud.patterns

/**
 * Spec §14 — produce ≥3 reasons (or all available) per triggered pattern set.
 *
 * Each condition becomes one Reason via `ConditionPhraser.phrase`. Reasons are
 * deduped by text to avoid "Unknown call." showing twice when two conditions
 * happen to phrase the same way.
 */
object PatternExplainer {
    fun explain(matches: List<Pair<ScenarioPattern, MatchResult>>): Explanation {
        val triggered = matches.filter { (_, r) -> r.matched }
        require(triggered.isNotEmpty()) { "explain called with no triggered patterns" }

        val maxLevel = triggered
            .map { (p, _) -> p.warning.level }
            .maxBy { it.severityRank() }

        val reasons = triggered.flatMap { (pattern, _) ->
            pattern.conditions.map { cond ->
                Reason(pattern.patternId, ConditionPhraser.phrase(cond))
            }
        }.distinctBy { it.text }

        return Explanation(level = maxLevel, reasons = reasons)
    }

    private fun WarningLevel.severityRank(): Int = when (this) {
        WarningLevel.MEDIUM -> 1
        WarningLevel.HIGH -> 2
        WarningLevel.CRITICAL -> 3
    }
}
