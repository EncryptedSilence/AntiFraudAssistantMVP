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
import com.qalqan.antifraud.domain.CallEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
        val sims = SimEnumerator(applicationContext)
        val slotsById = sims.slotsBySubscriptionId()
        router =
            CallStateRouter(this) { transition ->
                val mappedSlot = transition.subscriptionId?.let(slotsById::get)
                scope.launch { onTransition(transition.copy(subscriptionId = mappedSlot)) }
            }.also { it.register(subscriptionIds = slotsById.keys.toList()) }
    }

    private fun captureFactory(context: Context) {
        try {
            val r = repositoriesFactory(context)
            repos = r
            val updater = RiskCounterUpdater(r.contacts)
            val hook = captureHookFactory(context, r)
            capture =
                AutoCallCapture(
                    reader = CallLogReader(context.contentResolver),
                    builder =
                        CallEventBuilder(
                            digest = CallEntryDigest.create(context),
                            contacts = IsKnownContactResolver(r.contacts),
                            repeats = RepeatCallDetector(r.calls),
                        ),
                    calls = r.calls,
                    onCaptured = { event ->
                        updater.bump(event)
                        scope.launch { hook(event) }
                    },
                )
            scope.launch { CallObserverActionLog(r.actionLogger).observerStarted() }
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
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
        repos?.actionLogger?.let { logger ->
            runBlocking { CallObserverActionLog(logger).observerStopped() }
        }
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

        /**
         * Stage 9 hook — `:app` installs an [AlertPipeline.onCallCaptured]-bound function
         * here on `Application.onCreate`. Default no-op preserves the Stage 3 behavior
         * when `:feature:alerts` is excluded (e.g. in a stripped build flavor).
         */
        @VisibleForTesting
        var captureHookFactory: (Context, Repositories) -> (suspend (CallEvent) -> Unit) =
            { _, _ -> { _ -> } }

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

        /**
         * Stage 9 §23 #41 — re-build the §17.0.3 ongoing notification with live counts
         * from [PassiveCounters]. Safe to call from any thread; the actual `notify` call
         * is fire-and-forget on the IO dispatcher. Failures (KeyStore unavailable in tests)
         * swallow silently — the notification simply does not refresh.
         */
        fun refreshOngoingNotification(context: Context) {
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                val r =
                    try {
                        repositoriesFactory(context.applicationContext)
                    } catch (
                        @Suppress("TooGenericExceptionCaught") _: Exception,
                    ) {
                        return@launch
                    }
                try {
                    val counters = PassiveCounters(r)
                    val now = java.time.Instant.now()
                    val copy =
                        PassiveNotificationCopy(
                            eventsLast24h = counters.eventsLast24h(now),
                            alertsLast24h = counters.alertsLast24h(now),
                        )
                    val notif = CallObserverNotifications.build(context, copy)
                    androidx.core.app.NotificationManagerCompat
                        .from(context)
                        .notify(NOTIFICATION_ID, notif)
                } finally {
                    r.close()
                }
            }
        }
    }
}
