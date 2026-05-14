package com.qalqan.antifraud.alerts

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

/**
 * Spec §4.4.1 (1) + §17.0.1 — builds a [Notification] from [AlertContent] and the chosen
 * [AlertBand]. Full-screen intent is set for [AlertBand.FULL_SCREEN] and
 * [AlertBand.FULL_SCREEN_PLUS_OVERLAY]; for [AlertBand.REGULAR] only a heads-up.
 *
 * The "Why this alert" action is omitted from the heads-up — it lands inside the
 * full-screen activity (Phase 4) where there is room for it.
 */
class AlertNotificationBuilder {
    fun build(
        context: Context,
        content: AlertContent,
        band: AlertBand,
    ): Notification {
        val channel =
            when (band) {
                AlertBand.REGULAR -> AlertChannels.CHANNEL_MEDIUM
                AlertBand.FULL_SCREEN, AlertBand.FULL_SCREEN_PLUS_OVERLAY ->
                    AlertChannels.CHANNEL_CRITICAL
                AlertBand.SILENT -> error("SILENT band must not reach AlertNotificationBuilder")
            }
        val style =
            NotificationCompat.InboxStyle().also { s ->
                content.reasons.forEach { s.addLine(it) }
            }
        val builder =
            NotificationCompat.Builder(context, channel)
                .setCategory(Notification.CATEGORY_CALL)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(content.title)
                .setContentText(content.reasons.first())
                .setStyle(style)
                .setOngoing(false)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        if (band == AlertBand.FULL_SCREEN || band == AlertBand.FULL_SCREEN_PLUS_OVERLAY) {
            val fsIntent =
                Intent(context, CriticalAlertActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            val pi =
                PendingIntent.getActivity(
                    context,
                    /* requestCode = */ 0,
                    fsIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            builder.setFullScreenIntent(pi, /* highPriority = */ true)
        }
        return builder.build()
    }
}
