package com.qalqan.antifraud.scoring

import kotlin.math.min

/**
 * Spec §11.1 — context risk derived from the event's relationship signals.
 * Only the high-information signals contribute. Capped at 60 so context alone does not max EventRisk.
 */
object ContextRisk {
    private const val CAP = 60

    fun compute(signals: Set<LinkSignal>): Int {
        var score = 0
        if (LinkSignal.SMS_AFTER_CALL in signals) score += 25
        if (LinkSignal.CALL_AFTER_SMS in signals) score += 25
        if (LinkSignal.SITE_AFTER_CALL_OR_SMS in signals) score += 25
        if (LinkSignal.MULTIPLE_UNKNOWN_24H in signals) score += 20
        if (LinkSignal.REPEATING_THEME in signals) score += 15
        return min(score, CAP)
    }
}
