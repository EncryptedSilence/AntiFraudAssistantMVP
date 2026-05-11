package com.qalqan.antifraud.calls

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.annotation.VisibleForTesting
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.database.manual.CallEntryDigest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Spec §4.2.1 — a foreground service of type `phoneCall` that owns the telephony listener.
 * On Android 14+ `startForeground` MUST pass the matching `foregroundServiceType` constant.
 *
 * T27: IDLE transitions now trigger AutoCallCapture → reads latest CallLog row, builds
 * a CallEvent, persists it via Repositories.
 */
class CallObserverService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var router: CallStateRouter? = null
    private var capture: AutoCallCapture? = null
    private var repos: Repositories? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        startForegroundCompat()
        captureFactory(applicationContext)
        router = CallStateRouter(this) { transition ->
            scope.launch { onTransition(transition) }
        }.also { it.register() }
    }

    private fun captureFactory(context: Context) {
        try {
            val r = repositoriesFactory(context)
            repos = r
            capture = AutoCallCapture(
                reader = CallLogReader(context.contentResolver),
                builder = CallEventBuilder(
                    digest = CallEntryDigest.create(context),
                    contacts = IsKnownContactResolver(r.contacts),
                    repeats = RepeatCallDetector(r.calls),
                ),
                calls = r.calls,
            )
        } catch (e: Exception) {
            // AndroidKeyStore is unavailable in unit-test environments (Robolectric).
            // In production this path is never taken; capture stays null and onIdle is a no-op.
            android.util.Log.e(TAG, "AutoCallCapture init failed; IDLE events will be dropped", e)
        }
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
        scope.cancel()
        repos?.close()
        repos = null
        super.onDestroy()
    }

    private suspend fun onTransition(transition: CallTransition) {
        if (transition.state == CallTransition.State.IDLE) {
            capture?.onIdle(transition.subscriptionId)
        }
    }

    private fun startForegroundCompat() {
        val notif: Notification =
            CallObserverNotifications.build(
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
        private const val TAG = "CallObserverService"
        const val CHANNEL_ID = "antifraud_passive_observer"
        const val NOTIFICATION_ID = 0xCA11

        /**
         * Replaceable in unit tests to avoid the AndroidKeyStore dependency that is
         * unavailable under Robolectric. Production code always uses [Repositories.build].
         */
        @VisibleForTesting
        var repositoriesFactory: (Context) -> Repositories = Repositories::build

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
