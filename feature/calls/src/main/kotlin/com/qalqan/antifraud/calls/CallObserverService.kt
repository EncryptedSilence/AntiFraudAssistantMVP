package com.qalqan.antifraud.calls

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder

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
        val notif: Notification = CallObserverNotifications.build(
            this,
            PassiveNotificationCopy(eventsLast24h = 0, alertsLast24h = 0),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL)
        } else {
            @Suppress("DEPRECATION")
            startForeground(NOTIFICATION_ID, notif)
        }
    }

    private fun ensureChannel() {
        CallObserverNotifications.ensureChannel(this)
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
