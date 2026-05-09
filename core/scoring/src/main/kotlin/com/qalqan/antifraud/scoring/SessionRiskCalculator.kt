package com.qalqan.antifraud.scoring

import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Spec §11.3: weighted sum of the per-type max EventRisk inside the session.
 */
object SessionRiskCalculator {
    fun compute(callMax: Int, smsMax: Int, webMax: Int, answerMax: Int): Int {
        require(callMax in 0..100) { "callMax must be in 0..100" }
        require(smsMax in 0..100) { "smsMax must be in 0..100" }
        require(webMax in 0..100) { "webMax must be in 0..100" }
        require(answerMax in 0..100) { "answerMax must be in 0..100" }
        val weighted = callMax * 0.35 + smsMax * 0.30 + webMax * 0.20 + answerMax * 0.15
        return min(weighted.roundToInt(), 100)
    }
}
