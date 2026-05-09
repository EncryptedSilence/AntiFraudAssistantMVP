package com.qalqan.antifraud.correlation

import com.qalqan.antifraud.domain.PhoneHash
import com.qalqan.antifraud.domain.RiskCampaign
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.domain.RiskSession
import com.qalqan.antifraud.domain.SenderHash
import java.time.Instant

/**
 * Single entry point: given the world (open sessions, active campaigns) and a new event, return
 * what should happen next. Persistence is the caller's responsibility.
 */
class CorrelationOrchestrator {
    data class Outcome(
        val sessionOutcome: SessionCorrelator.Outcome,
        val campaignOutcome: CampaignCorrelator.Outcome,
    )

    fun absorb(
        event: RiskEvent,
        now: Instant,
        openSessions: List<RiskSession>,
        activeCampaigns: List<RiskCampaign>,
    ): Outcome {
        val sessionOutcome = SessionCorrelator.findOrOpen(event, openSessions, now)
        val (phoneHash, senderHash) = actorOf(event)
        val campaignOutcome =
            CampaignCorrelator.findOrOpen(
                actorPhoneHash = phoneHash,
                actorSenderHash = senderHash,
                now = now,
                activeCampaigns = activeCampaigns,
            )
        return Outcome(sessionOutcome, campaignOutcome)
    }

    private fun actorOf(event: RiskEvent): Pair<PhoneHash?, SenderHash?> =
        when (event) {
            is RiskEvent.Call -> event.event.phoneHash to null
            is RiskEvent.Sms -> null to event.event.senderHash
            is RiskEvent.Web -> null to null
            is RiskEvent.Answer -> null to null
        }
}
