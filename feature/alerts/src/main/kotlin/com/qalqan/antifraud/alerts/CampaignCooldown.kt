package com.qalqan.antifraud.alerts

import androidx.annotation.VisibleForTesting
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Spec §4.4.4 — same campaign warned at most twice within 24 h. In-memory only per the
 * Phase 1 brainstorm decision: cooldown is a UX nicety, not a privacy invariant.
 *
 * [allow] is idempotent for the *check* (returns the same boolean for the same args) but
 * has a side effect — it RECORDS the call. Callers must invoke it exactly once per
 * alert-dispatch decision.
 */
object CampaignCooldown {
    private val timestamps = ConcurrentHashMap<String, MutableList<Instant>>()
    private val window = Duration.ofHours(WINDOW_HOURS)

    @Synchronized
    fun allow(
        campaignId: String,
        now: Instant = Instant.now(),
    ): Boolean {
        val list = timestamps.getOrPut(campaignId) { mutableListOf() }
        list.removeAll { Duration.between(it, now) >= window }
        if (list.size >= MAX_PER_WINDOW) return false
        list.add(now)
        return true
    }

    @VisibleForTesting
    fun clearAllForTest() {
        timestamps.clear()
    }

    private const val WINDOW_HOURS = 24L
    private const val MAX_PER_WINDOW = 2
}
