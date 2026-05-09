package com.qalqan.antifraud.scoring

import com.qalqan.antifraud.domain.RiskBand

/**
 * Spec §11.5 — integer risk bands with no overlap.
 */
object RiskLevel {
    fun fromScore(score: Int): RiskBand {
        require(score in 0..100) { "score must be in 0..100, got $score" }
        return when (score) {
            in 0..30 -> RiskBand.LOW
            in 31..60 -> RiskBand.MEDIUM
            in 61..80 -> RiskBand.HIGH
            else -> RiskBand.CRITICAL
        }
    }
}
