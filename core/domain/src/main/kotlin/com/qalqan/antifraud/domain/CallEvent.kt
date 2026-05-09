package com.qalqan.antifraud.domain

import java.time.Instant

/**
 * Spec §16.2 — call metadata only. No audio, no voice analysis.
 */
data class CallEvent(
    val id: EventId,
    val phoneHash: PhoneHash,
    val simSlot: Int?,
    val direction: CallDirection,
    val startedAt: Instant,
    val endedAt: Instant?,
    val durationSec: Long,
    val isKnownContact: Boolean,
    val isRepeated: Boolean,
    val callRiskScore: Int,
    val linkedSessionId: SessionId?,
    val linkedCampaignId: CampaignId?
) {
    init {
        require(durationSec >= 0) { "durationSec must be non-negative" }
        endedAt?.let {
            require(!it.isBefore(startedAt)) { "endedAt must be on or after startedAt" }
        }
        require(callRiskScore in 0..100) { "callRiskScore must be in 0..100" }
        simSlot?.let { require(it >= 0) { "simSlot must be non-negative when present" } }
    }
}
