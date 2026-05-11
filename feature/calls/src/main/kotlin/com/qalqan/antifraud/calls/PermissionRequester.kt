package com.qalqan.antifraud.calls

import android.Manifest
import android.os.Build

/**
 * Spec §4.2.1 — runtime permissions to request on first run.
 * Spec §17.7 — permission-denied state must be loud, not silent.
 *
 * The Android-API surface (registering for results) is `:app`'s job; this class
 * just supplies the right `requestList()` and a tri-state summarizer that the
 * `:app` UI uses to drive the §17.7 banner.
 */
object PermissionRequester {

    fun requestList(): List<String> = buildList {
        add(Manifest.permission.READ_PHONE_STATE)
        add(Manifest.permission.READ_CALL_LOG)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun summarize(granted: Map<String, Boolean>): CallObserverPermissions.State {
        val phone = granted[Manifest.permission.READ_PHONE_STATE] == true
        val call = granted[Manifest.permission.READ_CALL_LOG] == true
        val both = phone && call
        val none = !phone && !call
        return when {
            both -> CallObserverPermissions.State.GRANTED
            none -> CallObserverPermissions.State.DENIED
            else -> CallObserverPermissions.State.PARTIAL
        }
    }
}
