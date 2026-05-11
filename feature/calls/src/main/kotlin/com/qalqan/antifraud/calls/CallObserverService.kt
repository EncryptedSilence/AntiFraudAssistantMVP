package com.qalqan.antifraud.calls

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Spec §4.2.1 — a foreground service of type `phoneCall` that owns the telephony listener.
 * On Android 14+ `startForeground` MUST pass the matching `foregroundServiceType` constant.
 *
 * Phase 3: skeleton with placeholder notification. Phase 4 replaces the notification
 * with the §17.0.3 passive-transparency copy. Phase 5 wires CallLog read on IDLE.
 */
class CallObserverService : Service() {
    private var router: CallStateRouter? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        startForegroundCompat()
        router = CallStateRouter(this) { transition -> onTransition(transition) }.also { it.register() }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        router?.unregister()
        router = null
        super.onDestroy()
    }

    private fun onTransition(
        @Suppress("UNUSED_PARAMETER") transition: CallTransition,
    ) {
        // Phase 5 attaches the CallLog reader here.
    }

    private fun startForegroundCompat() {
        val notif: Notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setOngoing(true)
                .setContentTitle("AntiFraud")
                .setContentText("Watching for fraud signals.")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
        } else {
            @Suppress("DEPRECATION")
            startForeground(NOTIFICATION_ID, notif)
        }
    }

    private fun ensureChannel() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "AntiFraud passive observer", NotificationManager.IMPORTANCE_LOW)
                    .apply {
                        description = "Shows that AntiFraud is watching for calls. Low priority, no sound."
                        enableVibration(false)
                        setSound(null, null)
                    },
            )
        }
    }

    companion object {
        const val CHANNEL_ID = "antifraud_passive_observer"
        const val NOTIFICATION_ID = 0xCA11

        fun start(context: Context) {
            val intent = Intent(context, CallObserverService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, CallObserverService::class.java))
        }
    }
}
