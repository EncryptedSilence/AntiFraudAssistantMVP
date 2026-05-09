package com.qalqan.antifraud.scoring

import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Spec Appendix B — sensitivity coefficients.
 */
enum class Sensitivity(val eventMultiplier: Double, val thresholdOffset: Int) {
    LOW(0.7, 10),
    STANDARD(1.0, 0),
    HIGH(1.2, -10),
    MAXIMUM(1.4, -15),
    ;

    fun applyTo(score: Int): Int {
        require(score in 0..100) { "score must be in 0..100" }
        return min((score * eventMultiplier).roundToInt(), 100).coerceAtLeast(0)
    }
}
