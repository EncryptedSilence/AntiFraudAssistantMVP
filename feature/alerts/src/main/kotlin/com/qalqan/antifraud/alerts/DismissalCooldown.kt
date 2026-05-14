package com.qalqan.antifraud.alerts

import androidx.annotation.VisibleForTesting
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Spec §17.0.1 — `Dismiss` records dismissal and lowers urgency for 5 min for the same
 * campaign. The cooldown lives in-memory only (process-death loses it) — that's an
 * intentional MVP choice per the Phase 1 brainstorm: cooldown is a UX nicety, not a
 * privacy invariant.
 *
 * The pipeline (Phase 6) consults [isCoolingDown] before posting an alert and downgrades
 * a CRITICAL+OVERLAY to a regular notification if the user just dismissed this campaign.
 */
object DismissalCooldown {
    private val dismissedAt = ConcurrentHashMap<String, Instant>()
    private val window: Duration = Duration.ofMinutes(MINUTES)

    fun record(
        campaignId: String,
        at: Instant = Instant.now(),
    ) {
        dismissedAt[campaignId] = at
    }

    fun isCoolingDown(
        campaignId: String,
        now: Instant = Instant.now(),
    ): Boolean {
        val t = dismissedAt[campaignId] ?: return false
        return Duration.between(t, now) < window
    }

    @VisibleForTesting
    fun clearAllForTest() {
        dismissedAt.clear()
    }

    private const val MINUTES = 5L
}
