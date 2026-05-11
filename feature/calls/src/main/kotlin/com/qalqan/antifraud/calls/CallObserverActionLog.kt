package com.qalqan.antifraud.calls

import com.qalqan.antifraud.database.log.ApplicationActionLogger
import com.qalqan.antifraud.domain.AppAction

/**
 * Spec §20.1 — log permission grants/denials and observer state changes.
 * Never logs the phone number, SMS body, or any user-typed text — the existing
 * `ApplicationActionLogEntry` invariants already reject the forbidden detail keys.
 */
class CallObserverActionLog(private val logger: ApplicationActionLogger) {
    suspend fun grant(permission: String) =
        logger.log(AppAction.PERMISSION_GRANTED, mapOf("permission" to permission))

    suspend fun deny(permission: String) =
        logger.log(AppAction.PERMISSION_DENIED, mapOf("permission" to permission))

    suspend fun observerStarted() =
        logger.log(AppAction.SETTING_CHANGED, mapOf("setting" to "auto_call_capture", "state" to "running"))

    suspend fun observerStopped() =
        logger.log(AppAction.SETTING_CHANGED, mapOf("setting" to "auto_call_capture", "state" to "stopped"))
}
