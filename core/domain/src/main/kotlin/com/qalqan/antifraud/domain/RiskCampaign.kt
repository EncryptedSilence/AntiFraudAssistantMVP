package com.qalqan.antifraud.domain

import java.time.Instant

/**
 * Spec §16.7 + §3.3 — multi-day grouping with a 14-day active correlation horizon.
 */
data class RiskCampaign(
    val campaignId: CampaignId,
    val startedAt: Instant,
    val lastEventAt: Instant,
    val status: CampaignStatus,
    val scenarioType: ScenarioCategory?,
    val relatedPhoneHashes: Set<PhoneHash>,
    val relatedSmsSenderHashes: Set<SenderHash>,
    val relatedDomainHashes: Set<DomainHash>,
    val relatedEventIds: List<EventId>,
    val relatedSessionIds: List<SessionId>,
    val userAnswerIds: List<AnswerId>,
    val triggeredPatternIds: List<PatternId>,
    val campaignRiskScore: Int,
    val campaignRiskBand: RiskBand,
    val explanation: String?,
) {
    init {
        require(!lastEventAt.isBefore(startedAt)) { "lastEventAt must be on or after startedAt" }
        require(campaignRiskScore in 0..100) { "campaignRiskScore must be in 0..100" }
    }

    companion object {
        const val ACTIVE_HORIZON_DAYS: Long = 14
    }
}
