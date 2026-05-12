package com.qalqan.antifraud.web

/**
 * Spec §5.5.1 / §11.5 — the question pipeline activates at `high`+ (score ≥ 60).
 * Stage 5 only records that the condition fired; Stage 8 / 9 render Q1 / Q2 / Q3.
 *
 * The thresholds mirror the band cutoffs in `:core:domain` `RiskBand.compute(...)`:
 *   - low:      0..29
 *   - medium:  30..59
 *   - high:    60..79
 *   - critical: 80..100
 *
 * We do not import `RiskBand` here to keep `:feature:web` independent of the domain
 * helpers; the integer thresholds are sufficient and pinned by the test.
 */
class PostSiteQuestionTrigger(private val actionLog: WebObserverActionLog) {
    suspend fun maybeRecord(score: Int) {
        if (score >= HIGH_THRESHOLD) {
            actionLog.questionTriggered()
        }
    }

    companion object {
        const val HIGH_THRESHOLD = 60
    }
}
