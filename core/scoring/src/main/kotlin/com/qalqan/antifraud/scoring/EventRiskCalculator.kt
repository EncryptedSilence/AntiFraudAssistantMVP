package com.qalqan.antifraud.scoring

import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.domain.SmsEvent
import com.qalqan.antifraud.domain.UserAnswer
import com.qalqan.antifraud.domain.WebEvent
import kotlin.math.min

/**
 * Spec §11.2: EventRisk = min(100, BaseRisk + ContextRisk + UserAnswerRisk).
 */
object EventRiskCalculator {
    fun compute(
        event: RiskEvent,
        contextSignals: Set<LinkSignal>,
        answersForEvent: List<UserAnswer>,
        lookalikeDomainMatch: Boolean
    ): Int = when (event) {
        is RiskEvent.Call -> computeForCall(event.event, contextSignals, answersForEvent)
        is RiskEvent.Sms -> computeForSms(event.event, contextSignals, answersForEvent)
        is RiskEvent.Web -> computeForWeb(event.event, contextSignals, answersForEvent, lookalikeDomainMatch)
        is RiskEvent.Answer -> 0 // answers contribute via the related event
    }

    fun computeForCall(
        call: CallEvent,
        contextSignals: Set<LinkSignal>,
        answersForEvent: List<UserAnswer>
    ): Int = cap(
        CallBaseRisk.compute(call) +
            ContextRisk.compute(contextSignals) +
            UserAnswerRisk.compute(answersForEvent)
    )

    fun computeForSms(
        sms: SmsEvent,
        contextSignals: Set<LinkSignal>,
        answersForEvent: List<UserAnswer>
    ): Int = cap(
        SmsBaseRisk.compute(sms) +
            ContextRisk.compute(contextSignals) +
            UserAnswerRisk.compute(answersForEvent)
    )

    fun computeForWeb(
        web: WebEvent,
        contextSignals: Set<LinkSignal>,
        answersForEvent: List<UserAnswer>,
        lookalikeMatch: Boolean
    ): Int = cap(
        WebBaseRisk.compute(web, lookalikeMatch) +
            ContextRisk.compute(contextSignals) +
            UserAnswerRisk.compute(answersForEvent)
    )

    private fun cap(score: Int): Int = min(score, 100).coerceAtLeast(0)
}
