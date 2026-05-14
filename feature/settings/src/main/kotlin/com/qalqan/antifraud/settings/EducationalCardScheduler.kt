package com.qalqan.antifraud.settings

/**
 * Spec §19A — show at most one educational card per 24 h. Gated by
 * [UserSettings.educationalCardsEnabled].
 */
object EducationalCardScheduler {
    private const val TWENTY_FOUR_HOURS_MS: Long = 24L * 60L * 60L * 1000L

    fun shouldShow(
        enabled: Boolean,
        lastShownAtMs: Long,
        nowMs: Long,
    ): Boolean = enabled && (nowMs - lastShownAtMs >= TWENTY_FOUR_HOURS_MS)
}
