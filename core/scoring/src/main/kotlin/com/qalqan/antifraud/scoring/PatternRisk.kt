package com.qalqan.antifraud.scoring

import kotlin.math.min

/**
 * Spec §11.1, §11.4 — capped at 60 per campaign so a single triggered pattern set does not
 * single-handedly push to "critical".
 */
object PatternRisk {
    private const val CAP = 60

    fun compute(triggeredPatternWeights: List<Int>): Int {
        val sum = triggeredPatternWeights.sumOf { it.coerceAtLeast(0) }
        return min(sum, CAP)
    }
}
