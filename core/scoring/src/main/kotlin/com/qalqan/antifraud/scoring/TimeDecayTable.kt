package com.qalqan.antifraud.scoring

import java.time.Duration

/**
 * Spec §10.3 — discrete decay table.
 */
object TimeDecayTable {
    fun coefficient(age: Duration): Double {
        val hours = age.toHours().coerceAtLeast(0)
        return when {
            hours <= 24 -> 1.0
            hours <= 24 * 3 -> 0.8
            hours <= 24 * 7 -> 0.6
            hours <= 24 * 14 -> 0.4
            else -> 0.0
        }
    }
}
