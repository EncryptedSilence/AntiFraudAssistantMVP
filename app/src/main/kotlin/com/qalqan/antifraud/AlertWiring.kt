package com.qalqan.antifraud

import android.content.Context
import com.qalqan.antifraud.alerts.AlertChannels
import com.qalqan.antifraud.alerts.AlertDispatcher
import com.qalqan.antifraud.alerts.AlertExplanationProvider
import com.qalqan.antifraud.alerts.AlertNotificationBuilder
import com.qalqan.antifraud.alerts.AlertPipeline
import com.qalqan.antifraud.calls.CallObserverService
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.AppAction
import com.qalqan.antifraud.sms.SmsBroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Stage 9 composition root. `:feature:alerts` does NOT depend on `:feature:calls` /
 * `:feature:sms`; the wiring lives here in `:app` so the producer modules can stay free
 * of `:feature:alerts` knowledge.
 *
 * T34: actionLogger writes an `AppAction.PATTERN_APPLIED` log entry with `source=alert`
 * per dispatched alert, then refreshes the §17.0.3 ongoing notification so the alert
 * counter on the home/passive surface reflects reality.
 */
object AlertWiring {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun installInto(applicationContext: Context) {
        CallObserverService.captureHookFactory = { ctx, repos ->
            val dispatcher = makeDispatcher(ctx, repos)
            val pipeline =
                AlertPipeline(
                    repos = repos,
                    dispatcher = dispatcher,
                    explanationProvider = AlertExplanationProvider(),
                )
            pipeline::onCallCaptured
        }
        SmsBroadcastReceiver.captureHookFactory = { ctx, repos ->
            val dispatcher = makeDispatcher(ctx, repos)
            val pipeline =
                AlertPipeline(
                    repos = repos,
                    dispatcher = dispatcher,
                    explanationProvider = AlertExplanationProvider(),
                )
            pipeline::onSmsCaptured
        }
        AlertChannels.ensure(applicationContext)
    }

    private fun makeDispatcher(
        ctx: Context,
        repos: Repositories,
    ): AlertDispatcher =
        AlertDispatcher(
            context = ctx,
            builder = AlertNotificationBuilder(),
            overlayLauncher = AlertDispatcher.overlayLauncherFor(ctx, campaignId = ""),
            actionLogger = {
                scope.launch {
                    repos.actionLogger.log(AppAction.PATTERN_APPLIED, mapOf("source" to "alert"))
                    CallObserverService.refreshOngoingNotification(ctx)
                }
            },
        )
}
