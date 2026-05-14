package com.qalqan.antifraud.alerts

import com.qalqan.antifraud.domain.RiskBand

/**
 * Spec §4.4.2 — what surface to use given the computed [RiskBand].
 *
 * - [SILENT]   = §4.4.2 row 5: low risk = log only, no notification.
 * - [REGULAR]  = §4.4.2 row 4: medium risk = regular notification (non-full-screen) within 10 s.
 * - [FULL_SCREEN] = §4.4.2 row 3: high risk = full-screen-intent within 5 s.
 * - [FULL_SCREEN_PLUS_OVERLAY] = §4.4.2 rows 1+2: critical risk = full-screen + overlay (if gated).
 */
enum class AlertBand {
    SILENT,
    REGULAR,
    FULL_SCREEN,
    FULL_SCREEN_PLUS_OVERLAY,
    ;

    companion object {
        fun from(band: RiskBand): AlertBand =
            when (band) {
                RiskBand.LOW -> SILENT
                RiskBand.MEDIUM -> REGULAR
                RiskBand.HIGH -> FULL_SCREEN
                RiskBand.CRITICAL -> FULL_SCREEN_PLUS_OVERLAY
            }
    }
}
