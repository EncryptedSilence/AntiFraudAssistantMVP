package com.qalqan.antifraud.alerts

import android.app.ActivityManager
import android.content.Context
import android.provider.Settings

/**
 * Spec §4.4.1 (2) — the overlay fires only when ALL of:
 *   - [canDrawOverlays] == true (user granted SYSTEM_ALERT_WINDOW),
 *   - [band] == FULL_SCREEN_PLUS_OVERLAY (i.e. RiskBand.CRITICAL),
 *   - [foregroundIsRelevant] == true ([RelevantForegroundAppDetector] resolved a known package).
 *
 * Two helpers: [permissionGranted] reads Settings.canDrawOverlays; [foregroundPackage]
 * does a best-effort detect without requiring PACKAGE_USAGE_STATS.
 */
object OverlayGate {
    fun shouldFire(
        canDrawOverlays: Boolean,
        band: AlertBand,
        foregroundIsRelevant: Boolean,
    ): Boolean = canDrawOverlays && band == AlertBand.FULL_SCREEN_PLUS_OVERLAY && foregroundIsRelevant

    fun permissionGranted(context: Context): Boolean = Settings.canDrawOverlays(context)

    /**
     * Best-effort foreground-package resolution that does NOT require PACKAGE_USAGE_STATS.
     * On Android 5.0+ `getRunningAppProcesses` returns only our own process, so this
     * always returns null on modern devices. The caller treats null as "irrelevant", so
     * the overlay simply doesn't fire — the full-screen alert still does (§4.4.3).
     *
     * If the app gains UsageStatsManager in a later stage, this method moves there.
     */
    fun foregroundPackage(context: Context): String? {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val procs = am?.runningAppProcesses
        val foreground =
            procs?.firstOrNull { it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
        return foreground?.processName
    }
}
