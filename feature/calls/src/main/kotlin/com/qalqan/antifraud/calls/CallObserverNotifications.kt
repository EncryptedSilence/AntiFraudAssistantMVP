package com.qalqan.antifraud.calls

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object CallObserverNotifications {

    fun ensureChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            nm.getNotificationChannel(CallObserverService.CHANNEL_ID) == null
        ) {
            nm.createNotificationChannel(
                NotificationChannel(
                    CallObserverService.CHANNEL_ID,
                    "AntiFraud passive observer",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "Shows that AntiFraud is watching for calls. Low priority, no sound."
                    enableVibration(false)
                    setSound(null, null)
                },
            )
        }
    }

    fun build(context: Context, copy: PassiveNotificationCopy): Notification =
        NotificationCompat.Builder(context, CallObserverService.CHANNEL_ID)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentTitle(copy.title)
            .setContentText(copy.body)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
}
