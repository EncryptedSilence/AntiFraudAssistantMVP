package com.qalqan.antifraud.web

import com.qalqan.antifraud.database.log.ApplicationActionLogger
import com.qalqan.antifraud.domain.AppAction

/**
 * Spec §20.1 — log site-submission events and lookalike matches WITHOUT logging the
 * domain, the URL, the typed input, or the seed string. The forbidden-detail-key
 * invariant is enforced by [com.qalqan.antifraud.domain.ApplicationActionLogEntry.init]
 * at construction time; we add a defense-in-depth assertion in [WebObserverActionLogTest].
 */
class WebObserverActionLog(private val logger: ApplicationActionLogger) {
    suspend fun manualSubmitted() =
        logger.log(
            AppAction.SETTING_CHANGED,
            mapOf("setting" to "manual_site_submitted", "state" to "recorded"),
        )

    suspend fun lookalikeTriggered(distance: Int) =
        logger.log(
            AppAction.SETTING_CHANGED,
            mapOf("setting" to "lookalike_match", "distance" to distance.toString()),
        )

    suspend fun questionTriggered() =
        logger.log(
            AppAction.SETTING_CHANGED,
            mapOf("setting" to "post_site_question", "state" to "triggered"),
        )
}
