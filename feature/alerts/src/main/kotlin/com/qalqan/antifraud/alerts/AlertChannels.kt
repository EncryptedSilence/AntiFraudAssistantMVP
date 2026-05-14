package com.qalqan.antifraud.alerts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build

/**
 * Spec §4.4.1 — channel definitions.
 *
 * - [CHANNEL_CRITICAL] = IMPORTANCE_HIGH + CATEGORY_CALL + vibration + alert sound;
 *   used for `critical` and `high` alerts (full-screen intent target).
 * - [CHANNEL_MEDIUM]   = IMPORTANCE_DEFAULT, no full-screen intent, regular sound;
 *   used for `medium` alerts (§4.4.2 row 4).
 *
 * The §17.0.3 passive observer channel `antifraud_passive_observer` is owned by
 * `:feature:calls.CallObserverNotifications` (Stage 3); Stage 9 does not redefine it.
 */
object AlertChannels {
    const val CHANNEL_CRITICAL = "antifraud_critical"
    const val CHANNEL_MEDIUM = "antifraud_medium"

    fun ensure(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureCritical(nm)
        ensureMedium(nm)
    }

    private fun ensureCritical(nm: NotificationManager) {
        if (nm.getNotificationChannel(CHANNEL_CRITICAL) != null) return
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val attrs =
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        val channel =
            NotificationChannel(
                CHANNEL_CRITICAL,
                "AntiFraud critical alerts",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description =
                    "High-importance fraud warnings shown over your dialer or lock screen."
                enableVibration(true)
                vibrationPattern = longArrayOf(0L, 400L, 200L, 400L)
                setSound(sound, attrs)
            }
        nm.createNotificationChannel(channel)
    }

    private fun ensureMedium(nm: NotificationManager) {
        if (nm.getNotificationChannel(CHANNEL_MEDIUM) != null) return
        val channel =
            NotificationChannel(
                CHANNEL_MEDIUM,
                "AntiFraud medium alerts",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Lower-importance fraud warnings; appears as a regular notification."
                enableVibration(false)
                setSound(null, null)
            }
        nm.createNotificationChannel(channel)
    }
}
