package com.qalqan.antifraud.alerts

import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Spec §4.4.3 — pre-Android 14 `USE_FULL_SCREEN_INTENT` is a normal permission and is
 * always granted; on Android 14+ it requires user opt-in via Settings, and the OS only
 * honors `Notification.fullScreenIntent` when `canUseFullScreenIntent()` is true.
 *
 * When denied: callers fall through to a regular heads-up notification on the same
 * channel — the IMPORTANCE_HIGH channel still produces an audible heads-up.
 */
class FullScreenIntentPermissionGate(private val context: Context) {
    fun fullScreenAllowed(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true
        val nm = context.getSystemService(NotificationManager::class.java) ?: return false
        return nm.canUseFullScreenIntent()
    }
}
