package com.qalqan.antifraud.sms

import android.Manifest

/**
 * Spec §4.2.2 — runtime permissions to request on first run. `POST_NOTIFICATIONS` is NOT
 * re-requested here; Stage 3's `PermissionRequester` already requests it for the §17.0.3
 * transparency notification.
 */
object SmsPermissionRequester {
    fun requestList(): List<String> =
        listOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
        )

    fun summarize(granted: Map<String, Boolean>): SmsObserverPermissions.State {
        val receive = granted[Manifest.permission.RECEIVE_SMS] == true
        val read = granted[Manifest.permission.READ_SMS] == true
        return when {
            receive && read -> SmsObserverPermissions.State.GRANTED
            !receive && !read -> SmsObserverPermissions.State.DENIED
            else -> SmsObserverPermissions.State.PARTIAL
        }
    }
}
