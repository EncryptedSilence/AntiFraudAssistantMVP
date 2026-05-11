package com.qalqan.antifraud.calls

import com.qalqan.antifraud.database.Repositories
import java.time.Instant

/**
 * Spec §17.0.3 — supplies the 24-h counts for the ongoing notification copy.
 * Stage 3 only counts CallEvent rows. SMS counts arrive in Stage 4 and the
 * alert counter wires up in Stage 9 (full-screen alert pipeline).
 */
class PassiveCounters(private val repos: Repositories) {
    suspend fun eventsLast24h(now: Instant): Int {
        val cutoff = now.minusSeconds(SECONDS_PER_DAY)
        return repos.calls.listSince(cutoff).size
    }

    @Suppress("UNUSED_PARAMETER", "FunctionOnlyReturningConstant")
    fun alertsLast24h(now: Instant): Int = 0

    private companion object {
        const val SECONDS_PER_DAY = 24L * 60L * 60L
    }
}
