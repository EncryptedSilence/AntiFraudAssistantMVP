package com.qalqan.antifraud.correlation

import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.scoring.LinkSignal
import java.time.Duration

/**
 * Spec §10.1 — derive observable link signals between two events.
 *
 * Order independence: the function returns the signal set as if A was emitted by the earlier event.
 */
object LinkSignalResolver {
    private val WINDOW_24H: Duration = Duration.ofHours(24)

    fun resolve(
        a: RiskEvent,
        b: RiskEvent,
    ): List<LinkSignal> {
        val signals = mutableSetOf<LinkSignal>()

        if (sameActor(a, b)) signals += LinkSignal.SAME_NUMBER

        val (earlier, later) = if (a.occurredAt.isBefore(b.occurredAt)) a to b else b to a
        val between = Duration.between(earlier.occurredAt, later.occurredAt).abs()

        if (between <= WINDOW_24H) {
            if (earlier is RiskEvent.Call && later is RiskEvent.Sms) signals += LinkSignal.SMS_AFTER_CALL
            if (earlier is RiskEvent.Sms && later is RiskEvent.Call) signals += LinkSignal.CALL_AFTER_SMS
            if ((earlier is RiskEvent.Call || earlier is RiskEvent.Sms) && later is RiskEvent.Web) {
                signals += LinkSignal.SITE_AFTER_CALL_OR_SMS
            }
        }

        if (signals.isEmpty()) {
            signals += if (between <= WINDOW_24H) LinkSignal.TEMPORAL_ONLY else LinkSignal.WEAK
        }

        return signals.toList()
    }

    private fun sameActor(
        a: RiskEvent,
        b: RiskEvent,
    ): Boolean =
        when {
            a is RiskEvent.Call && b is RiskEvent.Call -> a.event.phoneHash == b.event.phoneHash
            a is RiskEvent.Sms && b is RiskEvent.Sms -> a.event.senderHash == b.event.senderHash
            a is RiskEvent.Web && b is RiskEvent.Web -> a.event.domainHash == b.event.domainHash
            else -> false
        }
}
