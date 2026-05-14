package com.qalqan.antifraud.alerts

import androidx.annotation.VisibleForTesting
import com.qalqan.antifraud.correlation.CorrelationOrchestrator
import com.qalqan.antifraud.database.Repositories
import com.qalqan.antifraud.domain.CallEvent
import com.qalqan.antifraud.domain.RiskBand
import com.qalqan.antifraud.domain.RiskEvent
import com.qalqan.antifraud.domain.SmsEvent
import com.qalqan.antifraud.scoring.CampaignRiskScorer
import com.qalqan.antifraud.scoring.LinkSignal
import com.qalqan.antifraud.scoring.RiskLevel
import java.time.Duration
import java.time.Instant

/**
 * Spec §4.4 — the entry point invoked by the capture hooks. Computes a current
 * `CampaignRiskScore` from the orchestrator outcome + scorer, maps to a [RiskBand], maps to
 * an [AlertBand], and asks [dispatcher] to fire.
 *
 * `forcedScore` is a VisibleForTesting seam: when non-null, [computeScore] returns it
 * verbatim, bypassing the correlator + scorer for unit-test determinism.
 */
class AlertPipeline(
    private val repos: Repositories,
    private val dispatcher: AlertDispatcher,
    private val explanationProvider: AlertExplanationProvider,
    private val orchestrator: CorrelationOrchestrator = CorrelationOrchestrator(),
    @VisibleForTesting
    internal val forcedScore: Int? = null,
) {
    suspend fun onCallCaptured(event: CallEvent) {
        val now = Instant.now()
        val score = computeScore(RiskEvent.Call(event), now)
        val band = RiskLevel.fromScore(score)
        val alertBand = AlertBand.from(band)
        if (alertBand == AlertBand.SILENT) {
            dispatcher.dispatch(emptyContent(), AlertBand.SILENT, campaignId = "")
            return
        }
        val campaignId = event.linkedCampaignId?.value ?: synthesizeCallCampaignId(event)
        if (!CampaignCooldown.allow(campaignId, now)) return
        val reasons =
            explanationProvider.reasonsFor(
                event = event,
                band = band,
                triggeredPatternLabels = emptyList(),
            )
        val content = AlertContent(reasons = reasons)
        dispatcher.dispatch(content, alertBand, campaignId)
    }

    suspend fun onSmsCaptured(event: SmsEvent) {
        val now = Instant.now()
        val score = computeScore(RiskEvent.Sms(event), now)
        val band = RiskLevel.fromScore(score)
        val alertBand = AlertBand.from(band)
        if (alertBand == AlertBand.SILENT) {
            dispatcher.dispatch(emptyContent(), AlertBand.SILENT, campaignId = "")
            return
        }
        val campaignId = event.linkedCampaignId?.value ?: synthesizeSmsCampaignId(event)
        if (!CampaignCooldown.allow(campaignId, now)) return
        val reasons =
            explanationProvider.reasonsFor(
                event = event,
                band = band,
                triggeredPatternLabels = emptyList(),
            )
        val content = AlertContent(reasons = reasons)
        dispatcher.dispatch(content, alertBand, campaignId)
    }

    private suspend fun computeScore(
        event: RiskEvent,
        now: Instant,
    ): Int {
        forcedScore?.let { return it }
        val openSessions = repos.sessions.listOpen()
        val activeCampaigns = repos.campaigns.listActive()
        val outcome =
            orchestrator.absorb(
                event = event,
                now = now,
                openSessions = openSessions,
                activeCampaigns = activeCampaigns,
            )
        val contributions =
            when (event) {
                is RiskEvent.Call ->
                    listOf(
                        CampaignRiskScorer.Contribution(
                            eventId = event.event.id,
                            eventRisk = event.event.callRiskScore,
                            age = Duration.ZERO,
                            signals = setOf(LinkSignal.SAME_NUMBER),
                        ),
                    )
                is RiskEvent.Sms ->
                    listOf(
                        CampaignRiskScorer.Contribution(
                            eventId = event.event.id,
                            eventRisk = event.event.smsRiskScore,
                            age = Duration.ZERO,
                            signals = setOf(LinkSignal.SAME_NUMBER),
                        ),
                    )
                else -> emptyList()
            }
        return CampaignRiskScorer.compute(contributions, outcome.triggeredPatternWeights)
    }

    private fun emptyContent(): AlertContent =
        AlertContent(reasons = listOf("placeholder", "placeholder", "placeholder"))

    private fun synthesizeCallCampaignId(event: CallEvent): String = "call:${event.id.value}"

    private fun synthesizeSmsCampaignId(event: SmsEvent): String = "sms:${event.id.value}"
}
