package com.qalqan.antifraud.alerts

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

/**
 * Spec §4.4.2 — routes an [AlertBand] to the right surface(s):
 *   - SILENT  -> nothing.
 *   - REGULAR -> heads-up notification on [AlertChannels.CHANNEL_MEDIUM].
 *   - FULL_SCREEN -> heads-up notification on [AlertChannels.CHANNEL_CRITICAL] with
 *     `fullScreenIntent` pointing at [CriticalAlertActivity].
 *   - FULL_SCREEN_PLUS_OVERLAY -> same as FULL_SCREEN, plus [overlayLauncher] is invoked
 *     when [overlayShouldFire] is true.
 *
 * The action-logger callback records an `AppAction.PATTERN_APPLIED` entry (Stage 1 enum —
 * no new variant in Stage 9) for each dispatched alert; this is what bumps
 * `PassiveCounters.alertsLast24h`. A purpose-built `AppAction.ALERT_POSTED` variant is a
 * post-MVP refinement.
 */
open class AlertDispatcher(
    private val context: Context,
    private val builder: AlertNotificationBuilder,
    private val overlayLauncher: (AlertContent) -> Unit,
    private val actionLogger: () -> Unit,
) {
    open fun dispatch(
        content: AlertContent,
        band: AlertBand,
        campaignId: String,
        overlayShouldFire: Boolean = false,
    ) {
        if (band == AlertBand.SILENT) return
        val notif = builder.build(context, content, band)
        val id = campaignId.hashCode() and ID_MASK
        NotificationManagerCompat.from(context).notify(id, notif)
        actionLogger()
        if (band == AlertBand.FULL_SCREEN_PLUS_OVERLAY && overlayShouldFire) {
            overlayLauncher(content)
        }
    }

    /**
     * Default overlay launcher: starts [OverlayBannerActivity] with the alert reasons.
     * `:app` installs this via the wiring shim.
     */
    companion object {
        private const val ID_MASK = 0x7FFFFFFF

        fun overlayLauncherFor(
            context: Context,
            campaignId: String,
        ): (AlertContent) -> Unit =
            { content ->
                val intent =
                    Intent(context, OverlayBannerActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(OverlayBannerActivity.EXTRA_REASONS, content.reasons.toTypedArray())
                        putExtra(OverlayBannerActivity.EXTRA_CAMPAIGN_ID, campaignId)
                    }
                context.startActivity(intent)
            }
    }
}
