package com.qalqan.antifraud.database

import java.time.Duration

/**
 * Spec §15.2 retention defaults.
 */
data class RetentionPolicy(
    val sessionTtl: Duration = Duration.ofHours(SESSION_TTL_HOURS),
    val activeCampaignTtl: Duration = Duration.ofDays(ACTIVE_CAMPAIGN_TTL_DAYS),
    val archivedCampaignTtl: Duration = Duration.ofDays(ARCHIVED_CAMPAIGN_TTL_DAYS),
    val callEventTtl: Duration = Duration.ofDays(EVENT_TTL_DAYS),
    val smsEventTtl: Duration = Duration.ofDays(EVENT_TTL_DAYS),
    val webEventTtl: Duration = Duration.ofDays(EVENT_TTL_DAYS),
    val userAnswerTtl: Duration = Duration.ofDays(EVENT_TTL_DAYS),
    val warningLogTtl: Duration = Duration.ofDays(EVENT_TTL_DAYS),
    val actionLogTtl: Duration = Duration.ofDays(EVENT_TTL_DAYS),
) {
    companion object {
        val DEFAULT: RetentionPolicy = RetentionPolicy()
        private const val SESSION_TTL_HOURS = 24L
        private const val ACTIVE_CAMPAIGN_TTL_DAYS = 14L
        private const val ARCHIVED_CAMPAIGN_TTL_DAYS = 30L
        private const val EVENT_TTL_DAYS = 30L
    }
}
