package com.qalqan.antifraud.alerts

import android.content.Context
import androidx.core.app.NotificationManagerCompat

/**
 * Spec §4.4.3 + §17.0.4 — `POST_NOTIFICATIONS` denied disables active monitoring AND
 * shows a permanent in-app banner. The "loud failure" is intentional: silently swallowing
 * alerts after the user denied notifications would be a worse UX than the app pausing
 * with a visible reason.
 *
 * `:feature:calls` and `:feature:sms` consult this gate before starting their observer
 * pipelines; the home screen consults it for the red-banner UI (T39).
 */
class NotificationPermissionGate(private val context: Context) {
    fun activeMonitoringAllowed(): Boolean = NotificationManagerCompat.from(context).areNotificationsEnabled()
}
