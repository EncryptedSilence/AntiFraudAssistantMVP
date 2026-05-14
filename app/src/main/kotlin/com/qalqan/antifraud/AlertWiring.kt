package com.qalqan.antifraud

import android.content.Context
import com.qalqan.antifraud.alerts.AlertChannels
import com.qalqan.antifraud.alerts.AlertDispatcher
import com.qalqan.antifraud.alerts.AlertExplanationProvider
import com.qalqan.antifraud.alerts.AlertNotificationBuilder
import com.qalqan.antifraud.alerts.AlertPipeline
import com.qalqan.antifraud.calls.CallObserverService

/**
 * Stage 9 composition root. `:feature:alerts` does NOT depend on `:feature:calls` /
 * `:feature:sms`; the wiring lives here in `:app` so the producer modules can stay free
 * of `:feature:alerts` knowledge. T34 extends this with the action-logger callback;
 * T31 adds the SMS hook.
 */
object AlertWiring {
    fun installInto(applicationContext: Context) {
        CallObserverService.captureHookFactory = { ctx, repos ->
            val dispatcher = makeDispatcher(ctx)
            val pipeline =
                AlertPipeline(
                    repos = repos,
                    dispatcher = dispatcher,
                    explanationProvider = AlertExplanationProvider(),
                )
            pipeline::onCallCaptured
        }
        AlertChannels.ensure(applicationContext)
    }

    private fun makeDispatcher(ctx: Context): AlertDispatcher =
        AlertDispatcher(
            context = ctx,
            builder = AlertNotificationBuilder(),
            overlayLauncher = AlertDispatcher.overlayLauncherFor(ctx, campaignId = ""),
            actionLogger = { /* T34 wires this to ApplicationActionLogger */ },
        )
}
