package com.qalqan.antifraud.database

import java.time.Instant

/**
 * Spec §15.2 — applies the configured TTLs to every retained entity. Intended to run on
 * WorkManager once per day; safe to invoke ad hoc.
 */
class RetentionPurger internal constructor(
    private val db: AntifraudDatabase,
    private val policy: RetentionPolicy,
) {
    suspend fun purge(now: Instant) {
        db.callEventDao().deleteOlderThan(now.minus(policy.callEventTtl).toEpochMilli())
        db.smsEventDao().deleteOlderThan(now.minus(policy.smsEventTtl).toEpochMilli())
        db.webEventDao().deleteOlderThan(now.minus(policy.webEventTtl).toEpochMilli())
        db.userAnswerDao().deleteOlderThan(now.minus(policy.userAnswerTtl).toEpochMilli())
        db.riskSessionDao().deleteOlderThan(now.minus(policy.sessionTtl).toEpochMilli())
        db.riskCampaignDao().deleteArchivedOlderThan(
            now.minus(policy.archivedCampaignTtl).toEpochMilli(),
        )
    }

    companion object {
        fun forRepositories(
            repositories: Repositories,
            policy: RetentionPolicy = RetentionPolicy.DEFAULT,
        ): RetentionPurger = RetentionPurger(repositories.db, policy)
    }
}
