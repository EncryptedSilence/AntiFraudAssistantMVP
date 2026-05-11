package com.qalqan.antifraud.calls

/**
 * Spec §17.0.3 — passive-transparency ongoing notification copy.
 * Locale-aware strings live in `:app` resources (Stage 8); Stage 3 ships English-only
 * to keep the privacy-boundary tests deterministic.
 */
data class PassiveNotificationCopy(
    val eventsLast24h: Int,
    val alertsLast24h: Int,
) {
    init {
        require(eventsLast24h >= 0) { "eventsLast24h must be non-negative" }
        require(alertsLast24h >= 0) { "alertsLast24h must be non-negative" }
    }

    val title: String = "Watching for fraud signals"
    val body: String = "Last 24 h: $eventsLast24h events, $alertsLast24h alerts."
}
