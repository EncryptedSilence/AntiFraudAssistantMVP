package com.qalqan.antifraud.settings

/**
 * Spec §15.2 + §16.10 + §20.1 — read-only retention values rendered on the Privacy screen.
 *
 * The values are pinned constants here because they live across multiple modules
 * (`:core:database` `RetentionPolicy` for events, `ApplicationActionLogger` for the action
 * log, `ExportProfileRepository` for exports). This file is the single render-time source
 * of truth for the Privacy screen; downstream changes to any of those modules MUST update
 * the corresponding row here.
 */
object RetentionDisplay {
    data class Row(val key: String, val days: Int)

    fun rows(): List<Row> =
        listOf(
            Row(key = "events_active_horizon_days", days = ACTIVE_HORIZON_DAYS),
            Row(key = "events_archive_days", days = ARCHIVE_DAYS),
            Row(key = "action_log_days", days = ACTION_LOG_DAYS),
            Row(key = "export_profile_days", days = EXPORT_PROFILE_DAYS),
        )

    private const val ACTIVE_HORIZON_DAYS: Int = 14
    private const val ARCHIVE_DAYS: Int = 30
    private const val ACTION_LOG_DAYS: Int = 30
    private const val EXPORT_PROFILE_DAYS: Int = 30
}
