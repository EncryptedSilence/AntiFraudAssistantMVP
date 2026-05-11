package com.qalqan.antifraud.sms

import com.qalqan.antifraud.database.log.ApplicationActionLogger
import com.qalqan.antifraud.domain.AppAction

/**
 * Spec §20.1 — log SMS-permission grants/denials and sweep state changes.
 * Never logs the sender, body, or any user-typed text — the existing
 * `ApplicationActionLogEntry` invariants already reject the forbidden detail keys.
 */
class SmsObserverActionLog(private val logger: ApplicationActionLogger) {
    suspend fun grant(permission: String) =
        logger.log(AppAction.PERMISSION_GRANTED, mapOf("permission" to permission))

    suspend fun deny(permission: String) =
        logger.log(AppAction.PERMISSION_DENIED, mapOf("permission" to permission))

    suspend fun sweepStarted() =
        logger.log(
            AppAction.SETTING_CHANGED,
            mapOf("setting" to "auto_sms_sweep", "state" to "running"),
        )

    suspend fun sweepStopped() =
        logger.log(
            AppAction.SETTING_CHANGED,
            mapOf("setting" to "auto_sms_sweep", "state" to "stopped"),
        )
}
