package com.qalqan.antifraud.alerts

import com.qalqan.antifraud.database.log.ApplicationActionLogger
import com.qalqan.antifraud.domain.AppAction

/**
 * Spec §22 Stage 8 + §20.1 — writes an `AppAction.PERMISSION_GRANTED` /
 * `_DENIED` entry whenever the Stage 9 alert-permission ActivityResult fires.
 *
 * The Stage 8 `OnboardingSequencer` owns the `ApplicationActionLogger` (already obtained
 * for its own bookkeeping) and the coroutine scope; this helper is a small pure adapter
 * so it can be unit-tested without dragging in `:app` UI code.
 */
object AlertPermissionResultLogger {
    suspend fun log(
        logger: ApplicationActionLogger,
        permission: String,
        granted: Boolean,
    ) {
        logger.log(
            if (granted) AppAction.PERMISSION_GRANTED else AppAction.PERMISSION_DENIED,
            mapOf("permission" to permission),
        )
    }
}
