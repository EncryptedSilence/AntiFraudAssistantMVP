package com.qalqan.antifraud.web

/**
 * Spec §12.3 — when a typed domain is within Levenshtein distance 2 of a curated
 * [LookalikeSeedCatalog] entry (but not equal to it), the orchestrator passes
 * `lookalikeMatch = true` to `WebBaseRisk.compute(...)` and records the seed string
 * locally for the explainability path.
 *
 * Distance 0 means exact match — the user actually typed the real bank's domain —
 * which is NOT a lookalike. The detector's contract is to return `null` in that case.
 */
data class LookalikeMatch(val seed: String, val distance: Int) {
    init {
        require(distance in 1..MAX_LOOKALIKE_DISTANCE) {
            "lookalike distance must be in 1..$MAX_LOOKALIKE_DISTANCE; got $distance"
        }
        require(seed.isNotBlank()) { "seed must not be blank" }
    }

    companion object {
        const val MAX_LOOKALIKE_DISTANCE = 2
    }
}
