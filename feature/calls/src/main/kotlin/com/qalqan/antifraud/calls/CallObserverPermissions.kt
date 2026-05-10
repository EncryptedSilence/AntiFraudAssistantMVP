package com.qalqan.antifraud.calls

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager

/**
 * Spec §4.2.1 — auto call capture requires READ_PHONE_STATE and READ_CALL_LOG.
 * Reports a tri-state so the UI can distinguish "denied", "partial", and "granted"
 * per the §17.7 permission-denied state requirement.
 */
class CallObserverPermissions(private val context: Context) {
    enum class State { DENIED, PARTIAL, GRANTED }

    fun state(): State {
        val granted = REQUIRED.count { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
        return when (granted) {
            REQUIRED.size -> State.GRANTED
            0 -> State.DENIED
            else -> State.PARTIAL
        }
    }

    companion object {
        val REQUIRED: List<String> = listOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
        )
    }
}
