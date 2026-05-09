package com.qalqan.antifraud.scoring

/**
 * Spec §10.1 link signals + §10.2 strength table.
 */
enum class LinkSignal {
    SAME_NUMBER,
    USER_CONFIRMED,
    SMS_AFTER_CALL,
    CALL_AFTER_SMS,
    SITE_AFTER_CALL_OR_SMS,
    MULTIPLE_UNKNOWN_24H,
    REPEATING_THEME,
    TEMPORAL_ONLY,
    WEAK
}

object LinkStrengthTable {
    fun coefficient(signal: LinkSignal): Double = when (signal) {
        LinkSignal.SAME_NUMBER, LinkSignal.USER_CONFIRMED -> 1.0
        LinkSignal.SMS_AFTER_CALL, LinkSignal.CALL_AFTER_SMS -> 0.9
        LinkSignal.SITE_AFTER_CALL_OR_SMS -> 0.8
        LinkSignal.MULTIPLE_UNKNOWN_24H, LinkSignal.REPEATING_THEME -> 0.7
        LinkSignal.TEMPORAL_ONLY -> 0.4
        LinkSignal.WEAK -> 0.2
    }

    fun combine(signals: Set<LinkSignal>): Double =
        signals.maxOfOrNull(::coefficient) ?: 0.0
}
