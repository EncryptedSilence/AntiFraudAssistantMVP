package com.qalqan.antifraud.patterns

import com.qalqan.antifraud.domain.PatternId

/**
 * Spec §14 — explanation surface for a triggered set of patterns.
 *
 * `level` is the maximum warning level across triggered patterns (MEDIUM < HIGH < CRITICAL).
 * `reasons` is the deduped, ordered list of per-condition phrases the §17 UI renders.
 */
data class Reason(val patternId: PatternId, val text: String) {
    init {
        require(text.isNotBlank()) { "Reason.text must not be blank" }
    }
}

data class Explanation(
    val level: WarningLevel,
    val reasons: List<Reason>,
) {
    init {
        require(reasons.isNotEmpty()) { "Explanation must have at least one reason" }
    }

    companion object {
        const val MIN_REASONS_TARGET: Int = 3
    }
}
