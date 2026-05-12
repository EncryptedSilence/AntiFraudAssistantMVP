package com.qalqan.antifraud.web

/**
 * Spec §12.3 — flag a typed domain as a lookalike when it is within Levenshtein
 * distance 2 of a [LookalikeSeedCatalog] entry but not equal to it.
 *
 * Why distance 2 and not 1: human typos and one-character substitutions ("kasp1.kz",
 * "halikbank.kz") need to match, but the false-positive rate climbs sharply past 2.
 * The threshold is also the one §12.3 already encodes in `WebBaseRisk`.
 *
 * The matcher runs against ~20 seeds × 1 distance call each = 20 DP computations per
 * input, each O(|input| × |seed|) ≈ O(15 × 15) = 225 cells. Negligible on any device.
 */
class LookalikeDetector(private val seeds: Set<String>) {
    /**
     * @return the closest seed within [LookalikeMatch.MAX_LOOKALIKE_DISTANCE] (excluding
     * exact matches), or `null` when no seed is within threshold or the input is empty.
     */
    fun match(canonical: String): LookalikeMatch? {
        if (canonical.isBlank()) return null
        var best: LookalikeMatch? = null
        seeds.forEach { seed ->
            val d = levenshtein(canonical, seed)
            if (d in 1..LookalikeMatch.MAX_LOOKALIKE_DISTANCE) {
                val current = best
                if (current == null || d < current.distance) {
                    best = LookalikeMatch(seed = seed, distance = d)
                }
            }
        }
        return best
    }

    /**
     * Standard iterative two-row Levenshtein. Pure Kotlin; no Android deps.
     */
    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)

        for (i in 1..a.length) {
            curr[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = minOf(
                    curr[j - 1] + 1,
                    prev[j] + 1,
                    prev[j - 1] + cost,
                )
            }
            System.arraycopy(curr, 0, prev, 0, curr.size)
        }
        return prev[b.length]
    }
}
